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

package dev.ww23.image.converter;

import dev.ww23.image.util.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencv.core.Core.BORDER_CONSTANT;
import static org.opencv.core.Core.NORM_MINMAX;
import static org.opencv.core.Core.add;
import static org.opencv.core.Core.addWeighted;
import static org.opencv.core.Core.copyMakeBorder;
import static org.opencv.core.Core.dft;
import static org.opencv.core.Core.flip;
import static org.opencv.core.Core.idft;
import static org.opencv.core.Core.log;
import static org.opencv.core.Core.magnitude;
import static org.opencv.core.Core.merge;
import static org.opencv.core.Core.normalize;
import static org.opencv.core.Core.split;
import static org.opencv.core.Core.vconcat;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_COMPLEX;
import static org.opencv.imgproc.Imgproc.putText;


/**
 * @author ww23
 */
@Deprecated
public class DftConverter implements Converter {

    @Override
    public Mat start(Mat src) {
        src.convertTo(src, CV_32F);
        List<Mat> planes = new ArrayList<>(2);
        Mat com = new Mat();
        planes.add(0, src);
        planes.add(1, Mat.zeros(src.size(), CV_32F));
        merge(planes, com);
        dft(com, com);
        return com;
    }

    @Override
    public void inverse(Mat com) {
        List<Mat> planes = new ArrayList<>(2);
        idft(com, com);
        split(com, planes);
        normalize(planes.get(0), com, 0, 255, NORM_MINMAX, CV_8UC3);
    }

    @Override
    public void addTextWatermark(Mat com, String watermark) {
        Scalar s = new Scalar(0, 0, 0, 0);
        Point p = new Point(com.cols() / 3, com.rows() / 3);
        putText(com, watermark, p, FONT_HERSHEY_COMPLEX, 1.0, s, 3,
                8, false);
        flip(com, com, -1);
        putText(com, watermark, p, FONT_HERSHEY_COMPLEX, 1.0, s, 3,
                8, false);
        flip(com, com, -1);
    }

    @Override
    public void addImageWatermark(Mat com, Mat watermark) {
        List<Mat> planes = new ArrayList<>(2);
        List<Mat> newPlanes = new ArrayList<>(2);
        Mat temp = new Mat();
        int col = (com.cols() - watermark.cols()) >> 1;
        int row = ((com.rows() >> 1) - watermark.rows()) >> 1;
        watermark.convertTo(watermark, CV_32F);
        copyMakeBorder(watermark, watermark, row, row, col, col, BORDER_CONSTANT, Scalar.all(0));
        planes.add(0, watermark);
        flip(watermark, temp, -1);
        planes.add(1, temp);
        vconcat(planes, watermark);

        newPlanes.add(0, watermark);
        newPlanes.add(1, watermark);
        merge(newPlanes, watermark);
        Utils.fixSize(watermark, com);
        addWeighted(watermark, 8, com, 1, 0.0, com);

        split(com, planes);
    }

    @Override
    public Mat showWatermark(Mat src) {
        List<Mat> newPlanes = new ArrayList<>(2);
        Mat mag = new Mat();
        split(src, newPlanes);
        magnitude(newPlanes.get(0), newPlanes.get(1), mag);
        add(Mat.ones(mag.size(), CV_32F), mag, mag);
        log(mag, mag);
        mag.convertTo(mag, CV_8UC1);
        normalize(mag, mag, 0, 255, NORM_MINMAX, CV_8UC1);
        return mag;
    }
}
