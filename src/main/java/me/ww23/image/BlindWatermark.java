package me.ww23.image;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class BlindWatermark {

    public static void main(String[] args) {

       if (args.length == 0) {
           help();
       }

       String command = args[0];

       String arg1;
       String arg2;
       String arg3;
       String arg4;

       switch (command) {
           case "encode":
               arg1 = args[1];
               if (args.length < 5) {
                   help();
               }
               arg2 = args[2];
               arg3 = args[3];
               arg4 = args[4];
               if (arg1.equals("-t")) {
                   encode(arg2, arg3, arg4, true);
               } else if (arg1.equals("-i")) {
                   encode(arg2, arg3, arg4, false);
               }
               System.out.println("ENCODE SUCCESSFUL");
               break;
           case "decode":
               arg1 = args[1];
               arg2 = args[2];
               if (args.length == 3) {
                   decode(arg1, arg2);
               } else if (args.length == 4) {
                   arg3 = args[3];
                   decode(arg1, arg2, arg3);
               } else {
                   help();
               }
               System.out.println("DECODE SUCCESSFUL");
               break;
           default:
               help();
       }

//         encode("gakki.png", "mywife", "gakki-wm-text.jpg", true);
//         decode("gakki-wm-text.jpg", "gakki-text-dc.jpg");
    }

    /**
     * 添加水印
     *
     * @param image     原图
     * @param watermark 水印
     * @param output    水印为本 或 图片水印路径
     * @param option    false:图片水印 true:文字水印
     */
    private static void encode(String image, String watermark, String output, Boolean option) {

        //load image
        Mat srcImg = imread(image, CV_LOAD_IMAGE_COLOR);

        if (srcImg.empty()) {
            System.exit(1);
        }

        //merge color channels
        MatVector newPlanes = new MatVector(3);

        //split color channels
        MatVector color = new MatVector(3);
        split(srcImg, color);

        MatVector[] planes = {new MatVector(2), new MatVector(2), new MatVector(2)};
        for (int i = 0; i < color.size(); i++) {
            color.get(i).convertTo(color.get(i), CV_32F);
            Mat comImg = startDFT(color.get(i));
            if (option) {
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
    private static void decode(String wmImg, String output) {

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
    private static void decode(String srcImg, String wmImg, String output) {
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
//        dct(comImg, comImg);
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
//        idct(comImg, comImg);
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
    private static Mat optimizedImage(Mat srcImg) {
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
     * @param wm     水印图
     */
    private static void createWaterMark(Mat comImg, Mat wm) {
        MatVector combine = new MatVector(2);
        Mat iwm = new Mat();

        copyMakeBorder(wm, wm, 0, comImg.rows() / 2 - wm.rows(),
                0, comImg.cols() - wm.cols(), BORDER_CONSTANT, Scalar.all(0));
        combine.put(0, wm);
        flip(wm, iwm, -1);
        combine.put(1, iwm);
        vconcat(combine, wm);
    }

    private static void help() {
        System.out.println("Usage: java -jar InvisibleWatermark.jar <commands> [args...] \n" +
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
