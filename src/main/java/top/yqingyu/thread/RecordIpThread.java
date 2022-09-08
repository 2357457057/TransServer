package top.yqingyu.thread;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.HttpUtils;
import top.yqingyu.common.utils.ThreadUtil;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import static top.yqingyu.main.MainConfig.SERVER_CONF;

/**
 * @author YYJ
 * @version 1.0.0
 * @date 2022/5/16 3:49
 * @description 记录接入IP
 * @modified by
 */
@Slf4j
public class RecordIpThread implements Runnable {


    private static final ExecutorService IP_RECORD_THREAD_POOL = ThreadUtil.createQyFixedThreadPool(SERVER_CONF.getData("IP_REC").getIntValue("pool_size"),"IpRec",null);
    private String ip;

    public RecordIpThread() {
    }

    public RecordIpThread(String ip) {
        this.ip = ip;
    }

    public static void execute(String ip){
        if(StringUtils.isNotBlank(ip))
        IP_RECORD_THREAD_POOL.execute(new RecordIpThread(ip));
    }

    @SneakyThrows
    @Override
    public void run() {

        HashMap<String, String> header = new HashMap<>();

        header.put("token",SERVER_CONF.getData("IP_REC").getString("token"));
        header.put("content-type", "application/json");

        DataMap dataMap = new DataMap();
        dataMap.put("ip",ip);
        CloseableHttpResponse post = (CloseableHttpResponse)HttpUtils.doPost(SERVER_CONF.getData("IP_REC").getString("host"), "/web/viewnum", "POST", header, new HashMap<>(), dataMap.toString());

//        HttpResponse post = HttpUtils.doPost("http://localhost:4728", "/web/viewnum", "POST", header, new HashMap<>(), "{\"ip\" : \"" + ip + "\"}");
        HttpEntity entity = post.getEntity();


        log.info("非法消息入库成功{}", EntityUtils.toString(entity));


    }
}