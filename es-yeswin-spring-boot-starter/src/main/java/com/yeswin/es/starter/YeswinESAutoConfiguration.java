package com.yeswin.es.starter;


import com.yeswin.es.starter.config.YeswinESProperty;
import jakarta.annotation.Resource;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.Objects;

@Configuration
@ConditionalOnClass(RestHighLevelClient.class)
@EnableConfigurationProperties(YeswinESProperty.class)
@ComponentScan(basePackages = "com.yeswin.es.starter")
public class YeswinESAutoConfiguration {

    @Resource
    private YeswinESProperty yeswinESProperty;

    /**
     * 无需账号密码认证的客户端
     *
     * @return
     */
//    @Bean
//    @ConditionalOnMissingBean(RestHighLevelClient.class)
//    public RestHighLevelClient initNoneAuthHlcClient() {
//        HttpHost[] hosts = Arrays.stream(yeswinESProperty.getHost().split(",")).map((host) -> {
//            String[] hostParts = host.split(":");
//            String hostName = hostParts[0];
//            Integer port = Integer.valueOf(hostParts[1]);
//            return new HttpHost(hostName, port, HttpHost.DEFAULT_SCHEME_NAME);
//        }).filter(Objects::nonNull).toArray(HttpHost[]::new);
//
//        return new RestHighLevelClient(RestClient.builder(hosts));
//    }


    /**
     * 需要账号密码认证的客户端
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(RestHighLevelClient.class)
    @Primary
    public RestHighLevelClient initAuthHlcClient() {
        HttpHost[] hosts = Arrays.stream(yeswinESProperty.getHost().split(",")).map((host) -> {
            return new HttpHost(host, yeswinESProperty.getPort(), "https");
        }).filter(Objects::nonNull).toArray(HttpHost[]::new);

        // 生成凭证(明文密码，非明文需要先处理)
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(yeswinESProperty.getEsUser(), yeswinESProperty.getEsPassword()));

        //返回带验证的客户端
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(hosts)
                        .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                            @Override
                            public HttpAsyncClientBuilder customizeHttpClient
                                    (HttpAsyncClientBuilder httpClientBuilder) {
                                httpClientBuilder.disableAuthCaching();
                                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                            }
                        }));
        return client;
    }
}
