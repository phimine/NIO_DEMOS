package com.java.nio.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * better to run with @link{NIO_SocketChannel}
 * 
 * @author FuMingming
 */
public class NIO_ServerSocketChannel {

	public static void main(String[] args) throws IOException {
		ServerSocketChannel ssChannel = ServerSocketChannel.open();
		ssChannel.socket().bind(new InetSocketAddress(9527));
		
		// configure the channel to be in blocking mode that the thread will be blocked while invoking accept()
		ssChannel.configureBlocking(true);

		// configure the channel to be in non-blocking mode
		// ssChannel.configureBlocking(false);

		while (true) {
			// System.out.println("Try to accept client socket...");
			SocketChannel clientSocket = ssChannel.accept();
			
			// Once received one client socket, do something... for instance output the read data.
			if (null != clientSocket) {
				ByteBuffer outputBuf = ByteBuffer.allocate(256);
				int lengh = clientSocket.read(outputBuf);
				
				if (lengh > 0) {
					outputBuf.flip();
					while (outputBuf.hasRemaining()) {
						System.out.print((char) outputBuf.get());
					}
				}
			}
		}
	}
}
