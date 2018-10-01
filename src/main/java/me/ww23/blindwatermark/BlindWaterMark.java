package me.ww23.blindwatermark;

import org.bytedeco.javacpp.indexer.FloatIndexer;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class BlindWaterMark {

    private static MatVector newPlanes = new MatVector(3);
    private static boolean text = false;

//    private static Mat im1, im2, im3, im4, im5, im6;
//    private static Mat temp, im11, im12, im13, im14;
//    private static Mat imi, imd, imr;
//    private static float a, b, c, d;

    public static void main(String[] args) {

        if(args.length  < 5) {
            help();
        }

        BlindWaterMark bwm = new BlindWaterMark();

        String arg1 = args[1];
        String arg2 = args[2];
        String arg3;
        String arg4;
        String command = args[0];

        switch (command) {
            case "encode":
                if (arg1.equals("-t")) {
                    text = true;
                }
                arg3 = args[3];
                arg4 = args[4];
                bwm.encode(arg2, arg3, arg4);
                System.out.println("ENCODE SUCCESSFUL");
                break;
            case "decode":
                if (args.length == 3) {
                    bwm.decode(arg1, arg2);
                } else if (args.length == 4) {
                    arg3 = args[3];
                    bwm.decode(arg1, arg2, arg3);
                } else {
                    help();
                }
                System.out.println("DECODE SUCCESSFUL");
                break;
            default:
                help();
        }

//        text = true;
//        bwm.encode("gakki.png", "mywife", "gakki-wm-text.png");
//        bwm.decode("gakki-wm-text.png", "gakki-text-dc.png");
    }

    private void encode(String image, String watermark, String output) {

        //load image
        Mat srcImg = imread(image, CV_LOAD_IMAGE_COLOR);

        if (srcImg.empty()) {
            System.exit(1);
        }

        //split color channels
        MatVector color = new MatVector(3);
        split(srcImg, color);

        MatVector[] planes = {new MatVector(2), new MatVector(2), new MatVector(2)};
        for (int i = 0; i < color.size(); i++) {
            color.get(i).convertTo(color.get(i), CV_32F);
            Mat comImg = startDFT(color.get(i));
            if (text) {
                addTextWaterMark(comImg, watermark);
            } else {
                addImageWaterMark(comImg, watermark);
            }
            inverseDFT(comImg, planes[i]);
            newPlanes.put(i, comImg);
        }

        Mat nImg = new Mat();
        merge(newPlanes, nImg);

        imwrite(output, nImg);
    }

    /**
     * 文本水印解码
     *
     * @param wmImg  加了文本水印的图像
     * @param output 图像中文本水印
     */
    private void decode(String wmImg, String output) {

        Mat decImg = imread(wmImg, CV_LOAD_IMAGE_GRAYSCALE);
        if (decImg.empty()) {
            System.exit(1);
        }
        decImg.convertTo(decImg, CV_32F);

        decImg = startDFT(decImg);
        MatVector newPlanes = new MatVector(2);
        Mat mag = new Mat();
        split(decImg, newPlanes);
        magnitude(newPlanes.get(0), newPlanes.get(1), mag);
        add(Mat.ones(mag.size(), CV_32F).asMat(), mag, mag);
        log(mag, mag);
//        shiftDFT(mag);
        mag.convertTo(mag, CV_8UC1);
        normalize(mag, mag, 0, 255, NORM_MINMAX, CV_8UC1, null);

        imwrite(output, mag);
    }

    /**
     * 图片水印解码
     *
     * @param srcImg 原图
     * @param wmImg  加了图片水印的图像
     * @param output 图像中的水印
     */
    private void decode(String srcImg, String wmImg, String output) {
        Mat decImg = imread(srcImg, CV_LOAD_IMAGE_GRAYSCALE);
        Mat wm = imread(wmImg, CV_LOAD_IMAGE_GRAYSCALE);

        decImg.convertTo(decImg, CV_32F);
        wm.convertTo(wm, CV_32F);
        if (decImg.empty() || wm.empty()) {
            System.exit(1);
        }

        //srcImg - wmImg
        subtract(wm, startDFT(decImg), startDFT(wm));

        MatVector newPlanes = new MatVector(2);
        split(wm, newPlanes);
        wm = newPlanes.get(0);

        imwrite(output, wm);
    }

    /**
     * 将图像进行DFT
     *
     * @param srcImg 源图像
     * @return 转化后的图像
     */
    private static Mat startDFT(Mat srcImg) {
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
     * 优化图像大小
     *
     * @param srcImg 源图像
     * @return 优化后的图像
     */
    private Mat optimizedImage(Mat srcImg) {
        Mat padded = new Mat();
        int opRows = getOptimalDFTSize(srcImg.rows());
        int opCols = getOptimalDFTSize(srcImg.cols());
        copyMakeBorder(srcImg, padded, 0, opRows - srcImg.rows(),
                0, opCols - srcImg.cols(), BORDER_CONSTANT, Scalar.all(0));
        return padded;
    }

    /**
     * 添加文本水印
     *
     * @param comImg 频谱图
     */
    private static void addTextWaterMark(Mat comImg, String watermark) {

        Scalar s = new Scalar(0x00, 0);
        Point p = new Point(comImg.size().width() / 3, comImg.size().height() / 3);

        // add text
        putText(comImg, watermark, p, CV_FONT_HERSHEY_COMPLEX, 1.5, s, 3,
                20, false);
        // 旋转图片
        flip(comImg, comImg, -1);

        putText(comImg, watermark, p, CV_FONT_HERSHEY_COMPLEX, 1.5, s, 3,
                20, false);
        flip(comImg, comImg, -1);
    }

    /**
     * 添加图片水印
     *
     * @param comImg 频谱图
     */
    private static void addImageWaterMark(Mat comImg, String watermark) {
        Mat wm = imread(watermark, CV_LOAD_IMAGE_GRAYSCALE);
        MatVector planes = new MatVector(2);
        wm.convertTo(wm, CV_32F);
        if (wm.empty()) {
            System.exit(1);
        }
        //same size
        createWaterMark(comImg, wm);
        //水印编码
        //...

        //same channel
        planes.put(0, wm);
        planes.put(1, wm);
        merge(planes, wm);

        //add mark
        addWeighted(wm, 0.5, comImg, 1, 0.0, comImg);
    }

    /**
     * 将图片水印对称并扩展成原图的大小
     *
     * @param comImg 原图频域图
     * @param wm 水印图
     */
    private static void createWaterMark(Mat comImg, Mat wm) {
        MatVector combine = new MatVector(2);
        Mat iwm = new Mat();
//        System.out.println(comImg.rows() / 2 - wm.rows());
//        System.out.println(comImg.cols() - wm.cols());
        copyMakeBorder(wm, wm, 0, comImg.rows() / 2 - wm.rows(),
                0, comImg.cols() - wm.cols(), BORDER_CONSTANT, Scalar.all(0));
        combine.put(0, wm);
        flip(wm, iwm, -1);
        combine.put(1, iwm);
        vconcat(combine, wm);
    }

//    小波变换
//    private static Mat startDWT(Mat srcImg) {
//
//        imi = new Mat(srcImg.rows(), srcImg.cols(), CV_8U);
//        srcImg.copyTo(imi);
//
//        srcImg.convertTo(srcImg, CV_32F, 1.0, 0.0);
//        im1 = new Mat(srcImg.rows() / 2, srcImg.cols(), CV_32F);
//        im2 = new Mat(srcImg.rows() / 2, srcImg.cols(), CV_32F);
//
//        im3 = new Mat(srcImg.rows() / 2, srcImg.cols() / 2, CV_32F);
//        im4 = new Mat(srcImg.rows() / 2, srcImg.cols() / 2, CV_32F);
//
//        im5 = new Mat(srcImg.rows() / 2, srcImg.cols() / 2, CV_32F);
//        im6 = new Mat(srcImg.rows() / 2, srcImg.cols() / 2, CV_32F);
//
//        FloatIndexer im1Indexer = im1.createIndexer();
//        FloatIndexer im2Indexer = im2.createIndexer();
//        FloatIndexer im3Indexer = im3.createIndexer();
//        FloatIndexer im4Indexer = im4.createIndexer();
//
//        for (int row = 0; row < srcImg.rows(); row += 2) {
//            int col = 0;
//            while (col < srcImg.cols()) {
//                FloatIndexer floatIndexer = srcImg.createIndexer();
//                a = floatIndexer.get(row, col);
//                b = floatIndexer.get(row + 1, col);
//                c = (float) ((a + b) * 0.707);
//                d = (float) ((a - b) * 0.707);
//
//                im1Indexer.put(row / 2, col, c);
//                im2Indexer.put(row / 2, col, d);
//                col++;
//            }
//        }
//
//        for (int row = 0; row < srcImg.rows() / 2; row++) {
//            int col = 0;
//            while (col < srcImg.cols() - 2) {
//                FloatIndexer floatIndexer = im1.createIndexer();
//                a = floatIndexer.get(row, col);
//                b = floatIndexer.get(row, col + 1);
//                c = (float) ((a + b) * 0.707);
//                d = (float) ((a - b) * 0.707);
//
//                im3Indexer.put(row, col / 2, c);
//                im4Indexer.put(row, col / 2, d);
//                col += 2;
//            }
//        }
//
//        for (int row = 0; row < srcImg.rows() / 2; row++) {
//            int col = 0;
//            while (col < srcImg.cols() - 2) {
//                FloatIndexer floatIndexer = im2.createIndexer();
//                a = floatIndexer.get(row, col);
//                b = floatIndexer.get(row, col + 1);
//                c = (float) ((a + b) * 0.707);
//                d = (float) ((a - b) * 0.707);
//
//                im1Indexer.put(row, col / 2, c);
//                im2Indexer.put(row, col / 2, d);
//                col += 2;
//            }
//        }
//
//        imr = Mat.zeros(srcImg.rows(), srcImg.cols(), CV_32F).asMat();
//        imd = Mat.zeros(512, 512, CV_32F).asMat();
//        im3.copyTo(imd.adjustROI(0, 0, 256, 256));
//        im4.copyTo(imd.adjustROI(0, 255, 256, 256));
//        im5.copyTo(imd.adjustROI(255, 0, 256, 256));
//        im6.copyTo(imd.adjustROI(255, 255, 256, 256));
//
//        return srcImg;
//    }
//
//    private static void inverseDWF(Mat comImg) {
//
//        im11 = Mat.zeros(comImg.rows() / 2, comImg.cols(), CV_32F).asMat();
//        im12 = Mat.zeros(comImg.rows() / 2, comImg.cols(), CV_32F).asMat();
//        im13 = Mat.zeros(comImg.rows() / 2, comImg.cols(), CV_32F).asMat();
//        im14 = Mat.zeros(comImg.rows() / 2, comImg.cols(), CV_32F).asMat();
//
//        FloatIndexer im3Indexer = im3.createIndexer();
//        FloatIndexer im4Indexer = im4.createIndexer();
//        FloatIndexer im5Indexer = im5.createIndexer();
//        FloatIndexer im6Indexer = im6.createIndexer();
//        FloatIndexer im11Indexer = im11.createIndexer();
//        FloatIndexer im12Indexer = im12.createIndexer();
//        FloatIndexer im13Indexer = im13.createIndexer();
//        FloatIndexer im14Indexer = im14.createIndexer();
//
//        for (int row = 0; row < comImg.rows() / 2; row++) {
//            int col = 0;
//            while (col < comImg.cols() / 2) {
//                im11Indexer.put(row, col, im3Indexer.get(row, col));
//                im12Indexer.put(row, col, im4Indexer.get(row, col));
//                im13Indexer.put(row, col, im5Indexer.get(row, col));
//                im14Indexer.put(row, col, im6Indexer.get(row, col));
//                col++;
//            }
//        }
//
//        for (int row = 0; row < comImg.rows() / 2; row++) {
//            int col = 0;
//            while (col < comImg.cols() - 2) {
//                a = im11Indexer.get(row, col);
//                b = im12Indexer.get(row, col);
//                c = (float) ((a + b) * 0.707);
//                im11Indexer.put(row, col, c);
//                d = (float) ((a - b) * 0.707);
//                im11Indexer.put(row, col + 1, d);
//
//                a = im13Indexer.get(row, col);
//                b = im14Indexer.get(row, col);
//                c = (float) ((a + b) * 0.707);
//                im13Indexer.put(row, col, c);
//                d = (float) ((a - b) * 0.707);
//                im13Indexer.put(row, col + 1, d);
//                col += 2;
//            }
//        }
//
//        temp = Mat.zeros(comImg.rows(), comImg.cols(), CV_32F).asMat();
//        FloatIndexer imrIndexer = imr.createIndexer();
//        FloatIndexer tempIndexer = temp.createIndexer();
//
//        for (int row = 0; row < comImg.rows() / 2; row++) {
//            int col = 0;
//            while (col < comImg.cols()) {
//                imrIndexer.put(row, col, im11Indexer.get(row, col));
//                tempIndexer.put(row, col, im13Indexer.get(row, col));
//                col++;
//            }
//        }
//
//        for (int row = 0; row < comImg.rows() - 2; row += 2) {
//            int col = 0;
//            while (col < comImg.cols()) {
//                a = imrIndexer.get(row, col);
//                b = tempIndexer.get(row, col);
//                c = (float) ((a + b) * 0.707);
//                imrIndexer.put(row, col, c);
//                d = (float) ((a - b) * 0.707);
//                imrIndexer.put(row + 1, col, d);
//                col++;
//            }
//        }
//    }

    private static void help() {
        System.out.println("Usage: java -jar BlindWaterMark.jar <commands> [args...] \n" +
                "   commands: \n" +
                "       encode <option> <image-src>  <watermark-text>       <image-encoded(text)>\n" +
                "       encode <option> <image-src>  <watermark-image>      <image-encoded(image)>\n" +
                "       decode <image-encode(text)>  <image-decode>\n" +
                "       decode <image-src>           <image-encoded(image)> <image-decode>\n" +
                "   options: \n" +
                "       -i add image watermark\n" +
                "       -t add text  watermark\n"
        );
        System.exit(-1);
    }
}
