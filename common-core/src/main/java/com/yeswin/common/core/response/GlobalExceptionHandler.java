package com.yeswin.common.core.response;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(YeswinException.class)
    public YeswinResponse handleException(YeswinException exception) {
        return YeswinResponse.fail(exception.getCode(), exception.getMsg());
    }

    @ExceptionHandler(Exception.class)
    public YeswinResponse handleException(Exception exception) {
        log.error("接口调用异常, exceptionMsg:{} ", exception.getMessage(), exception);
        return YeswinResponse.fail(YeswinCode.DEFAULT_EXCEPTION_CODE, "请求异常，请稍后重试");
    }

    @ExceptionHandler(Throwable.class)
    public YeswinResponse handleThrowable(Throwable throwable) {
        log.error("接口调用异常，throwableMsg:{} ", throwable.getMessage(), throwable);
        return YeswinResponse.fail(YeswinCode.DEFAULT_THROWABLE_CODE, "请求异常，请稍后重试");
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public YeswinResponse handleHttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException exception) {
        log.error("接口调用异常，throwableMsg:{} ", exception.getMessage(), exception);
        return YeswinResponse.fail(YeswinCode.DEFAULT_HTTP_MEDIA_TYPE_NOT_ACCEPTABLE_CODE, "请求异常，请稍后重试");
    }
}
