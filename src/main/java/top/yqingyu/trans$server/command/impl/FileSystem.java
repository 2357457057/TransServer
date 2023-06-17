package top.yqingyu.trans$server.command.impl;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.qymsg.extra.bean.FileObj;
import top.yqingyu.common.utils.LocalDateTimeUtil;
import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.common.utils.VirtualConsoleTable;
import top.yqingyu.trans$server.annotation.Command;
import top.yqingyu.trans$server.bean.ClientInfo;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.MainConfig;

import java.io.File;
import java.io.IOException;
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
public class FileSystem {
    private final String separator;
    String spase = "( ){1,4}";
    private static final Logger logger = LoggerFactory.getLogger(FileSystem.class);

    public FileSystem() {
        separator = System.getProperty("file.separator");
    }


    @Command("ls")
    public void ls(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws IOException {
        QyMsg clone = MainConfig.NORM_MSG.clone();
        rtnMsg.add(clone);
        String id = msg.getFrom();
        VirtualConsoleTable table = new VirtualConsoleTable();
        ClientInfo clientInfo = RegistryCenter.getClientInfo(id);
        String currentPath = clientInfo.getCurrentPath();
        File file = new File(currentPath);

        FileObj fileObj = new FileObj(file);
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                fileObj.getChild().add(new FileObj(f));
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
        table.append("total: ").append("" + (files == null ? 0 : files.length)).newLine();
        table.append("$>");
        clone.putMsg(table.toString());
    }

    @Command("cd( ){1,4}.*")
    public void cd(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws IOException {
        QyMsg clone = MainConfig.NORM_MSG.clone();
        rtnMsg.add(clone);
        String id = msg.getFrom();
        ClientInfo clientInfo = RegistryCenter.getClientInfo(id);
        String currentPath = clientInfo.getCurrentPath();
        String msgStr = MsgHelper.gainMsg(msg).trim();
        File result = compositingFile(currentPath, msgStr);

        if (result.exists()) {
            clientInfo.setCurrentPath(result.getAbsolutePath() + separator);
            clone.putMsg("ok\n$>");
        } else {
            clone.putMsg("not exists!\n$>");
        }
    }

    @Command("pwd")
    public void pwd(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws IOException {
        QyMsg clone = MainConfig.NORM_MSG.clone();
        rtnMsg.add(clone);
        String id = msg.getFrom();
        ClientInfo clientInfo = RegistryCenter.getClientInfo(id);
        String currentPath = clientInfo.getCurrentPath();
        clone.putMsg(currentPath);
    }

    @Command("mkdir( ){1,4}.*")
    public void mkdir(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws IOException {
        QyMsg clone = MainConfig.NORM_MSG.clone();
        rtnMsg.add(clone);
        String id = msg.getFrom();
        ClientInfo clientInfo = RegistryCenter.getClientInfo(id);
        String currentPath = clientInfo.getCurrentPath();
        String msgStr = MsgHelper.gainMsg(msg).trim();
        File result = compositingFile(currentPath, msgStr);

        if (result.exists()) {
            clone.putMsg("dir is exists!\n$>");
        } else {
            clone.putMsg(result.mkdirs() ? "ok! \n$>" : "fail! \n$>");
        }
    }

    @Command("rm( ){1,4}.*")
    public void rm(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws IOException {
        QyMsg clone = MainConfig.NORM_MSG.clone();
        rtnMsg.add(clone);
        String id = msg.getFrom();
        ClientInfo clientInfo = RegistryCenter.getClientInfo(id);
        String currentPath = clientInfo.getCurrentPath();
        String msgStr = MsgHelper.gainMsg(msg).trim();
        File result = compositingFile(currentPath, msgStr);
        if (result.exists()) {
            clone.putMsg(result.delete() ? "ok! \n$>" : "fail! \n$>");
        } else {
            clone.putMsg("not exists!\n$>");
        }
    }

    @Command("([F]|f)ilesystem")
    public void help(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) {
        QyMsg clone = MainConfig.NORM_MSG.clone();
        rtnMsg.add(clone);
        StringBuilder sb = new StringBuilder();
        sb.append("ls").append("--------").append("显示当前路径下所有文件").append("\n");
        sb.append("cd").append("--------").append("进入目录").append("\n");
        sb.append("pwd").append("--------").append("当前路径").append("\n");
        sb.append("\n$>");
        clone.putMsg(sb.toString());
    }

    private File compositingFile(String currentPath, String msg) {
        String[] split = msg.split(spase);
        String inputPath = split[1];
        String resultPath;
        if (inputPath.indexOf(separator) == 0) {
            resultPath = inputPath;
        } else if (inputPath.indexOf("..") == 0) {
            inputPath = inputPath.replaceFirst("[.]{2}(" + separator + ")?", "");
            String[] path = currentPath.split(separator);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < path.length - 1; i++) {
                sb.append(path[i]).append(separator);
            }
            resultPath = sb.append(inputPath).toString();
        } else {
            resultPath = currentPath + inputPath;
        }
        if (StringUtil.lastIndexOf(inputPath, separator) != resultPath.length() - 1) {
            resultPath += separator;
        }
        logger.info("resultPath {}", resultPath);
        return new File(resultPath);
    }
}
