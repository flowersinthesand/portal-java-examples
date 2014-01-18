package io.github.flowersinthesand.portal.example;

import io.github.flowersinthesand.portal.DefaultServer;
import io.github.flowersinthesand.portal.Server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan
@EnableScheduling
public class AppConfig {

	// Make Server as a bean
	@Bean
	public Server server() {
		// HttpExchange and WebSocket actions will be configured in Bootstrap
		// class. If you are using WebApplicationContext, you can do that
		// here injecting ServletContext
		return new DefaultServer();
	}

}
