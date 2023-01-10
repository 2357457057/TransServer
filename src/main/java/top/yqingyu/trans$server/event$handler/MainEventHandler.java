package top.yqingyu.trans$server.event$handler;

import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.nio$server.core.EventHandler;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.MsgType;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.utils.PercentUtil;
import top.yqingyu.common.utils.ThreadUtil;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.MainConfig;
import top.yqingyu.trans$server.thread.DealMsgThread;
import top.yqingyu.trans$server.thread.RecordIpThread;
import top.yqingyu.trans$server.thread.ClientTransThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.event.MainEventHandler
 * @description
 * @createTime 2022年09月07日 17:48:00
 */
@Slf4j
public class MainEventHandler extends EventHandler {

    public MainEventHandler(Selector selector) throws IOException {
       super(selector);
    }

    @Override
    protected void loading() {

    }
    /**
     *  TODO 待优化！ 长消息需更多时间
     *
     * @param selector
     * @param socketChannel
     * @throws IOException
     */
    @Override
    public void read(Selector selector, SocketChannel socketChannel) throws IOException {
        AtomicReference<SocketChannel> socketChannel_C = new AtomicReference<>();
        AtomicReference<String> ip = new AtomicReference<>();

        String name = Thread.currentThread().getName();

        try {
            FutureTask<String> futureTask = new FutureTask<>(() -> {
                ThreadUtil.setThisThreadName(name);
                LocalDateTime now1 = LocalDateTime.now();
                socketChannel_C.set(socketChannel);

                InetSocketAddress socketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                ip.set(socketAddress.getHostString());

                QyMsg thisMsg = MsgTransfer.readQyMsg(socketChannel, MainConfig.Main_PartitionMsgQueue, 0L);

                MsgType type = thisMsg.getMsgType();

                QyMsg clone;

                if (RegistryCenter.isRegistered(thisMsg.getFrom())) {

                    if (MsgType.HEART_BEAT == type) {

                        clone = MainConfig.HEART_BEAT_MSG.clone();
                        clone.putMsg(MainConfig.HEART_BEAT);

                        socketChannel.register(selector, SelectionKey.OP_WRITE);
                        if (PercentUtil.percentTrue(MainConfig.HEART_BEAT_percent))
                            MsgTransfer.writeQyMsg(socketChannel, clone);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        log.debug("{}", thisMsg.toString());
                    } else {

                        new DealMsgThread(socketChannel, selector).deal(thisMsg);
                        LocalDateTime now2 = LocalDateTime.now();
                        long nanos = LocalDateTimeUtil.between(now1, now2, ChronoUnit.MILLIS);
                        log.info("命令执行完成：{}  | {}ms", MsgHelper.gainMsg(thisMsg), nanos);
                    }
                } else {
                    if (MsgType.AC == type && MainConfig.AC_STR.equals(MsgHelper.gainMsgValue(thisMsg, "AC_STR"))) {

                        if (RegistryCenter.registrationClient(socketChannel, selector, thisMsg)) {
                            socketChannel.register(selector, SelectionKey.OP_WRITE);

                            clone = MainConfig.AC_MSG.clone();
                            clone.putMsg(MainConfig.AC_STR);

                            MsgTransfer.writeQyMsg(socketChannel, clone);
                            socketChannel.register(selector, SelectionKey.OP_READ);

                            log.info("{}: {}", MainConfig.AC_STR, thisMsg);
                        }
                    } else {
                        InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                        log.info("未注册的消息关闭连接{}:{} {} ", remoteAddress.getHostString(), remoteAddress.getPort(), thisMsg);
                        clone = MainConfig.ERR_MSG.clone();
                        clone.putMsg("注册中心无此id");
                        MsgTransfer.writeQyMsg(socketChannel, clone);
                        socketChannel.close();
                    }

                }
                return "";
            });

            ClientTransThread.POOL.execute(futureTask);
            futureTask.get(MainConfig.CLIENT_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
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
    public void write(Selector selector, SocketChannel socketChannel) throws Exception {

    }

    @Override
    public void assess(Selector selector, SocketChannel socketChannel) throws Exception {

    }
}
