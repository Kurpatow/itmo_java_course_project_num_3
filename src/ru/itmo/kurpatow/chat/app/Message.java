package ru.itmo.kurpatow.chat.app;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private long id;
    private String sender;
    private String text;
    private LocalDateTime dateTime;

    public Message(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        if (sender == null) throw new IllegalArgumentException(
                "В классе Message переданное значение sender = null");
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (sender == null || text.length() <1) throw new IllegalArgumentException(
                "Текст сообщения должен содержать хотя бы один символ");
        this.text = text;
    }

    public void setDateTime(){
        dateTime = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Message{ " +
                "sender = " + sender + '\'' +
                ", text =  " + text + '\'' +
                ", date & time = " + dateTime +
                '}';
    }

    public static Message getMessage(String sender, String text){
        return new Message(sender, text);
    }
}
