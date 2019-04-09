package com.inforefiner.cloud.log.config;


import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

//import org.elasticsearch.common.transport.InetSocketTransportAddress;

@Configuration
public class EsConfig {

    private Logger logger = LoggerFactory.getLogger(EsConfig.class);

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private int port;

    @Value("${elasticsearch.cluster.name}")
    private String clusterName;

    @Value("${elasticsearch.timeout:60s}")
    private String timeout;

    @Value("${elasticsearch.username:admin}")
    public String username;

    @Value("${elasticsearch.password:123456}")
    public String password;

    @Bean
    public Client client() throws Exception {
        logger.info("creating es client with cluster.name = {}, host = {}, port = {}", clusterName, host, port);
        Settings settings = Settings.builder()
                .put("cluster.name", clusterName)
                .put("client.transport.sniff", false)
                .put("xpack.security.user", username + ":" + password)
                .build();
        return new PreBuiltXPackTransportClient(settings).addTransportAddress(
                new InetSocketTransportAddress(InetAddress.getByName(host), port));

    }
}
