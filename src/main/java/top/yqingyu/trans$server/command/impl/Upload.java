package top.yqingyu.trans$server.command.impl;

import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.qymsg.extra.bean.TransObj;
import top.yqingyu.common.utils.FileUtil;
import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.common.utils.VirtualConsoleTable;
import top.yqingyu.trans$server.bean.ClientInfo;
import top.yqingyu.trans$server.command.ParentCommand;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.MainConfig;
import top.yqingyu.trans$server.thread.UploadThread;

import java.io.File;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class Upload extends ParentCommand {
    public Upload() {
        super("upload");
    }

    @Override
    protected void deal(SocketChannel socketChannel, Selector selector, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        DataMap dataMap = msg.getDataMap();
        String from = msg.getFrom();
        ClientInfo clientInfo = RegistryCenter.getClientInfo(from);
        String currentPath = clientInfo.getCurrentPath();
        List<TransObj> list = dataMap.getList("upload", TransObj.class);
        List<TransObj> isExist = new ArrayList<>();

        QyMsg qyMsg = MainConfig.NORM_MSG.clone();
        rtnMsg.add(qyMsg);

        try {
            for (TransObj obj : list) {
                String path = StringUtil.isEmpty(obj.getSavePath()) ? (currentPath + obj.getFileName()) : obj.getSavePath();
                File file = new File(path);
                if (file.exists()) {
                    if (obj.isOverwrite()) {
                        file.delete();
                        FileUtil.createSizeFile2(file, obj.getSize());
                    } else if (obj.isRename()) {
                        path = file.getParent() + obj.getNewName();
                        FileUtil.createSizeFile2(new File(path), obj.getSize());
                    } else {
                        isExist.add(obj);
                    }
                } else {
                    FileUtil.createSizeFile2(file, obj.getSize());
                }
                obj.setSavePath(path);
            }
        } catch (Exception e) {
            qyMsg.putMsgData("msg3", e.getMessage());
            qyMsg.putMsgData("code", "-9");
        }

        if (isExist.size() > 0) {
            VirtualConsoleTable table = new VirtualConsoleTable();
            table.append("Filename").append("SavePath").newLine();
            for (TransObj transObj : isExist) {
                table.append(transObj.getFileName()).append(transObj.getSavePath()).newLine();
            }
            qyMsg.putMsg(table.toString());
            qyMsg.putMsgData("msg2", "File already exists");
            qyMsg.putMsgData("code", "-9");
        }


        //全部存在。
        if (isExist.size() != 0 && isExist.size() == list.size())
            return;

        UploadThread.CONTAINER_READY.put(msg.getFrom(), list);
        qyMsg.putMsg("ok");
    }
}
