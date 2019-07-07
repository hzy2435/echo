package com.hzy.nio.server;

import com.hzy.info.HostInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NIOEchoServer {
    public static void main(String[] args) throws IOException {
        // 1. NIO 的实现考虑到性能的问题以及响应时间问题，需要设置一个线程池，采用固定大小的线程池
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        // 2. NIO 的处理是基于 Channel 控制的，所以有一个 Selector 就是负责管理所有的 Channel
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        // 3. 需要为其设置一个非阻塞的状态机制
        serverChannel.configureBlocking(false);
        // 4. 服务器上需要提供一个网络的监听端口
        serverChannel.bind(new InetSocketAddress(HostInfo.PORT));
        // 5. 需要设置一个 Selector, 作为一个选择器的出现，目的是管理所有的 Channel
        Selector selector = Selector.open();
        // 6. 将当前的 Channel 注册到 Selector 之中
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);   // 连接时处理
        System.out.println("服务端已启动，监听端口为：" + HostInfo.PORT);
        // 7. NIO 采用的是轮询模式，每当发现有用户连接的时候就需要启动一个线程(线程池管理)
        int select = 0; // 接收轮询状态
        while((select = selector.select()) > 0) {   // 实现了轮询处理
            Set<SelectionKey> selectionKeys = selector.selectedKeys();  // 获取全部的 Key
            Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();
            while (selectionKeyIterator.hasNext()) {
                SelectionKey selectionKey = selectionKeyIterator.next();    // 获取每一个 Key 的信息
                if(selectionKey.isAcceptable()) {   // 为连接模式
                    SocketChannel client = serverChannel.accept();  // 等待连接
                    if(client != null)  {
                        executorService.submit(new EchoClientHandler(client));
                    }
                }
                selectionKeyIterator.remove();
            }
        }

        executorService.shutdown();
        serverChannel.close();
    }

    private static class EchoClientHandler implements Runnable {

        private SocketChannel client;
        boolean flag = true;

        public EchoClientHandler(SocketChannel client) {
            this.client = client;
            // 严格意义上来讲，当已经成功地连接上了服务器，并且需要进行进一步的处理之前要发送一些消息给客户端
        }

        @Override
        public void run() {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (flag) {  // 需要不断进行交互
                try {
                    buffer.clear(); // 清空缓冲区
                    int messageCount = this.client.read(buffer);    // 向缓冲区中读取数据
                    // 接收消息
                    String readMessage = new String(buffer.array(), 0, messageCount).trim();
                    // 回应数据消息
                    String writeMessage = "[ECHO] " + readMessage + "\n";
                    if("byebye".equalsIgnoreCase(readMessage)) {
                        writeMessage = "ByeBye~ 下次见!";
                        this.flag = false;
                    }
                    // 数据输入通过缓存的形式完成，而数据的输出同样需要进行缓存操作
                    buffer.clear(); // 为写入新的返回数据而定义
                    buffer.put(writeMessage.getBytes());    // 发送内容
                    buffer.flip();  // 重置缓冲区
                    this.client.write(buffer);  // 回应数据
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                this.client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
