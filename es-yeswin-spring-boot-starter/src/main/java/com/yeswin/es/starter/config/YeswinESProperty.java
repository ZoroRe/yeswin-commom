package com.yeswin.es.starter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yeswin.es")
public class YeswinESProperty {

    @Value("${yeswin.es.rest.host:https://127.0.0.1}")
    private String host;
    @Value("${yeswin.es.rest.port:9200}")
    private Integer port;
    @Value("${yeswin.es.rest.username}")            //读取ES用户名
    private String esUser;
    @Value("${yeswin.es.rest.password}")            //读取ES密码
    private String esPassword;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getEsUser() {
        return esUser;
    }

    public void setEsUser(String esUser) {
        this.esUser = esUser;
    }

    public String getEsPassword() {
        return esPassword;
    }

    public void setEsPassword(String esPassword) {
        this.esPassword = esPassword;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
