package Server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

class ServerPanel extends JPanel {
    private static final String STR_SERVER_STARTING = "Server starting";
    private static final String STR_SERVER_STARTED = "Server started";
    private static final String STR_AT_PORT = " at port ";
    private static final String STR_WHO_AM_I = "Server: ";
    private static final String LABEL_PORT = "Port: ";
    private static final String LABEL_SAY = "Say: ";
    private static final String BUTTON_START = "Start";
    private static final String BUTTON_SAY = "Say";
    private static final String TITLE_SETTINGS = "服务器设置";

    private static final int TEXT_ROWS = 10;
    private static final int TEXT_COLUMNS = 40;
    private static final int BOX_INTERVAL = 10;
    private static final int BOX_PADDING = 10;
    private static final int INBOX_INTERVAL = 10;
    private static final int INBOX_PADDING = 5;
    private static final int DEFALUT_PORT = 8888;

    private JLabel labelA;
    private JTextField textFieldA;
    private JButton buttonA;
    private JTextArea textAreaB;
    private JLabel labelC;
    private JTextField textFieldC;
    private JButton buttonC;

    private Thread threadServer;
    private ServerSocket server;
    private Messager messagerSend;
    private Messager messagerPrint;

    private int getPort() {
        try {
            int port = Integer.valueOf(textFieldA.getText());
            return port;
        } catch (NumberFormatException e) {
            return DEFALUT_PORT;
        }
    }

    public ServerPanel() {
        messagerPrint = new Messager((message) -> {
            textAreaB.append(message + "\n");
        });
        messagerSend = new Messager();

        // 第一行
        labelA = new JLabel(LABEL_PORT);
        textFieldA = new JTextField(TEXT_COLUMNS);
        textFieldA.setBorder(BorderFactory.createLineBorder(Color.gray));
        buttonA = new JButton(BUTTON_START);
        buttonA.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    // 关闭原服务，开启一个新服务
                    if (threadServer != null) {
                        server.close();
                        threadServer.interrupt();
                        while (threadServer.isAlive())
                            ;
                    }

                    int port = getPort();
                    messagerPrint.message.println(STR_SERVER_STARTING + STR_AT_PORT + port);
                    server = new ServerSocket(port);
                    threadServer = new Thread(new Server(server, messagerPrint, messagerSend));
                    threadServer.start();
                    messagerPrint.message.println(STR_SERVER_STARTED + STR_AT_PORT + port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Box lineAContent = Box.createHorizontalBox();
        lineAContent.add(labelA);
        lineAContent.add(Box.createHorizontalStrut(INBOX_INTERVAL));
        lineAContent.add(textFieldA);
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
                if (threadServer == null || messagerSend.message == null)
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

public class ServerGUI implements Runnable {
    private static final String TITLE_SERVER = "服务器";

    private JFrame frame;
    private ServerPanel panel;

    @Override
    public void run() {
        frame = new JFrame(TITLE_SERVER);
        panel = new ServerPanel();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        Thread threadGUI = new Thread(new ServerGUI());
        threadGUI.start();
    }
}