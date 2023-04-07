//package top.yqingyu.trans$server.event$handler;
//
//import lombok.extern.slf4j.Slf4j;
//import top.yqingyu.common.bean.NetChannel;
//import top.yqingyu.common.server$nio.core.ChannelStatus;
//import top.yqingyu.common.server$nio.core.EventHandler;
//import top.yqingyu.common.qymsg.MsgTransfer;
//import top.yqingyu.common.qymsg.QyMsg;
//import top.yqingyu.common.utils.Status;
//import top.yqingyu.common.utils.ThreadUtil;
//import top.yqingyu.trans$server.component.RegistryCenter;
//import top.yqingyu.trans$server.exception.ExceptionHandle;
//import top.yqingyu.trans$server.thread.DealMsg;
//import top.yqingyu.trans$server.thread.RecordIpThread;
//import top.yqingyu.trans$server.main.MainConfig;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.nio.channels.Selector;
//import java.util.concurrent.*;
//
///**
// * @author YYJ
// * @version 1.0.0
// * @ClassName top.yqingyu.event.C$IEventHandler
// * @description
// * @createTime 2022年09月07日 21:06:00
// */
//@Slf4j
//public class S$CtEventHandler extends EventHandler {
//
//    public S$CtEventHandler(Selector selector) throws IOException {
//        super(selector);
//    }
//
//    @Override
//    protected void loading() {
//
//    }
//
//    @Override
//    public void read(Selector selector, NetChannel netChannel) {
//        String name = Thread.currentThread().getName();
//        Thread thread = new Thread(() -> {
//            ThreadUtil.setThisThreadName(name);
//            try {
//
//            } catch (Exception e) {
//                ExceptionHandle.serverExecHandle(e, netChannel);
//            } finally {
//                Status.statusFalse(NET_CHANNELS.get(netChannel.hashCode()), ChannelStatus.READ);
//            }
//        });
//        thread.setDaemon(true);
//        READ_POOL.execute(thread);
//    }
//
//    @Override
//    public void write(Selector selector, NetChannel selectionKey) throws IOException {
//    }
//
//    @Override
//    public void assess(Selector selector, NetChannel socketChannel) throws Exception {
//
//    }
//}
