package com.ttqeducation.activitys.others;

/**
 * 吕杰
 * 
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ttqeducation.R;

public class UserOwnSetActivity extends Activity {

	// 标题栏部分；
	private LinearLayout titleBackLayout = null; // 标题栏返回按钮；
	private TextView titleTextView = null;			// 标题栏文字；

	private LinearLayout changePwdLayout = null;	// 修改密码；
	private LinearLayout aboutKKQLayout = null;		// 关于课课清；

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userown_set);

		initView();
	}

	public void initView() {
		// 标题栏部分 实例化；
		this.titleTextView = (TextView) (super.findViewById(R.id.action_bar).findViewById(R.id.title_text));
		this.titleTextView.setText("个人设置");
		this.titleBackLayout = (LinearLayout) (super
				.findViewById(R.id.action_bar).findViewById(R.id.title_back_layout));
		this.titleBackLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				UserOwnSetActivity.this.finish();
			}
		});

		this.changePwdLayout = (LinearLayout) super.findViewById(R.id.layout_changePwd);
		this.aboutKKQLayout = (LinearLayout) super.findViewById(R.id.layout_aboutKKQ);
		
		this.changePwdLayout.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent changePwdIntent = new Intent(UserOwnSetActivity.this, ChangePwdActivity.class);
				startActivity(changePwdIntent);
				// UserOwnSetActivity.this.finish();
			}
		});
		
		this.aboutKKQLayout.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent aboutKKQIntent = new Intent(UserOwnSetActivity.this, AboutKKQActivity.class);
				startActivity(aboutKKQIntent);
				// UserOwnSetActivity.this.finish();
			}
		});
	}

	
	// 窗口提示信息；
	public void showToast(String toastMessage) {
		Toast toast = Toast.makeText(UserOwnSetActivity.this, toastMessage,
				Toast.LENGTH_SHORT);
		toast.show();
	}

}
