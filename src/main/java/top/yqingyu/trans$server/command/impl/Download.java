package top.yqingyu.trans$server.command.impl;

import io.netty.channel.ChannelHandlerContext;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.qymsg.extra.bean.TransObj;
import top.yqingyu.common.utils.ObjectUtil;
import top.yqingyu.common.utils.UUIDUtil;
import top.yqingyu.trans$server.annotation.Command;
import top.yqingyu.trans$server.bean.ClientInfo;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.MainConfig;
import top.yqingyu.trans$server.trans.DownloadThread;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Command
public class Download  {
    @Command("download")
    public void deal(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        DataMap dataMap = msg.getDataMap();
        String from = msg.getFrom();
        List<TransObj> list = new ArrayList<>();
        list.add(dataMap.getObject("download", TransObj.class));
        ClientInfo clientInfo = RegistryCenter.getClientInfo(from);
        String currentPath = clientInfo.getCurrentPath();
        ConcurrentHashMap<String,TransObj> existsMap = new ConcurrentHashMap<>();
        for (TransObj transObj : list) {
            fileDeal(transObj, currentPath, existsMap);
        }
        DownloadThread.addTask(from, existsMap);
        QyMsg clone = MainConfig.NORM_MSG.clone();
        clone.putMsg("ok");
        rtnMsg.add(clone);
    }

    void fileDeal(TransObj transObj, String curPath, ConcurrentHashMap<String,TransObj> existsMap) throws IOException, ClassNotFoundException {
        String fileName = transObj.getFileName();
        LinkedList<File> queue = new LinkedList<>();
        File file = new File(fileName);
        if (!file.exists())
            file = new File(curPath + fileName);

        queue.add(file);

        while (!queue.isEmpty()) {
            File poll = queue.poll();
            if (poll != null && poll.exists()) {
                if (poll.isFile()) {
                    TransObj clone = ObjectUtil.cloneObjSerial(transObj);
                    clone.setSize(poll.length());
                    clone.setFileName(poll.getName());
                    clone.setSavePath(poll.getAbsolutePath());
                    clone.setFileId(UUIDUtil.randomUUID().toString2());
                    existsMap.put(clone.getFileId(), clone);
                } else if (poll.isDirectory()) {
                    queue.addAll(Arrays.asList(Objects.requireNonNull(poll.listFiles())));
                }
            } else {
                break;
            }
        }

    }

}

