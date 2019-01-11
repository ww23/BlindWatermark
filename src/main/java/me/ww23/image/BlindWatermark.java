/*
 * Copyright 2018 ww23(https://github.com/ww23/BlindWatermark).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ww23.image;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class BlindWatermark {

    private Option option = Option.DCT_IMAGE;

    public static void main(String[] args) {

        if (args.length < 4) {
            help();
        }

        BlindWatermark bwm = new BlindWatermark();

        args[1] = args[1].substring(1);
        switch (args[1]) {
            case "tc":
            case "ct":
                bwm.option = Option.DCT_TEXT;
                break;
            case "ic":
            case "ci":
                bwm.option = Option.DCT_IMAGE;
                break;
            case "tf":
            case "ft":
                bwm.option = Option.DFT_TEXT;
                break;
            case "if":
            case "fi":
                bwm.option = Option.DFT_IMAGE;
                break;
            default:
                help();
        }

        switch (args[0]) {
            case "encode":
                bwm.encode(args[2], args[3], args[4], bwm.option);
                break;
            case "decode":
                switch (bwm.option) {
                    case DCT_TEXT:
                        bwm.decodeDCT(args[2], args[3]);
                        break;
                    case DFT_TEXT:
                        bwm.decodeDFT(args[2], args[3]);
                        break;
                    case DFT_IMAGE:
                        bwm.decodeDFT(args[2], args[3], args[4]);
                        break;
                    case DCT_IMAGE:
                        bwm.decodeDCT(args[2], args[3], args[4]);
                        break;
                }
                break;
            default:
                help();
        }
//        bwm.encode("gakki.png", "wm.png", "gakki-test.jpg", bwm.option);
//        bwm.decodeDCT("gakki.png", "gakki-test.jpg","test.jpg");
    }

    public enum Option {
        DFT_IMAGE, DFT_TEXT, DCT_IMAGE, DCT_TEXT
    }

    /**
     * 添加水印
     *
     * @param image     原图路径
     * @param watermark 图片水印路径 或 水印文本
     * @param output    加水印图片路径
     * @param option    false:图片水印 true:文字水印
     */
    private void encode(String image, String watermark, String output, Option option) {
        Mat srcImg = imread(image, CV_LOAD_IMAGE_COLOR);

        //split color channels
        MatVector color = new MatVector(3);
        split(srcImg, color);

        MatVector planes = new MatVector(2);
        for (int i = 0; i < color.size(); i++) {
            Mat comImg = new Mat();
            switch (option) {
                case DFT_TEXT:
                    comImg = startDFT(color.get(i));
                    addMirrorTextWatermark(comImg, watermark);
                    inverseDFT(comImg, planes);
                    break;
                case DFT_IMAGE:
                    comImg = startDFT(color.get(i));
                    addMirrorImageWatermark(comImg, watermark);
                    inverseDFT(comImg, planes);
                    break;
                case DCT_TEXT:
                    comImg = startDCT(color.get(i));
                    addTextWatermark(comImg, watermark);
                    inverseDCT(comImg);
                    break;
                case DCT_IMAGE:
                    comImg = startDCT(color.get(i));
                    addImageWatermark(comImg, watermark);
                    inverseDCT(comImg);
                    break;
            }
            color.put(i, comImg);
        }

        Mat res = new Mat();
        merge(color, res);

        if (res.rows() != srcImg.rows() || res.cols() != srcImg.cols()) {
            res = new Mat(res, new Rect(0, 0, srcImg.size().width(), srcImg.size().height()));
        }

        imwrite(output, res);
    }

    /**
     * 文本水印解码 DFT
     *
     * @param wmImg  加了文本水印的图像
     * @param output 文本水印
     */
    private void decodeDFT(String wmImg, String output) {
        Mat decImg = startDFT(imread(wmImg, CV_LOAD_IMAGE_GRAYSCALE));

        MatVector newPlanes = new MatVector(2);
        Mat mag = new Mat();
        split(decImg, newPlanes);
        magnitude(newPlanes.get(0), newPlanes.get(1), mag);
        add(Mat.ones(mag.size(), CV_32F).asMat(), mag, mag);
        log(mag, mag);

        mag.convertTo(mag, CV_8UC1);
        normalize(mag, mag, 0, 255, NORM_MINMAX, CV_8UC1, null);

        imwrite(output, mag);
    }

    /**
     * 图片水印解码 DFT
     *
     * @param srcImg 原图
     * @param wmImg  加了图片水印的图像
     * @param output 图像中的水印
     */
    private void decodeDFT(String srcImg, String wmImg, String output) {
        Mat src = startDFT(imread(srcImg, CV_LOAD_IMAGE_GRAYSCALE));
        Mat wm = startDFT(imread(wmImg, CV_LOAD_IMAGE_GRAYSCALE));

        subtract(wm, src, wm);

        MatVector newPlanes = new MatVector(2);
        split(wm, newPlanes);
        wm = newPlanes.get(0);

        imwrite(output, wm);
    }

    /**
     * 文本水印解码 DCT
     *
     * @param wmImg  加了文本水印的图像
     * @param output 文本水印
     */
    private void decodeDCT(String wmImg, String output) {
        Mat decImg = startDCT(imread(wmImg, CV_LOAD_IMAGE_GRAYSCALE));

        decImg.convertTo(decImg, COLOR_RGB2HSV);
        Mat low = new Mat(new Scalar(1, 1, 1, 0));
        Mat upp = new Mat(new Scalar(5, 5, 5, 0));
        inRange(decImg, low, upp, decImg);

        normalize(decImg, decImg, 0, 255, NORM_MINMAX, CV_8UC1, null);
        imwrite(output, decImg);
    }

    /**
     * 图片水印解码 DCT
     *
     * @param srcImg 原图路径
     * @param wmImg  加了图片水印的图像路径
     * @param output 输出水印路径
     */
    private void decodeDCT(String srcImg, String wmImg, String output) {
        Mat src = startDCT(imread(srcImg, CV_LOAD_IMAGE_GRAYSCALE));
        Mat wm = startDCT(imread(wmImg, CV_LOAD_IMAGE_GRAYSCALE));

        subtract(wm, src, wm);

        wm.convertTo(wm, CV_8U);

        equalizeHist(wm, wm);

        imwrite(output, wm);
    }

    /**
     * 将图像进行DFT
     *
     * @param srcImg 源图像
     * @return 转化后的图像
     */
    private static Mat startDFT(Mat srcImg) {
        srcImg.convertTo(srcImg, CV_32F);
        MatVector planes = new MatVector(2);
        Mat comImg = new Mat();
        planes.put(0, srcImg);
        planes.put(1, Mat.zeros(srcImg.size(), CV_32F).asMat());
        merge(planes, comImg);
        dft(comImg, comImg);
        return comImg;
    }

    /**
     * DFT逆变换
     *
     * @param comImg DFT后的图像
     * @param planes 图像变量
     */
    private static void inverseDFT(Mat comImg, MatVector planes) {
        idft(comImg, comImg);
        split(comImg, planes);
        normalize(planes.get(0), comImg, 0, 255, NORM_MINMAX, CV_8UC3, null);
    }

    /**
     * 将图像进行DCT
     *
     * @param srcImg 源图像
     * @return 转化后的图像
     */
    private static Mat startDCT(Mat srcImg) {
        if ((srcImg.cols() & 1) != 0) {
            copyMakeBorder(srcImg, srcImg, 0, 0,
                    0, 1, BORDER_CONSTANT, Scalar.all(0));
        }
        if ((srcImg.rows() & 1) != 0) {
            copyMakeBorder(srcImg, srcImg, 0, 1,
                    0, 0, BORDER_CONSTANT, Scalar.all(0));
        }
        srcImg.convertTo(srcImg, CV_32F);
        dct(srcImg, srcImg);
        return srcImg;
    }

    /**
     * DCT逆变换
     *
     * @param comImg DCT后的图像
     */
    private static void inverseDCT(Mat comImg) {
        idct(comImg, comImg);
    }

    /**
     * 添加文本水印
     *
     * @param comImg    图像
     * @param watermark 水印
     */
    private static void addTextWatermark(Mat comImg, String watermark) {
        putText(comImg, watermark,
                new Point(comImg.size().width() / 4, comImg.size().height() / 4),
                CV_FONT_HERSHEY_COMPLEX, 1.0,
                new Scalar(2, 2, 2, 0), 2, 8, false);
    }

    /**
     * 添加对称的文本水印
     *
     * @param comImg    图像
     * @param watermark 水印
     */
    private static void addMirrorTextWatermark(Mat comImg, String watermark) {
        Scalar s = new Scalar(0, 0, 0, 0);
        Point p = new Point(comImg.size().width() / 3, comImg.size().height() / 3);

        putText(comImg, watermark, p, CV_FONT_HERSHEY_COMPLEX, 1.0, s, 3,
                8, false);
        //旋转图片
        flip(comImg, comImg, -1);

        putText(comImg, watermark, p, CV_FONT_HERSHEY_COMPLEX, 1.0, s, 3,
                8, false);
        flip(comImg, comImg, -1);
    }

    /**
     * 添加图片水印
     *
     * @param comImg    图像
     * @param watermark 图片水印路径
     */
    private static void addImageWatermark(Mat comImg, String watermark) {
        Mat wm = imread(watermark, CV_LOAD_IMAGE_GRAYSCALE);
        wm.convertTo(wm, CV_32F);
        createImageWaterMark(comImg, wm);
        addWeighted(wm, 0.01, comImg, 1, 0.0, comImg);
    }

    /**
     * 添加对称的图片水印
     *
     * @param comImg    图像
     * @param watermark 图片水印路径
     */
    private static void addMirrorImageWatermark(Mat comImg, String watermark) {
        Mat wm = imread(watermark, CV_LOAD_IMAGE_GRAYSCALE);
        MatVector planes = new MatVector(2);
        wm.convertTo(wm, CV_32F);

        createMirrorImageWaterMark(comImg, wm);

        planes.put(0, wm);
        planes.put(1, wm);
        merge(planes, wm);

        addWeighted(wm, 0.5, comImg, 1, 0.0, comImg);
    }

    /**
     * 将水印扩展成原图的大小
     *
     * @param comImg    原图
     * @param watermark 水印图
     */
    private static void createImageWaterMark(Mat comImg, Mat watermark) {
        copyMakeBorder(watermark, watermark,
                comImg.rows() / 8, comImg.rows() - watermark.rows() - comImg.rows() / 8,
                comImg.cols() / 8, comImg.cols() - watermark.cols() - comImg.cols() / 8,
                BORDER_CONSTANT, Scalar.all(0));
    }

    /**
     * 将水印对称并扩展成原图的大小
     *
     * @param comImg    原图
     * @param watermark 水印图
     */
    private static void createMirrorImageWaterMark(Mat comImg, Mat watermark) {
        MatVector combine = new MatVector(2);
        Mat wm = new Mat();

        copyMakeBorder(watermark, watermark, 0, comImg.rows() / 2 - watermark.rows(),
                0, comImg.cols() - watermark.cols(), BORDER_CONSTANT, Scalar.all(0));

        combine.put(0, watermark);
        flip(watermark, wm, -1);
        combine.put(1, wm);

        vconcat(combine, watermark);
    }

    /**
     * 优化图像大小
     *
     * @param srcImg 源图像
     * @return 优化后的图像
     */
    private static Mat optimizedImage(Mat srcImg) {
        Mat padded = new Mat();
        int opRows = getOptimalDFTSize(srcImg.rows());
        int opCols = getOptimalDFTSize(srcImg.cols());
        copyMakeBorder(srcImg, padded, 0, opRows - srcImg.rows(),
                0, opCols - srcImg.cols(), BORDER_CONSTANT, Scalar.all(0));
        return padded;
    }

    private static void help() {
        System.out.println("Usage: java -jar BlindWatermark.jar <commands> [args...] \n" +
                "   commands: \n" +
                "       encode <option> <image-src> <watermark-text> <image-encoded(text)>\n" +
                "       encode <option> <image-src> <watermark-image> <image-encoded(image)>\n" +
                "       decode <option> <image-encode(text)> <image-decode>\n" +
                "       decode <option> <image-src> <image-encoded(image)> <image-decode>\n" +
                "   options: \n" +
                "       -c discrete cosine transform\n" +
                "       -f discrete fourier transform\n" +
                "       -i image watermark\n" +
                "       -t text  watermark\n" +
                "   example: \n" +
                "       encode -ft foo.png test bar.png"
        );
        System.exit(-1);
    }
}