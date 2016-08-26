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
	
	//===[ Handler Message What Enum ]==========================================
	public static final int MSG_WHAT_BUTTON_A 		= 1;
	public static final int MSG_WHAT_BUTTON_B 		= 2;
	public static final int MSG_WHAT_BUTTON_X 		= 3;
	public static final int MSG_WHAT_BUTTON_Y 		= 4;
	public static final int MSG_WHAT_BUTTON_L1 		= 10;
	public static final int MSG_WHAT_BUTTON_R1 		= 11;
	public static final int MSG_WHAT_BUTTON_L2 		= 12;
	public static final int MSG_WHAT_BUTTON_R2 		= 13;
	public static final int MSG_WHAT_BUTTON_START 	= 20;
	public static final int MSG_WHAT_BUTTON_SELECT 	= 21;
	public static final int MSG_WHAT_JOYSTICK_MOVE 	= 100;
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
	
	/*
	 * Public Methods
	 */
	
	public boolean handleKeyDown(int keyCode, KeyEvent event) {
		
		if (!_isEnable) return false;
		
		if (Dpad.isDpadDevice(event) || Dpad.isGamepadDevice(event)) {

			switch (keyCode) {
			case KeyEvent.KEYCODE_BUTTON_A:
				sendMessageAtHandler(MSG_WHAT_BUTTON_A, event.getRepeatCount(), (int)event.getEventTime(), null);
				return true;
			case KeyEvent.KEYCODE_BUTTON_B:
				sendMessageAtHandler(MSG_WHAT_BUTTON_B, event.getRepeatCount(), (int)event.getEventTime(), null);
				return true;
			case KeyEvent.KEYCODE_BUTTON_X:
				sendMessageAtHandler(MSG_WHAT_BUTTON_X, event.getRepeatCount(), (int)event.getEventTime(), null);
				return true;
			case KeyEvent.KEYCODE_BUTTON_Y:
				sendMessageAtHandler(MSG_WHAT_BUTTON_Y, event.getRepeatCount(), (int)event.getEventTime(), null);
				return true;
			case KeyEvent.KEYCODE_BUTTON_L1:
				sendMessageAtHandler(MSG_WHAT_BUTTON_L1, event.getRepeatCount(), (int)event.getEventTime(), null);
				return true;
			case KeyEvent.KEYCODE_BUTTON_R1:
				sendMessageAtHandler(MSG_WHAT_BUTTON_R1, event.getRepeatCount(), (int)event.getEventTime(), null);
				return true;
			case KeyEvent.KEYCODE_BUTTON_L2:
				sendMessageAtHandler(MSG_WHAT_BUTTON_L2, event.getRepeatCount(), (int)event.getEventTime(), null);
				return true;
			case KeyEvent.KEYCODE_BUTTON_R2:
				sendMessageAtHandler(MSG_WHAT_BUTTON_R2, event.getRepeatCount(), (int)event.getEventTime(), null);
				return true;
			case KeyEvent.KEYCODE_BUTTON_START:
				sendMessageAtHandler(MSG_WHAT_BUTTON_START, event.getRepeatCount(), (int)event.getEventTime(), null);
				return true;
			case KeyEvent.KEYCODE_BUTTON_SELECT:
				sendMessageAtHandler(MSG_WHAT_BUTTON_SELECT, event.getRepeatCount(), (int)event.getEventTime(), null);
				return true;
			default:
				Log.i(TAG, "onKeyDown ELSE keyCode"+KeyEvent.keyCodeToString(event.getKeyCode()));
				break;
			}
			
		}
		
		return false;
	}
	
	public boolean handleGenericMotionEvent(MotionEvent event) {

		if (_InputManager != null)
			_InputManager.onGenericMotionEvent(event);
		
		if (!_isEnable) return false;
		
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
		
		if (!_isEnable) return;
		
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

			if (!_joystickMoveDatas.isEmpty()) {
				
				if (_joystickMoveDatas.size() == 1) { // size == 1
					JoystickMoveData checkData = _joystickMoveDatas.peek(); // not remove
					if (checkData.xy.isZero()) {
						//  case [x:0,y:0]
						//  getFront => send => popFront
						_joystickMoveDatas.poll(); // remove
						sendMessageAtHandler(MSG_WHAT_JOYSTICK_MOVE, (int) checkData.event.getEventTime(), -1, checkData.xy);
					}else{
						//  case not [x:0,y:0]
						//  getFront => send
						sendMessageAtHandler(MSG_WHAT_JOYSTICK_MOVE, (int) checkData.event.getEventTime(), -1, checkData.xy);
					}
				}else{
					// size > 0
					// getFront => send => popFront 
					JoystickMoveData data = _joystickMoveDatas.poll(); // remove
					sendMessageAtHandler(MSG_WHAT_JOYSTICK_MOVE, (int) data.event.getEventTime(), -1, data.xy);
				}
			}
		}
	}
}
