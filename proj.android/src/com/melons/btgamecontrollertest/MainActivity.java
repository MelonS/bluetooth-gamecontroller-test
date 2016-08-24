package com.melons.btgamecontrollertest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;

import com.melons.manager.InputManagerCompat;
import com.melons.manager.InputManagerCompat.InputDeviceListener;

public class MainActivity extends Activity implements InputDeviceListener {
	private static final String TAG = "MainActivity";
	
	private InputManagerCompat _InputManager = null;
	
	private TextView _tv1 = null;
	private TextView _tv2 = null;
	private TextView _tv3 = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		_tv1 = (TextView)findViewById(R.id.tv1);
		_tv2 = (TextView)findViewById(R.id.tv2);
		_tv3 = (TextView)findViewById(R.id.tv3);
		
		_InputManager = InputManagerCompat.Factory.getInputManager(this);
		_InputManager.registerInputDeviceListener(this, null);
		
		findControllers();
	}

	void findControllers() {
        int[] deviceIds = _InputManager.getInputDeviceIds();
        Log.i(TAG, "findControllers deviceCount:["+deviceIds.length+"]");
        for (int deviceId : deviceIds) {
        	Log.i(TAG, "findControllers deviceId:"+deviceId);
            InputDevice dev = _InputManager.getInputDevice(deviceId);
            int sources = dev.getSources();
            Log.i(TAG, "findControllers sources:"+sources);
            // if the device is a gamepad/joystick, create a ship to represent it
            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) ||
                    ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
                // if the device has a gamepad or joystick
            	Log.i(TAG, "findControllers JOYSTICK OR GAMEPAD!!!");
            }
        }
    }
	
	/*
	 * implements InputDeviceListener
	 */
	@Override
	public void onInputDeviceAdded(int deviceId) {
		Log.i(TAG, "onInputDeviceAdded deviceId:"+deviceId);
	}

	@Override
	public void onInputDeviceChanged(int deviceId) {
		Log.i(TAG, "onInputDeviceChanged deviceId:"+deviceId);
	}

	@Override
	public void onInputDeviceRemoved(int deviceId) {
		Log.i(TAG, "onInputDeviceRemoved deviceId:"+deviceId);
	}

	/*
	 * Input Methods
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//Log.i(TAG, "onKeyDown keyCode:"+keyCode+" event:"+event);
		boolean handled = false;
		if ((event.getSource() & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) {
			//if (event.getRepeatCount() == 0) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
						|| keyCode == KeyEvent.KEYCODE_BUTTON_A)
				{
					Log.i(TAG, "onKeyDown CENTER or BTN_A");
					_tv1.setText("KeyDown BTN_A :"+event.getEventTime()+" repeatCount:"+event.getRepeatCount());
					handled = true;
				}else{
					Log.i(TAG, "onKeyDown ELSE event:"+event);
				}
			//}
			if (handled) {
				return true;
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		//Log.i(TAG, "onKeyUp keyCode:"+keyCode+" event:"+event);
		boolean handled = false;
		if ((event.getSource() & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) {
			//if (event.getRepeatCount() == 0) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
						|| keyCode == KeyEvent.KEYCODE_BUTTON_A)
				{
					Log.i(TAG, "onKeyUp CENTER or BTN_A");
					_tv2.setText("KeyUp BTN_A :"+event.getEventTime()+" repeatCount:"+event.getRepeatCount());
					handled = true;
				}else{
					Log.i(TAG, "onKeyUp ELSE event:"+event);
				}
			//}
			if (handled) {
				return true;
			}
		}
		
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		//Log.i(TAG, "onGenericMotionEvent event:"+event);
		
		if (_InputManager != null)
			_InputManager.onGenericMotionEvent(event);
		
		return super.onGenericMotionEvent(event);
	}
	
	
}
