package com.java.nio.examples;

import com.java.nio.*;
import com.java.nio.http.HttpMessageReaderFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by jjenkov on 19-10-2015.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: 38\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                "<html><body>Hello World!</body></html>";

        byte[] httpResponseBytes = httpResponse.getBytes("UTF-8");
        
        IMessageProcessor messageProcessor = (request, writeProxy) -> {
            System.out.println("Message Received from socket: " + request.socketId);
            System.out.println("Received content :" + new String(request.sharedArray, request.offset, request.length));
            Message response = writeProxy.getMessage();
            response.socketId = request.socketId;
            response.writeToMessage(httpResponseBytes);

            writeProxy.enqueue(response);
        };

        Server server = new Server(9527, new HttpMessageReaderFactory(), messageProcessor);
        server.start();
    }
}
