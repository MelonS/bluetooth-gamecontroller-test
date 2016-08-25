package com.melons.manager;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.melons.common.Vec2;
import com.melons.input.Dpad;
import com.melons.input.Joystick;
import com.melons.manager.InputManagerCompat.InputDeviceListener;

public class GameControllerManager implements InputDeviceListener {
	private static final String TAG = "GameControllerManager";
	
	public static final int MSG_WHAT_BUTTON_A 		= 1;
	public static final int MSG_WHAT_BUTTON_B 		= 2;
	public static final int MSG_WHAT_JOYSTICK_MOVE 	= 3;

	private static final int JOYSTICK_MOVE_SEND_PERIOD = 10; // millisecond
	
	private static GameControllerManager __inst = null;
	
	@SuppressWarnings("unused")
	private Activity _activity = null;
	private Context _context = null;
	private Handler _handler = null;
	
	private InputManagerCompat _InputManager = null;
	
	private BlockingQueue<JoystickMoveData> _joystickMoveDatas = new LinkedBlockingQueue<JoystickMoveData>();
	
	public static GameControllerManager getInstance() {
		if (__inst == null) {
			__inst = new GameControllerManager();
		}
		return __inst;
	}
	
	private GameControllerManager() {
		
	}
	
	public void init(Activity act, Handler handler) {
		_activity = act;
		_context = act.getApplicationContext();
		_handler = handler;
				
		_InputManager = InputManagerCompat.Factory.getInputManager(_context);
		_InputManager.registerInputDeviceListener(this, null);
		
		printFindControllers();
		
		runScheduleJoystickMoveEvent();
	}

	/*
	 * Implement Methods
	 */
	
	@Override
	public void onInputDeviceAdded(int deviceId) {
		Log.i(TAG, "onInputDeviceAdded deviceId:"+deviceId);
		printFindControllers();
	}

	@Override
	public void onInputDeviceChanged(int deviceId) {
		Log.i(TAG, "onInputDeviceChanged deviceId:"+deviceId);
		printFindControllers();
	}

	@Override
	public void onInputDeviceRemoved(int deviceId) {
		Log.i(TAG, "onInputDeviceRemoved deviceId:"+deviceId);
		printFindControllers();
	}
	
	/*
	 * Public Methods
	 */
	
	public boolean handleKeyDown(int keyCode, KeyEvent event) {
		
		if (Dpad.isDpadDevice(event)) {

			if (keyCode == KeyEvent.KEYCODE_BUTTON_A) {
				//Log.i(TAG, "onKeyDown KEYCODE_BUTTON_A");
				sendMessageAtHandler(MSG_WHAT_BUTTON_A, event.getRepeatCount(), (int)event.getEventTime(), null);
				return true;
			}else if (keyCode == KeyEvent.KEYCODE_BUTTON_B) {
				//Log.i(TAG, "onKeyDown KEYCODE_BUTTON_B");
				sendMessageAtHandler(MSG_WHAT_BUTTON_B, event.getRepeatCount(), (int)event.getEventTime(), null);
				return true;
			}else{
				Log.i(TAG, "onKeyDown ELSE event:"+event);
			}
		}
		
		return false;
	}
	
	public boolean handleGenericMotionEvent(MotionEvent event) {

		if (_InputManager != null)
			_InputManager.onGenericMotionEvent(event);
		
		if (Dpad.isDpadDevice(event)) {
			Log.i(TAG, "InputDevice.SOURCE_DPAD");
			//TODO
		}
		
		if (Joystick.isJoystickDevice(event) && Joystick.isMoveAction(event)) {
			//Log.i(TAG, "InputDevice.SOURCE_JOYSTICK ACTION MOVE");
			
			final int historySize = event.getHistorySize();
			//Log.i(TAG, "historySize:"+historySize);
			
			for (int i = 0; i < historySize; ++i) {
				Vec2 xy = Joystick.processJoystickInput(event, i);
				handleJoystickMoveEvent(event, xy);
			}
			
			Vec2 xy = Joystick.processJoystickInput(event, -1);
			handleJoystickMoveEvent(event, xy);
			
			return true;
		}
		
		return false;
	}
	
