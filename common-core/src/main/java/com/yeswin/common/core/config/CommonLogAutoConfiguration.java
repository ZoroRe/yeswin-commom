package com.yeswin.common.core.config;

import com.yeswin.common.core.aspect.AbstractWebLogAspect;
import com.yeswin.common.core.aspect.SimpleWebLogAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
// TODO: 2022/11/3
@ComponentScan("com.yeswin")
public class CommonLogAutoConfiguration {

    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnMissingBean(AbstractWebLogAspect.class)
    public SimpleWebLogAspect simpleWebLogAspect() {
        log.info("注入简单切面类");
        return new SimpleWebLogAspect();
    }
}
