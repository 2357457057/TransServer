package top.yqingyu.trans$server.command.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.qymsg.DataType;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.qymsg.extra.bean.FileObj;
import top.yqingyu.common.utils.LocalDateTimeUtil;
import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.common.utils.VirtualConsoleTable;
import top.yqingyu.trans$server.annotation.Command;
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
@Command
public class FileSystem extends ParentCommand {
    private final String separator;
    private static final Logger logger = LoggerFactory.getLogger(FileSystem.class);
    String filesystem = "(([Ff])ilesystem)";
    String spase = "( ){1,4}";

    public FileSystem() {
        super("([Ff]ilesystem)( ){1,4}(ls|cd|pwd|mkdir|rm|help)(( ){1,4}.*)?");
        separator = System.getProperty("file.separator");
    }

    @Override
    protected void deal(SocketChannel socketChannel, Selector selector, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        String id = msg.getFrom();
        ClientInfo clientInfo = RegistryCenter.getClientInfo(id);
        String msgStr = MsgHelper.gainMsg(msg).trim();
        QyMsg clone = MainConfig.NORM_MSG.clone();
        clone.putMsg("filesystem");
        rtnMsg.add(clone);
        StringBuilder sb = new StringBuilder("\n");
        boolean isApi = msgStr.matches("Filesystem");

        String ls = filesystem + spase + "ls.*";
        String cd = filesystem + spase + "cd" + spase + ".*";
        String pwd = filesystem + spase + "pwd.*";
        String mkdir = filesystem + spase + "mkdir.*";
        String rm = filesystem + spase + "rm.*";
        String help = filesystem + spase + "help.*";

        String currentPath = clientInfo.getCurrentPath();
        File file = new File(currentPath);

        if (msgStr.matches(ls)) {
            VirtualConsoleTable table = new VirtualConsoleTable();
            FileObj fileObj = new FileObj(file);
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    fileObj.getChild().add(new FileObj(f));
                    if (!isApi) {
                        table
                                .append(f.getName())
                                .append(f.length() + "")
                                .append(LocalDateTimeUtil.format(LocalDateTimeUtil.FULL, LocalDateTimeUtil.of(new Date(f.lastModified()))));
                        if (f.isFile()) {
                            table.append("file");
                        } else {
                            table.append("dir");
                        }
                        table.newLine();
                    }
                }
            }
            if (isApi) {
                clone.setDataType(DataType.OBJECT);
                clone.putMsgData("filesystem", fileObj);
            } else {
                table.append("total: ").append("" + (files == null ? 0 : files.length)).newLine();
                table.append("$>");
                clone.setDataType(DataType.JSON);
                clone.putMsg(table.toString());
            }
        } else if (msgStr.matches(cd)) {
            File result = compositingFile(currentPath, msgStr);
            if (result.exists()) {
                clientInfo.setCurrentPath(result.getAbsolutePath());
                clone.putMsg("ok\n$>");
            } else {
                clone.putMsg("not exists!\n$>");
            }
        } else if (msgStr.matches(pwd)) {
            clone.putMsg(currentPath);
        } else if (msgStr.matches(mkdir)) {
            File result = compositingFile(currentPath, msgStr);
            if (result.exists()) {
                clone.putMsg("dir is exists!\n$>");
            } else {
                clone.putMsg(result.createNewFile() ? "ok! \n$>" : "fail! \n$>");
            }
        } else if (msgStr.matches(rm)) {
            File result = compositingFile(currentPath, msgStr);
            if (result.exists()) {
                clone.putMsg(result.delete() ? "ok! \n$>" : "fail! \n$>");
            } else {
                clone.putMsg("not exists!\n$>");
            }
        } else if (msgStr.matches(help)) {
            sb.append("ls").append("--------").append("显示当前路径下所有文件").append("\n");
            sb.append("cd").append("--------").append("进入目录").append("\n");
            sb.append("pwd").append("--------").append("当前路径").append("\n");
            sb.append("\n$>");
            clone.putMsg(sb.toString());
        } else {
            clone.putMsg("unknown filesystem command");
        }
    }

    private File compositingFile(String currentPath, String msg) {
        String[] split = msg.split(spase);
        String resultPath;
        if (split[2].indexOf("/") == 0) {
            resultPath = split[2];
        } else {
            resultPath = currentPath + split[2];
        }
        if (StringUtil.lastIndexOf(resultPath, separator) != resultPath.length() - 1) {
            resultPath += separator;
        }
        logger.info("resultPath {}", resultPath);
        return new File(resultPath);
    }
}
