package top.yqingyu.trans$server.command.impl;

import top.yqingyu.common.qymsg.DataType;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.qymsg.extra.bean.FileObj;
import top.yqingyu.common.utils.LocalDateTimeUtil;
import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.trans$server.bean.ClientInfo;
import top.yqingyu.trans$server.command.ParentCommand;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.MainConfig;

import java.io.File;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.trans$server.command.impl.FileSystem
 * @description
 * @createTime 2023年02月18日 19:08:00
 */
public class FileSystem extends ParentCommand {
    private final String separator;

    public FileSystem() {
        super("([Ff]ilesystem)( ){1,4}(ls|cd|pwd|help)(( ){1,4}.*)?");
        separator = System.getProperty("file.separator");
    }

    @Override
    protected void deal(SocketChannel socketChannel, Selector selector, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        String id = msg.getFrom();
        ClientInfo clientInfo = RegistryCenter.getClientInfo(id);
        String msgStr = MsgHelper.gainMsg(msg).trim();
        String filesystem = "(([Ff])ilesystem)";
        String spase = "( ){1,4}";
        QyMsg clone = MainConfig.NORM_MSG.clone();
        clone.putMsg("filesystem");
        rtnMsg.add(clone);
        StringBuilder sb = new StringBuilder("\n");
        boolean isApi = msgStr.matches("Filesystem");

        String ls = filesystem + spase + "ls.*";
        String cd = filesystem + spase + "cd" + spase + ".*";
        String pwd = filesystem + spase + "pwd.*";
        String help = filesystem + spase + "help.*";

        String currentPath = clientInfo.getCurrentPath();
        File file = new File(currentPath);

        if (msgStr.matches(ls)) {
            FileObj fileObj = new FileObj(file);
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    fileObj.getChild().add(new FileObj(f));
                    if (!isApi) sb.append(f.getName()).append("\t\t")
                            .append(f.length()).append("\t\t")
                            .append(LocalDateTimeUtil.format(LocalDateTimeUtil.FULL, LocalDateTimeUtil.of(new Date(f.lastModified())))).append("\n");
                }
            }
            if (isApi) {
                clone.setDataType(DataType.OBJECT);
                clone.putMsgData("filesystem", fileObj);
            } else {
                sb.append("total: ").append(files == null ? 0 : files.length).append("\n");
                sb.append("$>");
                clone.setDataType(DataType.JSON);
                clone.putMsg(sb.toString());
            }
        } else if (msgStr.matches(cd)) {
            String[] split = msgStr.split(spase);
            String resultPath = "";
            if (split[2].indexOf("/") == 0) {
                resultPath = split[2];
                if (StringUtil.lastIndexOf(resultPath, separator) != resultPath.length() - 1) {
                    resultPath += separator;
                }
            } else {
                resultPath = resultPath + split[2];
            }
            File result = new File(resultPath);
            if (result.exists()) {
                clientInfo.setCurrentPath(resultPath);
                clone.putMsg("ok\n$>");
            } else {
                clone.putMsg("not exists!\n$>");
            }
        } else if (msgStr.matches(pwd)) {
            clone.putMsg(currentPath);
        } else if (msgStr.matches(help)) {
            sb.append("ls").append("--------").append("显示当前路径下所有文件").append("\n");
            sb.append("cd").append("--------").append("进入目录").append("\n");
            sb.append("pwd").append("--------").append("当前路径").append("\n");
            sb.append("\n$>");
            clone.putMsg(sb.toString());
        }

    }
}
