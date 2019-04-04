package com.inforefiner.cloud.log.config;


import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

@Configuration
//@EnableElasticsearchRepositories(basePackages = "com.merce.woven.app.repository")
public class EsConfig {

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private int port;

    @Value("${elasticsearch.clusterName}")
    private String clusterName;

    @Value("${elasticsearch.timeout:60s}")
    private String timeout;

    @Value("${elasticsearch.username:admin}")
    public String username;

    @Value("${elasticsearch.password:123456}")
    public String password;

    @Bean
    public TransportClient client() throws Exception {
        Settings settings = Settings.builder()
                .put("cluster.name", clusterName)
                .build();
        return new PreBuiltTransportClient(settings).addTransportAddress(
                new InetSocketTransportAddress(InetAddress.getByName(host), port));

    }
}
