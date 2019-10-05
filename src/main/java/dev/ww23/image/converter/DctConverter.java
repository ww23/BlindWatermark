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

package dev.ww23.image.converter;

import dev.ww23.image.util.Utils;

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

import static org.bytedeco.javacpp.opencv_imgproc.putText;
import static org.bytedeco.javacpp.opencv_imgproc.CV_FONT_HERSHEY_COMPLEX;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_RGB2HSV;

/**
 * @author ww23
 */
public class DctConverter implements Converter {

    @Override
    public Mat start(Mat src) {
        if ((src.cols() & 1) != 0) {
            copyMakeBorder(src, src, 0, 0, 0, 1, BORDER_CONSTANT, Scalar.all(0));
        }
        if ((src.rows() & 1) != 0) {
            copyMakeBorder(src, src, 0, 1, 0, 0, BORDER_CONSTANT, Scalar.all(0));
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
                new Point(com.cols() >> 2, com.rows() >> 2),
                CV_FONT_HERSHEY_COMPLEX, 2.0,
                new Scalar(2, 2, 2, 0), 2, 8, false);
    }

    @Override
    public void addImageWatermark(Mat com, Mat watermark) {
        Mat mask = new Mat();
        inRange(watermark, new Mat(new Scalar(0, 0, 0, 0)), new Mat(new Scalar(0, 0, 0, 0)), mask);
        Mat i2 = new Mat(watermark.size(), watermark.type(), new Scalar(2, 2, 2, 0));
        i2.copyTo(watermark, mask);
        watermark.convertTo(watermark, CV_32F);
        int row = (com.rows() - watermark.rows()) >> 1;
        int col = (com.cols() - watermark.cols()) >> 1;
        copyMakeBorder(watermark, watermark, row, row, col, col, BORDER_CONSTANT, Scalar.all(0));
        Utils.fixSize(watermark, com);
        addWeighted(watermark, 0.03, com, 1, 0.0, com);
    }

    @Override
    public Mat showWatermark(Mat src) {
        src.convertTo(src, COLOR_RGB2HSV);
        inRange(src, new Mat(new Scalar(0, 0, 0, 0)), new Mat(new Scalar(16, 16, 16, 0)), src);
        normalize(src, src, 0, 255, NORM_MINMAX, CV_8UC1, null);
        return src;
    }
}
