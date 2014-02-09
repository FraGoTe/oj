package com.power.oj.bean;

import com.jfinal.plugin.activerecord.Model;

public class User_ext extends Model<User_ext>{
	public static final User_ext dao = new User_ext();
	public static final String UID = "uid";
	public static final String TID = "tid";
	public static final String REALNAME = "realname";
	public static final String PHONE = "phone";
	public static final String QQ = "qq";
	public static final String BLOG = "blog";
	public static final String SHARE = "share";
	public static final String ONLINE = "online";
	public static final String LEVEL = "level";
	public static final String CREDIT = "credit";
	public static final String EXP = "exp";
	public static final String CHECKIN = "checkin";
	public static final String CHECKIN_TIMES = "checkin_times";
	public static final String LAST_SEND_DRIFT = "last_send_drift";
	public static final String SEND_DRIFT = "send_drift";
	public static final String LAST_GET_DRIFT = "last_get_drift";
	public static final String GET_DRIFT = "get_drift";
}
