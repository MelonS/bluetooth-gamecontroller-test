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
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.melons.common.Vec2;
import com.melons.manager.GameControllerManager;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	
	private static int TEXT_VIEW_COUNT = 12;
	private TextView[] _tvs;
	
	private int[] _tvAlpha = new int[TEXT_VIEW_COUNT];
	
	private DebugHandler _handler = new DebugHandler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initTextViews();
		
		GameControllerManager.getInstance().init(this, _handler);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		GameControllerManager.callSetup();
		GameControllerManager.callSetEnable(true);
	}

	private void initTextViews() {
		LinearLayout mainLayout = (LinearLayout)findViewById(R.id.main_layout);
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		_tvs = new TextView[TEXT_VIEW_COUNT];
        for (int l = 0; l < TEXT_VIEW_COUNT; ++l)
        {
        	_tvs[l] = new TextView(this);
        	_tvs[l].setTextSize(21);
        	_tvs[l].setLayoutParams(lp);
        	_tvs[l].setId(l);
        	_tvs[l].setText((l + 1) + ": something");
        	mainLayout.addView(_tvs[l]);
        }
        
        for (int i = 0; i < TEXT_VIEW_COUNT; ++i)
        {
        	_tvAlpha[i] = 255;
        }
	}
	

	@SuppressLint("HandlerLeak")
	class DebugHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch (msg.what) {
			case GameControllerManager.MSG_WHAT_BUTTON_A:
				printKeyDown(msg, 0, "BUTTON_A", Color.rgb(255, 0, 0));
				break;
			case GameControllerManager.MSG_WHAT_BUTTON_B:
				printKeyDown(msg, 1, "BUTTON_B", Color.rgb(255, 125, 0));
				break;
			case GameControllerManager.MSG_WHAT_BUTTON_X:
				printKeyDown(msg, 2, "BUTTON_X", Color.rgb(255, 255, 0));
				break;
			case GameControllerManager.MSG_WHAT_BUTTON_Y:
				printKeyDown(msg, 3, "BUTTON_Y", Color.rgb(125, 255, 0));
				break;
			case GameControllerManager.MSG_WHAT_BUTTON_L1:
				printKeyDown(msg, 4, "BUTTON_L1", Color.rgb(0, 255, 0));
				break;
			case GameControllerManager.MSG_WHAT_BUTTON_R1:
				printKeyDown(msg, 5, "BUTTON_R1", Color.rgb(0, 255, 125));
				break;
			case GameControllerManager.MSG_WHAT_BUTTON_L2:
				printKeyDown(msg, 6, "BUTTON_L2", Color.rgb(0, 255, 255));
				break;
			case GameControllerManager.MSG_WHAT_BUTTON_R2:
				printKeyDown(msg, 7, "BUTTON_R2", Color.rgb(0, 125, 255));
				break;
			case GameControllerManager.MSG_WHAT_BUTTON_START:
				printKeyDown(msg, 8, "BUTTON_START", Color.rgb(0, 0, 255));
				break;
			case GameControllerManager.MSG_WHAT_BUTTON_SELECT:
				printKeyDown(msg, 9, "BUTTON_SELECT", Color.rgb(125, 0, 255));
				break;
				
			case GameControllerManager.MSG_WHAT_JOYSTICK_MOVE:
			{
				int eventTime = msg.arg1;
				Vec2 xy = (Vec2)msg.obj;
				if (xy != null) {
					Log.i(TAG,"x:["+xy.x+"] y:["+xy.y+"]");
					
					_tvs[10].setText("x:["+xy.x+"] y:["+xy.y+"]");
					_tvs[11].setText("time:"+eventTime);
					
					if (xy.isZero()) {
						_tvs[10].setBackgroundColor(Color.TRANSPARENT);
						_tvs[11].setBackgroundColor(Color.TRANSPARENT);
					}else{
						_tvs[10].setBackgroundColor(Color.RED);
						_tvs[11].setBackgroundColor(Color.GREEN);
					}
				}
			}
				break;
			}
		}
	}
	
	private void printKeyDown(Message msg, int index, String key_name, int color) {
		
		_tvs[index].setText("KeyDown ["+key_name+"] repeatCount:"+msg.arg1+" time:"+msg.arg2);
		changeTextViewBackgroundAlpha(index, color);
	}
	
	private void changeTextViewBackgroundAlpha(int index, int color) {
		
		_tvAlpha[index] -= 10;
		if (_tvAlpha[index] <= 0) 
			_tvAlpha[index] = 255;
		
		int c = Color.argb(_tvAlpha[index], Color.red(color), Color.green(color), Color.blue(color));
		_tvs[index].setBackgroundColor(c);
	}

	/*
	 * Input Methods
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i(TAG, "onKeyDown keyCode:"+keyCode+" event:"+event);

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
		Log.i(TAG, "onGenericMotionEvent event:"+event);

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
