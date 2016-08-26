package com.melons.input;

import android.view.InputDevice;
import android.view.MotionEvent;

import com.melons.common.Vec2;

public class Joystick {
	
	@SuppressWarnings("unused")
	private static final String TAG = "Joystick";

	public static boolean isJoystickDevice(MotionEvent event) {
		
		if ((event.getSource() & InputDevice.SOURCE_JOYSTICK)
				== InputDevice.SOURCE_JOYSTICK) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isMoveAction(MotionEvent event) {
		
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			return true;
		} else {
			return false;
		}
	}
	
	public static JoystickMoveData processJoystickInput(MotionEvent event, int historyPos) {

	    InputDevice mInputDevice = event.getDevice();
	    
	    int axisX = -1;	    
	    Float x = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_X, historyPos); 
	    if (x != null) {
	    	axisX = MotionEvent.AXIS_X;
	    } else {
	    	x = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_HAT_X, historyPos);
	    	if (x != null) {
	    		axisX = MotionEvent.AXIS_HAT_X;
	    	} else {
	    		x = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Z, historyPos);
	    		if (x != null) {
	    			axisX = MotionEvent.AXIS_Z;
	    		} else {
	    			x = Float.valueOf(0.0f);
	    	    	//Log.e(TAG, "ERROR - x");
	    		}
	    	}
	    }
	    
	    int axisY = -1;
	    Float y = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Y, historyPos);
	    if (y != null) {
	    	axisY = MotionEvent.AXIS_Y;
	    } else {
	    	y = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_HAT_Y, historyPos);
	    	if (y != null) {
	    		axisY = MotionEvent.AXIS_HAT_Y;
	    	} else {
	    		y = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_RZ, historyPos);
	    		if (y != null) {
	    			axisY = MotionEvent.AXIS_RZ;
	    		} else {
	    			y = Float.valueOf(0.0f);
	    	    	//Log.e(TAG, "ERROR - y");
	    		}
	    	}
	    }
	    	    
	    //Log.i(TAG, "x:["+x+"] y:["+y+"]");
	    
	    Vec2 xy = new Vec2(x, y);
	    JoystickMoveData ret = new JoystickMoveData(event, xy, axisX, axisY);
	    return ret;
	}
	
	/*
	 * Private static Methods
	 */
	private static Float getCenteredAxis(MotionEvent event, InputDevice device, int axis, int historyPos) {
		
	    final InputDevice.MotionRange range = device.getMotionRange(axis, event.getSource());
	    
	    // A joystick at rest does not always report an absolute position of
	    // (0,0). Use the getFlat() method to determine the range of values
	    // bounding the joystick axis center.
	    if (range != null) {
	        final float flat = range.getFlat();
	        final float value =
	                historyPos < 0 ? event.getAxisValue(axis):
	                event.getHistoricalAxisValue(axis, historyPos);

	        // Ignore axis values that are within the 'flat' region of the
	        // joystick axis center.
	        if (Math.abs(value) > flat) {
	            return Float.valueOf(value);
	        }
	        else{
	        	//Log.w(TAG, "getCenteredAxis abs(value):"+Math.abs(value)+" <= flat:"+flat);
	        }
	    }
	    else{
	    	//Log.w(TAG, "getCenteredAxis Range IS NULL");
	    }
	    
	    return null;
	}
}
