package com.melons.input;

import android.view.MotionEvent;

import com.melons.common.Vec2;

public class JoystickMoveData {
	
	public MotionEvent event;
	public Vec2 xy;
	public int axisX; // -1, MotionEvent.AXIS_X, MotionEvent.AXIS_HAT_X, MotionEvent.AXIS_Z
	public int axisY; // -1, MotionEvent.AXIS_Y, MotionEvent.AXIS_HAT_Y, MotionEvent.AXIS_RZ
	
	public JoystickMoveData(MotionEvent event_, Vec2 xy_, int axisX_, int axisY_) {
		this.event = event_;
		this.xy    = xy_;
		this.axisX = axisX_;
		this.axisY = axisY_;
	}
	
	public boolean isAxis_LStick() {
		if (axisX == MotionEvent.AXIS_X
				|| axisY == MotionEvent.AXIS_Y)
			return true;
		else
			return false;
	}
	
	public boolean isAxis_HAT() {
		if (axisX == MotionEvent.AXIS_HAT_X
				|| axisY == MotionEvent.AXIS_HAT_Y)
			return true;
		else
			return false;
	}
	
	public boolean isAxis_RStick() {
		if (axisX == MotionEvent.AXIS_Z
				|| axisY == MotionEvent.AXIS_RZ)
			return true;
		else
			return false;
	}
}