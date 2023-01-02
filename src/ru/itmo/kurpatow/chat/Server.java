package ru.itmo.kurpatow.chat;

import ru.itmo.kurpatow.chat.app.Connection;
import ru.itmo.kurpatow.chat.app.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    public Server(int port) {
        this.port = port;
    }

    private final int port;
    private final ArrayBlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<>(10, true);
    private final CopyOnWriteArrayList<Connection> connections = new CopyOnWriteArrayList<>();


    public void start() throws IOException, ClassNotFoundException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен...");
            SendToClients sendToClients = new SendToClients();
            sendToClients.start();
            while (true) {
                Socket socket = serverSocket.accept();
                Clients clients = new Clients(socket);
                clients.start();
            }
        }
    }

    class Clients extends Thread {
        private final Connection connection;

        public Clients(Socket socket) throws IOException {
            this.connection = new Connection(socket);
        }

        @Override
        public void run() {
            long id = Thread.currentThread().getId();
            connection.setId(id);
            connections.add(connection);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Message message = connection.readMessage();
                    message.setId(id);
                    printMessage(message);
                    blockingQueue.add(message);
                } catch (IOException | ClassNotFoundException e) {
                    connections.remove(connection);
                    Thread.currentThread().interrupt();
                    System.out.println(Thread.currentThread().getName() + " соединение прервано.");

                }
            }
        }
    }

    class SendToClients extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message message;
                try {
                    message = blockingQueue.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for (Connection currentConnection : connections) {
                    try {
                        if (currentConnection.getId() != message.getId()) {
                            currentConnection.sendMessage(message);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private void printMessage(Message message) {
        System.out.println("Новое сообщение: " + message);
    }

    public static void main(String[] args) {
        int port = 8099;
        Server messageServer = new Server(port);
        try {
            messageServer.start();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
