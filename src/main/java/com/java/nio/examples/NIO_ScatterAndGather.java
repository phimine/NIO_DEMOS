package com.java.nio.examples;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NIO_ScatterAndGather {
	public static void main(String[] args) throws IOException {
		scatterReads();
		
		gatherWrites();
	}
	
	public static void scatterReads() throws IOException {
		/**
		 * FILE CONTENT: 
		 * <head>12345678901234567890123456789012345</head><body>1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345</body>
		 */
		RandomAccessFile raf = new RandomAccessFile("..\\scatterReads.txt", "r");
		FileChannel inChannel = raf.getChannel();
		
		ByteBuffer headBuff = ByteBuffer.allocate(48);
		ByteBuffer bodyBuff = ByteBuffer.allocate(128);
		ByteBuffer[] buffArray = { headBuff, bodyBuff };
		inChannel.read(buffArray);
		
		headBuff.flip();
		bodyBuff.flip();
		
		System.out.println("HEAD CONTENT: ");
		while (headBuff.hasRemaining()) {
			System.out.print((char) headBuff.get());
		}
		
		System.out.println("\nBODY CONTENT: ");
		while (bodyBuff.hasRemaining()) {
			System.out.print((char) bodyBuff.get());
		}
		
		inChannel.close();
		raf.close();
	}

	public static void gatherWrites() throws IOException {
		Path path = Paths.get("..\\gatherWrites.txt");
		if (!Files.exists(path)) {
			Files.createFile(path);
		}
		RandomAccessFile raf = new RandomAccessFile("..\\gatherWrites.txt", "rw");
		FileChannel inChannel = raf.getChannel();
		
		ByteBuffer src1 = ByteBuffer.allocate(1024);
		ByteBuffer src2 = ByteBuffer.allocate(1024);
		ByteBuffer[] srcs = { src1, src2 };
		
		src1.put("This is the content of the first source!\n\t".getBytes());
		src2.put("This is the content of the second source!".getBytes());
		
		src1.flip();
		src2.flip();
		
		inChannel.position(50);
		inChannel.write(srcs);
		inChannel.close();
		raf.close();
	}
}
