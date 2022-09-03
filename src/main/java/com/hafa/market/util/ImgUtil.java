package com.hafa.market.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author heavytiger
 * @version 1.0
 * @description 图片处理工具类
 * @date 2022/4/23 23:35
 */
public class ImgUtil {
    public static boolean png2jpg(InputStream in, File file) throws IOException {
        BufferedImage pngImage = ImageIO.read(in);
        BufferedImage newBufferedImage = new BufferedImage(pngImage.getWidth(), pngImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        newBufferedImage.createGraphics().drawImage(pngImage, 0, 0, Color.WHITE, null);
        ImageIO.write(newBufferedImage, "jpg", file);
        return true;
    }
}
