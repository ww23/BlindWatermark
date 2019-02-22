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

import static org.bytedeco.javacpp.opencv_core.CV_8U;

import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;

public class ImageDecoder extends Decoder {

    public ImageDecoder(Converter converter) {
        super(converter);
    }

    @Override
    public void decode(String image, String watermark) {
        this.decode(image, watermark, image + "-decode.png");
    }

    @Override
    public void decode(String image, String watermark, String output) {
        imwrite(output, this.converter.showImageWatermark(
                        this.converter.start(Checker.read(image, CV_8U)),
                        this.converter.start(Checker.read(watermark, CV_8U))));
    }
}
