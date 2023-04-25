package top.yqingyu.trans$server.trans;

import top.yqingyu.common.qydata.ConcurrentDataMap;
import top.yqingyu.common.qymsg.extra.bean.TransObj;

import java.net.Socket;
import java.util.List;

public record DownloadThread(String clientId,Socket socket) implements Runnable {
    public static final ConcurrentDataMap<String, List<TransObj>> DOWNLOAD_READY_CONTAINER = new ConcurrentDataMap<>();

    @Override
    public void run() {
    }

    public static void addTask(String id, List<TransObj> list) {
        if (DOWNLOAD_READY_CONTAINER.containsKey(id)) {
            List<TransObj> transObjs = DOWNLOAD_READY_CONTAINER.get(id);
            transObjs.addAll(list);
        } else {
            DOWNLOAD_READY_CONTAINER.put(id, list);
        }
    }


}
