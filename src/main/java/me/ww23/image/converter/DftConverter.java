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

import me.ww23.image.util.Utils;

import static org.bytedeco.javacpp.opencv_core.CV_8UC3;
import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.CV_32F;
import static org.bytedeco.javacpp.opencv_core.NORM_MINMAX;
import static org.bytedeco.javacpp.opencv_core.BORDER_CONSTANT;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_core.Scalar;
import static org.bytedeco.javacpp.opencv_core.Point;
import static org.bytedeco.javacpp.opencv_core.merge;
import static org.bytedeco.javacpp.opencv_core.split;
import static org.bytedeco.javacpp.opencv_core.dft;
import static org.bytedeco.javacpp.opencv_core.idft;
import static org.bytedeco.javacpp.opencv_core.normalize;
import static org.bytedeco.javacpp.opencv_core.copyMakeBorder;
import static org.bytedeco.javacpp.opencv_core.flip;
import static org.bytedeco.javacpp.opencv_core.vconcat;
import static org.bytedeco.javacpp.opencv_core.addWeighted;
import static org.bytedeco.javacpp.opencv_core.magnitude;
import static org.bytedeco.javacpp.opencv_core.add;
import static org.bytedeco.javacpp.opencv_core.log;

import static org.bytedeco.javacpp.opencv_imgproc.CV_FONT_HERSHEY_COMPLEX;
import static org.bytedeco.javacpp.opencv_imgproc.putText;


/**
 * @author ww23
 */
public class DftConverter implements Converter {

    @Override
    public Mat start(Mat src) {
        src.convertTo(src, CV_32F);
        MatVector planes = new MatVector(2);
        Mat com = new Mat();
        planes.put(0, src);
        planes.put(1, Mat.zeros(src.size(), CV_32F).asMat());
        merge(planes, com);
        dft(com, com);
        return com;
    }

    @Override
    public void inverse(Mat com) {
        MatVector planes = new MatVector(2);
        idft(com, com);
        split(com, planes);
        normalize(planes.get(0), com, 0, 255, NORM_MINMAX, CV_8UC3, null);
    }

    @Override
    public void addTextWatermark(Mat com, String watermark) {
        Scalar s = new Scalar(0, 0, 0, 0);
        Point p = new Point(com.cols() / 3, com.rows() / 3);
        putText(com, watermark, p, CV_FONT_HERSHEY_COMPLEX, 1.0, s, 3,
                8, false);
        flip(com, com, -1);
        putText(com, watermark, p, CV_FONT_HERSHEY_COMPLEX, 1.0, s, 3,
                8, false);
        flip(com, com, -1);
    }

    @Override
    public void addImageWatermark(Mat com, Mat watermark) {
        MatVector planes = new MatVector(2);
        watermark.convertTo(watermark, CV_32F);
        Mat temp = new Mat();
        int col = (com.cols() - watermark.cols()) >> 1;
        int row = ((com.rows() >> 1) - watermark.rows()) >> 1;
        copyMakeBorder(watermark, watermark, row, row, col, col, BORDER_CONSTANT, Scalar.all(0));
        planes.put(0, watermark);
        flip(watermark, temp, -1);
        planes.put(1, temp);
        vconcat(planes, watermark);

        planes.put(0, watermark);
        planes.put(1, watermark);
        merge(planes, watermark);
        Utils.fixSize(watermark, com);
        addWeighted(watermark, 8, com, 1, 0.0, com);

        split(com, planes);
    }

    @Override
    public Mat showWatermark(Mat src) {
        MatVector newPlanes = new MatVector(2);
        Mat mag = new Mat();
        split(src, newPlanes);
        magnitude(newPlanes.get(0), newPlanes.get(1), mag);
        add(Mat.ones(mag.size(), CV_32F).asMat(), mag, mag);
        log(mag, mag);
        mag.convertTo(mag, CV_8UC1);
        normalize(mag, mag, 0, 255, NORM_MINMAX, CV_8UC1, null);
        return mag;
    }
}
