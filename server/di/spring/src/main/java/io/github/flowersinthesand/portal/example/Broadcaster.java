package io.github.flowersinthesand.portal.example;

import io.github.flowersinthesand.portal.Server;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Broadcaster {

	// Inject server
	@Autowired
	private Server server;

	@Scheduled(fixedDelay = 5000)
	private void broadcast() {
		// Create a message
		final Map<String, String> message = new LinkedHashMap<>();
		message.put("username", "System");
		message.put("message", "May I have your attention please.");

		// Broadcast a message to all the socket in the server
		server.all().send("message", message);
	}

}
