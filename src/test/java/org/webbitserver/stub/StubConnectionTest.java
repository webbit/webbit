package org.webbitserver.stub;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.junit.Test;
import org.webbitserver.netty.contrib.EventSourceMessage;

public class StubConnectionTest {

	@Test
	public void testNullHttpRequest() throws Exception {
		StubConnection target = new StubConnection(null);
		assertEquals(null, target.httpRequest());
	}

	@Test
	public void testSendMultipleStringMessages() throws Exception {
		StubConnection target = new StubConnection();
		
		target.send("testing");
		target.send("this");
		target.send("out");
		
		List<String> expected = new ArrayList<String>();
		expected.add("testing");
		expected.add("this");
		expected.add("out");
		
		assertEquals(expected, target.sentMessages());
	}
	
	@Test
	public void testSendEventSourceMessages() throws Exception {
		StubConnection target = new StubConnection(new StubHttpRequest());
		
		EventSourceMessage message = new EventSourceMessage("dummy data");
		target.send(message);
		
		List<String> expected = new ArrayList<String>();
		expected.add("data: dummy data\n");
		expected.add("\n");
		
		assertEquals(expected, target.sentMessages());
		
		target.send(message);
		expected.add("data: dummy data\n");
		expected.add("\n");
		
		assertEquals(expected, target.sentMessages());
	}

	@Test
	public void testPing() throws Exception {
		StubConnection target = new StubConnection(null);
		
		target.ping("ring!");
		target.ping("ping!");
		
		List<String> expected = new ArrayList<String>();
		expected.add("ring!");
		expected.add("ping!");
		
		assertEquals(expected, target.sentPings());
	}

	@Test
	public void testClosed() throws Exception {
		StubConnection target = new StubConnection(null);
		target.close();
		assertEquals(true, target.closed());
	}

	@Test
	public void testData() throws Exception {
		StubConnection target = new StubConnection(null);
		
		target.data("0", "test");
		target.data("1", "me");
		
		Map<String, String> expected = new HashMap<String, String>();
		expected.put("0", "test");
		expected.put("1", "me");
		
		assertEquals(expected, target.data());
	}

	@Test
	public void testHandlerExecutor() throws Exception {
		StubConnection target = new StubConnection(null);
		Executor actual = target.handlerExecutor();
		assertEquals(target, actual);
	}

	@Test
	public void testVersion() throws Exception {
		StubConnection target = new StubConnection(null);
		assertEquals(null, target.version());
		
		target.version("0.0.1");
		assertEquals("0.0.1", target.version());
	}
	
	@Test
	public void testRunnable() throws Exception {
		StubConnection target = new StubConnection();
		TestRunner e = new TestRunner();
		
		assertEquals(false, e.getRun());
		target.execute(e);
		assertEquals(true, e.getRun());
	}
	
	class TestRunner implements Runnable {
		private boolean run = false; 
        
		public void run() {
            run = true;
        }
		
		public boolean getRun() {
			return run;
		}
    }

}
