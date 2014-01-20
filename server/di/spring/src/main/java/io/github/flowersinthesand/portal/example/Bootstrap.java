package io.github.flowersinthesand.portal.example;

import io.github.flowersinthesand.portal.Server;
import io.github.flowersinthesand.wes.atmosphere.AtmosphereBridge;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@WebListener
public class Bootstrap implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		// Create an application context configured by annotation
		@SuppressWarnings("resource")
		ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		
		// Find a server as a component managed by Spring
		final Server server = context.getBean(Server.class);
		
		// Deliver HttpExchange and WebSocket produced by Atmosphere to the portal server
		new AtmosphereBridge(event.getServletContext(), "/chat").httpAction(server.httpAction()).websocketAction(server.websocketAction());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {}

}
