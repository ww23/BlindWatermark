/*
 * Copyright (c) 2020 ww23(https://github.com/ww23/BlindWatermark).
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

package dev.ww23.image.util;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import static org.opencv.core.Core.BORDER_CONSTANT;
import static org.opencv.core.Core.copyMakeBorder;
import static org.opencv.core.Core.getOptimalDFTSize;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.highgui.HighGui.imshow;
import static org.opencv.highgui.HighGui.waitKey;
import static org.opencv.imgcodecs.Imgcodecs.imread;

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
        imshow(Utils.class.toString(), mat);
        waitKey(0);
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
        return "^[ -~]+$".matches(str);
    }

    public static Mat drawNonAscii(String watermark) {
        Font font = new Font("Default", Font.PLAIN, 64);
        FontMetrics metrics = new Canvas().getFontMetrics(font);
        int width = metrics.stringWidth(watermark);
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
        Mat res = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CV_8U);
        res.put(0, 0, pixels);
        return res;
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
