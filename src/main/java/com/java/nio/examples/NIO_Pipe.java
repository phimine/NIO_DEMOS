package com.java.nio.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;

public class NIO_Pipe {
	public static void main(String[] args) throws IOException {
		Pipe pipe = Pipe.open();
		SinkChannel sinkChannel = pipe.sink();
		
		String data = "write data into sink channel, and read out from source channel..." + System.currentTimeMillis();
		ByteBuffer buff = ByteBuffer.allocate(128);
		buff.clear();
		buff.put(data.getBytes());
		buff.flip();
		while (buff.hasRemaining()) {
			sinkChannel.write(buff);
		}
		
		SourceChannel sourceChannel = pipe.source();
		ByteBuffer desBuff = ByteBuffer.allocate(128);
		int bytesRead = sourceChannel.read(desBuff);
		System.out.println("read from source channel: " + bytesRead);
		desBuff.flip();
		System.out.print("CONTENT: ");
		while (desBuff.hasRemaining()) {
			System.out.print((char) desBuff.get());
		}
		System.out.println();
		
		bytesRead = sourceChannel.read(desBuff);
		System.out.println("read from source channel: " + bytesRead);
	}
}
