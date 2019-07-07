package com.hzy.bio.server;

import com.hzy.info.HostInfo;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BIOEchoServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(HostInfo.PORT);    // 设置监听端口
        System.out.println("服务端已经启动，监听端口为：" + HostInfo.PORT);

        ExecutorService executorService = Executors.newFixedThreadPool(4);  // 设置线程池
        boolean flag = true;
        while (flag) {
            Socket socket = serverSocket.accept();
            executorService.submit(new EchoClientHandler(socket));
        }

        executorService.shutdown();
        serverSocket.close();
    }

    public static class EchoClientHandler implements Runnable {
        private Socket client;  // 每一个客户端都需要启动一个任务(task)来执行
        private Scanner scanner;
        private PrintStream out;
        private boolean flag = true;

        public EchoClientHandler(Socket client) {
            this.client = client;   // 保存每一个客户端操作
            try {
                this.scanner = new Scanner(this.client.getInputStream());
                this.scanner.useDelimiter("\n");    // 设置换行符
                this.out = new PrintStream(this.client.getOutputStream());
            } catch (IOException e) {
            }
        }

        @Override
        public void run() {
            while (flag) {
                if(this.scanner.hasNext()) {    // 现在有数据进行输入
                    String str = this.scanner.next().trim();    // 去掉多余的空格
                    if("byebye".equalsIgnoreCase(str)) {
                        this.out.println("ByeByeBye...");
                        this.flag = false;
                    } else {
                        this.out.println("[ECHO] " + str);
                    }
                }
            }

            this.scanner.close();
            this.out.close();
            try {
                this.client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
