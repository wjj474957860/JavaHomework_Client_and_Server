/*
 * 这个在书本上的“聊天室”例子加上排序功能，可以体现出“服务器端与客户端”的小程序
 * 这个程序代表的是服务器端，Client类代表的是客户端。
 * 服务器端与客户端之间可进行对话与排序功能，使用排序功能的约定是输入一行整数，数之间以空格隔开
 * 第一个数是选择排序的算法，1是冒泡；2是插入；3是选择，例如：1 3 2 4  表示用冒泡来对数据 3 2 4
 * 进行排序，输入其他内容则为对话内容。
 *
 * 作者：吴建杰
 * 学号：20102100035
 */
package server;

import MySort.BaseSort;
import MySort.BubbleSort;
import MySort.InsertSort;
import MySort.SelectSort;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;

public class Server extends JFrame {

    private ObjectInputStream m_input;   // 输入流
    private ObjectOutputStream m_output; // 输出流
    private JTextField m_enter;  // 输入区域
    private JTextArea m_display; // 显示区域
    private int m_clientNumber = 0; // 连接的客户数
    Integer[] number = null;
    Integer choice = null;
    BaseSort base = null;

    public Server() // 在图形界面中添加组件
    {
        super("聊天与排序程序之服务器端");
        Container c = getContentPane();
        m_enter = new JTextField();
        m_enter.setEnabled(false);
        m_enter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) { // 向客户端发送数据
                try {
                    String s = event.getActionCommand();
                    m_output.writeObject(s);
                    m_output.flush();
                    mb_displayAppend("服务器端: " + s);
                    m_enter.setText(""); // 清除输入区域的原有内容
                } catch (Exception e) {
                    System.err.println("发生异常:" + e);
                    e.printStackTrace();
                } // try-catch结构结束
            } // 方法actionPerformed结束
            } // 实现接口ActionListener的内部类结束
                ); // addActionListener方法调用结束
        c.add(m_enter, BorderLayout.NORTH);
        m_display = new JTextArea();
        c.add(new JScrollPane(m_display), BorderLayout.CENTER);
    } // J_ChatServer构造方法结束

    public void mb_displayAppend(String s) {
        m_display.append(s + "\n");
        m_display.setCaretPosition(m_display.getText().length());
        m_enter.requestFocusInWindow(); // 转移输入焦点到输入区域
    } // 方法mb_displayAppend结束

    public boolean mb_isEndSession(String m) {
        if (m.equalsIgnoreCase("q")) {
            return (true);
        }
        if (m.equalsIgnoreCase("quit")) {
            return (true);
        }
        if (m.equalsIgnoreCase("exit")) {
            return (true);
        }
        if (m.equalsIgnoreCase("end")) {
            return (true);
        }
        if (m.equalsIgnoreCase("结束")) {
            return (true);
        }
        return (false);
    } // 方法mb_isEndSession结束

    public String getArrayString(Integer[] number) {
        String s = "";
        for (int i = 0; i < number.length; i++) {
            s += number[i] + " ";
        }
        return s;
    }//方法getArrayString的结束，功能是返回一组数连接起来的字符串

    public void choice(Integer i) {
        try {
            String[] sort = {"冒泡", "插入", "选择"};
            m_output.writeObject(" 正在"+sort[i - 1] +"排序中……");
            mb_displayAppend("服务器端: " + sort[i - 1] + "正在排序中……");
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void mb_run() {
        try {
            ServerSocket server = new ServerSocket(5000);
            String m; // 来自客户端的消息
            while (true) {
                m_clientNumber++;
                mb_displayAppend("等待连接[" + m_clientNumber + "]");
                Socket s = server.accept();
                mb_displayAppend("接收到客户端连接[" + m_clientNumber + "]");
                m_output = new ObjectOutputStream(s.getOutputStream());
                m_input = new ObjectInputStream(s.getInputStream());
                m_output.writeObject("连接成功");
                m_output.flush();
                m_enter.setEnabled(true);
                do {
                    m = (String) m_input.readObject();
                    String[] data = m.split(" ");
                    
                    if (data[0].equals("1")|| data[0].equals("2")|| data[0].equals("3")) {      //判断一下客户端是否想排序，是则进行下面操作
                        choice = Integer.parseInt(data[0]);
                        number = new Integer[data.length - 1];
                        for (int i = 1; i < data.length; i++) {
                            number[i-1] = Integer.parseInt(data[i]);
                        }
                        switch (choice) {
                            case 1:                                                          //冒泡排序
                                base = new BubbleSort(number);
                                base.sort();
                                number = (Integer[]) base.getResult();
                                break;
                            case 2:                                                         //插入排序
                                base = new InsertSort(number);
                                base.sort();
                                number = (Integer[]) base.getResult();
                                break;
                            case 3:                                                         //选择排序
                                base = new SelectSort(number);
                                base.sort();
                                number = (Integer[]) base.getResult();
                                break;

                        }
                        choice(choice);
                        Thread.sleep(1000);
                        m_output.writeObject(getArrayString(number));
                        m_output.flush();
                        mb_displayAppend("服务器端: " + getArrayString(number));
                    }
                    else{
                        mb_displayAppend("客户端: " + m);
                    }
                } while (!mb_isEndSession(m));// do-while循环结束
                m_output.writeObject("q"); // 通知客户端退出程序
                m_output.flush();
                m_enter.setEnabled(false);
                m_output.close();
                m_input.close();
                s.close();
                mb_displayAppend("连接[" + m_clientNumber + "]结束");
            } // while循环结束
        } catch (Exception e) {
            System.err.println("发生异常:" + e);
            e.printStackTrace();
            mb_displayAppend("连接[" + m_clientNumber + "]发生异常");
        } // try-catch结构结束
    } // 方法mb_run结束

    public static void main(String args[]) {
        Server app = new Server();

        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setSize(350, 300);
        app.setVisible(true);
        app.mb_run();
    } // 方法main结束
} // 类J_ChatServer结束
