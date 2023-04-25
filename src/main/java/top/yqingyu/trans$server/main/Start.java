package top.yqingyu.trans$server.main;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.qymsg.netty.QyMsgServerInitializer;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.utils.InitUtil;
import top.yqingyu.common.utils.ThreadUtil;
import top.yqingyu.trans$server.event$handler.MainHandler;
import top.yqingyu.trans$server.event$handler.S2CtHandler;
import top.yqingyu.trans$server.exception.ExceptionHandle;
import top.yqingyu.trans$server.thread.ClientMonitorThread;
import top.yqingyu.trans$server.trans.ClientTransThread;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.main.Start
 * @description
 * @createTime 2022年09月07日 15:40:00
 */
@Slf4j
public class Start {
    public static void main(String[] args) throws Exception {

        MainConfig.banner();
        log.info("loading main config");
        MainConfig.load();

        log.info("loading s$ct config");
        S$CtConfig.load();

        log.info("init Transfer");
        MsgTransfer.init(
                32,
                MainConfig.MSG_BODY_LENGTH_MAX
        );
        log.info("starting main server");
        InitUtil.init("top.yqingyu.trans$server");

        log.info("starting main server");
        NioEventLoopGroup serverGroup = new NioEventLoopGroup(2, ThreadUtil.createThFactoryC("BOSS", "Th"));
        NioEventLoopGroup mainClientGroup = new NioEventLoopGroup(ThreadUtil.createThFactoryC("Main", "handler"));
        NioEventLoopGroup s2ctClientGroup = new NioEventLoopGroup(8, ThreadUtil.createThFactoryC("S2Ct", "handler"));
        try {
            ServerBootstrap mainServerBootstrap = new ServerBootstrap();
            mainServerBootstrap.group(serverGroup, mainClientGroup);
            mainServerBootstrap.channel(NioServerSocketChannel.class);
            QyMsgServerInitializer mainInitializer = new QyMsgServerInitializer(new MainHandler());
            mainInitializer.setQyMsgExceptionHandler(new ExceptionHandle());
            mainServerBootstrap.childHandler(mainInitializer);
            ChannelFuture mainChannelFuture = mainServerBootstrap.bind(MainConfig.PORT_MAIN).sync();

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(serverGroup, s2ctClientGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            QyMsgServerInitializer initializer = new QyMsgServerInitializer(new S2CtHandler());
            initializer.setQyMsgExceptionHandler(new ExceptionHandle());
            serverBootstrap.childHandler(initializer);
            ChannelFuture channelFuture = serverBootstrap.bind(MainConfig.PORT_INTER).sync();


            ClientTransThread.init();
            ClientMonitorThread.init();

            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            long startTime = runtime.getStartTime();
            log.info("START SUCCESS PID:{} in {} mills ", runtime.getPid(), System.currentTimeMillis() - startTime);

            mainChannelFuture.channel().closeFuture().sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            serverGroup.shutdownGracefully();
            mainClientGroup.shutdownGracefully();
            s2ctClientGroup.shutdownGracefully();
        }


    }
}
