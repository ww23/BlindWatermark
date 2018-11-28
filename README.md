
# BlindWatermark
[![Travis (.org)](https://img.shields.io/travis/USER/REPO.svg)](https://travis-ci.org/ww23/BlindWatermark)

Java 盲水印
在图片上加肉眼无法分辨的水印

* 原理
     * Encode:  
     原图 --- 傅立叶变换 ---> 频域图 + 水印 --- 逆变换 ---> 带水印图
     * Decode(文字水印):  
     带水印图 --- 傅立叶变换 ---> 频域图
     * Decode(图片水印):  
     原图 --- 傅立叶变换 --> 原图频域图  
     带水印图 --- 傅立叶变换 ---> 频域图 - 原图频域图

* 文件  
    ├── gakki-img-dc.png   * 反解水印图(图片水印)  
    ├── gakki-text-dc.png  * 反解水印图(文字水印)  
    ├── gakki-wm-img.png   * 带图片盲水印的图  
    ├── gakki-wm-text.png  * 带文字盲水印的图  
    ├── gakki.png          * 原图  
    └── wm.png             * 图片水印  

### Usage:
    
    Usage: java -jar BlindWatermark.jar <commands> [args...]
        commands:
                encode <option> <image-src>  <watermark-text>       <image-encoded(text)>
                encode <option> <image-src>  <watermark-image>      <image-encoded(image)>
                decode <image-encode(text)>  <image-decode>
                decode <image-src>           <image-encoded(image)> <image-decode>
        options:
                -i add image watermark
                -t add text  watermark
### Build:

	gradle build

### Demo:

    原图:
![image](https://github.com/ww23/BlindWatermark/blob/master/gakki.png)

    加文字水印:
    java -jar BlindWatermark.jar encode -t gakki.png mywife gakki-wm-text.png
![image](https://github.com/ww23/BlindWatermark/blob/master/gakki-wm-text.png)

    加图片水印:
    java -jar BlindWatermark.jar encode -i gakki.png wm.png gakki-wm-img.png
![image](https://github.com/ww23/BlindWatermark/blob/master/wm.png)
![image](https://github.com/ww23/BlindWatermark/blob/master/gakki-wm-img.png)

    文字水印解码:
    java -jar BlindWatermark.jar decode gakki-wm-text.png gakki-text-dc.png
![image](https://github.com/ww23/BlindWatermark/blob/master/gakki-text-dc.png)

    图片水印解码:
    java -jar BlindWatermark.jar decode gakki.png gakki-wm-img.png gakki-img-dc.png
![image](https://github.com/ww23/BlindWatermark/blob/master/gakki-img-dc.png)

### Reference:

* [OpenCV](https://docs.opencv.org/3.4/d8/d01/tutorial_discrete_fourier_transform.html) Discrete Fourier Transform	
