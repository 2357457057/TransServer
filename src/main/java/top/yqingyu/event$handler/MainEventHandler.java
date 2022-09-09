package top.yqingyu.event$handler;

import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.nio$server.event.EventHandler;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.MsgType;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.utils.PercentUtil;
import top.yqingyu.common.utils.ThreadUtil;
import top.yqingyu.component.RegistryCenter;
import top.yqingyu.main.MainConfig;
import top.yqingyu.thread.DealMsgThread;
import top.yqingyu.thread.RecordIpThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static top.yqingyu.main.MainConfig.*;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.event.MainEventHandler
 * @description
 * @createTime 2022年09月07日 17:48:00
 */
@Slf4j
public class MainEventHandler extends EventHandler {

    public MainEventHandler(Selector selector, ThreadPoolExecutor pool) {
        super(selector, pool);
    }

    @Override
    public void read(Selector selector, SelectionKey selectionKey) throws IOException {
        AtomicReference<SocketChannel> socketChannel_C = new AtomicReference<>();
        AtomicReference<String> ip = new AtomicReference<>();

        String name = Thread.currentThread().getName();

        try {
            FutureTask<String> futureTask = new FutureTask<>(() -> {
                ThreadUtil.setThisThreadName(name);
                LocalDateTime now1 = LocalDateTime.now();
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                socketChannel_C.set(socketChannel);

                InetSocketAddress socketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                ip.set(socketAddress.getHostString());

                QyMsg thisMsg = MsgTransfer.readQyMsg(socketChannel, Main_PartitionMsgQueue, 0L);

                MsgType type = thisMsg.getMsgType();

                QyMsg clone;

                if (RegistryCenter.isRegistered(thisMsg.getFrom())) {

                    if (MsgType.HEART_BEAT == type) {

                        clone = HEART_BEAT_MSG.clone();
                        clone.putMsg(HEART_BEAT);

                        socketChannel.register(selector, SelectionKey.OP_WRITE);
                        if (PercentUtil.percentTrue(HEART_BEAT_percent))
                            MsgTransfer.writeQyMsg(socketChannel, clone);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        log.debug("{}", thisMsg.toString());
                    } else {

                        new DealMsgThread(socketChannel, selector).deal(thisMsg);
                        LocalDateTime now2 = LocalDateTime.now();
                        long nanos = LocalDateTimeUtil.between(now1, now2, ChronoUnit.MICROS);
                        log.info("命令执行完成：{}  | {}ns", thisMsg.toString(), nanos);
                    }
                } else {
                    if (MsgType.AC == type && MainConfig.AC_STR.equals(MsgHelper.gainMsgValue(thisMsg, "AC_STR"))) {

                        if (RegistryCenter.registrationClient(selectionKey, selector, thisMsg)) {
                            socketChannel.register(selector, SelectionKey.OP_WRITE);

                            clone = AC_MSG.clone();
                            clone.putMsg(AC_STR);

                            MsgTransfer.writeQyMsg(socketChannel, clone);
                            socketChannel.register(selector, SelectionKey.OP_READ);

                            log.info("{}: {}", AC_STR, thisMsg);
                        }
                    } else {
                        InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                        log.info("未注册的消息关闭连接{}:{} {} ", remoteAddress.getHostString(), remoteAddress.getPort(), thisMsg);
                        clone = ERR_MSG.clone();
                        clone.putMsg("注册中心无此id");
                        MsgTransfer.writeQyMsg(socketChannel, clone);
                        socketChannel.close();
                    }

                }
                return "";
            });

            POOL.execute(futureTask);
            futureTask.get(CLIENT_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.error("", e);
            RecordIpThread.execute(ip.get());
            SocketChannel a = socketChannel_C.get();
            if (a != null) {
                a.close();
            }
        } catch (ExecutionException e) {
            log.error("", e);
            if (e.getMessage().matches(".*(NumberFormatException|Cannot.*Boolean[.]booleanValue).*"))
                RecordIpThread.execute(ip.get());
            SocketChannel a = socketChannel_C.get();
            if (a != null) {
                a.close();
            }
        } catch (Exception e) {

            SocketChannel a = socketChannel_C.get();
            if (a != null) {
                a.close();
            }
            e.addSuppressed(new Exception("客户端响应超时"));
            log.error("", e);
        }
    }


    @Override
    public void write(Selector selector, SelectionKey selectionKey) throws IOException {

    }
}
