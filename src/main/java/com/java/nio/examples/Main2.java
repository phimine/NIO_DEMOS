package com.java.nio.examples;

import com.java.nio.*;
import com.java.nio.http.HttpMessageReaderFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by jjenkov on 19-10-2015.
 */
public class Main2 {

    public static void main(String[] args) throws IOException {
    	DatagramChannel channel = DatagramChannel.open();
//    	channel.socket().bind(new InetSocketAddress(9998));
		
		String newData = "New String to write to file..." + System.currentTimeMillis();
		ByteBuffer buf = ByteBuffer.allocate(48);
		buf.put(newData.getBytes());
//		buf.flip();
//		while (buf.hasRemaining()) {
//			System.out.print((char) buf.get());
//		}
		buf.flip();
		int bytesSent = channel.send(buf, new InetSocketAddress("localhost", 9998));
		System.out.println(bytesSent);
		
    }
}
