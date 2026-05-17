package com.agrodrone;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@MapperScan("com.agrodrone.mapper")
public class AgroDroneApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgroDroneApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("农业无人机智能作业调度平台后端启动成功：http://localhost:8081");
    }
}
