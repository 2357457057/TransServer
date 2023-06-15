//package top.yqingyu.trans$server.event$handler;
//
//import cn.hutool.core.date.LocalDateTimeUtil;
//import lombok.extern.slf4j.Slf4j;
//import top.yqingyu.common.bean.NetChannel;
//import top.yqingyu.common.server$nio.core.ChannelStatus;
//import top.yqingyu.common.server$nio.core.EventHandler;
//import top.yqingyu.common.qymsg.MsgHelper;
//import top.yqingyu.common.qymsg.MsgTransfer;
//import top.yqingyu.common.qymsg.MsgType;
//import top.yqingyu.common.qymsg.QyMsg;
//import top.yqingyu.common.utils.Status;
//import top.yqingyu.common.utils.ThreadUtil;
//import top.yqingyu.trans$server.component.RegistryCenter;
//import top.yqingyu.trans$server.exception.ExceptionHandle;
//import top.yqingyu.trans$server.main.MainConfig;
//import top.yqingyu.trans$server.thread.MsgAdapter;
//import top.yqingyu.trans$server.thread.RecordIpThread;
//import top.yqingyu.trans$server.thread.ClientTransThread;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicReference;
//
///**
// * @author YYJ
// * @version 1.0.0
// * @ClassName top.yqingyu.event.MainEventHandler
// * @description
// * @createTime 2022年09月07日 17:48:00
// */
//@Slf4j
//public class MainEventHandler extends EventHandler {
//
//    public MainEventHandler(Selector selector) throws IOException {
//        super(selector);
//    }
//
//    @Override
//    protected void loading() {
//
//    }
//
//    /**
//     * TODO 待优化！ 长消息需更多时间
//     *
//     * @param selector
//     * @param netChannel
//     * @throws IOException
//     */
//    @Override
//    public void read(Selector selector, NetChannel netChannel) throws IOException {
//        String name = Thread.currentThread().getName();
//        Thread thread = new Thread(() -> {
//            ThreadUtil.setThisThreadName(name);
//            try {
//                FutureTask<String> futureTask = new FutureTask<>(() -> {
//                    ThreadUtil.setThisThreadName(name);
//                    LocalDateTime now1 = LocalDateTime.now();
//                    QyMsg thisMsg = MsgTransfer.readQyMsg(netChannel.getNChannel(), MainConfig.Main_PartitionMsgQueue, 0L);
//                    MsgType type = thisMsg.getMsgType();
//                    QyMsg clone;
//
//                    if (RegistryCenter.isRegistered(thisMsg.getFrom())) {
//                        if (MsgType.HEART_BEAT == type) {
//                            log.debug("{}", thisMsg);
//                        } else {
//                            new MsgAdapter(netChannel.getNChannel(), selector).deal(thisMsg);
//                            LocalDateTime now2 = LocalDateTime.now();
//                            long nanos = LocalDateTimeUtil.between(now1, now2, ChronoUnit.MILLIS);
//                            log.info("命令执行完成：{}  | {}ms", MsgHelper.gainMsg(thisMsg), nanos);
//                        }
//                    } else {
//                        if (MsgType.AC == type && MainConfig.AC_STR.equals(MsgHelper.gainMsgValue(thisMsg, "AC_STR"))) {
//                            if (RegistryCenter.registrationClient(netChannel.getNChannel(), selector, thisMsg)) {
//                                netChannel.register(selector, SelectionKey.OP_WRITE);
//                                clone = MainConfig.AC_MSG.clone();
//                                clone.putMsg(MainConfig.AC_STR);
//                                MsgTransfer.writeQyMsg(netChannel.getNChannel(), clone);
//                                netChannel.register(selector, SelectionKey.OP_READ);
//                                log.info("{}: {}", MainConfig.AC_STR, thisMsg);
//                            }
//                        } else {
//                            InetSocketAddress remoteAddress = (InetSocketAddress) netChannel.getRemoteAddress();
//                            log.info("未注册的消息关闭连接{}:{} {} ", remoteAddress.getHostString(), remoteAddress.getPort(), thisMsg);
//                            clone = MainConfig.ERR_MSG.clone();
//                            clone.putMsg("注册中心无此id");
//                            MsgTransfer.writeQyMsg(netChannel.getNChannel(), clone);
//                            netChannel.close();
//                        }
//                    }
//                    return "ok";
//                });
//                ClientTransThread.POOL.execute(futureTask);
//               futureTask.get(MainConfig.CLIENT_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
//            } catch (Exception e) {
//                ExceptionHandle.serverExecHandle(e, netChannel);
//            } finally {
//                Status.statusFalse(NET_CHANNELS.get(netChannel.hashCode()), ChannelStatus.READ);
//            }
//        });
//        thread.setDaemon(true);
//        ClientTransThread.POOL.execute(thread);
//    }
//
//
//    @Override
//    public void write(Selector selector, NetChannel socketChannel) throws Exception {
//
//    }
//
//    @Override
//    public void assess(Selector selector, NetChannel socketChannel) throws Exception {
//
//    }
//}
