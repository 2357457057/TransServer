package top.yqingyu.trans$server.trans;

import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.qydata.ConcurrentDataMap;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.extra.bean.TransObj;
import top.yqingyu.common.utils.IoUtil;
import top.yqingyu.common.utils.LocalDateTimeUtil;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

@Slf4j
public record DownloadThread(String clientId, Socket socket) implements Runnable {
    public static final ConcurrentDataMap<String, ConcurrentDataMap<String,TransObj>> DOWNLOAD_READY_CONTAINER = new ConcurrentDataMap<>();

    @Override
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        ConcurrentDataMap<String,TransObj> transObjs = DOWNLOAD_READY_CONTAINER.remove(clientId);
        try {
            MsgTransfer.writeQyBytes(socket, IoUtil.objToSerializBytes((Serializable) transObjs));
        } catch (Exception ignored) {
            log.error("", ignored);
        }
        Iterator<String> iterator = transObjs.keySet().iterator();
        while (iterator.hasNext()) {
            TransObj next = transObjs.get(iterator.next());
            try {
                MsgTransfer.writeQyBytes(socket, next.getFileId().getBytes(StandardCharsets.UTF_8));
                File file = new File(next.getSavePath());
                log.info("开始传输 {}",file.getName());
                IoUtil.writeFile(file, socket);
            } catch (Exception e) {
                log.error("", e);
            }
            iterator.remove();
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        log.info("传输完成 cost {}", LocalDateTimeUtil.between(now, LocalDateTime.now()));
    }

    public static void addTask(String id, ConcurrentDataMap<String,TransObj> map) {
        if (DOWNLOAD_READY_CONTAINER.containsKey(id)) {
            ConcurrentDataMap<String,TransObj> transObjs = DOWNLOAD_READY_CONTAINER.get(id);
            transObjs.putAll(map);
        } else {
            DOWNLOAD_READY_CONTAINER.put(id, map);
        }
    }


}
