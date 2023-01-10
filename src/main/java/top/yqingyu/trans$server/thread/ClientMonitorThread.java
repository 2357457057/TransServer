package top.yqingyu.trans$server.thread;

import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.ThreadUtil;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.MainConfig;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


/**
 * @author YYJ
 * @version 1.0.0
 * @date 2022/4/24 01:24
 * @description 监控客户端心跳包
 * @modified by
 */
@Slf4j
public class ClientMonitorThread implements Runnable {


    private static final Logger logger = LoggerFactory.getLogger(ClientMonitorThread.class);

    public static void init() {

        DataMap cfg = MainConfig.SERVER_CONF.getData("CMT");

        ScheduledExecutorService service = Executors
                .newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new ClientMonitorThread(),cfg.getIntValue("init") , cfg.getIntValue("period"), cfg.getTimeUnit("unit"));
        log.info("heart beat timeout thread ok");
    }

    @Override
    public void run() {

        ThreadUtil.setThisThreadName("QY-CMThread");
        LocalDateTime now = LocalDateTime.now();

        RegistryCenter.REGISTRY_CENTER.forEach((clientId, clientInfo) -> {

            LocalDateTime localDateTime = clientInfo.getLocalDateTime();

            long subTime = LocalDateTimeUtil.between(localDateTime, now, ChronoUnit.MILLIS);

            if (subTime > MainConfig.SERVER_CONF.getData("CMT").getIntValue("timeout")) {
                RegistryCenter.REGISTRY_CENTER.remove(clientId);
                logger.info("检测到客户端【{}】心跳检测超时，超时时间{},WAN：{},LAN：{}", clientId, subTime, clientInfo.getWAN_Address(), clientInfo.getLAN_Address());
            }


        });
    }
}
