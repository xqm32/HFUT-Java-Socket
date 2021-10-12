package Server;

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

class ClientHandler implements Runnable {
    private static final String STR_CLIENT_DISCONNECTED = "A client disconnected";

    private Socket client;
    private Messager messagerPrint;
    private Scanner messagerRecive;

    public ClientHandler(Socket client, Messager messagerPrint) {
        this.client = client;
        this.messagerPrint = messagerPrint;

        try {
            messagerRecive = new Scanner(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (messagerRecive.hasNextLine()) {
            messagerPrint.message.println(messagerRecive.nextLine());
        }

        // 客户端断开连接
        try {
            client.close();
            messagerPrint.message.println(STR_CLIENT_DISCONNECTED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class Server implements Runnable {
    private static final String STR_SERVER_CLOSE = "Server closed";
    private static final String STR_NEW_CLIENT = "A new client connected";

    private ServerSocket server;
    private Socket client;
    private Messager messagerPrint;
    // 如果不需要服务器自动群发至客户端，此条应被注释
    // private Messager messagerSend;
    private ArrayList<PrintWriter> clientSenders;

    public Server(ServerSocket server, Messager messagerPrint, Messager messagerSend) {
        this.server = server;
        this.messagerPrint = messagerPrint;
        // 如果不需要服务器自动群发至客户端，此条应被注释
        // this.messagerSend = messagerSend;
        this.clientSenders = new ArrayList<>();
        messagerSend.message = (message) -> {
            for (PrintWriter i : clientSenders)
                i.println(message);
            // 刷新并删除已断开连接的客户端
            clientSenders.removeIf((PrintWriter i) -> {
                return i.checkError();
            });
        };
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                client = server.accept();
                clientSenders.add(new PrintWriter(client.getOutputStream()));
                new Thread(new ClientHandler(client, messagerPrint)).start();
                messagerPrint.message.println(STR_NEW_CLIENT);
            }
            server.close();
        } catch (SocketException e) {
            // 服务端被关闭
            messagerPrint.message.println(STR_SERVER_CLOSE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        final int DEFALUT_PORT = 8888;
        Messager messagerSend = new Messager();
        try {
            new Thread(new Server(new ServerSocket(DEFALUT_PORT), new Messager((message) -> {
                System.out.println(message);
            }), messagerSend)).start();
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
