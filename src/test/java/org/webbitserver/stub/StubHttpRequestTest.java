package org.webbitserver.stub;

import static org.junit.Assert.assertEquals;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class StubHttpRequestTest {

	@Test
	public void testUri() throws Exception {
		StubHttpRequest target = new StubHttpRequest();
		assertEquals("/", target.uri());

		target.uri("https://github.com/joewalnes/webbit");
		assertEquals("https://github.com/joewalnes/webbit", target.uri());

		StubHttpRequest targetUri = new StubHttpRequest("https://github.com/joewalnes/webbit");
		assertEquals("https://github.com/joewalnes/webbit", targetUri.uri());
	}

	@Test
	public void testHeader() throws Exception {
		StubHttpRequest target = new StubHttpRequest();
		assertEquals(false, target.hasHeader("Content-Length"));
		assertEquals(null, target.header("Content-Length"));

		target.header("Content-Length", "23");
		assertEquals(true, target.hasHeader("Content-Length"));

		List<Map.Entry<String, String>> expected = new ArrayList<Map.Entry<String, String>>();
		expected.add(new AbstractMap.SimpleEntry<String, String>("Content-Length", "23"));

		assertEquals(expected, target.allHeaders());

		assertEquals(null, target.header("charset"));
		assertEquals("23", target.header("Content-Length"));

		target.header("charset", "utf8");
		expected.add(new AbstractMap.SimpleEntry<String, String>("charset", "utf8"));
		assertEquals(expected, target.allHeaders());
	}

	@Test
	public void testQueryParam() throws Exception {
		StubHttpRequest target = new StubHttpRequest();
		assertEquals(null, target.queryParam(null));

		StubHttpRequest targetUri = new StubHttpRequest("https://g.com/?a=12");
		assertEquals("12", targetUri.queryParam("a"));
	}

	@Test
	public void testQueryParamKeys() throws Exception {
		StubHttpRequest target = new StubHttpRequest("https://g.com/?a=12&b=hello");

		Set<String> expected = new HashSet<String>();
		expected.add("b");
		expected.add("a");

		assertEquals(expected, target.queryParamKeys());
	}

	@Test
	public void testMethod() throws Exception {
		StubHttpRequest target = new StubHttpRequest();
		assertEquals("GET", target.method());

		target.method("POST");
		assertEquals("POST", target.method());
	}

	@Test
	public void testBody() throws Exception {
		StubHttpRequest target = new StubHttpRequest();
		assertEquals(null, target.body());

		target.body("<h1>Hello World!</h1>");
		assertEquals("<h1>Hello World!</h1>", target.body());
	}

	@Test
	public void testData() throws Exception {
		StubHttpRequest target = new StubHttpRequest();

		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("joe", "bloggs");

		assertEquals(expected, target.data("joe", "bloggs").data());
	}

	@Test
	public void testRemoteAddress() throws Exception {
		StubHttpRequest target = new StubHttpRequest();

		SocketAddress expected = new InetSocketAddress("localhost", 0);
		assertEquals(expected, target.remoteAddress());

		target.remoteAddress(null);
		assertEquals(null, target.remoteAddress());

		SocketAddress newRemote = new InetSocketAddress("mysite.com", 80);
		assertEquals(newRemote, target.remoteAddress(newRemote).remoteAddress());
	}

	@Test
	public void testId() throws Exception {
		StubHttpRequest target = new StubHttpRequest();
		assertEquals("StubID", target.id());

		target.id(1234);
		assertEquals(1234, target.id());
	}

	@Test
	public void testTimestamp() throws Exception {
		StubHttpRequest target = new StubHttpRequest();
		assertEquals(0L, target.timestamp());

		target.timestamp(50L);
		assertEquals(50L, target.timestamp());
	}

}
