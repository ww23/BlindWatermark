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

package me.ww23.image.converter;

import static org.bytedeco.javacpp.opencv_core.CV_8U;
import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.CV_32F;
import static org.bytedeco.javacpp.opencv_core.BORDER_CONSTANT;
import static org.bytedeco.javacpp.opencv_core.NORM_MINMAX;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.Scalar;
import static org.bytedeco.javacpp.opencv_core.Point;
import static org.bytedeco.javacpp.opencv_core.copyMakeBorder;
import static org.bytedeco.javacpp.opencv_core.dct;
import static org.bytedeco.javacpp.opencv_core.idct;
import static org.bytedeco.javacpp.opencv_core.addWeighted;
import static org.bytedeco.javacpp.opencv_core.inRange;
import static org.bytedeco.javacpp.opencv_core.normalize;
import static org.bytedeco.javacpp.opencv_core.subtract;

import static org.bytedeco.javacpp.opencv_imgproc.COLOR_RGB2HSV;
import static org.bytedeco.javacpp.opencv_imgproc.CV_FONT_HERSHEY_COMPLEX;
import static org.bytedeco.javacpp.opencv_imgproc.putText;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;

public class DctConverter implements Converter {

    @Override
    public Mat start(Mat src) {
        if ((src.cols() & 1) != 0) {
            copyMakeBorder(src, src, 0, 0,
                    0, 1, BORDER_CONSTANT, Scalar.all(0));
        }
        if ((src.rows() & 1) != 0) {
            copyMakeBorder(src, src, 0, 1,
                    0, 0, BORDER_CONSTANT, Scalar.all(0));
        }
        src.convertTo(src, CV_32F);
        dct(src, src);
        return src;
    }

    @Override
    public void inverse(Mat com) {
        idct(com, com);
    }

    @Override
    public void addTextWatermark(Mat com, String watermark) {
        putText(com, watermark,
                new Point(com.size().width() >> 2, com.size().height() >> 2),
                CV_FONT_HERSHEY_COMPLEX, 1.0,
                new Scalar(2, 2, 2, 0), 2, 8, false);

//        byte[] temp = new byte[com.rows() * com.cols() * (int) com.elemSize()];
//        com.data().get(temp);
//
//        BufferedImage bufferedImage = new BufferedImage(com.cols(), com.rows(), BufferedImage.TYPE_BYTE_GRAY);
//        bufferedImage.getRaster().setDataElements(0, 0, com.cols(), com.rows(), temp);
//
//        byte[] pixels = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
//
//        Mat mat = new Mat(com.rows(), com.cols(), com.type(), new BytePointer(pixels));
//
//        opencv_highgui.imshow("test", mat);
//        opencv_highgui.waitKey(-1);
    }

    @Override
    public void addImageWatermark(Mat com, Mat watermark) {
        watermark.convertTo(watermark, CV_32F);
        copyMakeBorder(watermark, watermark,
                com.rows() / 8, com.rows() - watermark.rows() - com.rows() / 8,
                com.cols() / 8, com.cols() - watermark.cols() - com.cols() / 8,
                BORDER_CONSTANT, Scalar.all(0));
        addWeighted(watermark, 0.01, com, 1, 0.0, com);
    }

    @Override
    public Mat showTextWatermark(Mat src) {
        src.convertTo(src, COLOR_RGB2HSV);
        Mat low = new Mat(new Scalar(1, 1, 1, 0));
        Mat upp = new Mat(new Scalar(5, 5, 5, 0));
        inRange(src, low, upp, src);
        normalize(src, src, 0, 255, NORM_MINMAX, CV_8UC1, null);
        return src;
    }

    @Override
    public Mat showImageWatermark(Mat src, Mat watermark) {
        subtract(watermark, src, watermark);
        watermark.convertTo(watermark, CV_8U);
        equalizeHist(watermark, watermark);
        return watermark;
    }
}
