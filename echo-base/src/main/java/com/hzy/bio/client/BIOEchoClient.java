package com.hzy.bio.client;

import com.hzy.info.HostInfo;
import com.hzy.util.InputUtil;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class BIOEchoClient {
    public static void main(String[] args) throws IOException {
        Socket client = new Socket(HostInfo.HOST, HostInfo.PORT);   // 指定服务器端地址和端口号
        Scanner scanner = new Scanner(client.getInputStream());     // 接收服务端响应数据
        scanner.useDelimiter("\n"); // 设置分隔符
        PrintStream out = new PrintStream(client.getOutputStream());    // 向服务端发送信息
        boolean flag = true;    // 循环标志

        while (flag) {
            String str = InputUtil.getString("请输入要发送的数据: ");
            out.println(str);   // 向服务端发送消息
            if(scanner.hasNext()) { // 获取服务端响应数据
                String resData = scanner.next().trim();
                System.out.println(resData);
            }
            if("byebye".equalsIgnoreCase(str)) {
                flag = false;
            }
        }
    }
}
