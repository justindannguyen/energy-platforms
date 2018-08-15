package com.justin.energyeurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EnergyEurekaServerApplication {

	public static void main(final String[] args) {
		SpringApplication.run(EnergyEurekaServerApplication.class, args);
	}
}
