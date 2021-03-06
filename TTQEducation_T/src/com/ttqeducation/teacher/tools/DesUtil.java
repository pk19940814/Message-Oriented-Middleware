package com.ttqeducation.teacher.tools;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import com.ttqeducation.teacher.beans.ContextApplication;
import com.ttqeducation.teacher.beans.DataTable;
import com.ttqeducation.teacher.beans.TeacherInfo;
import com.ttqeducation.teacher.network.GetDataByWS;

import android.app.Activity;
import android.content.SharedPreferences;
import android.text.StaticLayout;
import android.util.Base64;
import android.util.Log;


public class DesUtil {
	
	/**
	 * 保存前一次向数据库添加的tokenID明文,用来具体的方法调用；
	 */
	public static String tokenID = "";
		
    // 解密
    public static String DecryptDoNet(String message, String key)
            throws Exception {
        byte[] bytesrc = Base64.decode(message.getBytes(), Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        byte[] retByte = cipher.doFinal(bytesrc);
        return new String(retByte);
    }
    
    /**
     * 加密函数；
     * @param message
     * @param key
     * @return
     * @throws Exception
     */
    public static String EncryptAsDoNet(String message, String key)
            throws Exception {
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        byte[] encryptbyte = cipher.doFinal(message.getBytes());
        return new String(Base64.encode(encryptbyte, Base64.DEFAULT));
    }
    
 
    /**
     * 返回tokenID的密文；
     * @param userID   只需要传递用户编号；
     * @param key		加密方式
     * @param type		1:表示调用公司端的方法需要返回的tokenID, 2:表示调用的是学校端的方法返回的tokenID;
     * @return
     */
    public static String getDesTokenID(String userID, String key, int type){
    	String tokenIDStr = userID;
    	tokenIDStr = tokenIDStr+DateUtil.convertDateToString("yyyyMMddHHmmss", new Date());
    	Random random = new Random();
    	int k = random.nextInt(8000)+1000;
    	tokenIDStr = tokenIDStr+""+k;
    	if(type == 1){		// 表示调用公司端的方法
    		tokenIDStr = tokenIDStr+"|";
    	}
    	try {
    		DesUtil.tokenID = tokenIDStr;
			tokenIDStr = EncryptAsDoNet(tokenIDStr, key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.i("error", "DesUtil-->tokenID加密错误...");
			tokenIDStr = "";
			e.printStackTrace();
		}
    	return tokenIDStr;
    }
 
    /**
     * 获取userID的密文；
     * @return
     */ 
    public static String getDesUserID(){
    	String userID = TeacherInfo.getInstance().execTeacherID+"|"+TeacherInfo.getInstance().execTeacherPwd;
    	try {
			userID = EncryptAsDoNet(userID, "Admin310");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.i("error", "DesUtil-->getDesUserID()-->userID加密错误...");
			userID = "";
			e.printStackTrace();
		}
    	return userID;
    }
    
    /**
     * 学校端调用，向数据库中添加tokenID;
     */
    public static void addTokenIDToSchoolWS(){
    	
    	String dt_token = null;	
		// 方法名
		String methodName = "pub_TokenIDManage_BSTeacher_Add ";
 
		// 获取数据
		GetDataByWS getDataTool = GetDataByWS.getInstance();
		// 存放参数的map
		Map<String, String> paramsMap = new HashMap<String, String>();
		String userID = getDesUserID();
		String tokenID = getDesTokenID(TeacherInfo.getInstance().execTeacherID, "Admin407", 2);
		paramsMap.put("userID", getDesUserID());
		paramsMap.put("tokenID", tokenID);
		SharedPreferences pre = ContextApplication.getAppContext().getSharedPreferences("TTQAndroid", Activity.MODE_PRIVATE);
		String schoolWebServiceUrl = pre.getString("school_service_url", "");
		getDataTool.setURL(schoolWebServiceUrl);
		try {
			dt_token = getDataTool.getDataAsString(methodName, paramsMap);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.i("error", "schoolWebServiceUrl="+schoolWebServiceUrl);
			Log.i("error", "密文：  userID="+userID+"   tokenID="+tokenID);
			Log.i("error", "DesUtil-->addTokenIDToWS(String userID, String tokenID)...出错了。。。");
			e.printStackTrace();
		}
    }
}
