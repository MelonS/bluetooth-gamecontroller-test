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

import com.melons.input.Dpad;
import com.melons.input.InputManagerCompat;
import com.melons.input.Joystick;
import com.melons.input.JoystickMoveData;
import com.melons.input.InputManagerCompat.InputDeviceListener;

public class GameControllerManager implements InputDeviceListener {
	private static final String TAG = "GameControllerManager";
	
	//===[ Handler Message What Enum ]==========================================
	public static final int MSG_WHAT_BUTTON_A 				= 1;
	public static final int MSG_WHAT_BUTTON_B 				= 2;
	public static final int MSG_WHAT_BUTTON_X 				= 3;
	public static final int MSG_WHAT_BUTTON_Y 				= 4;
	public static final int MSG_WHAT_BUTTON_L1 				= 10;
	public static final int MSG_WHAT_BUTTON_R1 				= 11;
	public static final int MSG_WHAT_BUTTON_L2 				= 12;
	public static final int MSG_WHAT_BUTTON_R2 				= 13;
	public static final int MSG_WHAT_BUTTON_START 			= 20;
	public static final int MSG_WHAT_BUTTON_SELECT 	       	= 21;
	
	public static final int MSG_WHAT_JOYSTICK_MOVE_L_STICK	= 100;
	public static final int MSG_WHAT_JOYSTICK_MOVE_HAT		= 101;
	public static final int MSG_WHAT_JOYSTICK_MOVE_R_STICK	= 102;
	//==========================================================================
	
	//===[ Setting Value ]======================================================
	private static final int JOYSTICK_MOVE_SEND_PERIOD = 10; // millisecond
	//==========================================================================
	
	private static GameControllerManager __inst = null;
	@SuppressWarnings("unused")
	private Activity _activity = null;
	private Context _context = null;
	private Handler _handler = null;
	
	private InputManagerCompat _InputManager = null;
	private BlockingQueue<JoystickMoveData> _joystickMoveDatas = new LinkedBlockingQueue<JoystickMoveData>(); // thread safe
	
