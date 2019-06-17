package com.rol.beans;

import java.util.Date;

/**
 * Created by ramdani on 9/18/16.
 */

public class MessageBean {
    public String sender;
    public String message;
    public Date date_time;
    public String sender_name;
    public String sender_profile;
    public String senderUserId;
    public String receiverUserId;
    public String reciver_name;
    public String reciver_profile;
    public long timestamp;

    public MessageBean() {

    }

    public MessageBean(MessageBean post) {
        this.sender = post.sender;
        this.message = post.message;
        this.date_time = post.date_time;
        this.sender_name = post.sender_name;
        this.sender_profile = post.sender_profile;
        this.senderUserId = post.senderUserId;
        this.receiverUserId = post.receiverUserId;
        this.reciver_name = post.reciver_name;
        this.reciver_profile = post.reciver_profile;
        this.timestamp = post.timestamp;
    }
}
