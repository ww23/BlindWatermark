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

package dev.ww23.image;

import dev.ww23.image.converter.Converter;
import dev.ww23.image.converter.DftConverter;
import dev.ww23.image.dencoder.Decoder;
import dev.ww23.image.dencoder.Encoder;
import dev.ww23.image.dencoder.ImageEncoder;
import dev.ww23.image.dencoder.TextEncoder;
import dev.ww23.image.converter.DctConverter;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;


/**
 * @author ww23
 */
public class BlindWatermark {

    private static final String FOURIER = "f";
    private static final String COSINE = "c";
    private static final String IMAGE = "i";
    private static final String TEXT = "t";

    public static void main(String[] args) {

        Loader.load(opencv_java.class);

        if (args.length < 4) {
            help();
        }

        Converter converter = null;
        String option = args[1].substring(1);

        if (option.contains(FOURIER)) {
            converter = new DftConverter();
        } else if (option.contains(COSINE)) {
            converter = new DctConverter();
        } else {
            help();
        }

        switch (args[0]) {
            case "encode":
                Encoder encoder = null;
                if (option.contains(IMAGE)) {
                    encoder = new ImageEncoder(converter);
                } else if (option.contains(TEXT)) {
                    encoder = new TextEncoder(converter);
                } else {
                    help();
                }
                assert encoder != null;
                encoder.encode(args[2], args[3], args[4]);
                break;
            case "decode":
                Decoder decoder = new Decoder(converter);
                decoder.decode(args[2], args[3]);
                break;
            default:
                help();
        }
    }

    private static void help() {
        System.out.println("Usage: java -jar BlindWatermark.jar <commands>\n" +
                "   commands: \n" +
                "       encode <option> <original image> <watermark> <embedded image>\n" +
                "       decode <option> <original image> <embedded image>\n" +
                "   encode options: \n" +
                "       -c discrete cosine transform\n" +
                "       -f discrete fourier transform (Deprecated)\n" +
                "       -i image watermark\n" +
                "       -t text  watermark\n" +
                "   decode options: \n" +
                "       -c discrete cosine transform\n" +
                "       -f discrete fourier transform (Deprecated)\n" +
                "   example: \n" +
                "       encode -ct foo.png test bar.png" +
                "       decode -c  foo.png bar.png"
        );
        System.exit(-1);
    }
}