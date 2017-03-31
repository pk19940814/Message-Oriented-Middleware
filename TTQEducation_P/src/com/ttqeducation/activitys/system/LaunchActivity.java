package com.ttqeducation.activitys.system;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import com.ttqeducation.R;
import com.ttqeducation.activitys.study.StudyFragment;
import com.ttqeducation.beans.DataTable;
import com.ttqeducation.beans.UserCurrentState;
import com.ttqeducation.beans.UserInfo;
import com.ttqeducation.beans.dataTableWrongException;
import com.ttqeducation.network.GetDataByWS;
import com.ttqeducation.tools.DateUtil;
import com.ttqeducation.tools.DensityUtils;
import com.ttqeducation.tools.DesUtil;
import com.ttqeducation.tools.GeneralTools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

/**
 * 启动界面 需要做的事情： 1.显示滚动 2.判断：（1）如果是第一次使用——进入填写代码 （2）非第一次使用还有两种情况：密码已经过期——进入登录界面
 * 密码未过期——进入主界面
 * 
 * @author 王勤为
 */
public class LaunchActivity extends Activity {

	// 过期的天数
	private final int ExpirationDayNum = 30;
	
	private int[] moduleExpenseInfos = {2,2,2,2,2,2};		// 保存哪些模块需要收费及免费(一共为六个模块)；默认值无效；
	private int[] moduleUse = {1,1,1,1,1,1};				// 存放用户哪些模块可以使用；(默认都可以使用);
	
