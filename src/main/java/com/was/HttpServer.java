package com.was;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.was.common.LoadConfig;
import com.was.model.Settings;

public class HttpServer {
	private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
	private static final int NUM_THREADS = 50;
	private final int port;

	public HttpServer(int port) throws IOException {
		this.port = port;
	}

	public static void main(String[] args) {
		Settings settings = new LoadConfig().load();
		int port = settings.getPort();

		try {
			HttpServer webserver = new HttpServer(port);
			webserver.start(settings);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void start(Settings settings) throws IOException {
		ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
		try (ServerSocket server = new ServerSocket(port)) {
			while (true) {
				try {
					Socket request = server.accept();
					Runnable r = new RequestProcessor(request, settings);
					pool.submit(r);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}
}
