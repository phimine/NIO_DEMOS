package com.java.nio.http;

import com.java.nio.IMessageReader;
import com.java.nio.IMessageReaderFactory;

public class HttpMessageReaderFactory implements IMessageReaderFactory {

	public HttpMessageReaderFactory() {
	}

	@Override
	public IMessageReader createMessageReader() {
		return new HttpMessageReader();
	}
}
