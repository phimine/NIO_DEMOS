package com.java.nio;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Server {

    private SocketAccepter  socketAccepter  = null;
    private SocketProcessor socketProcessor = null;

    private int tcpPort = 0;
    private IMessageReaderFactory messageReaderFactory = null;
    private IMessageProcessor     messageProcessor = null;

    public Server(int tcpPort, IMessageReaderFactory messageReaderFactory, IMessageProcessor messageProcessor) {
        this.tcpPort = tcpPort;
        this.messageReaderFactory = messageReaderFactory;
        this.messageProcessor = messageProcessor;
    }

	public void start() throws IOException {
		// contains two threads:
		// 1) thread#1 -- listening the incoming socket channel and add it into a queue
		// 2) thread#2 -- loop process the channel queue
		Queue<Socket> socketQueue = new ArrayBlockingQueue<Socket>(1024); // move 1024 to ServerConfig

		this.socketAccepter = new SocketAccepter(tcpPort, socketQueue);
		Thread accepterThread = new Thread(this.socketAccepter);
		accepterThread.start();
		System.out.println("accpeter thread is started!");

		MessageBuffer readBuffer = new MessageBuffer();
		MessageBuffer writeBuffer = new MessageBuffer();
		this.socketProcessor = new SocketProcessor(socketQueue, readBuffer, writeBuffer, this.messageReaderFactory,
				this.messageProcessor);
		Thread processorThread = new Thread(this.socketProcessor);
		processorThread.start();
		System.out.println("processor thread is started!");
	}
}
