package com.melons.btgamecontrollertest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;
import com.melons.common.Vec2;
import com.melons.manager.GameControllerManager;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	
	private TextView _tv1 = null;
	private TextView _tv2 = null;
	private TextView _tv3 = null;
	private TextView _tv4 = null;
	
	private int _tv1Alpha = 255;
	private int _tv2Alpha = 255;
	
	private DebugHandler _handler = new DebugHandler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		_tv1 = (TextView)findViewById(R.id.tv1);
		_tv2 = (TextView)findViewById(R.id.tv2);
		_tv3 = (TextView)findViewById(R.id.tv3);
		_tv4 = (TextView)findViewById(R.id.tv4);
		
		GameControllerManager.getInstance().init(this, _handler);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		GameControllerManager.callSetup();
		GameControllerManager.callSetEnable(true);
	}



	@SuppressLint("HandlerLeak")
	class DebugHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch (msg.what) {
			case GameControllerManager.MSG_WHAT_BUTTON_A:
			{
				_tv1.setText("KeyDown [BUTTON_A] repeatCount:"+msg.arg1+" time:"+msg.arg2);
				
				_tv1Alpha -= 10;
				if (_tv1Alpha <= 0) _tv1Alpha = 255;
				int c = Color.argb(_tv1Alpha, Color.red(Color.YELLOW), Color.green(Color.YELLOW), Color.blue(Color.YELLOW));
				_tv1.setBackgroundColor(c);
			}
				break;
			case GameControllerManager.MSG_WHAT_BUTTON_B:
			{
				_tv2.setText("KeyDown [BUTTON_B] repeatCount:"+msg.arg1+" time:"+msg.arg2);
				
				_tv2Alpha -= 10;
				if (_tv2Alpha <= 0) _tv2Alpha = 255;
				int c = Color.argb(_tv2Alpha, Color.red(Color.parseColor("aqua")), Color.green(Color.parseColor("aqua")), Color.blue(Color.parseColor("aqua")));
				_tv2.setBackgroundColor(c);
			}
				break;
			case GameControllerManager.MSG_WHAT_JOYSTICK_MOVE:
			{
				int eventTime = msg.arg1;
				Vec2 xy = (Vec2)msg.obj;
				if (xy != null) {
					Log.i(TAG,"x:["+xy.x+"] y:["+xy.y+"]");
					
					_tv3.setText("x:["+xy.x+"] y:["+xy.y+"]");
					_tv4.setText("time:"+eventTime);
					
					if (xy.isZero()) {
						_tv3.setBackgroundColor(Color.TRANSPARENT);
						_tv4.setBackgroundColor(Color.TRANSPARENT);
					}else{
						_tv3.setBackgroundColor(Color.RED);
						_tv4.setBackgroundColor(Color.GREEN);
					}
				}
			}
				break;
			}
		}
	}

	/*
	 * Input Methods
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//Log.i(TAG, "onKeyDown keyCode:"+keyCode+" event:"+event);

		if (GameControllerManager.getInstance().handleKeyDown(keyCode, event))
			return true;
		
		return super.onKeyDown(keyCode, event);
	}

	/*
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyUp(keyCode, event);
	}
	*/

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		//Log.i(TAG, "onGenericMotionEvent event:"+event);

		if (GameControllerManager.getInstance().handleGenericMotionEvent(event))
			return true;
		
		return super.onGenericMotionEvent(event);
	}

 	@Override
	public void onBackPressed() {
		Log.i(TAG,"onBackPressed");
		//super.onBackPressed();
	}
	
}
