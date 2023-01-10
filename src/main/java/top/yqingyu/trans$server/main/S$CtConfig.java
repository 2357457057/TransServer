package top.yqingyu.trans$server.main;

import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.qymsg.DataType;
import top.yqingyu.common.qymsg.MsgType;
import top.yqingyu.common.qymsg.QyMsg;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.thread.S$CtConfig
 * @Description 服务器主动操作客户端进程
 * @createTime 2022年05月08日 11:52:00
 */
@Slf4j
public class S$CtConfig {

    public static final QyMsg NORM_MSG;
    public static final QyMsg HEART_BEAT_MSG;
    public static final QyMsg ERR_MSG_MSG;

    static {
        NORM_MSG = new QyMsg(MsgType.NORM_MSG, DataType.JSON);
        NORM_MSG.setFrom("TRANS_SERVER" + "CI");
        HEART_BEAT_MSG = new QyMsg(MsgType.HEART_BEAT, DataType.JSON);
        HEART_BEAT_MSG.setFrom("TRANS_SERVER" + "CI");
        ERR_MSG_MSG = new QyMsg(MsgType.ERR_MSG, DataType.JSON);
        ERR_MSG_MSG.setFrom("TRANS_SERVER" + "CI");
    }

    public static void load() {
    }
}
