package top.yqingyu.trans$server.command.impl;

import com.alibaba.fastjson2.JSONObject;
import top.yqingyu.common.qymsg.extra.bean.KeyValue;
import top.yqingyu.common.qymsg.extra.bean.StringKey;
import top.yqingyu.trans$server.command.Command;
import top.yqingyu.common.qydata.ChoiceHashMap;
import top.yqingyu.common.qydata.ConcurrentQyMap;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.trans$server.main.MainConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.command.impl.KeyCommand
 * @description
 * @createTime 2023年01月09日 23:00:00
 */
public class KeyCommand extends Command {

    public KeyCommand() {
        super("key");
    }

    private static final ChoiceHashMap<KeyValue.DataType, Method> DEAL_METHODS = new ChoiceHashMap<>();
    private static final ConcurrentQyMap<KeyValue.DataType, ConcurrentQyMap<String, Object>> ROOT_CONTAINER = new ConcurrentQyMap<>();

    static {
        Field[] dataType = KeyValue.DataType.class.getFields();
        Method[] methods = KeyCommand.class.getDeclaredMethods();

        for (Field field : dataType) {
            KeyValue.DataType dtp;
            try {
                dtp = (KeyValue.DataType) field.get(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            String typeName = dtp.getName();
            for (Method method : methods) {
                if (method.getName().matches("deal" + typeName + "Key")) {
                    method.setAccessible(true);
                    DEAL_METHODS.put(dtp, method);
                    break;
                }
                if ("emptyDeal".equals(method.getName())) {
                    method.setAccessible(true);
                    DEAL_METHODS.putDft(KeyValue.DataType.OTHER, method);
                    break;
                }
            }
        }
        ROOT_CONTAINER.put(KeyValue.DataType.STRING, new ConcurrentQyMap<>());

    }

    @Override
    protected void deal(SocketChannel socketChannel, Selector selector, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        DataMap map = msg.getDataMap();
        KeyValue key = map.getObject("key", KeyValue.class);
        Method method = DEAL_METHODS.get(key.getDataType());
        QyMsg invoke = (QyMsg) method.invoke(this, msg);
        String s = MsgHelper.gainMsg(invoke);
        s += "\n$>";
        invoke.putMsg(s);
        rtnMsg.add(invoke);
    }

    private QyMsg dealStringKey(QyMsg msg) throws CloneNotSupportedException {
        ConcurrentQyMap<String, Object> STRING_CONTAINER = ROOT_CONTAINER.get(KeyValue.DataType.STRING);
        DataMap map = msg.getDataMap();
        StringKey KeyVal = map.getObject("key", StringKey.class);
        KeyValue.OperatingState operatingState = KeyVal.getOperatingState();
        String key = KeyVal.getKey();
        String val = KeyVal.getVal();
        QyMsg clone = MainConfig.NORM_MSG.clone();
        DataMap dataMap = clone.getDataMap();
        dataMap.put("key", KeyVal);
        switch (operatingState) {
            case ADD -> {
                STRING_CONTAINER.put(key, val);
                clone.putMsg("success");
                dataMap.put("code", "0000");
            }
            case GET -> {
                String o = (String) STRING_CONTAINER.get(key);
                KeyVal.setVal(o);
                clone.putMsg("success: val: " + o);
                dataMap.put("code", "0000");
                if (o == null) {
                    clone.putMsg("key is expired or not exists");
                    dataMap.put("code", "0000");
                }
            }
            case REMOVE -> {
                String o = (String) STRING_CONTAINER.remove(key);
                clone.putMsg("success");
                dataMap.put("code", "0000");
            }
            default -> {
                clone.putMsg("fail");
                dataMap.put("code", "-1000");
            }
        }
        return clone;
    }


    private QyMsg emptyDeal(QyMsg msg) throws CloneNotSupportedException {
        QyMsg clone = MainConfig.NORM_MSG.clone();
        clone.putMsg("尚未支持的数据类型");
        DataMap dataMap = clone.getDataMap();
        dataMap.put("code", "-1000");
        JSONObject object = new JSONObject();
        dataMap.put("Key", object);
        return clone;

    }
}
