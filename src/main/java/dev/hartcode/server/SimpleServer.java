// Package declaration
package dev.hartcode.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Class representing a simple chat server
public class SimpleServer {
    private final List<PrintWriter> clientWriters = new ArrayList<>();

    public static void main(String[] args) {
        new SimpleServer().go();
    }

    // Method to start the server
    private void go() {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(8000));
            while (serverSocketChannel.isOpen()) {
                System.out.println("Waiting for client to connect...");
                SocketChannel clientSocket = serverSocketChannel.accept();
                System.out.println("A client has connected");
                PrintWriter writer = new PrintWriter(Channels.newWriter(clientSocket, StandardCharsets.UTF_8));
                clientWriters.add(writer);
                threadPool.submit(new ClientHandler(clientSocket));
                System.out.println("A connection has been secured");
            }
        } catch (IOException e) {
            System.out.println("No connection was made " + e.getMessage());
        }
    }

    // Method to send a message to all connected clients
    private void tellEveryone(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
            writer.flush();
        }
    }

    // Class to handle client connections
    public class ClientHandler implements Runnable {
        private final BufferedReader reader;

        public ClientHandler(SocketChannel channel) {
            reader = new BufferedReader(Channels.newReader(channel, StandardCharsets.UTF_8));
        }

        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                    tellEveryone(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
