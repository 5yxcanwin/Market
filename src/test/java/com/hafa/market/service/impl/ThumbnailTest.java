package com.hafa.market.service.impl;

import net.coobird.thumbnailator.Thumbnails;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

/**
 * @author heavytiger
 * @version 1.0
 * @description 测试压缩图片
 * @date 2022/4/23 22:44
 */
@SpringBootTest
class ThumbnailTest {
    /**
     * 生成等宽的缩略图，高度不定
     */
    @Test
    public void testCreatePic() {
        try {
            Thumbnails.of("C:\\Users\\DCM\\Desktop\\test.jpg")
                    .width(300)
                    .keepAspectRatio(true)
                    .toFile("C:\\Users\\DCM\\Desktop\\test_thumb.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
