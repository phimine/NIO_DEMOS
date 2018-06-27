package com.java.nio.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.java.nio.IMessageReader;
import com.java.nio.Message;
import com.java.nio.MessageBuffer;
import com.java.nio.Socket;

public class HttpMessageReader implements IMessageReader {

    private MessageBuffer messageBuffer    = null;

    private List<Message> completeMessages = new ArrayList<Message>();
    private Message       nextMessage      = null;

    public HttpMessageReader() {
    }

    @Override
	public void init(MessageBuffer readMessageBuffer) {
		this.messageBuffer = readMessageBuffer;
		if (null == this.nextMessage) {
			this.nextMessage = messageBuffer.getMessage();
			this.nextMessage.metaData = new HttpHeaders();
		}
	}

    @Override
    public void read(Socket socket, ByteBuffer byteBuffer) throws IOException {
        int bytesRead = socket.read(byteBuffer);
        System.out.println("read data from socket, the data length is : " + bytesRead);
        byteBuffer.flip();

		if (byteBuffer.remaining() == 0) {
			byteBuffer.clear();
			return;
		}

        this.nextMessage.writeToMessage(byteBuffer);

        System.out.println("nextMessage length ==== " + this.nextMessage.length);
		int endIndex = HttpUtil.parseHttpRequest(this.nextMessage.sharedArray, this.nextMessage.offset,
				this.nextMessage.offset + this.nextMessage.length, (HttpHeaders) this.nextMessage.metaData);
		if (endIndex != -1) {
			if (!socket.endOfStreamReached) {
				Message message = this.messageBuffer.getMessage();
				message.metaData = new HttpHeaders();

				if (endIndex < this.nextMessage.offset + this.nextMessage.length) {
					message.writePartialMessageToMessage(nextMessage, endIndex);
					// make nextMessage be a full message without other partial message
					nextMessage.length = endIndex - nextMessage.offset;
				}
				completeMessages.add(nextMessage);
				nextMessage = message;
			} else {
				completeMessages.add(nextMessage);
			}
        }
        byteBuffer.clear();
    }

    @Override
    public List<Message> getMessages() {
        return this.completeMessages;
    }

}
