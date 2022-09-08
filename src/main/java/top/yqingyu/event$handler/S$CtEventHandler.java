package top.yqingyu.event$handler;

import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.nio$server.event.EventHandler;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.utils.ThreadUtil;
import top.yqingyu.component.RegistryCenter;
import top.yqingyu.thread.DealMsgThread;
import top.yqingyu.thread.RecordIpThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static top.yqingyu.main.MainConfig.CI_PartitionMsgQueue;
import static top.yqingyu.main.MainConfig.CLIENT_RESPONSE_TIMEOUT;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.event.C$IEventHandler
 * @description
 * @createTime 2022年09月07日 21:06:00
 */
@Slf4j
public class S$CtEventHandler extends EventHandler {

    public S$CtEventHandler(Selector selector, ThreadPoolExecutor pool) {
        super(selector, pool);
    }

    @Override
    public void read(Selector selector, SelectionKey selectionKey) throws IOException {

        AtomicReference<SocketChannel> socketChannel = new AtomicReference<>();
        AtomicReference<String> ip = new AtomicReference<>();
        String name = Thread.currentThread().getName();

        try {
            FutureTask<String> futureTask = new FutureTask<>(() -> {
                ThreadUtil.setThisThreadName(name);

                socketChannel.set((SocketChannel) selectionKey.channel());
                InetSocketAddress socketAddress = (InetSocketAddress) socketChannel.get().getRemoteAddress();
                ip.set(socketAddress.getHostString());

                QyMsg QyMsg = MsgTransfer.readQyMsg(socketChannel.get(), CI_PartitionMsgQueue, 0L);
                if (RegistryCenter.isRegistered(QyMsg.getFrom())) {
                    log.debug("{}", QyMsg);
                    new DealMsgThread(socketChannel.get(), selector).deal2(QyMsg);

                } else {
                    socketChannel.get().close();
                    RecordIpThread.execute(ip.get());
                }
                return "";
            });
            POOL.execute(futureTask);
            futureTask.get(CLIENT_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);

        } catch (TimeoutException e) {
            log.error("", e);
            RecordIpThread.execute(ip.get());
            SocketChannel a = socketChannel.get();
            if (a != null) {
                a.close();
            }
        } catch (ExecutionException e) {
            log.error("", e);
            if (e.getMessage().contains("NumberFormatException"))
                RecordIpThread.execute(ip.get());
            SocketChannel a = socketChannel.get();
            if (a != null) {
                a.close();
            }
        } catch (Exception e) {
            SocketChannel a = socketChannel.get();
            if (a != null) {
                a.close();
            }
            e.addSuppressed(new Exception("客户端响应超时"));
        }

    }

    @Override
    public void write(Selector selector, SelectionKey selectionKey) throws IOException {
        System.out.println("write");
    }
}
