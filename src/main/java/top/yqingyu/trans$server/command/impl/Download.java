package top.yqingyu.trans$server.command.impl;

import io.netty.channel.ChannelHandlerContext;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.qymsg.extra.bean.TransObj;
import top.yqingyu.common.utils.ObjectUtil;
import top.yqingyu.common.utils.ReflectionUtil;
import top.yqingyu.trans$server.annotation.Command;
import top.yqingyu.trans$server.bean.ClientInfo;
import top.yqingyu.trans$server.command.ParentCommand;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.MainConfig;
import top.yqingyu.trans$server.trans.DownloadThread;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Command
public class Download extends ParentCommand {
    public Download() {
        super("download");
    }

    @Override
    protected void deal(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        DataMap dataMap = msg.getDataMap();
        String from = msg.getFrom();
        List<TransObj> list = dataMap.getList("download", TransObj.class);
        ClientInfo clientInfo = RegistryCenter.getClientInfo(from);
        String currentPath = clientInfo.getCurrentPath();
        List<TransObj> existsList = new ArrayList<>();
        for (TransObj transObj : list) {
            if (!fileDeal(transObj, existsList)) {
                transObj.setFileName(currentPath + transObj.getFileName());
                fileDeal(transObj, list);
            }
        }
        DownloadThread.addTask(from, existsList);
        QyMsg clone = MainConfig.NORM_MSG.clone();
        clone.putMsg("ok");
        rtnMsg.add(clone);
    }

    boolean fileDeal(TransObj transObj, List<TransObj> list) throws IOException, ClassNotFoundException {
        String fileName = transObj.getFileName();
        LinkedList<File> queue = new LinkedList<>();
        File file = new File(fileName);
        queue.add(file);

        while (!queue.isEmpty()) {
            File poll = queue.poll();
            if (poll != null && poll.exists()) {
                if (poll.isFile()) {
                    TransObj clone = ObjectUtil.cloneObjSerial(transObj);
                    clone.setSize(poll.length());
                    clone.setFileName(poll.getName());
                    clone.setSavePath(poll.getAbsolutePath());
                    list.add(clone);
                } else if (poll.isDirectory()) {
                    queue.addAll(Arrays.asList(Objects.requireNonNull(poll.listFiles())));
                }
            } else {
                break;
            }
        }

        return true;
    }

}

