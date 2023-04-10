package com.github.hitzaki.common.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Client {
    public static void main(String[] args) {
        try {
            new Client().startClient();
        } catch (Exception e) {
            System.out.println("客户端异常");
        }
    }

    //启动客户端
    public void startClient()throws Exception{
        //连接服务器
        SocketChannel socketChannel= SocketChannel.open(new InetSocketAddress("localhost",8801));
        //接收服务器响应数据
        Selector selector= Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector,SelectionKey.OP_READ);
        new Thread(new ClientReadThread(selector)).start();
        //向服务器发送消息
        Scanner scanner=new Scanner(System.in);
        while(scanner.hasNext()){
            String msg=scanner.nextLine();
            if(msg.length()>0){
                socketChannel.write(Charset.forName("UTF-8").encode(msg));
            }
        }
    }

    //从服务器端读数据的辅助线程
    public class ClientReadThread implements Runnable{
        Selector selector;
        public ClientReadThread(Selector selector){
            this.selector=selector;
        }
        @Override
        public void run(){
            try{
                readByServer();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        public void readByServer() throws Exception{
            for(;;){
                int num=selector.select();
                if(num==0)continue;
                Set<SelectionKey> keys=selector.selectedKeys();
                Iterator<SelectionKey> iterator=keys.iterator();
                //迭代访问key
                while(iterator.hasNext()){
                    SelectionKey key=iterator.next();
                    //如果是具备读的条件,则进行读取操作
                    if(key.isReadable()){
                        readOperator(selector,key);
                    }
                    iterator.remove();
                }
            }
        }
//处理可读状态
        private void readOperator(Selector selector,SelectionKey selectionKey)throws Exception{
            //1.从Key中获取已就绪的通道
            SocketChannel socketChannel=(SocketChannel)selectionKey.channel();
            //2.创建buffer
            ByteBuffer buffer= ByteBuffer.allocate(1024);
            //3.读取服务器消息
            int len=0;
            try {
                len = socketChannel.read(buffer);
            } catch (IOException e) {
                // 客户端关闭连接，取消SelectionKey并关闭SocketChannel
                selectionKey.cancel();
                socketChannel.close();
                System.out.println("服务器端异常：" + e.getMessage());
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
            System.out.println(str);
            //4.将channel再次注册到选择器
            socketChannel.register(selector,SelectionKey.OP_READ);
        }
    }

}
