package io.github.flowersinthesand.portal.testsuite;

import io.github.flowersinthesand.portal.DefaultServer;
import io.github.flowersinthesand.portal.Server;
import io.github.flowersinthesand.portal.Socket;
import io.github.flowersinthesand.portal.Socket.Reply;
import io.github.flowersinthesand.wes.Action;
import io.github.flowersinthesand.wes.VoidAction;
import io.github.flowersinthesand.wes.jwa.JwaBridge;
import io.github.flowersinthesand.wes.servlet.ServletBridge;

import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;

// This class is instantiated twice as a ServletContextListener and a ServerApplicationConfig
@WebListener
public class Bootstrap implements ServletContextListener, ServerApplicationConfig {

	// We need to declare server as static 
	// since there is no way to share this field without special measure
	static final Server server;
	static {
		// Create a portal server 
		server = new DefaultServer();
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
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		// Deliver HttpExchange produced by Servlet to the portal server
		new ServletBridge(event.getServletContext(), "/test").httpAction(server.httpAction());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {}

	@Override
	public Set<ServerEndpointConfig> getEndpointConfigs( Set<Class<? extends Endpoint>> classes) {
		// Deliver WebSocket produced by JWA to the portal server
		return Collections.singleton(new JwaBridge("/test").websocketAction(server.websocketAction()).config());
	}

	@Override
	public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
		return Collections.emptySet();
	}

}
