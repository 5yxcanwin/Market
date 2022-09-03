package com.hafa.market.exceptions;

import com.hafa.market.dto.ResultBean;
import com.hafa.market.enums.EnumResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

/**
 * @author heavytiger
 * @version 1.0
 * @description 全局异常处理类
 * @date 2022/4/24 20:41
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 用于处理超出大小限制的异常
     * @param e 可能存在的超出大小限制的异常
     * @return 返回状态码
     */
    @ExceptionHandler(MultipartException.class)
    public ResultBean<Object> overMaxSizeException(MaxUploadSizeExceededException e) {
        if (e.getCause().getCause() instanceof FileSizeLimitExceededException){     //单个文件大小超出限制抛出的异常
            log.error(e.getMessage());
            return new ResultBean<>(EnumResult.OVER_MAX_SIZE);
        }else if (e.getCause().getCause() instanceof SizeLimitExceededException){       //总文件大小超出限制抛出的异常
            log.error(e.getMessage());
            return new ResultBean<>(EnumResult.ALL_OVER_MAX_SIZE);
        }
        return new ResultBean<>(EnumResult.INTERNAL_SERVER_ERROR);
    }

}
