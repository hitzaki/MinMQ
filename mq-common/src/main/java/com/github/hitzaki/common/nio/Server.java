package com.github.hitzaki.common.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class Server {
    public static void main(String[] args) {
        try {
            new Server().startServer();
        } catch (Exception e) {
            System.out.println("服务器端异常");
        }
    }
    //服务器启动的方法
    public void startServer() throws Exception{
        //1.创建Selector选择器
        Selector selector = Selector.open();
        //2.创建ServerSocketChannel通道
        ServerSocketChannel serverSocketChannel= ServerSocketChannel.open();
        //3.为channel通道绑定监听端口,设置非阻塞,注册到选择器
        serverSocketChannel.bind(new InetSocketAddress("localhost",8801));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
        //4.循环等待新连接的接入
        for(;;){
            int select=selector.select();
            if(select==0){
                continue;
            }
            Set<SelectionKey> selectionKeys=selector.selectedKeys();
            Iterator<SelectionKey> iterator=selectionKeys.iterator();
            while(iterator.hasNext()){
                SelectionKey selectionKey=iterator.next();
                iterator.remove();
                //5.根据就绪状态调用对应业务逻辑
                //5.1.如果是accept状态
                if(selectionKey.isAcceptable()){
                    acceptOperator(serverSocketChannel,selector);
                }
                //5.2.如果是可读状态
                if(selectionKey.isReadable()){
                    readOperator(selector,selectionKey);
                }
            }
        }
    }


    private void acceptOperator(ServerSocketChannel serverSocketChannel, Selector selector)throws Exception{
        //1.获取SocketChannel对象,设置非阻塞
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        //2.注册到selector
        socketChannel.register(selector, SelectionKey.OP_READ);
        //3.回复信息到客户端,如成功提示
        socketChannel.write(Charset.forName("UTF-8").encode("已成功接入聊天室,请注意隐私安全"));
    }


    //处理可读状态
    private void readOperator(Selector selector,SelectionKey selectionKey)throws Exception{
        //1.从Key中获取已就绪的通道
        SocketChannel socketChannel=(SocketChannel)selectionKey.channel();
        //2.创建buffer
        ByteBuffer buffer= ByteBuffer.allocate(1024);
        //3.读取客户端消息
        int len=0;
        try {
            len = socketChannel.read(buffer);
        } catch (IOException e) {
            // 客户端关闭连接，取消SelectionKey并关闭SocketChannel
            selectionKey.cancel();
            socketChannel.close();
            System.out.println("客户端异常：" + e.getMessage());
            return;
        }
        if (len == -1) {
            // 如果客户端关闭连接，则关闭SocketChannel并取消SelectionKey
            selectionKey.cancel();
            socketChannel.close();
            System.out.println("客户端退出");
            return;
        }
        String str="";
        if(len>0){
            buffer.flip();
            str+=Charset.forName("UTF-8").decode(buffer);
        }
        //4.将channel再次注册到选择器
        socketChannel.register(selector,SelectionKey.OP_READ);
        //5.广播客户端的消息到其他客户端
        if(str.length()>0){
            System.out.println("客户端发送"+str);
            // castOtherClient(str,selector,socketChannel);
        }
    }
    private void castOtherClient(String message, Selector selector, SocketChannel sender) throws Exception {
        // 获取所有已连接的SocketChannel
        Set<SelectionKey> selectionKeys = selector.keys();
        for (SelectionKey selectionKey : selectionKeys) {
            // 排除发送消息的客户端，以及ServerSocketChannel
            Channel channel = selectionKey.channel();
            if (channel instanceof SocketChannel && channel != sender) {
                // 向其他客户端发送消息
                SocketChannel socketChannel = (SocketChannel) channel;
                socketChannel.write(Charset.forName("UTF-8").encode(message));
            }
        }
    }

}
