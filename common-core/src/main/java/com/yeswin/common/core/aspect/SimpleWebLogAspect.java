package com.yeswin.common.core.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Aspect
public class SimpleWebLogAspect extends AbstractWebLogAspect {
    public SimpleWebLogAspect() {
        log.info("------------------注入简单切面日志记录--------------------");
    }
}
