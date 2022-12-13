package com.yeswin.common.core.response;

import lombok.Data;

@Data
public class YeswinPage {
    private int page;
    private long total;


    public static long fixedPage(long page) {
       if (page < 1) {
           page = 1;
       }
       return page;
    }
}
