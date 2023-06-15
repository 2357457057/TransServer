package top.yqingyu.trans$server.trans;

import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.qydata.ConcurrentDataMap;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.extra.bean.TransObj;
import top.yqingyu.common.utils.IoUtil;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;

@Slf4j
public record DownloadThread(String clientId, Socket socket) implements Runnable {
    public static final ConcurrentDataMap<String, List<TransObj>> DOWNLOAD_READY_CONTAINER = new ConcurrentDataMap<>();

    @Override
    public void run() {
        List<TransObj> transObjs = DOWNLOAD_READY_CONTAINER.remove(clientId);
        try {
            MsgTransfer.writeQyBytes(socket, IoUtil.objToSerializBytes((Serializable) transObjs));
        } catch (Exception ignored) {
            log.error("", ignored);
        }
        Iterator<TransObj> iterator = transObjs.iterator();
        while (iterator.hasNext()) {
            TransObj next = iterator.next();
            try {
                File file = new File(next.getSavePath());
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
