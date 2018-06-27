package com.java.nio.examples;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

public class NIO_AsynchronousFileChannel {
	public static void main(String[] args) throws IOException {
		
		readTest();
		
		writeTest();
	}

	private static void readTest() throws IOException {
		Path path = Paths.get("..\\test.txt");
		AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
		
		// 1. the first read way via a Future: Future.isDone to determine if the read operation is finished.
		ByteBuffer desBuff = ByteBuffer.allocate(1024);
		desBuff.clear();
		Future<Integer> operation = fileChannel.read(desBuff, 0);
		while (!operation.isDone());
		desBuff.flip();
		byte[] data = new byte[desBuff.limit()];
		desBuff.get(data);
		desBuff.clear();
		System.out.println(new String(data));
		
		// 2. the second read way via a CompletionHandler: handler define two callback methods to handle the following operation after reading finished.
		fileChannel.read(desBuff, 0, desBuff, new CompletionHandler<Integer, ByteBuffer>() {
			@Override
			public void completed(Integer result, ByteBuffer attachment) {
				System.out.println("result = " + result);
				attachment.flip();
				byte[] data = new byte[result];
				attachment.get(data);
				attachment.clear();
				try {
					System.out.println(new String(data, "GBK"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				System.out.println("reading failed!");
				exc.printStackTrace();
			}
		});
	}
	
	private static void writeTest() throws IOException {
		Path path = Paths.get("..\\test-write.txt");
		if (!Files.exists(path)) {
			Files.createFile(path);
		}
		AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
		// 1. the first write way via a Future like reading
		ByteBuffer srcBuff = ByteBuffer.allocate(1024);
		srcBuff.put("write test哈哈哈".getBytes("UTF-8"));
		srcBuff.flip();
		Future<Integer> op = fileChannel.write(srcBuff, 0);
		while (!op.isDone());
//		srcBuff.clear();
		System.out.println("writting done via Future!");
		
		// output the content of current buffer
		srcBuff.flip();
		byte[] data2 = new byte[srcBuff.limit()];
		srcBuff.get(data2);
		System.out.println("after first write, the buffer content : " + new String(data2));
		srcBuff.clear();
		
		// 2. the second write way via a CompletionHandler like reading
		srcBuff.put("write test via CompletionHandler啊啊啊".getBytes("UTF-8"));
		
		// output the content of current buffer
		srcBuff.flip();
		byte[] data = new byte[srcBuff.limit()];
		srcBuff.get(data);
		System.out.println("after the second write, the buffer content : " + new String(data));
		
		srcBuff.flip();
		fileChannel.write(srcBuff, 0, srcBuff, new CompletionHandler<Integer, ByteBuffer>(){
			@Override
			public void completed(Integer result, ByteBuffer attachment) {
				System.out.println("result = " + result);
				System.out.println("writting done via CompletionHandler！");
				attachment.clear();
			}
			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				System.out.println("writting failed!");
				exc.printStackTrace();
			}
		});
	}
}