	private Dialog reminderUseDialog = null;	// 提醒用户缴费使用功能模块对话框；

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);
		
		try {

			this.check_FirstUse_Expiration(); //检查
		    	    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * 初始化方法
	 * 
	 * @throws ParseException
	 */
	public void initial() {
		// 设置背景图片
		// getWindow().setBackgroundDrawableResource(R.drawable.start_icon);
		// 隐藏ActionBar
		// ActionBar acBar = getActionBar();
		// acBar.hide();
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	/**
	 * 检查用户是否第一次使用，是否密码过期的方法
	 * 
	 * @throws ParseException
	 */
	@SuppressWarnings("unused")
	@SuppressLint("SimpleDateFormat")
	public void check_FirstUse_Expiration() throws ParseException {
		// 获取本地偏好设置，只能本应用读写
		SharedPreferences pre = getSharedPreferences("TTQAndroid", MODE_PRIVATE);
		SharedPreferences.Editor pre_editor = pre.edit();

		// 获取当前的时间
		Calendar c = Calendar.getInstance();
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = new Date();

		// 如果是第一次使用
		if (pre.getBoolean("ifFirstUse", false) == false) {
			
			// 延迟启动选择学校代码活动
			new Handler().postDelayed(new Runnable() {
				public void run() {
					Intent toChooseActivityIntent = new Intent(
							LaunchActivity.this, ChooseSchoolActivity.class);
					LaunchActivity.this.startActivity(toChooseActivityIntent);
					LaunchActivity.this.finish();
				}

			}, 1000);
		}
		// 如果不是第一次使用，判断密码是否过期
		else {
			// 获取上次登录时间
			String lastLoginDate_Str = pre.getString("lastLoginDate", null);
			Date lastLoginDate = f.parse(lastLoginDate_Str);
			if (lastLoginDate != null) {
				// 计算与今天相隔的时间，如果大于等于过期时间,则重新登录
				int days = GeneralTools.getInstance().daysBetween(
						lastLoginDate, currentDate);
				// 如果密码已经过期
				if (days >= this.ExpirationDayNum) {
					// 延迟启动登录活动
					new Handler().postDelayed(new Runnable() {
						public void run() {
							Intent toLoginActivityIntent = new Intent(
									LaunchActivity.this, LoginActivity.class);
							LaunchActivity.this
									.startActivity(toLoginActivityIntent);
							LaunchActivity.this.finish();
						}

					}, 1000);
				} else {
					// 如果密码没有过期，直接进入主界面					
					getModuleExpenseInfoByWS();
				}

			} else {
				// 如果没有获取到上次登录情况，则有可能是用户自己清除了数据，重新登录
				// 延迟启动登录活动
				new Handler().postDelayed(new Runnable() {
					public void run() {
						Intent toLoginActivityIntent = new Intent(
								LaunchActivity.this, LoginActivity.class);
						LaunchActivity.this
								.startActivity(toLoginActivityIntent);
						LaunchActivity.this.finish();
					}

				}, 1000);
				
			}

		}
	}
	
	// 从服务端获取哪些模块需要收费；
	//shen----------原来的部分被注释，此函数用来获得登陆用户学校时候可以使用付费模块，并获得需要付费模块的ID字符串
	public void getModuleExpenseInfoByWS(){
		
		new AsyncTask<Object, Object, DataTable>(){
			@Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				super.onPreExecute();
			}
			
			@Override
			protected DataTable doInBackground(Object... params) {
				// TODO Auto-generated method stub
				DataTable dt_moduleExpenseInfo = new DataTable();
				
				// 方法名
				String methodName = "APP_getModuleFlag";
	
				// 获取数据
				GetDataByWS getDataTool = GetDataByWS.getInstance();
				// 存放参数的map
				Map<String, String> paramsMap = new HashMap<String, String>();
				SharedPreferences pre = getSharedPreferences("TTQAndroid", MODE_PRIVATE);
				String schoolCode= pre.getString("schoolCode", "");
				String tokenID = DesUtil.getDesTokenID(UserCurrentState.getInstance().userID, "Admin203", 1);
				Log.i("lvjie", "传递参数：schoolCode="+schoolCode+"   tokenID="+tokenID);
				paramsMap.put("schoolCode", schoolCode);
				paramsMap.put("TokenID", tokenID);
				// 获取堂堂清公司的服务地址
				Resources res = getResources();
				String companyURL = (String) res.getText(R.string.company_service_url);
				getDataTool.setURL(companyURL);
				try {
					dt_moduleExpenseInfo = getDataTool.getDataAsTable(methodName, paramsMap);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.i("error", "1-->getModuleExpenseInfoByWS(String schoolCode)..出错了。。。");
					e.printStackTrace();
				}
				return dt_moduleExpenseInfo;
			}
				
			@Override
			protected void onPostExecute(DataTable result) {
				// TODO Auto-generated method stub
//				if (result != null) {
//					int count = result.getRowCount();
//					for (int i = 0; i < count; i++) {
//						try {
//							LaunchActivity.this.moduleExpenseInfos[0] = Integer.parseInt(result.getCell(i, "module_1"));
//							LaunchActivity.this.moduleExpenseInfos[1] = Integer.parseInt(result.getCell(i, "module_2"));
//							LaunchActivity.this.moduleExpenseInfos[2] = Integer.parseInt(result.getCell(i, "module_3"));
//							LaunchActivity.this.moduleExpenseInfos[3] = Integer.parseInt(result.getCell(i, "module_4"));
//							LaunchActivity.this.moduleExpenseInfos[4] = Integer.parseInt(result.getCell(i, "module_5"));
//							LaunchActivity.this.moduleExpenseInfos[5] = Integer.parseInt(result.getCell(i, "module_6"));
//							
//							Log.i("lvjie", "moduleExpenseInfos="+LaunchActivity.this.moduleExpenseInfos[0]+" "+
//							" "+LaunchActivity.this.moduleExpenseInfos[1]+" "+LaunchActivity.this.moduleExpenseInfos[2]+
//							" "+LaunchActivity.this.moduleExpenseInfos[3]+" "+LaunchActivity.this.moduleExpenseInfos[4]+
//							" "+LaunchActivity.this.moduleExpenseInfos[5]);
//						} catch (dataTableWrongException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				}else {
//					Log.i("error", "启动界面-->getModuleExpenseInfoByWS()...出错...");
//				}
				
//				if(result != null){
//					try {
//						if("true".equals(result.getCell(0, "isFree"))){
//							StudyFragment.schoolFlag = true;
//						}else{
//							StudyFragment.schoolFlag = false;
//						}
//						StudyFragment.chargeModuleString = result.getCell(0, "chargeModules");
//					} catch (dataTableWrongException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
				
				
				// 没有打开网络，然后提示，再退出；
				if(!GeneralTools.getInstance().isOpenNetWork1(LaunchActivity.this)){	
					initReminderUseDialog();
					return;
				}

				setModuleUseByExpenseInfos();
				//----------获得用户能否使用付费模块
				//checkUserJurisdiction();
					
				// 再一次进入系统，修改最后进入系统的时间；
				Log.i("lvjie", "LaunchActivity-->再一次进入系统，修改最后进入系统的时间");
				SharedPreferences pre = getSharedPreferences("TTQAndroid", MODE_PRIVATE);
				SharedPreferences.Editor pre_edit = pre.edit();
				Date currentDate = new Date();
				pre_edit.putString("lastLoginDate", DateUtil.convertDateToString("yyyy-MM-dd", currentDate));
				pre_edit.commit();
				
				getParentUnreadMesgByWS(UserInfo.getInstance().studentID, UserInfo.getInstance().classID);				

			}
			
		}.execute();
	}
	

	// 设置单例中的截止天数；用来判断模块的可用性；
	private void setDeadLineDays(){
		Date date1 = new Date();
		Date date2;
		try {
			Log.i("lvjie", "启动界面-->setDeadLineDays()...deadline="+UserInfo.getInstance().deadline);
			
			if(UserInfo.getInstance().deadline == null){		// 没有截止日期；
				UserInfo.getInstance().deadLineDays = -100;
				return;
			}
			date2 = DateUtil.convertStringToDate("yyyy-MM-dd", UserInfo.getInstance().deadline);
			
			UserInfo.getInstance().deadLineDays = DateUtil.daysBetween(date1, date2);			// 获取两个日期的相隔天数；
			Log.i("lvjie", "启动界面-->setDeadLineDays():  date1="+date1+"   date2="+date2+"   k="+UserInfo.getInstance().deadLineDays);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	// 根据模块收费情况以及用户缴费情况，判断模块是否可用；
	public void setModuleUseByExpenseInfos(){		
		for(int i=0; i<this.moduleExpenseInfos.length; i++){
			if(this.moduleExpenseInfos[i] == 0){
				UserInfo.getInstance().flag = 0;		// 当有一个模块收费，则设置表示为0；表示APP是需要收费的；
			}
		}
		
		setDeadLineDays();

		if(UserInfo.getInstance().deadLineDays >= 0){	// 没有到期，表示各个模块都可以用；

		}else {			// 到了截止时间，收费模块不可用；0表示收费，1表示免费；
			if(this.moduleExpenseInfos[0] == 0){
				this.moduleUse[0] = 0;
			}
			if(this.moduleExpenseInfos[1] == 0){
				this.moduleUse[1] = 0;
			}
			if(this.moduleExpenseInfos[2] == 0){
				this.moduleUse[2] = 0;
			}
			if(this.moduleExpenseInfos[3] == 0){
				this.moduleUse[3] = 0;
			}
			if(this.moduleExpenseInfos[4] == 0){
				this.moduleUse[4] = 0;
			}
			if(this.moduleExpenseInfos[5] == 0){
				this.moduleUse[5] = 0;
			}			
			UserInfo.getInstance().moduleUse = this.moduleUse;
		}
		
		Log.i("lvjie", "启动界面：deadline="+UserInfo.getInstance().deadline+"  deadLineDays="+UserInfo.getInstance().deadLineDays+
				"   moduleUse="+Arrays.toString(UserInfo.getInstance().moduleUse));
	}

	// 初始化提示对话框，没有联网，则无法进入系统；
	public void initReminderUseDialog(){
		if (this.reminderUseDialog == null) {
			View view = LayoutInflater.from(LaunchActivity.this).inflate(
					R.layout.dialog_reminder_authority_use, null);
			((TextView)view.findViewById(R.id.titleReminder_textView)).setText("提示");
			((TextView)view.findViewById(R.id.reminderInfo_textView)).setText("抱歉，您的网络没有打开，不能进入系统，请打开网络!");
			this.reminderUseDialog = new Dialog(LaunchActivity.this,
					R.style.alertdialog_style);
			this.reminderUseDialog.setContentView(view);
			this.reminderUseDialog.show();

			// 动态设置界面图片大小；
			DisplayMetrics metrics = getResources().getDisplayMetrics(); // 用来获取屏幕的分辨率；
			int screenWidthPX = metrics.widthPixels;
			int screenWidthDP = (int) DensityUtils.px2dp(LaunchActivity.this,
					screenWidthPX);

			// 设置图片的大小；
			int layoutWidthDP = (screenWidthDP * 4) / 5;
			int layoutWidthPX = DensityUtils
					.dp2px(LaunchActivity.this, layoutWidthDP);

			LayoutParams params = view.getLayoutParams();
			params.width = layoutWidthPX;
			view.setLayoutParams(params);

			// 添加点击事件
			view.findViewById(R.id.IKnow_textView).setOnClickListener(
					new OnClickListener(){

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							reminderUseDialog.dismiss();
							LaunchActivity.this.finish();
						}						
					});
			

		} else {
			this.reminderUseDialog.show();
		}
	}

	
	// 从服务端获取家长孩子所在班级老师信息，用来界面展示；
	public void getParentUnreadMesgByWS(String studentID, String classID){
		new AsyncTask<Object, Object, DataTable>() {
			@Override
			protected void onPreExecute() {
				UserCurrentState.getInstance().homeSchoolNew = 0;
			};
			@Override
			protected DataTable doInBackground(Object... params) {
				// TODO Auto-generated method stub
				DesUtil.addTokenIDToSchoolWS();		// 向服务端添加tokenID;
				DataTable dt_parentUnreadMeg = new DataTable();

				// 方法名
				String methodName = "APP_ParentUnreadMesg";
				// 存放参数的map
				Map<String, String> paramsMap = new HashMap<String, String>();
				String tokenID = "";
				try {
					tokenID = DesUtil.EncryptAsDoNet(DesUtil.tokenID, "Admin203");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String studentID = params[0].toString();
				String classID = params[1].toString();
				Log.i("lvjie", "传递的参数： studentID="+studentID+"  classID="+classID);
				paramsMap.put("studentID", studentID);
				paramsMap.put("classID", classID);
				paramsMap.put("TokenID", tokenID);
				// 获取数据
				GetDataByWS getDataTool = GetDataByWS.getInstance();
				// 从本地获取学校URL
				SharedPreferences pre = getSharedPreferences("TTQAndroid", MODE_PRIVATE);
				String schoolURL = pre.getString("school_service_url", null);
				if (schoolURL == null) {// 如果没有值
					return null;
				}
				getDataTool.setURL(schoolURL);
				try {
					dt_parentUnreadMeg = getDataTool.getDataAsTable(methodName, paramsMap);
				} catch (dataTableWrongException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return dt_parentUnreadMeg;
			}
			@Override
			protected void onPostExecute(DataTable result) {
				if (result != null) {
					int count = result.getRowCount();
					for (int i = 0; i < count; i++) {
						try {
							int unread = Integer.parseInt(result.getCell(i, "unread"));
							if(unread > 0){
								UserCurrentState.getInstance().homeSchoolNew++;
							}
							
						} catch (dataTableWrongException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else {
					Log.i("error", "启动界面-->getParentUnreadMesgByWS()...出错");
				}
				
				Log.i("lvjie", "homeSchoolNew="+UserCurrentState.getInstance().homeSchoolNew);
				// 进入主界面
				Intent toMainActivity = new Intent(LaunchActivity.this,
						MainActivity.class);
				LaunchActivity.this.startActivity(toMainActivity);
				LaunchActivity.this.finish();
			
			}
		
		}.execute(studentID, classID);
	}
	
}
