package top.yqingyu.thread;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.utils.ThreadUtil;
import top.yqingyu.component.RegistryCenter;
import top.yqingyu.main.MainConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

import static top.yqingyu.main.MainConfig.*;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.thread.ClientSocketCommunicationThread
 * @Description TODO
 * @createTime 2022年06月09日 23:09:00
 */

@Slf4j
public record ClientTransThread(Socket socket) implements Runnable, Callable<Boolean> {

    public static final ThreadPoolExecutor POOL = ThreadUtil.createQyFixedThreadPool(MAX_REGISTRY_NUM * 2, "Csc", null);
    public static final ConcurrentHashMap<String, Socket> CLIENT_TRANS_POOL = new ConcurrentHashMap<>();


    public static void init() throws IOException {

        POOL.execute(() -> {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(PORT_COMM);
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
        Boolean connected = futureTask.get(CLIENT_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);

        if (connected) {
            QyMsg QyMsg = MsgTransfer.readMsg(this.socket, MainConfig.MSG_TIMEOUT);;
            log.info(QyMsg.toString());

            QyMsg header = NORM_MSG.clone();
            header.putMsg("ok");

            MsgTransfer.writeMessage(socket,header);
            CLIENT_TRANS_POOL.put(QyMsg.getFrom(), socket);
        } else {
            if (socket != null)
                socket.close();
        }

    }

    @Override
    public Boolean call() throws Exception {


        QyMsg QyMsg = null;

        try {
            QyMsg = MsgTransfer.readMsg(this.socket, MainConfig.MSG_TIMEOUT);;
        } catch (TimeoutException e) {
            log.error("传输线程异常", e);
            RecordIpThread.execute(this.socket.getInetAddress().getHostAddress());
        } catch (ExecutionException e) {
            log.error("传输线程异常", e);
            if (e.getMessage().contains("NumberFormatException"))
                RecordIpThread.execute(this.socket.getInetAddress().getHostAddress());
        } catch (Exception e) {
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

