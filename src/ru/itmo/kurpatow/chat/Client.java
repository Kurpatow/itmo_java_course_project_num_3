package ru.itmo.kurpatow.chat;

import ru.itmo.kurpatow.chat.app.Connection;
import ru.itmo.kurpatow.chat.app.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
    private final int port;
    private final String ip;
    private BufferedReader reader;

    public Client(int port, String ip) {
        this.port = port;
        this.ip = ip;
    }

    public void start() throws Exception {
        reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Введите логин");
        String name = reader.readLine();
        System.out.println("Можете начинать общение.\n" +
                "Напишите сообщение в консоль и отправьте в чат, нажав клавишу Enter. \n" +
                "Для выхода введите команду exit и нажмите клавишу Enter");

        Connection connection = new Connection(getSocket());

        Thread sender = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String messageText = reader.readLine();
                    if ("exit".equalsIgnoreCase(messageText)) {
                        System.out.println("Вы вышли из чата.");
                        connection.close();
                        Thread.currentThread().interrupt();
                        break;
                    }
                    connection.sendMessage(Message.getMessage(name, messageText));
                    System.out.println(" ");
                }
            } catch (Exception e) {
                try {
                    connection.close();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                e.printStackTrace();
            }
        });

        Thread getter = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Message fromServer = connection.readMessage();
                    System.out.println(fromServer);
                }
            } catch (IOException | ClassNotFoundException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });
        sender.start();
        getter.start();
    }

    private Socket getSocket() throws IOException {
        return new Socket(ip, port);
    }

    public static void main(String[] args) {
        int port = 8099;
        String ip = "127.0.0.1";
        try {
            new Client(port, ip).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}