package com.java.nio.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * better to run with @link{NIO_ServerSocketChannel}
 * 
 * @author FuMingming
 */
public class NIO_SocketChannel {

	public static void main(String[] args) throws IOException {
		// 1. open a socket channel and connect
		SocketChannel sChannel = SocketChannel.open();
		sChannel.connect(new InetSocketAddress(9527));
		sChannel.configureBlocking(false);
		
		// 3. write data into socket channel
		String data = "HTTP/1.1 200 OK\r\n" + 
				"Content-Length: 38\r\n" + 
				"Content-Type: text/html\r\n" + 
				"\r\n" + 
				"<html><body>Hello World!</body></html>";
		ByteBuffer srcBuf = ByteBuffer.allocate(256);
		srcBuf.clear();
		srcBuf.put(data.getBytes("GBK"));
		srcBuf.flip();
		sChannel.write(srcBuf);
		
		// 2. read data from socket channel
		ByteBuffer desBuf = ByteBuffer.allocate(256);
		int readLen = sChannel.read(desBuf);
		if (readLen > 0) {
			desBuf.flip();
			while (desBuf.hasRemaining()) {
				System.out.print((char) desBuf.get());
			}
		}

		// 4. close a socket channel
		sChannel.close();
	}
}
