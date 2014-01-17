package io.github.flowersinthesand.portal.examples;

import io.github.flowersinthesand.portal.DefaultServer;
import io.github.flowersinthesand.portal.Server;
import io.github.flowersinthesand.portal.Socket;
import io.github.flowersinthesand.portal.Socket.Reply;
import io.github.flowersinthesand.wes.Action;
import io.github.flowersinthesand.wes.VoidAction;
import io.github.flowersinthesand.wes.vertx.VertxBridge;

import java.util.Timer;
import java.util.TimerTask;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Verticle;

public class Bootstrap extends Verticle {

	@Override
	public void start() {
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
		
		HttpServer httpServer = vertx.createHttpServer();
		
		// Attach request handler and WebSocket handler first before installation
		httpServer.requestHandler(new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest req) {
				String filename = req.path().equals("/") ? "/index.html" : req.path();
				req.response().sendFile("webapp" + filename);
			}
		});
		
		// Deliver HttpExchange and WebSocket produced by Vert.x to the portal server
		new VertxBridge(httpServer, "/test").httpAction(server.httpAction()).websocketAction(server.websocketAction());
		
		// Start a web server after installation
		httpServer.listen(8080);
	}
	
}
