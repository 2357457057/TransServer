package top.yqingyu.trans$server.event$handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.qymsg.netty.QyMsgServerHandler;
import top.yqingyu.common.utils.ThreadUtil;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.MainConfig;
import top.yqingyu.trans$server.trans.ClientTransThread;
import top.yqingyu.trans$server.thread.MsgAdapter;
import top.yqingyu.trans$server.thread.RecordIpThread;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@ChannelHandler.Sharable
public class S2CtHandler extends QyMsgServerHandler {
    private final MsgAdapter msgAdapter = new MsgAdapter();
    @Override
    protected QyMsg handle(ChannelHandlerContext ctx, QyMsg msg) throws ExecutionException, InterruptedException, TimeoutException {
        String name = Thread.currentThread().getName();
        FutureTask<QyMsg> futureTask = new FutureTask<>(() -> {
            ThreadUtil.setThisThreadName(name);
            if (RegistryCenter.isRegistered(msg.getFrom())) {
                log.debug("{}", msg);
                return msgAdapter.deal2(ctx, msg);
            } else {
                InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                RecordIpThread.execute(socketAddress.getHostString());
            }
            return null;
        });
        ClientTransThread.POOL.execute(futureTask);
        futureTask.get(MainConfig.CLIENT_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        return null;
    }
}
