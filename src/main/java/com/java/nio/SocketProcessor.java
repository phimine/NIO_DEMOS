package com.java.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;

/**
 * Created by jjenkov on 16-10-2015.
 */
public class SocketProcessor implements Runnable {

	private Queue<Socket> inboundSocketQueue = null;

	private MessageBuffer readMessageBuffer = null; // todo Not used now - but perhaps will be later - to check for
													// space in the buffer before reading from sockets
	private MessageBuffer writeMessageBuffer = null; // todo Not used now - but perhaps will be later - to check for
														// space in the buffer before reading from sockets (space for
														// more to write?)

	private IMessageReaderFactory messageReaderFactory = null;

	private Queue<Message> outboundMessageQueue = new LinkedList<>(); // todo use a better / faster queue.

	private Map<Long, Socket> socketMap = new HashMap<>();

	private ByteBuffer readByteBuffer = ByteBuffer.allocate(1024 * 1024);
	private ByteBuffer writeByteBuffer = ByteBuffer.allocate(1024 * 1024);
	private Selector readSelector = null;
	private Selector writeSelector = null;

	private IMessageProcessor messageProcessor = null;
	private WriteProxy writeProxy = null;

	private long nextSocketId = 16 * 1024; // start incoming socket ids from 16K - reserve bottom ids for pre-defined
											// sockets (servers).

	private Set<Socket> emptyToNonEmptySockets = new HashSet<>();
	private Set<Socket> nonEmptyToEmptySockets = new HashSet<>();

	public SocketProcessor(Queue<Socket> inboundSocketQueue, MessageBuffer readMessageBuffer,
			MessageBuffer writeMessageBuffer, IMessageReaderFactory messageReaderFactory,
			IMessageProcessor messageProcessor) throws IOException {
		this.inboundSocketQueue = inboundSocketQueue;
		System.out.println("inbound socket queue size: " + this.inboundSocketQueue.size());

		this.readMessageBuffer = readMessageBuffer;
		this.writeMessageBuffer = writeMessageBuffer;
		this.writeProxy = new WriteProxy(writeMessageBuffer, this.outboundMessageQueue);

		this.messageReaderFactory = messageReaderFactory;

		this.messageProcessor = messageProcessor;

		this.readSelector = Selector.open();
		this.writeSelector = Selector.open();
	}

