package io.github.flowersinthesand.portal.example;

import io.github.flowersinthesand.portal.ClusteredServer;
import io.github.flowersinthesand.portal.Socket;
import io.github.flowersinthesand.wes.Action;
import io.github.flowersinthesand.wes.atmosphere.AtmosphereBridge;

import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.instance.HazelcastInstanceFactory;

@WebListener
public class Bootstrap implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		// Use ClusteredServer
		final ClusteredServer server = new ClusteredServer();
		
		// Prepare a HazelcastInstance and its distributed topic
		HazelcastInstance hazelcast = HazelcastInstanceFactory.newHazelcastInstance(new Config());
		final ITopic<Map<String, Object>> topic = hazelcast.getTopic("wes:/chat");
		
		// Deliver message produced by Hazelcast to ClusteredServer
		topic.addMessageListener(new MessageListener<Map<String, Object>>() {
			@Override
			public void onMessage(Message<Map<String, Object>> message) {
				server.messageAction().on(message.getMessageObject());
			}
		});
		// Deliver message produced by ClusteredServer to Hazelcast 
		server.publishAction(new Action<Map<String, Object>>() {
			@Override
			public void on(Map<String, Object> message) {
				topic.publish(message);
			}
		});
		
		// Register a socket action for simple chat
		server.socketAction(new Action<Socket>() {
			@Override
			public void on(Socket socket) {
				socket.on("message", new Action<Map<String, String>>() {
					@Override
					public void on(Map<String, String> message) {
						// When sending event With Hazelcast, its data must be serializable
						server.all().send("message", message);
					}
				});
			}
		});
		
		// Deliver HttpExchange and WebSocket produced by Atmosphere to the portal server
		new AtmosphereBridge(event.getServletContext(), "/chat").httpAction(server.httpAction()).websocketAction(server.websocketAction());

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {}

}
