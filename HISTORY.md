0.4.6 (2012-02-23)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.4.5...v0.4.6)

* WebSocketClient issues HTTP request using abs_path instead of absoluteUri. This is to work around [buggy servers](https://github.com/igrigorik/em-websocket/issues/73). (Aslak Hellesøy)
* Add missing `Connection: Upgrade` and `Upgrade: websocket` headers to WebSocketClient. (Aslak Hellesøy)

0.4.5 (2012-02-21)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.4.3...v0.4.5)

* Use `copiedBuffer` instead of `wrappedBuffer` to prevent outbound binary messages from being corrupted (Aslak Hellesøy)

0.4.3 (2012-02-17)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.4.2...v0.4.3)

* Log outbound binary websocket messages sent with offset and length (Aslak Hellesøy)
* Update docs (and related examples and wiki pages) to work for the `Future` API update in `WebServer` ([#78](https://github.com/webbit/webbit/issues/78) Kushal Pisavadia)
* Consolidated WebSocket client API with WebServer API ([#76](https://github.com/webbit/webbit/pull/76) Aslak Hellesøy)

0.4.2 (2012-02-13)
==================

(There is no 0.4.1 because NettyWebServerTest#stopsServerCleanlyNotLeavingResourcesHanging failed after Maven had tagged git)

[Full changelog](https://github.com/webbit/webbit/compare/v0.4.0...v0.4.2)

* Bugfix: WebSocketConnection.send(byte[] message, int offset, int length) would fail if offset was bigger than length (Aslak Hellesøy)

0.4.0 (2012-02-08)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.3.8...v0.4.0)

In previous versions of Webbit, `WebServer.start()` was synchronous and `WebServer.stop()` was asynchronous (You could call `WebServer.join()` to wait for a complete shutdown.
As of this release, both `WebServer.start()` and `WebServer.stop()` are asynchronous, and they return a `Future<WebServer>` that can be used to wait for the operation to complete.

* Removed `WebServer.join()`. ([#70](https://github.com/webbit/webbit/issues/70) Aslak Hellesøy)
* `WebServer.start()` is asynchronous. ([#70](https://github.com/webbit/webbit/issues/70) Aslak Hellesøy)
* Added WebSocketConnection.send(byte[] message, int offset, int length) (Aslak Hellesøy)
* `WebSocketConnection.ping(String)` is now `WebSocketConnection.ping(byte[])` ([#71](https://github.com/webbit/webbit/issues/70) Aslak Hellesøy)
* Added `WebSocketConnection.pong(byte[])` ([#71](https://github.com/webbit/webbit/issues/70) Aslak Hellesøy)
* `WebSocketConnection.onPong(WebSocketConnection, String)` is now `WebSocketConnection.onPong(WebSocketConnection, byte[])` ([#71](https://github.com/webbit/webbit/issues/70) Aslak Hellesøy)
* Added `WebSocketConnection.onPing(byte[])` ([#71](https://github.com/webbit/webbit/issues/70) Aslak Hellesøy)
* Added `BaseWebSocketHandler` that implements `WebSocketHandler` with no-ops (except for sending pings back as pongs) (Aslak Hellesøy)
* Added `WebSocketHandler.onClose` and `WebSocketHandler.onClose` throw `Throwable` instead of `Exception` for consistency with other methods. (Aslak Hellesøy)

0.3.8 (2012-02-01)
==================

(There is no 0.3.7 release due to Maven hiccups)

[Full changelog](https://github.com/webbit/webbit/compare/v0.3.6...v0.3.8)

* Added support for HTTPS ([#63](https://github.com/webbit/webbit/pull/63), [#9](https://github.com/webbit/webbit/issues/9) Michael Rykov)
* WebSocketClient can reconnect. (Aslak Hellesøy)
* Added support for wss:// in WebSocketClient (Aslak Hellesøy)

0.3.6 (2012-01-30)
==================

(There is no 0.3.5 because Aslak was releasing from a new machine, and release failed on the 1st attempt)

[Full changelog](https://github.com/webbit/webbit/compare/v0.3.4...v0.3.6)

* WebSocketClient no longer throws exceptions caused by passing in a null HttpRequest to the connection constructor (Aslak Hellesøy)

0.3.4 (2012-01-30)
==================

(There is no 0.3.3 release due to Aslak hiccups)

[Full changelog](https://github.com/webbit/webbit/compare/v0.3.2...v0.3.4)

* All Autobahn tests are passing, except for incorrect closing behaviour in 8 of them (Alex Silverstein, Aslak Hellesøy)
* Fixed a race condition where the first incoming WebSocket messages could be lost. ([#65](https://github.com/webbit/webbit/issues/65) Aslak Hellesøy)
* Moved org.webbitserver.wsclient.WebSocket to org.webbitserver.netty.WebSocketClient. (Aslak Hellesøy)
* Improved WebSocketClient so that it receives onClose events when closed locally. (Aslak Hellesøy)
* WebSocketClient must be start()ed - it no longer connects in the constructor. (Aslak Hellesøy)
* EventSourceMessage.build() appends the `\n` so that it doesn't have to be done in other places. (Aslak Hellesøy)

0.3.2 (2012-01-19)
==================

(There is no 0.3.1 release due to Maven hiccups)

[Full changelog](https://github.com/webbit/webbit/compare/v0.3.0...v0.3.2)

* New, experimental WebSocket client. API is subject to change in the next few releases. (Aslak Hellesøy)
* WebbitException.fromException no longer throws NullPointerException if the channel is null. (Phil Dawes, Aslak Hellesøy)
* Upgrade to Netty 3.2.7. (Aslak Hellesøy)
* Removed deprecated StubWebSocketConnection and EventSourceConnection.send(String). (Aslak Hellesøy)

0.3.0 (2011-12-02)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.2.16...v0.3.0)

* Support HTTP partial content ranges ([#49](https://github.com/webbit/webbit/issues/49) Peter Royal) 
* Server can be restarted ([#46](https://github.com/webbit/webbit/pull/46), [#50](https://github.com/webbit/webbit/pull/50) chtheis)
* Wrap exceptions in WebbitException. This makes it easier to identify exceptions from Webbit in environments where Netty is used in other libs ([#52](https://github.com/webbit/webbit/pull/52) Neil Dunn)
* Add serialVersionUID for Serializable Exception classes to track any future backwards-incompatible changes. (KushalP)
* Added Netty decoder and handler for flash socket policy file requests. ([#51](https://github.com/webbit/webbit/pull/51) Nathan Mische)
* Added flashchatroom demo which shows how to use the the web-socket-js Flash websocket library with Webbit server. ([#51](https://github.com/webbit/webbit/pull/51) Nathan Mische)
* The [Static|Embedded]ResourceHandler APIs have changed slightly if you're subclassing them. (Aslak Hellesøy, Peter Royal)

0.2.16 (2011-11-20)
==================

(There is no 0.2.15 release due to Maven hiccups)

[Full changelog](https://github.com/webbit/webbit/compare/v0.2.14...v0.2.16)

* Easier dynamic post-processing of static assets by moving inner class' serve method up to AbstractResourceHandler.serve so it can be overridden more easily. (Aslak Hellesøy)

0.2.14 (2011-11-01)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.2.13...v0.2.14)

* Fixed a regression in 0.2.12 where inbound WebSocket messages were sometimes improperly read, because 2 threads used the UTF8Output. (Neil Dunn, Aslak Hellesøy)

0.2.13 (2011-11-01)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.2.12...v0.2.13)

* Fixed a regression in 0.2.12 where WebSocket and EventSource connections would be closed after the stale connection timeout. Now they stay open. (Aslak Hellesøy)

0.2.12 (2011-10-31)
==================

(There is no 0.2.11 release)

[Full changelog](https://github.com/webbit/webbit/compare/v0.2.10...v0.2.12)

* HTTP Connections are persistent unless the client sends Connection: close. (Aslak Hellesøy)
* Stale persistent HTTP connections are closed automatically after a configurable timeout (defaults to 5000ms). (Aslak Hellesøy)
* Upgraded Netty to 3.2.6.Final. (Aslak Hellesøy)
* Upgraded Mockito to 1.9.0-rc1. (Aslak Hellesøy)

0.2.10 (2011-10-12)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.2.9...v0.2.10)

* No changes - just mucking around with Maven

0.2.9 (2011-10-12)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.2.8...v0.2.9)

* Removed pom.xml parts that *should* work according to docs, but don't. Checking if make release still works. (Aslak Hellesøy)

0.2.8 (2011-10-12)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.2.7...v0.2.8)

* Attempt a *fully* automated release (Aslak Hellesøy)

0.2.7 (2011-10-12)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.2.6...v0.2.7)

* EVEN More Maven/Sonatype Yak Shaving (Aslak Hellesøy)

0.2.6 (2011-10-12)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.2.5...v0.2.6)

* More Maven/Sonatype Yak Shaving (Aslak Hellesøy)

0.2.5 (2011-10-12)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.2.4...v0.2.5)

* Made Maven upload artifact signatures again, so we can deploy jars (0.2.4 release failed). (Aslak Hellesøy)

0.2.4 (2011-10-12)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.2.3...v0.2.4)

* Added back support for Hixie 75/76 browsers, which broke in a regression in 0.2.2 (Aslak Hellesøy)
* Changed WebSocket logging slightly so it's easier to see what protocol version a ws connection is using (Aslak Hellesøy) 

0.2.3 (2011-10-11)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.2.2...v0.2.3)

* WebServer.close() will now force all connections to be closed. rather than waiting for browsers to close them. ([#29](https://github.com/webbit/webbit/issues/29) Joe Walnes)

0.2.2 (2011-10-11)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.2.1...v0.2.2)

* Fixed several WebSocket bugs related to big messages, fragmented frames and closing handshakes (Aslak Hellesøy).
* Integrated [Autobahn test suite](http://www.tavendo.de/autobahn/testsuite.html) to make the WebSocket implementation a lot more robust. ([#38](https://github.com/webbit/webbit/issues/38) Aslak Hellesøy)
* Stop blocking SimpleLogSink with InetSocketAddress.getHostName(). ([#39](https://github.com/webbit/webbit/issues/39) Kushal Pisavadia)
* Fixed bug involving proper string representation of hex/binary values in error reporting. (Aslak Hellesøy)

0.2.1 (2011-10-02)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.2.0...v0.2.1)

* Fixed incorrect decoding of large messages. (Aslak Hellesøy)
* Support compressed requests and send compressed responses where supported. ([#13](https://github.com/webbit/webbit/issues/13) James Abley)
* Added support for getting request body as a byte array. ([#35](https://github.com/webbit/webbit/pull/35) Peter Gillard-Moss)

0.2.0 (2011-08-08)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.1.17...v0.2.0)

* Implemented support for hybi-10 WebSockets, which is what Chromium 14 and higher speaks. ([#34](https://github.com/webbit/webbit/issues/34) Aslak Hellesøy)
* Added support for sending and receiving binary data as byte[] (Aslak Hellesøy)
* WebSocketHandler has two new methods: onPong(WebSocketConnection connection, String msg) and onMessage(WebSocketConnection connection, byte[] msg) (Aslak Hellesøy)

0.1.17 (2011-06-21)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.1.16...v0.1.17)

* Fixed spurious ClassCastException. ([#32](https://github.com/webbit/webbit/issues/32) Aslak Hellesøy)

0.1.16 (2011-06-21)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.1.13...v0.1.16)

* Fix for EmbeddedResourceHandler for windows. ([#33](https://github.com/webbit/webbit/pull/33) James Estes)
* Fixed illegal error content on non-error pages. (Joe Walnes)
* Added StubConnection, which can be used for stubbing both EventSource and WebSocket connections. (Aslak Hellesøy)

0.1.13 (2011-05-09)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.1.12...v0.1.13)

* Allow WebSocket message handler to throw anything. (Aslak Hellesøy)

0.1.12 (2011-05-05)
==================

[Full changelog](https://github.com/webbit/webbit/compare/v0.1.11...v0.1.12)

* Shut down ExecutorServices used by Netty web server (Matt Hellige)
* Fixed NPE when getting query parameters without a value: ?nothing=&some=thing (Aslak Hellesøy)
* Added EventSourceConnection.send(EventSourceMessage) (Aslak Hellesøy)
* Added BasicAuthenticationHandler for HTTP BASIC authentication (#8 Joe Walnes)

0.1.11 (2011-03-30)
==================

* Fixed a bug where static (embedded) resources were served improperly if they were of a certain size (Aslak Hellesøy)

0.1.10 (2011-03-30)
==================

* Added support for [Server-Sent Events/EventSource](http://dev.w3.org/html5/eventsource/) (#18 Aslak Hellesøy)
* Requests with full url as request uri (and not only path) are correctly matched. (Aslak Hellesøy)
* Added HttpRequest.queryParam(String key) and HttpRequest.queryParams(String key) (#20 Aslak Hellesøy)
* Added new AliasHandler for forwarding requests. (Aslak Hellesøy)
* Made it possible to call NettyHttpResponse.content() more than once. (Aslak Hellesøy)
* Added support for cookies. (#19 Aslak Hellesøy)
* Added HttpRequest.body() method. (Aslak Hellesøy)

0.1.1 (2011-02-19)
==================

* First release
