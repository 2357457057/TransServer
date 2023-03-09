package top.yqingyu.trans$server.thread;

import com.alibaba.fastjson2.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.HttpUtil;
import top.yqingyu.common.utils.LocalDateTimeUtil;
import top.yqingyu.common.utils.ThreadUtil;
import top.yqingyu.trans$server.main.MainConfig;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author YYJ
 * @version 1.0.0
 * @date 2022/5/16 3:49
 * @description 记录接入IP
 * @modified by
 */
@Slf4j
public class RecordIpThread implements Runnable {


    private static final ExecutorService IP_RECORD_THREAD_POOL = ThreadUtil.createQyFixedThreadPool(MainConfig.SERVER_CONF.getData("IP_REC").getIntValue("pool_size"), "IpRec", null);
    private String ip;
    private static volatile String token = "";
    private static volatile LocalDateTime lastLoginTime;
    private static final ReentrantLock lock = new ReentrantLock();

    public RecordIpThread() {
    }

    public RecordIpThread(String ip) {
        this.ip = ip;
    }

    public static void execute(String ip) {
        if (StringUtils.isNotBlank(ip))
            IP_RECORD_THREAD_POOL.execute(new RecordIpThread(ip));
    }

    @SneakyThrows
    @Override
    public void run() {
        HashMap<String, String> header = new HashMap<>();
        header.put("token", token);
        header.put("content-type", "application/json");
        DataMap dataMap = new DataMap();
        dataMap.put("ip", ip);
        JSONObject post = HttpUtil.doPost(MainConfig.SERVER_CONF.getData("IP_REC").getString("host") + "/web/viewnum", header, null, dataMap);
        String code = post.getString("code");
        if ("401".equals(code)) {
            login();
            execute(ip);
        } else {
            log.info("非法消息入库成功{}", post.toJSONString());
        }
    }

    static void login() throws Exception {
        try {
            lock.lock();
            boolean loginFlag = false;
            LocalDateTime tmp = LocalDateTime.now();
            if (lastLoginTime == null) {
                loginFlag = true;
            } else {
                long between = LocalDateTimeUtil.between(lastLoginTime, tmp, ChronoUnit.SECONDS);
                if (between >= 1200) {
                    loginFlag = true;
                }
            }
            if (!loginFlag) {
                return;
            }
            HashMap<String, String> header = new HashMap<>();
            header.put("content-type", "application/json");
            DataMap loginData = new DataMap();
            loginData.put("account", MainConfig.SERVER_CONF.getData("IP_REC").getString("account"));
            loginData.put("pwd", MainConfig.SERVER_CONF.getData("IP_REC").getString("pwd"));
            JSONObject post = HttpUtil.doPost(MainConfig.SERVER_CONF.getData("IP_REC").getString("host") + "/user/login", header, null, loginData);
            token = post.getJSONObject("token").getString("token");
            System.out.println(post.toJSONString());
            lastLoginTime = tmp;
        } finally {
            lock.unlock();
        }
    }
}
