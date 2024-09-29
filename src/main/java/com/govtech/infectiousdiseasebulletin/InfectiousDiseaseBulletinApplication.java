package com.govtech.infectiousdiseasebulletin;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.govtech.infectiousdiseasebulletin")
@ImportAutoConfiguration({FeignAutoConfiguration.class})
@EnableAsync
@EnableCaching
public class InfectiousDiseaseBulletinApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfectiousDiseaseBulletinApplication.class, args);
    }

}
