package com.atom.authservice.api.wechat.bean;

import lombok.Data;

/**
 * 事件消息请求
 *
 * @data: 2025/8/9
 * @author: yang lianhuan
 */
@Data
public class EventMsgReq {
    private String ToUserName;
    private String FromUserName;
    private Long CreateTime;
    private String MsgType;
    private String Event;
    private String EventKey;
    private String Ticket;
    private String Latitude;
    private String Longitude;
    private String Precision;
}
