package com;

import java.io.Serializable;

public class Message implements Serializable {
    private final MessegeType type;
    private final String data;

    public Message(MessegeType type) {
        this.type = type;
        data = null;
    }

    public Message(MessegeType type, String data) {
        this.type = type;
        this.data = data;
    }

    public MessegeType getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}
