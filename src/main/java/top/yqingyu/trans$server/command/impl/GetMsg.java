package top.yqingyu.trans$server.command.impl;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.qymsg.DataType;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.trans$server.annotation.Command;
import top.yqingyu.trans$server.main.MainConfig;

import java.util.ArrayList;


/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.command.impl.getMsg
 * @description
 * @createTime 2022年09月07日 10:36:00
 */
@Slf4j
@Command
public class GetMsg  {


    @Command("getMsg[ \\d]*")
    public void deal(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        String s = MsgHelper.gainMsg(msg);
        String replace = s.replaceAll("getMsg| ", "");
        int i = Integer.parseInt(replace);
        QyMsg clone = MainConfig.NORM_MSG.clone();
        clone.setDataType(DataType.OBJECT);
        clone.putMsg("1234567890".repeat(1024).repeat(i));
        rtnMsg.add(clone);
    }
}
