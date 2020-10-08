/*
 * Copyright (c) 2020-2029 Seppo Tiainen, gkos@gkos.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.combokey.basic.view.touchevent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.combokey.basic.view.touchevent.SimpleTouchEvent.Type;

import static java.lang.Math.abs;


public class SimpleTouchEventProcessor implements OnTouchListener {

	private final List<SimpleTouchEventListener> listeners = new LinkedList<SimpleTouchEventListener>();

	protected final Map<Integer, LinkedList<SimpleTouchEvent>> history = new HashMap<Integer, LinkedList<SimpleTouchEvent>>();
	private final static String TAG = "-TOUCHEVENT";

	public List<SimpleTouchEvent> processMotionEvent(MotionEvent event) {

		List<SimpleTouchEvent> simpleTouchEvents = getSimpleTouchEvent(event);

		for (SimpleTouchEvent simpleTouchEvent : simpleTouchEvents) {
			pushHistory(simpleTouchEvent);
		}
		
		return simpleTouchEvents;

	}


	private int deltaX = 0;
	private int deltaY = 0;
	private int startX = 0;
	private int startY = 0;


	private List<SimpleTouchEvent> getSimpleTouchEvent(MotionEvent event) {

		int actionCode = event.getAction() & MotionEvent.ACTION_MASK;
		int pointerId = -1;
		SimpleTouchEvent touchEvent = null;

		List<SimpleTouchEvent> events = new ArrayList<SimpleTouchEvent>();


		switch (actionCode) {
		case MotionEvent.ACTION_MOVE:
			pointerId = event.getPointerId(0);
			float x = event.getX(0); deltaX = (int) x - startX;
			float y = event.getY(0); deltaY = (int) y - startY;
			touchEvent = new SimpleTouchEvent(pointerId, Type.POINTER_MOVE, x,
					y, deltaX, deltaY);
			events.add(touchEvent);

			break;
			// ACTION_DOWN is about the first finger down. ACTION_POINTER_DOWN is about the second finger.
		case MotionEvent.ACTION_DOWN:
			pointerId = event.getPointerId(0);
			x = event.getX(0); startX = (int) x;
			y = event.getY(0); startY = (int) y;
			touchEvent = new SimpleTouchEvent(pointerId, Type.POINTER_DOWN, x,
					y, deltaX, deltaY);
			events.add(touchEvent);
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			pointerId = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			if (event.findPointerIndex(pointerId) < 0) break; // error with three fingers
			x = event.getX(event.findPointerIndex(pointerId)); deltaX = (int) x - startX; //startX = (int) x; // 2018-04-30
			y = event.getY(event.findPointerIndex(pointerId)); deltaY = (int) y - startY; //startY = (int) y; // 2018-04-30
			touchEvent = new SimpleTouchEvent(pointerId, Type.POINTER_DOWN, x,
					y, deltaX, deltaY);
			events.add(touchEvent);
			break;
			case MotionEvent.ACTION_UP:
			pointerId = event.getPointerId(0);
			x = event.getX(0); deltaX = (int) x - startX;
			y = event.getY(0); deltaY = (int) y - startY;
			touchEvent = new SimpleTouchEvent(pointerId, Type.POINTER_UP, x, y, deltaX, deltaY);
			events.add(touchEvent);
			deltaX = 0; deltaY = 0;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			pointerId = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			if (event.findPointerIndex(pointerId) < 0) break; // error with three fingers
			// Log.d("CMBO", "************* Pointer index: " + event.findPointerIndex(pointerId));
			x = event.getX(event.findPointerIndex(pointerId)); deltaX = (int) x - startX;
			y = event.getY(event.findPointerIndex(pointerId)); deltaY = (int) y - startY;
			touchEvent = new SimpleTouchEvent(pointerId, Type.POINTER_UP, x, y, deltaX, deltaY);
			events.add(touchEvent);
			deltaX = 0; deltaY = 0;
			break;
		}

		for (int i = 0; i < event.getPointerCount(); i++) {
			int pId = event.getPointerId(i);
			float x = event.getX(i);
			float y = event.getY(i);
			if (pId != pointerId)
				events.add(new SimpleTouchEvent(pId, Type.POINTER_MOVE, x, y, deltaX, deltaY));
		}

		return events;
	}

	public void addSimpleTouchEventListener(SimpleTouchEventListener listener) {
		if (!this.listeners.contains(listener))
			this.listeners.add(listener);
	}

	public void removeSimpleTouchEventListener(SimpleTouchEventListener listener) {
		this.listeners.remove(listener);
	}

	public boolean onTouch(View v, MotionEvent event) {
		try { // try/catch put back to business on 2019-01-30 due to some few crash reports
			//logEvent(event); // Just for debugging, comment this line for release!

			Log.d(TAG, "onTouch()");

			List<SimpleTouchEvent> touchEvents = processMotionEvent(event);

			for (SimpleTouchEvent simpleTouchEvent : touchEvents)
				notifyListeners(simpleTouchEvent);
			return false;

		} catch (Exception e) {
			Log.d(TAG, "Error", e);
			return true;
		}
	}

	private void notifyListeners(SimpleTouchEvent event) {
		for (SimpleTouchEventListener listener : listeners) {
			listener.touchEvent(event);
		}
	}

	private void logEvent(MotionEvent event) {
		Log.d(TAG, "-------------------------------------");
		Log.d(TAG, "Action: " + event.getAction());
		Log.d("Filter", "Action with mask: "
				+ getActionName(event.getAction() & MotionEvent.ACTION_MASK));
		Log.d(TAG, "Action with mask: "
				+ getActionName(event.getAction() & MotionEvent.ACTION_MASK));
		Log.d(TAG, "Pointer count: " + event.getPointerCount());
		Log
				.d(
						TAG,
						"Pointer id "
								+ ((event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> 8));
		for (int i = 0; i < event.getPointerCount(); i++) {
			Log.d(TAG, "Pointer id of pointer " + i + ": "
					+ event.getPointerId(i));
			Log.d(TAG, "Location of " + i + ": " + event.getX(i) + ", "
					+ event.getY(i));
		}

	}

	protected void pushHistory(SimpleTouchEvent event) {
		int index = event.getPointerIndex();
		LinkedList<SimpleTouchEvent> pointerHistory = history.get(index);
		if (pointerHistory == null)
			history.put(event.getPointerIndex(),
					pointerHistory = new LinkedList<SimpleTouchEvent>());
		
		pointerHistory.addFirst(event);

		if (pointerHistory.size() > 5)
			pointerHistory.removeLast();
	}

	private String getActionName(int action) {
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			return "ACTION_DOWN";
		case MotionEvent.ACTION_UP:
			return "ACTION_UP";
		case MotionEvent.ACTION_MOVE:
			return "ACTION_MOVE";
		case MotionEvent.ACTION_OUTSIDE:
			return "ACTION_OUTSIDE";
		case MotionEvent.ACTION_POINTER_DOWN:
			return "ACTION_POINTER_DOWN";
		case MotionEvent.ACTION_POINTER_UP:
			return "ACTION_POINTER_UP";
		default:
			throw new IllegalArgumentException();
		}
	}

}
