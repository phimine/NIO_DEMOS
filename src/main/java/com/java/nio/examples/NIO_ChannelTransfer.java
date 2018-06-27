package com.java.nio.examples;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class NIO_ChannelTransfer {
	public static void main(String[] args) throws IOException {
		RandomAccessFile fromFile = new RandomAccessFile("..\\fromFile.txt", "r"); 
		FileChannel fromChannel = fromFile.getChannel(); 
		
		RandomAccessFile toFile = new RandomAccessFile("..\\toFile.txt", "rw"); 
		FileChannel toChannel = toFile.getChannel(); 
		long position = 0; 
		long count = fromChannel.size(); 
		
//		toChannel.transferFrom(fromChannel, position, count);
//		System.out.println("toChannel.transferFrom(fromChannel, position, count), transfer successfully!");
		fromChannel.transferTo(position, count, toChannel);
		System.out.println("fromChannel.transferTo(position, count, toChannel);, transfer successfully!");
		
		fromFile.close();
		toFile.close();
	}

}
