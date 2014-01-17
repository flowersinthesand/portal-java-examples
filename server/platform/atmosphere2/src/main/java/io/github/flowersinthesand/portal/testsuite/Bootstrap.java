package io.github.flowersinthesand.portal.testsuite;

import io.github.flowersinthesand.portal.DefaultServer;
import io.github.flowersinthesand.portal.Server;
import io.github.flowersinthesand.portal.Socket;
import io.github.flowersinthesand.portal.Socket.Reply;
import io.github.flowersinthesand.wes.Action;
import io.github.flowersinthesand.wes.VoidAction;
import io.github.flowersinthesand.wes.atmosphere.AtmosphereBridge;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class Bootstrap implements ServletContextListener {
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		// Create a portal server
		Server server = new DefaultServer();
		
		// This action is equivalent to socket handler of server.js in the portal repo
		// https://github.com/flowersinthesand/portal/blob/1.1.1/test/server.js#L593-L611
		server.socketAction(new Action<Socket>() {
			@Override
			public void on(final Socket socket) {
				socket.on("echo", new Action<Object>() {
					@Override
					public void on(Object data) {
						socket.send("echo", data);
					}
				})
				.on("disconnect", new VoidAction() {
					@Override
					public void on() {
						new Timer(true).schedule(new TimerTask() {
							@Override
							public void run() {
								socket.close();
							}
						}, 100);
					}
				})
				.on("reply-by-server", new Action<Reply<Boolean>>() {
					@Override
					public void on(Reply<Boolean> reply) {
						if (reply.data()) {
							reply.done(reply.data());
						} else {
							reply.fail(reply.data());
						}
					}
				})
				.on("reply-by-client", new VoidAction() {
					@Override
					public void on() {
						socket.send("reply-by-client", 1, new Action<String>() {
							@Override
							public void on(String type) {
								socket.send(type);
							}
						});
					}
				});
			}
		});
		
		// Deliver HttpExchange and WebSocket produced by Atmosphere to the portal server
		new AtmosphereBridge(event.getServletContext(), "/test").httpAction(server.httpAction()).websocketAction(server.websocketAction());
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {}

}
