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

package me.ww23.image;

import me.ww23.image.dencoder.Decoder;
import me.ww23.image.dencoder.Encoder;
import me.ww23.image.dencoder.ImageEncoder;
import me.ww23.image.dencoder.TextEncoder;
import me.ww23.image.converter.Converter;
import me.ww23.image.converter.DctConverter;
import me.ww23.image.converter.DftConverter;
import me.ww23.image.util.Utils;
import org.bytedeco.javacpp.opencv_core;
import org.junit.jupiter.api.Test;

import static org.bytedeco.javacpp.opencv_core.*;

class BlindWatermarkTest {

    private Converter converterDct = new DctConverter();

    @Test
    void dct() {
        Converter converter = new DctConverter();
        Encoder encoder = new ImageEncoder(converter);
        encoder.encode("image/gakki-src.png", "image/watermark.png", "image/gakki-dct-img-ec.jpg");
        Decoder decoder = new Decoder(converter);
        decoder.decode("image/gakki-dct-img-ec.jpg", "image/gakki-dct-img-dc.jpg");
        encoder = new TextEncoder(converter);
        encoder.encode("image/gakki-src.png", "test", "image/gakki-dct-text-ec.jpg");
        decoder.decode("image/gakki-dct-text-ec.jpg", "image/gakki-dct-text-dc.jpg");
    }

    @Test
    void dft() {
        Converter converter = new DftConverter();
        Encoder encoder = new ImageEncoder(converter);
        encoder.encode("image/gakki-src.png", "image/watermark.png", "image/gakki-dft-img-ec.png");
        Decoder decoder = new Decoder(converter);
        decoder.decode("image/gakki-dft-img-ec.png", "image/gakki-dft-img-dc.png");
        encoder = new TextEncoder(converter);
        encoder.encode("image/gakki-src.png", "test", "image/gakki-dft-text-ec.png");
        decoder.decode("image/gakki-dft-text-ec.png", "image/gakki-dft-text-dc.png");
    }

    @Test
    void speedGray() {
        opencv_core.Mat src = Utils.read("image/gakki-src.png", CV_8U);
        converterDct.addTextWatermark(converterDct.start(src), "test");
        converterDct.inverse(src);
    }
}
