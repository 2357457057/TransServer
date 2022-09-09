package top.yqingyu.event$handler;


import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.nio$server.event.EventHandler;
import top.yqingyu.common.utils.IoUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.event$handler.HttpEventHandler
 * @description
 * @createTime 2022年09月09日 18:05:00
 */
@Slf4j
public class HttpEventHandler extends EventHandler {

    public HttpEventHandler(Selector selector, ThreadPoolExecutor pool) {
        super(selector, pool);
    }

    @Override
    public void read(Selector selector, SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();


        byte[] methodBytes = IoUtil.readBytes(socketChannel, 7);
        String method = new String(methodBytes, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        if (method.matches("(GET|POST|DELETE|PUT|HEAD|OPTIONS).{0,4}")) {
            int step = 0;
            do {
                byte[] bytes = IoUtil.readBytes(socketChannel, 32);
                step = bytes.length;
                String header = new String(bytes, StandardCharsets.UTF_8);


            } while (step > 0);

        }


//        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//
//        int length = 0;
//        boolean readEnd = false;
//        while (!readEnd) {
//            while ((length = socketChannel.read(byteBuffer)) > 0) {
//                byteBuffer.flip();
//                String tmp = new String(byteBuffer.array(), 0, length, StandardCharsets.UTF_8);
//                byteBuffer.clear();
//
//                //判断结束，get请求，末尾是两个换行
//                if (tmp.contains("\r\n\r\n")) {
//                    readEnd = true;
//                    break;
//                }
//                //header ;?/
//                int indexOf = tmp.indexOf("\r\n");
//                if (indexOf != -1) {
//                    log.info("{}",indexOf);
//                    System.out.println(tmp);
//                }else {
//                    System.out.println(tmp);
//                }
//            }
//        }
    }


    @Override
    public void write(Selector selector, SelectionKey selectionKey) throws IOException {

    }
}
