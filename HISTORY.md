In Git
======

* Server can be restarted ([#46](https://github.com/joewalnes/webbit/pull/46), [#50](https://github.com/joewalnes/webbit/pull/50) chtheis)
* Wrap exceptions in WebbitException. This makes it easier to identify exceptions from Webbit in environments where Netty is used in other libs ([#52](https://github.com/joewalnes/webbit/pull/52) Neil Dunn)
* Add serialVersionUID for Serializable Exception classes to track any future backwards-incompatible changes. (KushalP)

0.2.16 (2011-11-20)
==================

(There is no 0.2.15 release due to Maven hiccups)

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.2.14...v0.2.16)

* Easier dynamic post-processing of static assets by moving inner class' serve method up to AbstractResourceHandler.serve so it can be overridden more easily. (Aslak Hellesøy)

0.2.14 (2011-11-01)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.2.13...v0.2.14)

* Fixed a regression in 0.2.12 where inbound WebSocket messages were sometimes improperly read, because 2 threads used the UTF8Output. (Neil Dunn, Aslak Hellesøy)

0.2.13 (2011-11-01)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.2.12...v0.2.13)

* Fixed a regression in 0.2.12 where WebSocket and EventSource connections would be closed after the stale connection timeout. Now they stay open. (Aslak Hellesøy)

0.2.12 (2011-10-31)
==================

(There is no 0.2.11 release)

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.2.10...v0.2.12)

* HTTP Connections are persistent unless the client sends Connection: close. (Aslak Hellesøy)
* Stale persistent HTTP connections are closed automatically after a configurable timeout (defaults to 5000ms). (Aslak Hellesøy)
* Upgraded Netty to 3.2.6.Final. (Aslak Hellesøy)
* Upgraded Mockito to 1.9.0-rc1. (Aslak Hellesøy)

0.2.10 (2011-10-12)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.2.9...v0.2.10)

* No changes - just mucking around with Maven

0.2.9 (2011-10-12)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.2.8...v0.2.9)

* Removed pom.xml parts that *should* work according to docs, but don't. Checking if make release still works. (Aslak Hellesøy)

0.2.8 (2011-10-12)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.2.7...v0.2.8)

* Attempt a *fully* automated release (Aslak Hellesøy)

0.2.7 (2011-10-12)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.2.6...v0.2.7)

* EVEN More Maven/Sonatype Yak Shaving (Aslak Hellesøy)

0.2.6 (2011-10-12)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.2.5...v0.2.6)

* More Maven/Sonatype Yak Shaving (Aslak Hellesøy)

0.2.5 (2011-10-12)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.2.4...v0.2.5)

* Made Maven upload artifact signatures again, so we can deploy jars (0.2.4 release failed). (Aslak Hellesøy)

0.2.4 (2011-10-12)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.2.3...v0.2.4)

* Added back support for Hixie 75/76 browsers, which broke in a regression in 0.2.2 (Aslak Hellesøy)
* Changed WebSocket logging slightly so it's easier to see what protocol version a ws connection is using (Aslak Hellesøy) 

0.2.3 (2011-10-11)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.2.2...v0.2.3)

* WebServer.close() will now force all connections to be closed. rather than waiting for browsers to close them. ([#29](https://github.com/joewalnes/webbit/issues/29) Joe Walnes)

0.2.2 (2011-10-11)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.2.1...v0.2.2)

* Fixed several WebSocket bugs related to big messages, fragmented frames and closing handshakes (Aslak Hellesøy).
* Integrated [Autobahn test suite](http://www.tavendo.de/autobahn/testsuite.html) to make the WebSocket implementation a lot more robust. ([#38](https://github.com/joewalnes/webbit/issues/38) Aslak Hellesøy)
* Stop blocking SimpleLogSink with InetSocketAddress.getHostName(). ([#39](https://github.com/joewalnes/webbit/issues/39) Kushal Pisavadia)
* Fixed bug involving proper string representation of hex/binary values in error reporting. (Aslak Hellesøy)

0.2.1 (2011-10-02)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.2.0...v0.2.1)

* Fixed incorrect decoding of large messages. (Aslak Hellesøy)
* Support compressed requests and send compressed responses where supported. ([#13](https://github.com/joewalnes/webbit/issues/13) James Abley)
* Added support for getting request body as a byte array. ([#35](https://github.com/joewalnes/webbit/pull/35) Peter Gillard-Moss)

0.2.0 (2011-08-08)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.1.17...v0.2.0)

* Implemented support for hybi-10 WebSockets, which is what Chromium 14 and higher speaks. ([#34](https://github.com/joewalnes/webbit/issues/34) Aslak Hellesøy)
* Added support for sending and receiving binary data as byte[] (Aslak Hellesøy)
* WebSocketHandler has two new methods: onPong(WebSocketConnection connection, String msg) and onMessage(WebSocketConnection connection, byte[] msg) (Aslak Hellesøy)

0.1.17 (2011-06-21)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.1.16...v0.1.17)

* Fixed spurious ClassCastException. ([#32](https://github.com/joewalnes/webbit/issues/32) Aslak Hellesøy)

0.1.16 (2011-06-21)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.1.13...v0.1.16)

* Fix for EmbeddedResourceHandler for windows. ([#33](https://github.com/joewalnes/webbit/pull/33) James Estes)
* Fixed illegal error content on non-error pages. (Joe Walnes)
* Added StubConnection, which can be used for stubbing both EventSource and WebSocket connections. (Aslak Hellesøy)

0.1.13 (2011-05-09)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.1.12...v0.1.13)

* Allow WebSocket message handler to throw anything. (Aslak Hellesøy)

0.1.12 (2011-05-05)
==================

[Full changelog](https://github.com/joewalnes/webbit/compare/v0.1.11...v0.1.12)

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
