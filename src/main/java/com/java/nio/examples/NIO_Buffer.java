package com.java.nio.examples;

import java.nio.ByteBuffer;

/**
 * The instance of Buffer#equals and Buffer#compareTo
 */
public class NIO_Buffer {
	public static void main(String[] args) {
		ByteBuffer buff1 = ByteBuffer.allocate(24);
		ByteBuffer buff2 = ByteBuffer.allocate(24);
		ByteBuffer buff3 = ByteBuffer.allocate(24);
		
		buff1.put("123456789".getBytes());
		buff2.put("023456789".getBytes());
		buff3.put("023456789000".getBytes());
		buff1.flip();
		buff2.flip();
		buff3.flip();
		
		System.out.println("buff1.equals(buff2) return: " + buff1.equals(buff2));													// false
		System.out.println("buff1.compareTo(buff2) return: " + buff1.compareTo(buff2));									// 1: "123456789" > "023456789"
		buff1.compareTo(buff2);
		buff1.get();
		buff2.get();
		buff3.get();
		System.out.println("do one get for both, then buff1.equals(buff2) return: " + buff1.equals(buff2));			// true
		System.out.println("do one get for both, buff1.compareTo(buff2) return: " + buff1.compareTo(buff2));		// 0: "23456789" = "23456789"
		System.out.println("do one get for both, buff1.compareTo(buff3) return: " + buff1.compareTo(buff3));  	// -3:  "23456789" < "23456789000"
	}
}
