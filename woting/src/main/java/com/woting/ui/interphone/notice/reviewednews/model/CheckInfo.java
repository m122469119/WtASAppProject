package com.woting.ui.interphone.notice.reviewednews.model;

import java.io.Serializable;

/**
 * 审核消息的对象
 */
public class CheckInfo implements Serializable {
	private String InviteTime;
	private String InviteCount;
	private String UserName;		//邀请对象名
	private String InvitedUserName;	//邀请人名
	private String BeInviteUserId;	//邀请对象UserId
	private String InviteUserId;	//邀请人ID
	private String Portrait;	//
	private int CheckType = 1;

	public String getPortrait() {
		return Portrait;
	}

	public void setPortrait(String portrait) {
		Portrait = portrait;
	}

	public String getInvitedUserName() {
		return InvitedUserName;
	}
	public void setInvitedUserName(String invitedUserName) {
		InvitedUserName = invitedUserName;
	}
	public String getInviteTime() {
		return InviteTime;
	}
	public void setInviteTime(String inviteTime) {
		InviteTime = inviteTime;
	}
	public String getInviteCount() {
		return InviteCount;
	}
	public void setInviteCount(String inviteCount) {
		InviteCount = inviteCount;
	}
	public String getUserName() {
		return UserName;
	}
	public void setUserName(String userName) {
		UserName = userName;
	}
	public String getBeInviteUserId() {
		return BeInviteUserId;
	}
	public void setBeInviteUserId(String beInviteUserId) {
		BeInviteUserId = beInviteUserId;
	}
	public String getInviteUserId() {
		return InviteUserId;
	}
	public void setInviteUserId(String inviteUserId) {
		InviteUserId = inviteUserId;
	}
	public int getCheckType() {
		return CheckType;
	}
	public void setCheckType(int checkType) {
		CheckType = checkType;
	}
}
