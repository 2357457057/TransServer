package top.yqingyu.trans$server.event$handler;

import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.bean.NetChannel;
import top.yqingyu.common.server$nio.core.EventHandler;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.utils.ThreadUtil;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.thread.DealMsgThread;
import top.yqingyu.trans$server.thread.RecordIpThread;
import top.yqingyu.trans$server.main.MainConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.event.C$IEventHandler
 * @description
 * @createTime 2022年09月07日 21:06:00
 */
@Slf4j
public class S$CtEventHandler extends EventHandler {

    public S$CtEventHandler(Selector selector) throws IOException {
        super(selector);
    }

    @Override
    protected void loading() {

    }

    @Override
    public void read(Selector selector, NetChannel channel) throws IOException {

        AtomicReference<NetChannel> socketChannel = new AtomicReference<>();
        AtomicReference<String> ip = new AtomicReference<>();
        String name = Thread.currentThread().getName();

        try {
            FutureTask<String> futureTask = new FutureTask<>(() -> {
                ThreadUtil.setThisThreadName(name);

                socketChannel.set(channel);
                InetSocketAddress socketAddress = (InetSocketAddress) socketChannel.get().getRemoteAddress();
                ip.set(socketAddress.getHostString());

                QyMsg QyMsg = MsgTransfer.readQyMsg(socketChannel.get().getNChannel(), MainConfig.CI_PartitionMsgQueue, 0L);
                if (RegistryCenter.isRegistered(QyMsg.getFrom())) {
                    log.debug("{}", QyMsg);
                    new DealMsgThread(socketChannel.get().getNChannel(), selector).deal2(QyMsg);

                } else {
                    socketChannel.get().close();
                    RecordIpThread.execute(ip.get());
                }
                return "";
            });
            READ_POOL.execute(futureTask);
            futureTask.get(MainConfig.CLIENT_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);

        } catch (TimeoutException e) {
            log.error("", e);
            RecordIpThread.execute(ip.get());
            NetChannel a = socketChannel.get();
            if (a != null) {
                a.close();
            }
        } catch (ExecutionException e) {
            log.error("", e);
            if (e.getMessage().matches(".*(NumberFormatException|Cannot.*Boolean[.]booleanValue).*"))
                RecordIpThread.execute(ip.get());
            NetChannel a = socketChannel.get();
            if (a != null) {
                a.close();
            }
        } catch (Exception e) {
            NetChannel a = socketChannel.get();
            if (a != null) {
                a.close();
            }
            e.addSuppressed(new Exception("客户端响应超时"));
        }

    }

    @Override
    public void write(Selector selector, NetChannel selectionKey) throws IOException {
        System.out.println("write");
    }

    @Override
    public void assess(Selector selector, NetChannel socketChannel) throws Exception {

    }
}
