package dev.hartcode.client;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Class representing a chat client
public class ChatClient {
    private JTextField textField;
    private JTextArea textArea;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;

    public static void main(String[] args) {
        new ChatClient().go();
    }

    // Method to start the chat client
    public void go() {
        setUpNetworking();

        JScrollPane scrollPane = createScrollableTextArea();
        textField = new JTextField(30);
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        JPanel mainPanel = new JPanel();
        mainPanel.add(scrollPane);
        mainPanel.add(textField);
        mainPanel.add(sendButton);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new IncomingReader());

        JFrame frame = new JFrame("Chat Server");
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        frame.setSize(500, 400);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    // Method to set up networking
    private void setUpNetworking() {
        try {
            InetSocketAddress socketAddress = new InetSocketAddress(8000);
            SocketChannel socketChannel = SocketChannel.open(socketAddress);
            bufferedReader = new BufferedReader(Channels.newReader(socketChannel, StandardCharsets.UTF_8));
            printWriter = new PrintWriter(Channels.newWriter(socketChannel, StandardCharsets.UTF_8));
            System.out.println("Server connection established");
        } catch (IOException e) {
            System.out.println("Could not connect to the server");
        }
    }

    // Method to send a message
    private void sendMessage() {
        printWriter.println(textField.getText());
        printWriter.flush();
        textField.setText("");
        textField.requestFocus();
    }

    // Method to create a scrollable text area
    private JScrollPane createScrollableTextArea() {
        textArea = new JTextArea(20, 36);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        return scrollPane;
    }

    // Class to handle incoming messages
    public class IncomingReader implements Runnable {
        public void run() {
            String message;
            try {
                while ((message = bufferedReader.readLine()) != null) {
                    System.out.println("Message -> " + message);
                    textArea.append(message + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
