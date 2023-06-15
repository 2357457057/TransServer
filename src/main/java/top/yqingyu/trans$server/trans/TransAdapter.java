package top.yqingyu.trans$server.trans;

import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.QyMsg;

import java.net.Socket;

import static top.yqingyu.trans$server.trans.ClientTransThread.CLIENT_TRANS_POOL;
import static top.yqingyu.trans$server.trans.ClientTransThread.POOL;

public class TransAdapter {

    public static void adapter(QyMsg qyMsg) {
        if (qyMsg == null) return;

        String msg = MsgHelper.gainMsg(qyMsg);
        String from = qyMsg.getFrom();
        Socket socket = CLIENT_TRANS_POOL.remove(from);
        switch (msg) {
            case "upload" -> POOL.execute(new UploadThread(qyMsg.getFrom(), socket));
            case "download" -> POOL.execute(new DownloadThread(qyMsg.getFrom(), socket));
            default -> {
            }
        }


    }
}
