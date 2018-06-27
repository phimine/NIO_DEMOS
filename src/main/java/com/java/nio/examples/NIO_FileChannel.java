package com.java.nio.examples;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIO_FileChannel {

	public static void main(String[] args) throws IOException {
		// 1. open a channel connecting to a file
		RandomAccessFile raf = new RandomAccessFile("..\\fileChannelTest.txt", "rw");
		FileChannel fileChannel = raf.getChannel();
		
		// 2. write data into file via fileChannel
		// 2.1 firstly, write some data into buffer
		String data = "This is a couple of data that will be written into file! current time: " + System.currentTimeMillis() + "\n\t maybe some 汉字 will be better!";
		byte[] srcBytes = data.getBytes("GBK");
		ByteBuffer srcBuf = ByteBuffer.allocate(256);
		srcBuf.put(srcBytes);
		// 2.2 then read from buffer and write data into channel
		srcBuf.flip();
		while (srcBuf.hasRemaining()) {
			fileChannel.write(srcBuf);
			System.out.println();
		}
		srcBuf.clear();
		
		// 3. read data from file via fileChannel
		ByteBuffer desBuf = ByteBuffer.allocate(256);
		fileChannel.read(desBuf, 0);
		System.out.println("buff.position :" + desBuf.position() + ", desBuf.limit:" + desBuf.limit());
		desBuf.flip();
		System.out.println("buff.position :" + desBuf.position() + ", desBuf.limit:" + desBuf.limit());
		byte[] b = new byte[desBuf.limit()];
		int i = 0;
		while (desBuf.hasRemaining()) {
			b[i++] = desBuf.get();
		}
		System.out.println(new String(b, "GBK"));
		desBuf.clear();
		
		// 4. close file channel
		fileChannel.close();
		raf.close();
		
		// test truncate()
		truncate();
	}

	public static void truncate() throws IOException {
		RandomAccessFile raf = new RandomAccessFile("..\\fileChannelTest.txt", "rw");
		FileChannel fileChannel = raf.getChannel();
		
		System.out.println("before truncate, size: " + fileChannel.size());
		fileChannel.truncate(48);
		System.out.println("before truncate, size: " + fileChannel.size());
		
		fileChannel.close();
		raf.close();
	}
}
