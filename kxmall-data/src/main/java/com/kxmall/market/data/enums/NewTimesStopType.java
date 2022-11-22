package com.kxmall.market.data.enums;

/**
 * @@description:
 * @author: fy
 * @date: 2020/03/09 21:49
 **/
public enum NewTimesStopType {

    START_STATES(0, "显示"),
    STOP_STATES(1, "暂停");

    private int code;

    private String msg;

    NewTimesStopType(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }
}
