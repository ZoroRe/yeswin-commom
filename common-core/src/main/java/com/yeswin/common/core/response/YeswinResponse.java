package com.yeswin.common.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonNaming
@ToString
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YeswinResponse<T> {
    private int code;
    private String msg;
    private YeswinPage page;
    private T data;


    public YeswinResponse() {
    }

    public YeswinResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static YeswinResponse fail(int code, String msg) {
        YeswinResponse response = new YeswinResponse(code, msg);
        return response;
    }

    public YeswinResponse page(long total, int page){
        this.page = new YeswinPage();
        this.page.setTotal(total);
        this.page.setPage(page);
        return this;
    }

    public static YeswinResponse success() {
        YeswinResponse response = new YeswinResponse(YeswinCode.OK, "OK");
        return response;
    }

    public static YeswinResponse successWithMsg(String msg) {
        YeswinResponse response = new YeswinResponse();
        response.code= YeswinCode.OK;
        response.msg = msg;
        return response;
    }

    public static <T> YeswinResponse<T> success(T data){
        YeswinResponse response = YeswinResponse.success();
        response.data = data;
        return response;
    }
}