	/*
	 * Private Methods
	 */
	
	private void sendMessageAtHandler(int what, int arg1, int arg2, Object obj) {
		if (_handler != null) {
			Message msg = _handler.obtainMessage();
			msg.what = what;
			msg.arg1 = arg1;
			msg.arg2 = arg2;
			msg.obj = obj;
			_handler.sendMessage(msg);
		}
	}
	
	private void printFindControllers() {
		
		boolean isFind = false;
		
        int[] deviceIds = _InputManager.getInputDeviceIds();
        //Log.i(TAG, "findControllers deviceCount:["+deviceIds.length+"]");
        for (int deviceId : deviceIds) {
        	//Log.i(TAG, "findControllers deviceId:"+deviceId);
            InputDevice dev = _InputManager.getInputDevice(deviceId);
            int sources = dev.getSources();
            //Log.i(TAG, "findControllers sources:"+sources);
            // if the device is a gamepad/joystick, create a ship to represent it
            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) ||
                    ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
            	
                // if the device has a gamepad or joystick
            	isFind = true;
            }
        }
        
        if (isFind) {
        	Log.i(TAG, "printFindControllers : Find the JOYSTICK OR GAMEPAD!!!");
        }else{
        	Log.i(TAG, "printFindControllers : Not find to JOYSTICK OR GAMEPAD!!!");
        }
    }
	
	private void handleJoystickMoveEvent(MotionEvent event, Vec2 xy) {
		
		// 데이터 => index(순서대로) : event+xy 컨테이너(큐)에 뒤에다가 넣는다.
		_joystickMoveDatas.offer(new JoystickMoveData(event, xy));
	}

	private void runScheduleJoystickMoveEvent() {
		JoystickMoveEventScheduler job = new JoystickMoveEventScheduler();
		Timer t = new Timer();
		t.schedule(job, 0, JOYSTICK_MOVE_SEND_PERIOD);
	}
	
	/*
	 * Inner Classes
	 */
	
	class JoystickMoveData {
		public MotionEvent event;
		public Vec2 xy;
		public JoystickMoveData(MotionEvent event_, Vec2 xy_) {
			this.event = event_;
			this.xy    = xy_;
		}
	}
	
	class JoystickMoveEventScheduler extends TimerTask {
		@Override
		public void run() {
			//Log.i(TAG, "JoystickMoveEventScheduler run() : "+System.currentTimeMillis());

			if (!_joystickMoveDatas.isEmpty()) {
				
				//Log.i(TAG, "_joystickMoveDatas Size:"+_joystickMoveDatas.size());
				
				if (_joystickMoveDatas.size() == 1) { // 1개 있을 때
					JoystickMoveData checkData = _joystickMoveDatas.peek();
					if (checkData.xy.isZero()) {
						//  2-2. [0,0] 일때.
						//    getFront => send => popFront
						_joystickMoveDatas.poll(); // remove
						sendMessageAtHandler(MSG_WHAT_JOYSTICK_MOVE, (int) checkData.event.getEventTime(), -1, checkData.xy);
					}else{
						//  2-1. [0,0] 이 아닐때.
						//    getFront => send	
						//sendMessageAtHandler(MSG_WHAT_JOYSTICK_MOVE, (int) checkData.event.getEventTime(), -1, checkData.xy); // TODO : 사용할 코드.
						sendMessageAtHandler(MSG_WHAT_JOYSTICK_MOVE, (int) System.currentTimeMillis(), -1, checkData.xy); // TODO : 테스트 코드.
					}
				}else{
					// 여러개 있을 때
					// getFront => send => popFront 
					JoystickMoveData data = _joystickMoveDatas.poll(); // remove
					sendMessageAtHandler(MSG_WHAT_JOYSTICK_MOVE, (int) data.event.getEventTime(), -1, data.xy);
				}
			}
		}
	}
}