	public void run() {
		while (true) {
			try {
				System.out.println("Socket processor is running...");
				executeCycle();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(1000 * 10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void executeCycle() throws IOException {
//		System.out.println("Socket processor is running - taking new sockets");
		takeNewSockets();

//		System.out.println("Socket processor is running - reading from sockets");
		readFromSockets();

//		System.out.println("Socket processor is running - writting to sockets");
		writeToSockets();
	}

	public void takeNewSockets() throws IOException {
		Socket newSocket = this.inboundSocketQueue.poll();
//		System.out.println("socket pop : " + newSocket);

		while (newSocket != null) {
			newSocket.socketId = this.nextSocketId++;
//			System.out.println("socketId set to: " + newSocket.socketId);
			newSocket.socketChannel.configureBlocking(false);
//			System.out.println("socket channle configre blocking setting to false!");

//			System.out.println("create message reader!");
			newSocket.messageReader = this.messageReaderFactory.createMessageReader();
			System.out.println("init message reader......");
			newSocket.messageReader.init(this.readMessageBuffer);
			System.out.println("init message reader......finished!");

//			System.out.println("create message writter!"); 
			newSocket.messageWriter = new MessageWriter();

//			System.out.println("put socket into socket map!");
			this.socketMap.put(newSocket.socketId, newSocket);
//			System.out.println("{socketId : " + newSocket.socketId + "}");

			// socketChannel?
//			System.out.println("register socket channel and attach socket!");
			SelectionKey key = newSocket.socketChannel.register(this.readSelector, SelectionKey.OP_READ, newSocket);

			newSocket = this.inboundSocketQueue.poll();
//			System.out.println("socket taking end!\n\t");
		}
	}

	public void readFromSockets() throws IOException {
		int readReady = this.readSelector.selectNow();
		System.out.println("try to read ready selector! ready count: " + readReady);

		if (readReady > 0) {
			Set<SelectionKey> selectedKeys = this.readSelector.selectedKeys();
			Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

			while (keyIterator.hasNext()) {
				SelectionKey key = keyIterator.next();
				readFromSocket(key);
				keyIterator.remove();
			}
			selectedKeys.clear();
		}
		System.out.println("reading end!\n\t ");
	}

	private void readFromSocket(SelectionKey key) throws IOException {
		Socket socket = (Socket) key.attachment();
		socket.messageReader.read(socket, this.readByteBuffer);

		if (socket.endOfStreamReached) {
			List<Message> fullMessages = socket.messageReader.getMessages();
			if (fullMessages.size() > 0) {
				for (Message message : fullMessages) {
					message.socketId = socket.socketId;
					// the message processor will eventually push outgoing messages into an IMessageWriter for this socket.
					this.messageProcessor.process(message, this.writeProxy);
					
					message.releaseMessageBuffer();
				}
				fullMessages.clear();
			}
			
			System.out.println("socket channel read finished! read selector cancel the selection key related to the socket: " + socket.socketId);
			// this.socketMap.remove(socket.socketId);
			key.attach(null);
			key.cancel();
			// key.channel().close();
		}
	}

	public void writeToSockets() throws IOException {
		// Take all new messages from outboundMessageQueue
//		System.out.println("Taking all new messages from outboundMessageQueue");
		takeNewOutboundMessages();

		// Cancel all sockets which have no more data to write.
//		System.out.println("Cancel all sockets which have no more data to write");
		cancelEmptySockets();

		// Register all sockets that *have* data and which are not yet registered.
//		System.out.println("Register all sockets that *have* data and which are not yet registered");
		registerNonEmptySockets();

		// Select from the Selector.
		int writeReady = this.writeSelector.selectNow();
		System.out.println("select from selector, write ready count : " + writeReady);

		if (writeReady > 0) {
			Set<SelectionKey> selectionKeys = this.writeSelector.selectedKeys();
			Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

			while (keyIterator.hasNext()) {
				SelectionKey key = keyIterator.next();
				Socket socket = (Socket) key.attachment();
				socket.messageWriter.write(socket, this.writeByteBuffer);
				if (socket.messageWriter.isEmpty()) {
					this.nonEmptyToEmptySockets.add(socket);
				}
				keyIterator.remove();
			}
			selectionKeys.clear();
		}
	}

	private void registerNonEmptySockets() throws ClosedChannelException {
//		System.out.println("emptyToNonEmptySockets size : " + emptyToNonEmptySockets.size());
		for (Socket socket : emptyToNonEmptySockets) {
			socket.socketChannel.register(this.writeSelector, SelectionKey.OP_WRITE, socket);
		}
		emptyToNonEmptySockets.clear();
	}

	private void cancelEmptySockets() {
//		System.out.println("nonEmptyToEmptySockets size : " + nonEmptyToEmptySockets.size());
		for (Socket socket : nonEmptyToEmptySockets) {
			SelectionKey key = socket.socketChannel.keyFor(this.writeSelector);
			this.socketMap.remove(socket.socketId);
			key.cancel();
			key.attach(null);
			try {
				key.channel().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		nonEmptyToEmptySockets.clear();
	}

	private void takeNewOutboundMessages() {
		Message outMessage = this.outboundMessageQueue.poll();
//		System.out.println("outbond message queue pop out : " + outMessage);
		while (outMessage != null) {
			Socket socket = this.socketMap.get(outMessage.socketId);
//			System.out.println("socket Map get by socketID: " + outMessage.socketId);
			System.out.println(socket);

			if (socket != null) {
				MessageWriter messageWriter = socket.messageWriter;
				if (messageWriter.isEmpty()) {
					messageWriter.enqueue(outMessage);
					nonEmptyToEmptySockets.remove(socket);
					emptyToNonEmptySockets.add(socket); // not necessary if removed from nonEmptyToEmptySockets in prev. statement.
				} else {
					messageWriter.enqueue(outMessage);
				}
			}

			outMessage = this.outboundMessageQueue.poll();
		}
	}

}
