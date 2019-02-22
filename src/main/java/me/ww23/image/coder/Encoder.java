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

package me.ww23.image.coder;

import me.ww23.image.converter.Converter;
import me.ww23.image.util.Checker;

import static org.bytedeco.javacpp.opencv_core.CV_8S;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_core.split;
import static org.bytedeco.javacpp.opencv_core.merge;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;

public abstract class Encoder {

    Converter converter;

    Encoder(Converter converter) {
        this.converter = converter;
    }

    public void encode(String image, String watermark, String output) {
        Mat src = Checker.read(image, CV_8S);

        MatVector color = new MatVector(3);
        split(src, color);

        for (int i = 0; i < color.size(); i++) {
            Mat com = this.converter.start(color.get(i));
            this.addWatermark(com, watermark);
            this.converter.inverse(com);
            color.put(i, com);
        }

        Mat res = new Mat();
        merge(color, res);

        if (res.rows() != src.rows() || res.cols() != src.cols()) {
            res = new Mat(res, new Rect(0, 0, src.size().width(), src.size().height()));
        }

        imwrite(output, res);
    }

    protected abstract void addWatermark(Mat com, String watermark);
}
