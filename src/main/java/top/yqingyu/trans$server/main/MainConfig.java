package top.yqingyu.trans$server.main;

import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.qymsg.DataType;
import top.yqingyu.common.qymsg.MsgType;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.utils.ThreadUtil;
import top.yqingyu.common.utils.YamlUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author YYJ
 * @version 1.0.0
 * @date 2022/4/20 17:12
 * @description
 * @modified by
 */
@Slf4j
public class MainConfig {


    public final static DataMap SERVER_CONF = YamlUtil.loadYaml("server", YamlUtil.LoadType.OUTER).getDataMap("trans_server.yml").getData("server");
    public final static String AC_STR = SERVER_CONF.getString("ac_str");
    public static String HEART_BEAT = SERVER_CONF.getString("heart_beat");
    public static double HEART_BEAT_percent = SERVER_CONF.getDoubleValue("heart-beat-percent");
    public final static int PORT_MAIN = SERVER_CONF.getIntValue("main_port");
    public final static int PORT_INTER = SERVER_CONF.getIntValue("inner_port");
    public final static int PORT_COMM = SERVER_CONF.getIntValue("comm_port");
    public final static int MSG_TIMEOUT = SERVER_CONF.getIntValue("msg_timeout");
    public static final int MAX_REGISTRY_NUM = SERVER_CONF.getIntValue("max_registry_num"); //客户端最大数量
    public static final int CLIENT_RESPONSE_TIMEOUT = SERVER_CONF.getIntValue("client_response_timeout"); //客户端响应最大时间
    public static final long CMT_POLLING_INTERVAL  = SERVER_CONF.getData("CMT").getLongValue("period"); //客户端监控线程扫描间隔
    public static final long CMT_INIT_TIME  = SERVER_CONF.getData("CMT").getLongValue("init"); //客户端监控线程初始时间
    public static final TimeUnit CMT_TIME_UNIT  = SERVER_CONF.getData("CMT").getTimeUnit("unit"); //客户端监控线程时间配置时间单位
    public static final long CLIENT_ALIVE_SCAN_TIME = SERVER_CONF.getData("CMT").getLongValue("timeout"); //客户端掉线时间。
    public static final int MSG_BODY_LENGTH_MAX = SERVER_CONF.getData("msg").getIntValue("body-length-max"); //客户端每条消息最大值 超过将会拆分。


    public static final ExecutorService MSG_POOL = ThreadUtil.createQyFixedThreadPool(MAX_REGISTRY_NUM * 2, "Msg", null);

    public static final QyMsg HEART_BEAT_MSG = new QyMsg(MsgType.HEART_BEAT, DataType.STRING);
    public static final QyMsg NORM_MSG = new QyMsg(MsgType.NORM_MSG, DataType.OBJECT);
    public static final QyMsg AC_MSG = new QyMsg(MsgType.AC, DataType.JSON);
    public static final QyMsg ERR_MSG = new QyMsg(MsgType.ERR_MSG, DataType.JSON);

    public static final LinkedBlockingQueue<QyMsg> Main_PartitionMsgQueue = new LinkedBlockingQueue<>();
    public static final LinkedBlockingQueue<QyMsg> CI_PartitionMsgQueue = new LinkedBlockingQueue<>();

    public static void load() throws Exception {
        HEART_BEAT_MSG.setFrom("TRANS_SERVER");
        NORM_MSG.setFrom("TRANS_SERVER");
        AC_MSG.setFrom("TRANS_SERVER");
        ERR_MSG.setFrom("TRANS_SERVER");
    }


    static void banner() {
        System.out.println("""

                Trans Server Starting...
                             *                 *                  *              *        \s
                                                                          *             * \s
                                            *            *                             ___\s
                      *               *                                          |     | |\s
                            *              _________##                 *        / \\    | |\s
                                          @\\\\\\\\\\\\\\\\\\##    *     |              |--o|===|-|\s
                      *                  @@@\\\\\\\\\\\\\\\\##\\       \\|/|/            |---|   |C|\s
                                        @@ @@\\\\\\\\\\\\\\\\\\\\\\    \\|\\\\|//|/     *   /     \\  |N|\s
                                 *     @@@@@@@\\\\\\\\\\\\\\\\\\\\\\    \\|\\|/|/         |  C    | |S|\s
                                      @@@@@@@@@----------|    \\\\|//          |  H    |=|A|\s
                           __         @@ @@@ @@__________|     \\|/           |  N    | | |\s
                      ____|_@|_       @@@@@@@@@__________|     \\|/           |_______| |_|\s
                    =|__ _____ |=     @@@@ .@@@__________|      |             |@| |@|  | |\s
                    ____0_____0__\\|/__@@@@__@@@__________|_\\|/__|___\\|/__\\|/___________|_|_
                                                                                              \s
                                                                                 -- by Qy Severus\s
                """);
    }


}
