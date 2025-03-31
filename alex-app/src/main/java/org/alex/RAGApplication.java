package org.alex;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableDiscoveryClient
@EnableAspectJAutoProxy(proxyTargetClass = true)
@MapperScan(basePackages = "org.alex.rag.mapper")
@SpringBootApplication(scanBasePackages = {"org.alex"})
public class RAGApplication {

    public static void main(String[] args) {
        SpringApplication.run(RAGApplication.class, args);
    }
}
