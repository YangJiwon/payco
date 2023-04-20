package com.was;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.junit.BeforeClass;
import org.junit.Test;

import com.was.common.LoadConfig;
import com.was.model.Settings;

public class RequestProcessorTest {
	private final String Host = "http://www.server1.com:8080/";
	private static HttpServer webServer;
	private static Settings settings;

	@BeforeClass
	public static void setUp() throws IOException {
		settings = new LoadConfig().load();
		webServer = new HttpServer(8080);

		new Thread(() -> {
			try {
				webServer.start(settings);
			} catch (IOException ignored) {}
		}).start();
	}

	@Test
	public void 정상() throws IOException {
		HttpUriRequest request = new HttpGet(Host);

		int statusCode = getStatusCode(request);
		assertEquals(statusCode, 200);
	}

	@Test
	public void 제한된_확장자() throws IOException {
		HttpUriRequest request = new HttpGet(Host + ".exe");

		int statusCode = getStatusCode(request);
		assertEquals(statusCode, 403);
	}

	@Test
	public void 루트_디렉토리_접근() throws IOException {
		HttpUriRequest request = new HttpGet(Host + "../../");

		int statusCode = getStatusCode(request);
		assertEquals(statusCode, 403);
	}

	@Test
	public void 존재하지않는_루트_접근() throws IOException {
		HttpUriRequest request = new HttpGet(Host + "NotExist");

		int statusCode = getStatusCode(request);
		assertEquals(statusCode, 404);
	}

	@Test
	public void Hello_구현체_접근() throws IOException {
		HttpUriRequest request = new HttpGet(Host + "Hello");

		String msg = getMsg(request);
		assertEquals(msg, "Hello");
	}

	@Test
	public void service_Hello_구현체_접근() throws IOException {
		HttpUriRequest request = new HttpGet(Host + "service.Hello");

		String msg = getMsg(request);
		assertEquals(msg, "Service Hello");
	}

	@Test
	public void 쿼리스트링값_확인() throws IOException {
		String param1 = "name";
		String param2 = "name2";
		HttpUriRequest request = new HttpGet(String.format("%sHello?param=%s&param2=%s", Host, param1, param2));

		String msg = getMsg(request);
		assertEquals(msg, String.format("Hello %s %s", param1, param2));
	}

	@Test
	public void Hello_매핑_구현체_접근() throws IOException {
		HttpUriRequest request = new HttpGet(Host + "Greeting");

		String msg = getMsg(request);
		assertEquals(msg, "Hello");
	}

	@Test
	public void service_Hello_매핑_구현체_접근() throws IOException {
		HttpUriRequest request = new HttpGet(Host + "super.Greeting");

		String msg = getMsg(request);
		assertEquals(msg, "Service Hello");
	}

	@Test
	public void body값_확인() throws IOException {
		String postParam1 = "postName";

		HttpPost request = new HttpPost(Host + "Hello");
		StringEntity requestEntity = new StringEntity(String.format("{\"postParam1\" : \"%s\"}", postParam1), "utf-8");
		requestEntity.setContentType(new BasicHeader("Content-Type", "application/json"));
		request.setEntity(requestEntity);

		String msg = getMsg(request);
		assertEquals(msg, String.format("Hello %s", postParam1));
	}

	private int getStatusCode(HttpUriRequest request) throws IOException {
		HttpResponse httpResponse = HttpClientBuilder.create().build().execute( request );
		return httpResponse.getStatusLine().getStatusCode();
	}

	private String getMsg(HttpUriRequest request) throws IOException {
		HttpResponse httpResponse = HttpClientBuilder.create().build().execute( request );
		InputStream inputStream = httpResponse.getEntity().getContent();

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		for (int data = inputStream.read(); data != -1; data = inputStream.read()) {
			byteArrayOutputStream.write(data);
		}
		return byteArrayOutputStream.toString();
	}
}
