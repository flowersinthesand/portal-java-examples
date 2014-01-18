package io.github.flowersinthesand.portal.example;

import io.github.flowersinthesand.portal.Server;
import io.github.flowersinthesand.portal.Socket;
import io.github.flowersinthesand.wes.Action;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

@Component
public class Broadcaster {

	// Inject server
	@Autowired
	private Server server;
	private Faker faker = new Faker();

	@Scheduled(fixedDelay = 4000)
	void broadcast() {
		// Create a fake message
		final Map<String, String> message = new LinkedHashMap<>();
		message.put("content", faker.paragraph());
		message.put("source", faker.name());

		// Broadcast a message to all socket in the server
		server.all(new Action<Socket>() {
			@Override
			public void on(Socket socket) {
				socket.send("message", message);
			}
		});
	}

}