	private boolean _isEnable = false;
	
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
	}
	
	public void setup() {
		_InputManager = InputManagerCompat.Factory.getInputManager(_context);
		_InputManager.registerInputDeviceListener(this, null);
		
		printFindControllers();
		
		runScheduleJoystickMoveEvent();
	}

	public void setEnable(boolean isOn) {
		_isEnable = isOn;
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
	 * Public Static Methods 
	 */
	
	public static void callSetup() {
		getInstance().setup();
	}
	
	public static void callSetEnable(boolean isOn) {
		getInstance().setEnable(isOn);
	}
	
	public static boolean callIsFindGameController() {
		return getInstance().isFindGameController();
	}
	
	/*
	 * Public Methods
	 */
	
	public boolean handleKeyDown(int keyCode, KeyEvent event) {
		
		if (!_isEnable) return false;
		
		if (Dpad.isDpadDevice(event) || Dpad.isGamepadDevice(event)) {

			int msg_what = -1;
			
			switch (keyCode) {
			case KeyEvent.KEYCODE_BUTTON_A:		msg_what = MSG_WHAT_BUTTON_A;			break;
			case KeyEvent.KEYCODE_BUTTON_B:		msg_what = MSG_WHAT_BUTTON_B;			break;
			case KeyEvent.KEYCODE_BUTTON_X:		msg_what = MSG_WHAT_BUTTON_X;			break;
			case KeyEvent.KEYCODE_BUTTON_Y:		msg_what = MSG_WHAT_BUTTON_Y;			break;
			case KeyEvent.KEYCODE_BUTTON_L1:		msg_what = MSG_WHAT_BUTTON_L1;		break;
			case KeyEvent.KEYCODE_BUTTON_R1:		msg_what = MSG_WHAT_BUTTON_R1;		break;
			case KeyEvent.KEYCODE_BUTTON_L2:		msg_what = MSG_WHAT_BUTTON_L2;		break;
			case KeyEvent.KEYCODE_BUTTON_R2:		msg_what = MSG_WHAT_BUTTON_R2;		break;
			case KeyEvent.KEYCODE_BUTTON_START:	msg_what = MSG_WHAT_BUTTON_START;		break;
			case KeyEvent.KEYCODE_BUTTON_SELECT:	msg_what = MSG_WHAT_BUTTON_SELECT;	break;
			default:
				Log.i(TAG, "onKeyDown ELSE keyCode"+KeyEvent.keyCodeToString(event.getKeyCode()));
				break;
			}
			
			if (msg_what > 0) {
				sendMessageAtHandler(msg_what, event.getRepeatCount(), (int)event.getEventTime(), null);
				sendNativeMethod(msg_what, event.getEventTime(), -1.0f, -1.0f);
				return true;
			}
			
 		}
		
		return false;
	}
	
	public boolean handleGenericMotionEvent(MotionEvent event) {

		if (_InputManager != null)
			_InputManager.onGenericMotionEvent(event);
		
		if (!_isEnable) return false;
		
		if (Joystick.isJoystickDevice(event) && Joystick.isMoveAction(event)) {
			//Log.i(TAG, "InputDevice.SOURCE_JOYSTICK ACTION MOVE");
			
			final int historySize = event.getHistorySize();
			//Log.i(TAG, "historySize:"+historySize);
			
			for (int i = 0; i < historySize; ++i) {
				JoystickMoveData data = Joystick.processJoystickInput(event, i);
				handleJoystickMoveEvent(data);
			}
			
			JoystickMoveData data = Joystick.processJoystickInput(event, -1);
			handleJoystickMoveEvent(data);
			
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
	
	private void sendNativeMethod(int what, long eventTime, float x, float y) {
		// btn => x,y == -1.0f,-1.0f
		
		if (what >= MSG_WHAT_JOYSTICK_MOVE_L_STICK) {
			// stick
		}else{
			// button
		}
	}
	
	private boolean isFindGameController() {

		if (_InputManager != null) {
	        int[] deviceIds = _InputManager.getInputDeviceIds();
	        
	        for (int deviceId : deviceIds) {
	        	
	            InputDevice dev = _InputManager.getInputDevice(deviceId);
	            int sources = dev.getSources();
	            
	            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
	            		|| ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
	            	return true;
	            }
	        }
		}
		
		return false;
	}
	
	private void printFindControllers() {
		
		boolean isFind = isFindGameController();
		
        if (isFind) {
        	Log.i(TAG, "printFindControllers : Find the JOYSTICK OR GAMEPAD!!!");
        }else{
        	Log.i(TAG, "printFindControllers : Not find to JOYSTICK OR GAMEPAD!!!");
        }
    }
	
	private void handleJoystickMoveEvent(JoystickMoveData data) {
		
		if (!_isEnable) return;
		
		// 데이터 => index(순서대로) : event+xy 컨테이너(큐)에 뒤에다가 넣는다.
		_joystickMoveDatas.offer(data);
	}

	private void runScheduleJoystickMoveEvent() {
		JoystickMoveEventScheduler job = new JoystickMoveEventScheduler();
		Timer t = new Timer();
		t.schedule(job, 0, JOYSTICK_MOVE_SEND_PERIOD);
	}
	
	/*
	 * Inner Classes
	 */
	
	class JoystickMoveEventScheduler extends TimerTask {
		
		private int lastMsgWhat = -1;
		
		@Override
		public void run() {
			if (!_joystickMoveDatas.isEmpty()) {
				
				JoystickMoveData sendData = null;
				
				if (_joystickMoveDatas.size() == 1) { // size == 1
					JoystickMoveData checkData = _joystickMoveDatas.peek(); // not remove
					if (checkData.xy.isZero()) {
						//  case [x:0,y:0]
						//  getFront => send => popFront
						_joystickMoveDatas.poll(); // remove
						sendData = checkData;
					}else{
						//  case not [x:0,y:0]
						//  getFront => send
						sendData = checkData;
					}
				}else{
					// size > 0
					// getFront => send => popFront 
					JoystickMoveData data = _joystickMoveDatas.poll(); // remove
					sendData = data;
				}
				
				if (sendData != null) {
					int msg_what = -1;
					if (sendData.isAxis_LStick()) {
						//Log.i(TAG, "L_STICK x:"+sendData.xy.x+" y:"+sendData.xy.y);
						msg_what    = MSG_WHAT_JOYSTICK_MOVE_L_STICK;
						lastMsgWhat = MSG_WHAT_JOYSTICK_MOVE_L_STICK;
					}else if (sendData.isAxis_HAT()) {
						//Log.i(TAG, "HAT x:"+sendData.xy.x+" y:"+sendData.xy.y);
						msg_what    = MSG_WHAT_JOYSTICK_MOVE_HAT;
						lastMsgWhat = MSG_WHAT_JOYSTICK_MOVE_HAT;
					}else if (sendData.isAxis_RStick()) {
						//Log.i(TAG, "R_STICK x:"+sendData.xy.x+" y:"+sendData.xy.y);
						msg_what    = MSG_WHAT_JOYSTICK_MOVE_R_STICK;
						lastMsgWhat = MSG_WHAT_JOYSTICK_MOVE_R_STICK;
					}else{
						msg_what = lastMsgWhat;
						Log.i(TAG, "ELSE:"+msg_what+" x:"+sendData.xy.x+" y:"+sendData.xy.y);
					}
					
					if (msg_what > 0) {
						sendMessageAtHandler(msg_what, (int) sendData.event.getEventTime(), -1, sendData.xy);
						sendNativeMethod(msg_what, sendData.event.getEventTime(), sendData.xy.x, sendData.xy.y);
					}else{
						//Log.e(TAG, "JoystickMoveEventScheduler ERROR 2");
					}
					
				}else{
					//Log.e(TAG, "JoystickMoveEventScheduler ERROR 1");
				}
			}
		}
	}
}
