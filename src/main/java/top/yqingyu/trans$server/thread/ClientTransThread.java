package top.yqingyu.trans$server.thread;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.utils.ThreadUtil;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.MainConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.thread.ClientSocketCommunicationThread
 * @Description TODO
 * @createTime 2022年06月09日 23:09:00
 */

@Slf4j
public record ClientTransThread(Socket socket) implements Runnable, Callable<Boolean> {

    public static final ThreadPoolExecutor POOL = ThreadUtil.createQyFixedThreadPool(128, MainConfig.MAX_REGISTRY_NUM * 2, MainConfig.CLIENT_ALIVE_SCAN_TIME, "Csc", null);
    public static final ConcurrentHashMap<String, Socket> CLIENT_TRANS_POOL = new ConcurrentHashMap<>();


    public static void init() throws IOException {

        POOL.execute(() -> {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(MainConfig.PORT_COMM);
                while (true) {
                    Socket socket = serverSocket.accept();
                    POOL.execute(new ClientTransThread(socket));
                }
            } catch (IOException e) {
                log.error("奇奇怪怪异常", e);
            }
        });
        log.info("client stream deal thread ok");
    }


    @SneakyThrows
    @Override
    public void run() {
        FutureTask<Boolean> futureTask = new FutureTask<>(new ClientTransThread(this.socket));
        POOL.execute(futureTask);
        Boolean connected = futureTask.get(MainConfig.CLIENT_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);

        if (connected) {
            QyMsg msg = MsgTransfer.readQyMsg(this.socket, new LinkedBlockingQueue<>(), new AtomicBoolean(true));
            log.info(msg.toString());

            QyMsg header = MainConfig.NORM_MSG.clone();
            header.putMsg("ok");

            MsgTransfer.writeQyMsg(socket, header);
            CLIENT_TRANS_POOL.put(msg.getFrom(), socket);
            //TODO 此处会阻断其他链接。。 记得改下
            POOL.execute(new UploadThread(msg.getFrom(), socket));
        } else {
            if (socket != null)
                socket.close();
        }

    }

    @Override
    public Boolean call() throws Exception {


        QyMsg QyMsg = null;

        try {
            QyMsg = MsgTransfer.readQyMsg(this.socket, new LinkedBlockingQueue<>(), new AtomicBoolean(true));
        } catch (Exception e) {
            RecordIpThread.execute(this.socket.getInetAddress().getHostAddress());
            log.error("传输线程异常", e);
            return false;
        }

        if (RegistryCenter.isRegistered(QyMsg.getFrom())) {
            log.info("连接成功 {}", QyMsg);
            return true;
        } else {
            return false;
        }

    }

}

