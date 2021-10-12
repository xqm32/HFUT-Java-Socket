package Client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

class ClientPanel extends JPanel {
    // 满足实验要求
    private static final String STR_CONNECTING = "Connecting";
    private static final String STR_CONNECTED = "Connected";
    private static final String STR_TO_SERVER = " to server ";
    private static final String STR_AT_PORT = " at port ";
    private static final String STR_CONNECTION_REFUSED = "Connection refused";
    private static final String STR_WHO_AM_I = "Client: ";
    private static final String LABEL_HOST = "Server IP: ";
    private static final String LABEL_PORT = "Server Port: ";
    private static final String LABEL_SAY = "Say: ";
    private static final String BUTTON_CONNECT = "Connect";
    private static final String BUTTON_SAY = "Say";
    private static final String TITLE_SETTINGS = "客户机设置";
    private static final String DEFAULT_HOST = "127.0.0.1";

    private static final int TEXT_ROWS = 10;
    private static final int TEXT_COLUMNS = 30;
    private static final int BOX_INTERVAL = 10;
    private static final int BOX_PADDING = 10;
    private static final int INBOX_INTERVAL = 10;
    private static final int INBOX_PADDING = 5;
    private static final int DEFALUT_PORT = 8888;

    private JLabel labelHost;
    private JTextField textFieldHost;
    private JLabel labelPort;
    private JTextField textFieldPort;
    private JButton buttonA;
    private JTextArea textAreaB;
    private JLabel labelC;
    private JTextField textFieldC;
    private JButton buttonC;

    private Thread threadClient;
    private Socket client;
    private Messager messagerSend;
    private Messager messagerPrint;

    private String getHost() {
        String host = textFieldHost.getText();
        if (host.isEmpty())
            return DEFAULT_HOST;
        else
            return host;
    }

    private int getPort() {
        try {
            int port = Integer.valueOf(textFieldPort.getText());
            return port;
        } catch (NumberFormatException e) {
            return DEFALUT_PORT;
        }
    }

    public ClientPanel() {
        messagerPrint = new Messager((message) -> {
            textAreaB.append(message + "\n");
        });
        messagerSend = new Messager();

        // 第一行
        labelHost = new JLabel(LABEL_HOST);
        textFieldHost = new JTextField(TEXT_COLUMNS / 2);
        textFieldHost.setBorder(BorderFactory.createLineBorder(Color.gray));
        labelPort = new JLabel(LABEL_PORT);
        textFieldPort = new JTextField(TEXT_COLUMNS / 2);
        textFieldPort.setBorder(BorderFactory.createLineBorder(Color.gray));
        buttonA = new JButton(BUTTON_CONNECT);
        buttonA.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    // 关闭原服务，开启一个新服务
                    if (threadClient != null) {
                        client.close();
                        threadClient.interrupt();
                        while (threadClient.isAlive())
                            ;
                    }

                    String host = getHost();
                    int port = getPort();
                    messagerPrint.message.println(STR_CONNECTING + STR_TO_SERVER + host + STR_AT_PORT + port);
                    client = new Socket(host, port);
                    threadClient = new Thread(new Client(client, messagerPrint, messagerSend));
                    threadClient.start();
                    messagerPrint.message.println(STR_CONNECTED + STR_TO_SERVER + host + STR_AT_PORT + port);
                } catch (ConnectException e) {
                    messagerPrint.message.println(STR_CONNECTION_REFUSED);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Box lineAContent = Box.createHorizontalBox();
        lineAContent.add(labelHost);
        lineAContent.add(Box.createHorizontalStrut(INBOX_INTERVAL));
        lineAContent.add(textFieldHost);
        lineAContent.add(Box.createHorizontalStrut(INBOX_INTERVAL));
        lineAContent.add(labelPort);
        lineAContent.add(Box.createHorizontalStrut(INBOX_INTERVAL));
        lineAContent.add(textFieldPort);
        lineAContent.add(Box.createHorizontalStrut(INBOX_INTERVAL));
        lineAContent.add(buttonA);

        Box lineABorder = Box.createVerticalBox();
        lineABorder.add(Box.createVerticalStrut(INBOX_PADDING));
        lineABorder.add(lineAContent);
        lineABorder.add(Box.createVerticalStrut(INBOX_PADDING));

        Box lineA = Box.createHorizontalBox();
        lineABorder.setBorder(BorderFactory.createTitledBorder(TITLE_SETTINGS));
        lineA.add(Box.createHorizontalStrut(BOX_PADDING));
        lineA.add(lineABorder);
        lineA.add(Box.createHorizontalStrut(BOX_PADDING));

        // 第二行
        textAreaB = new JTextArea(TEXT_ROWS, TEXT_COLUMNS);
        textAreaB.setEditable(false);
        JScrollPane scrollPaneB = new JScrollPane(textAreaB);
        scrollPaneB.setBorder(BorderFactory.createLineBorder(Color.gray));

        Box lineB = Box.createHorizontalBox();
        lineB.add(Box.createHorizontalStrut(BOX_PADDING));
        lineB.add(scrollPaneB);
        lineB.add(Box.createHorizontalStrut(BOX_PADDING));

        // 第三行
        labelC = new JLabel(LABEL_SAY);
        textFieldC = new JTextField(TEXT_COLUMNS);
        textFieldC.setBorder(BorderFactory.createLineBorder(Color.gray));
        buttonC = new JButton(BUTTON_SAY);
        buttonC.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (threadClient == null || messagerSend.message == null)
                    return;

                String say = textFieldC.getText();
                // 不允许打印空内容
                if (say.isEmpty())
                    return;

                messagerSend.message.println(STR_WHO_AM_I + say);
                messagerPrint.message.println(STR_WHO_AM_I + say);
                // 清空输入框
                textFieldC.setText("");
            }
        });

        Box lineC = Box.createHorizontalBox();
        lineC.add(Box.createHorizontalStrut(BOX_PADDING));
        lineC.add(labelC);
        lineC.add(Box.createHorizontalStrut(INBOX_INTERVAL));
        lineC.add(textFieldC);
        lineC.add(Box.createHorizontalStrut(INBOX_INTERVAL));
        lineC.add(buttonC);
        lineC.add(Box.createHorizontalStrut(BOX_PADDING));

        Box mainBox = Box.createVerticalBox();
        mainBox.add(Box.createVerticalStrut(BOX_PADDING));
        mainBox.add(lineA);
        mainBox.add(Box.createVerticalStrut(BOX_INTERVAL));
        mainBox.add(lineB);
        mainBox.add(Box.createVerticalStrut(BOX_INTERVAL));
        mainBox.add(lineC);
        mainBox.add(Box.createVerticalStrut(BOX_PADDING));

        add(mainBox);
    }
}

public class ClientGUI implements Runnable {
    private static final String TITLE_CLIENT = "客户端";

    private JFrame frame;
    private ClientPanel panel;

    @Override
    public void run() {
        frame = new JFrame(TITLE_CLIENT);
        panel = new ClientPanel();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        Thread threadGUI = new Thread(new ClientGUI());
        threadGUI.start();
    }
}
