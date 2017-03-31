package com.push.server.cache;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class AndroidUserCache {
	
	//�洢�����û����û�ID����Ӧ��Channel
	private static Map<String, Channel> onlineUser = new HashMap<String, Channel>();
	private static Map<String, String> deviceNum = new HashMap<String, String>();
	
	/*
	 * ͬ����������û�
	 */

	public synchronized void put(String id,Channel sc){
		onlineUser.put(id, sc);
	}
	
	/*
	 * ͬ���Ƴ������û�
	 */
	public synchronized void remove(String id){
		onlineUser.remove(id);
		deviceNum.remove(id);
	}
	
	/*
	 * �����û���¼���豸
	 */
	public synchronized void put(String userID, String deviceID){
		deviceNum.put(userID, deviceID);
	}
	
	/*
	 * ��ȡ��¼���豸ID
	 */
	public String getDeviceID(String userID){
		return deviceNum.get(userID);
	}
	/*
	 * ��ȡ�����û�������
	 */
	public int size(){
		return onlineUser.size();
	}
	
	/*
	 * �жϸ��û��Ƿ�����
	 */
	public boolean isOnline(String id){
		return onlineUser.containsKey(id);
	}
	
	/*
	 * �õ����û���Channel
	 */
	public Channel getChannel(String id){
		return onlineUser.get(id);
	}
	
	/*
	 * ��ȡ���������û���Map
	 */
	public Map<String, Channel> getOnlineUser(){
		return onlineUser;
	}
	
}
