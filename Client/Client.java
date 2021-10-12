package Client;

import java.io.*;
import java.net.*;
import java.util.*;

class Messager {
    @FunctionalInterface
    static interface canPrint {
        public void println(String message);
    }

    public canPrint message;

    public Messager() {
    }

    public Messager(canPrint message) {
        this.message = message;
    }
}

public class Client implements Runnable {
    private static final String STR_CLIENT_CLOSE = "Client closed because server closed";

    private Socket client;
    private Messager messagerPrint;
    private Scanner messagerRecive;
    private PrintWriter serverSender;

    public Client(Socket client, Messager messagerPrint, Messager messagerSend) {
        this.messagerPrint = messagerPrint;

        try {
            messagerRecive = new Scanner(new InputStreamReader(client.getInputStream()));
            serverSender = new PrintWriter(client.getOutputStream());
            messagerSend.message = (message) -> {
                serverSender.println(message);
                serverSender.flush();
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && messagerRecive.hasNextLine()) {
            messagerPrint.message.println(messagerRecive.nextLine());
        }

        try {
            client.close();
        } catch (NullPointerException e) {
            messagerPrint.message.println(STR_CLIENT_CLOSE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        final String STR_CONNECTION_REFUSED = "Connection refused";
        final String DEFALUT_HOST = "127.0.0.1";
        final int DEFALUT_PORT = 8888;
        Messager messagerSend = new Messager();
        try {
            new Thread(new Client(new Socket(DEFALUT_HOST, DEFALUT_PORT), new Messager((message) -> {
                System.out.println(message);
            }), messagerSend)).start();
        } catch (ConnectException e) {
            System.out.println(STR_CONNECTION_REFUSED);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread() {
            @Override
            public void run() {
                Scanner input = new Scanner(new InputStreamReader(System.in));
                while (input.hasNextLine()) {
                    messagerSend.message.println(input.nextLine());
                }
            }
        }.start();
    }
}
