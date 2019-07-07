package com.hzy.nio.client;

import com.hzy.info.HostInfo;
import com.hzy.util.InputUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NIOEchoClient {
    public static void main(String[] args) throws IOException {
        // 1. 打开客户端连接通道
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.connect(new InetSocketAddress(HostInfo.HOST, HostInfo.PORT)); // 连接
        // 2. 开辟缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        boolean flag = true;

        while (flag) {
            buffer.clear(); // 清空缓冲区
            String str = InputUtil.getString("请输入要发送的内容：").trim();
            buffer.put(str.getBytes()); // 将输入的数据保存在缓冲区中
            buffer.flip();  // 重置缓冲区
            clientChannel.write(buffer);    // 发送数据
            buffer.clear(); // 在读取之前进行缓冲区清空
            int readCount = clientChannel.read(buffer);
            buffer.flip();
            System.out.println(new String(buffer.array(), 0, readCount));
            if("byebye".equalsIgnoreCase(str)) {
                flag = false;
            }
        }

        clientChannel.close();
    }
}
