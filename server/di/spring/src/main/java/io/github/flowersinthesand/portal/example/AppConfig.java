package io.github.flowersinthesand.portal.example;

import java.util.Map;

import io.github.flowersinthesand.portal.DefaultServer;
import io.github.flowersinthesand.portal.Server;
import io.github.flowersinthesand.portal.Socket;
import io.github.flowersinthesand.wes.Action;

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
		// It's not required to attach socket action here
		final DefaultServer server = new DefaultServer();
		server.socketAction(new Action<Socket>() {
			@Override
			public void on(Socket socket) {
				socket.on("message", new Action<Map<String, Object>>() {
					@Override
					public void on(Map<String, Object> message) {
						server.all().send("message", message);
					}
				});
			}
		});

		// HttpExchange and WebSocket actions will be configured in Bootstrap
		// class. If you are using WebApplicationContext, you can do that
		// here injecting ServletContext
		return server;
	}

}
