package com.java.nio;

public interface IMessageProcessor {

    public void process(Message message, WriteProxy writeProxy);

}
