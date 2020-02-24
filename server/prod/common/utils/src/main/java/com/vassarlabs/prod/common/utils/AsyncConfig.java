package com.vassarlabs.prod.common.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
     
	/**
	 * create thread pool for implementing Asynchronous call to external APIs
	 * @return
	 */
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
    	ThreadPoolTaskExecutor threadPoolExecutor = new ThreadPoolTaskExecutor();
    	threadPoolExecutor.setCorePoolSize(100);
    	threadPoolExecutor.setQueueCapacity(1000);
    	threadPoolExecutor.setThreadNamePrefix("APIAsync");
    	threadPoolExecutor.initialize();
    	
        return threadPoolExecutor;
    }
}
