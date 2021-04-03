package com.caid.utopia.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("!test")
public class WebConfig {

	@Bean
	@LoadBalanced
	public RestTemplate getRT() {
		return new RestTemplate();
	}
}
