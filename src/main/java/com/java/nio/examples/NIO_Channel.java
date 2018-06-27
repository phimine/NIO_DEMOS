package com.java.nio.examples;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIO_Channel {
	public static void main(String[] args) throws IOException {
		// 1. create a file channel to a .txt file.
		RandomAccessFile raf = new RandomAccessFile("..\\test.txt", "rw");
		FileChannel inChannel = raf.getChannel();
		
		// 2. read data from channel to ByteBuffer, 1024 length as one group.
		ByteBuffer desBuff = ByteBuffer.allocate(1024);
		int readLen = inChannel.read(desBuff);
		
		// 3. loop to read data to buffer until all data have been read.
		while (readLen != -1) {
			System.out.println("Read byte length: " + readLen);
			// set buffer's limit to current position and set position to 0
			desBuff.flip(); 
			
			// output the read data
			byte[] outBytes = new byte[desBuff.limit()]; // limit() means how many bytes have been read in buffer
			desBuff.get(outBytes);
			System.out.println(new String(outBytes, "GBK"));
			
			// set buffer's position to 0 and set limit to the capacity, not really clear the data from buffer
			desBuff.clear();
			readLen = inChannel.read(desBuff);
		}
		// 4. close file
		raf.close();
	}
}
