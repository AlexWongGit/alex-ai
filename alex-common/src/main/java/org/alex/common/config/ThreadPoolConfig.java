package org.alex.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description: 线程池配置
 * @Author wangzf
 * @Date 2025/3/17
 */
@Configuration
@EnableAsync
@RefreshScope
public class ThreadPoolConfig {

    @Value("${executor.queue-size:100}")
    private int queueCapacity;

    @Value("${executor.keep-alive-seconds:60}")
    private int keepAliveSeconds;


    @Bean(name = "commonExecutor")
    @Primary
    public TaskExecutor exportExecutor() {
        ThreadPoolTaskExecutor taskExecutor  = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        taskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.setThreadNamePrefix("comomon-");
        taskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();
        return taskExecutor;
    }

}
