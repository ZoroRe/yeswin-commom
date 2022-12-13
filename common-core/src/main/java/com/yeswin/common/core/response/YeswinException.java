package com.yeswin.common.core.response;

import lombok.Data;

@Data
public class YeswinException extends RuntimeException {
    private int code;
    private String msg;

    public YeswinException(int code, String msg) {
        super(msg);
    }
}
