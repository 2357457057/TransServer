package top.yqingyu.trans$server.event$handler;


import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.qymsg.MsgType;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.qymsg.netty.QyMsgServerHandler;
import top.yqingyu.common.utils.LocalDateTimeUtil;
import top.yqingyu.common.utils.ThreadUtil;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.MainConfig;
import top.yqingyu.trans$server.thread.MsgAdapter;
import top.yqingyu.trans$server.thread.RecordIpThread;
import top.yqingyu.trans$server.trans.ClientTransThread;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ChannelHandler.Sharable
@Slf4j
public class MainHandler extends QyMsgServerHandler {
    private final MsgAdapter msgAdapter = new MsgAdapter();
    @Override
    protected QyMsg handle(ChannelHandlerContext ctx, QyMsg msg) throws ExecutionException, InterruptedException, TimeoutException {
        String name = Thread.currentThread().getName();
        LocalDateTime now1 = LocalDateTime.now();
        FutureTask<QyMsg> futureTask = new FutureTask<>(() -> {
            ThreadUtil.setThisThreadName(name);
            MsgType type = msg.getMsgType();
            QyMsg clone;

            switch (type) {
                case NORM_MSG -> {
                    if (RegistryCenter.isRegistered(msg.getFrom())) {
                        return msgAdapter.deal(ctx, msg);
                    } else {
                        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                        RecordIpThread.execute(remoteAddress.getHostString());
                        log.info("未注册的消息关闭连接{}:{} {} ", remoteAddress.getHostString(), remoteAddress.getPort(), msg);
                        ctx.close();
                    }
                }
                case HEART_BEAT -> {
                    if (RegistryCenter.isRegistered(msg.getFrom())) {
                        return null;
                    } else {
                        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                        RecordIpThread.execute(remoteAddress.getHostString());
                        log.info("未注册的消息关闭连接{}:{} {} ", remoteAddress.getHostString(), remoteAddress.getPort(), msg);
                        ctx.close();
                    }
                }
                case AC -> {
                    if (RegistryCenter.registrationClient(ctx, msg)) {
                        clone = MainConfig.AC_MSG.clone();
                        clone.putMsg(MainConfig.AC_STR);
                        log.info("{}: {}", MainConfig.AC_STR, msg);
                        return clone;
                    }
                }
                default -> {
                    return null;
                }
            }
            return null;
        });
        ClientTransThread.POOL.execute(futureTask);
        QyMsg rtnMsg = futureTask.get(MainConfig.CLIENT_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        long micros = LocalDateTimeUtil.between(now1, LocalDateTime.now(), ChronoUnit.MICROS);
        log.debug("Req {}", msg);
        log.debug("Resp {} micros {}", micros, rtnMsg);
        return rtnMsg;
    }
}
