package com.kkambbak.client.exception;

import lombok.Getter;

@Getter
public class ClientException extends RuntimeException {
    private final String code;
    private final String message;

    public ClientException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}