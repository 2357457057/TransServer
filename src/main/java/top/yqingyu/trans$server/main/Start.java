package top.yqingyu.trans$server.main;

import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.server$nio.CreateServer;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.utils.InitUtil;
import top.yqingyu.common.utils.ThreadUtil;
import top.yqingyu.trans$server.event$handler.MainEventHandler;
import top.yqingyu.trans$server.event$handler.S$CtEventHandler;
import top.yqingyu.trans$server.thread.ClientMonitorThread;
import top.yqingyu.trans$server.thread.ClientTransThread;

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
        CreateServer
                .createDefault(MainConfig.PORT_MAIN, "Main")
                .implEvent(MainEventHandler.class)
                .defaultFixRouter(16)
                .listenPort()
                .start();

        log.info("starting s$ct server");
        CreateServer
                .createDefault("S$Ct")
                .implEvent(S$CtEventHandler.class)
                .defaultFixRouter(8, 4)
                .listenPort(MainConfig.PORT_INTER)
                .start();


        ClientTransThread.init();
        ClientMonitorThread.init();

        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        long startTime = runtime.getStartTime();
        log.info("START SUCCESS PID:{} in {} mills ", runtime.getPid(), System.currentTimeMillis() - startTime);
    }
}
