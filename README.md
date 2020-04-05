
# BlindWatermark 

在图片上加隐藏的水印

* 原理
     * Encode:  
     原图 --- 变换 ---> 变换域 + 水印 --- 逆变换 ---> 带水印图
     * Decode:  
     带水印图 --- 变换 ---> 变换域

### Usage
    
    Usage: java -jar BlindWatermark.jar <commands>
        commands:
            encode <option> <original image> <watermark> <embedded image>
            decode <option> <original image> <embedded image>
        encode options:
            -c discrete cosine transform
            -f discrete fourier transform (Deprecated)
            -i image watermark
            -t text  watermark
        decode options:
            -c discrete cosine transform
            -f discrete fourier transform (Deprecated)
        example:
            encode -ct input.png watermark output.png
            decode -c  input.png output.png

### Build

	gradle build

### Demo

    原图:
![image](image/gakki-src.png)

    加文字水印:
    java -jar BlindWatermark.jar encode -ct gakki-src.png 测试test gakki-dct-text-ec.jpg
![image](image/gakki-dct-text-ec.jpg)

    文字水印解码:
    java -jar BlindWatermark.jar decode -c gakki-dct-text-ec.jpg gakki-dct-text-dc.jpg
![image](image/gakki-dct-text-dc.jpg)

    加图片水印:
    java -jar BlindWatermark.jar encode -ci gakki-src.png watermark.png gakki-dct-img-ec.jpg
![image](image/gakki-dct-img-ec.jpg)

    图片水印解码:
    java -jar BlindWatermark.jar decode -c gakki-dct-img-ec.jpg gakki-dct-img-dc.jpg
![image](image/gakki-dct-img-dc.jpg)

### License
[Apache-2.0](https://github.com/ww23/BlindWatermark/blob/master/LICENSE)