/*
 * Copyright (c) 2019 ww23(https://github.com/ww23/BlindWatermark).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ww23.image.util;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_highgui;
import sun.font.FontDesignMetrics;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.getOptimalDFTSize;
import static org.bytedeco.javacpp.opencv_core.copyMakeBorder;
import static org.bytedeco.javacpp.opencv_core.BORDER_CONSTANT;
import static org.bytedeco.javacpp.opencv_core.Scalar;
import static org.bytedeco.javacpp.opencv_core.CV_8U;
import static org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

/**
 * @author ww23
 */
public class Utils {

    public static Mat read(String image, int type) {
        Mat src = imread(image, type);
        if (src.empty()) {
            System.out.println("File not found!");
            System.exit(-1);
        }
        return src;
    }

    public static void show(Mat mat) {
        opencv_highgui.imshow(Utils.class.toString(), mat);
        opencv_highgui.waitKey(-1);
    }

    public static Mat optimalDft(Mat srcImg) {
        Mat padded = new Mat();
        int opRows = getOptimalDFTSize(srcImg.rows());
        int opCols = getOptimalDFTSize(srcImg.cols());
        copyMakeBorder(srcImg, padded, 0, opRows - srcImg.rows(),
                0, opCols - srcImg.cols(), BORDER_CONSTANT, Scalar.all(0));
        return padded;
    }

    public static boolean isAscii(String str) {
        return str.matches("^[ -~]+$");
    }

    public static Mat drawNonAscii(String watermark) {
        Font font = new Font("Default", Font.PLAIN, 64);
        FontDesignMetrics metrics = FontDesignMetrics.getMetrics(font);
        int width = 0;
        for (int i = 0; i < watermark.length(); i++) {
            width += metrics.charWidth(watermark.charAt(i));
        }
        int height = metrics.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        graphics.drawString(watermark, 0, metrics.getAscent());
        graphics.dispose();
        byte[] pixels = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        return new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CV_8U, new BytePointer(pixels));
    }

    public static void fixSize(Mat src, Mat mirror) {
        if (src.rows() != mirror.rows()) {
            copyMakeBorder(src, src, 0, mirror.rows() - src.rows(),
                    0, 0, BORDER_CONSTANT, Scalar.all(0));
        }
        if (src.cols() != mirror.cols()) {
            copyMakeBorder(src, src, 0, 0,
                    0, mirror.cols() - src.cols(), BORDER_CONSTANT, Scalar.all(0));
        }
    }

}
