package top.yqingyu.trans$server.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.qydata.ConcurrentDataMap;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.extra.bean.TransObj;
import top.yqingyu.common.utils.LocalDateTimeUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;

public record UploadThread(String clientId, Socket socket) implements Runnable {
    public static final ConcurrentDataMap<String, List<TransObj>> CONTAINER_READY = new ConcurrentDataMap<>();
    private static final Logger logger = LoggerFactory.getLogger(UploadThread.class);


    @Override
    public void run() {
        if (!check()) return;
        List<TransObj> objList = CONTAINER_READY.remove(clientId);
        TransObj trans;
        int defaultBufSize = 1024 * 8;
        byte[] bytes = new byte[defaultBufSize];
        ByteBuffer buffer = ByteBuffer.allocate(defaultBufSize);
        do {
            trans = getFile(objList);
            if (trans == null) {
                break;
            }
            String savePath = trans.getSavePath();
            long size = trans.getSize();
            File file = new File(savePath);
            try {
                FileChannel writeChannel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE);
                long position = 0;
                LocalDateTime now = LocalDateTime.now();
                for (; position < size; ) {
                    if (size - position < defaultBufSize) {
                        bytes = new byte[(int) (size - position)];
                    }
                    InputStream stream = socket.getInputStream();
                    int read = stream.read(bytes);
                    buffer.clear();
                    buffer.put(bytes, 0, read);
                    buffer.flip();
                    int limit = buffer.limit();
                    do {
                        position += writeChannel.write(buffer, position);
                    } while (limit != buffer.position());
                }
                writeChannel.close();
                logger.info("文件上传成功 {} cost:{}ms", file.getAbsolutePath(), LocalDateTimeUtil.between(now, LocalDateTime.now()));
            } catch (IOException e) {
                logger.error("", e);
            }
            bytes = new byte[defaultBufSize];
        } while (trans.isHasNext());
    }

    private boolean check() {
        if (!CONTAINER_READY.containsKey(clientId)) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("", e);
            }
            return false;
        }
        return true;
    }

    private TransObj getFile(List<TransObj> list) {
        String s = null;
        try {
            s = MsgTransfer.readMessage(socket);
        } catch (IOException e) {
            logger.error("", e);
        }
        for (TransObj obj : list) {
            if (obj.getFileId().equals(s)) {
                return obj;
            }
        }
        return null;
    }
}
