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

package com.combokey.basic.view.touchevent.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.view.MotionEvent;

import com.combokey.basic.view.touchevent.SimpleTouchEvent;
import com.combokey.basic.view.touchevent.SimpleTouchEventProcessor;

public class FilteringTouchEventProcessor extends SimpleTouchEventProcessor {

	final List<SimpleTouchEventFilter> filters = new ArrayList<SimpleTouchEventFilter>();
	protected final Map<Integer, LinkedList<SimpleTouchEvent>> filteredHistory = new HashMap<Integer, LinkedList<SimpleTouchEvent>>();

	@Override
	public List<SimpleTouchEvent> processMotionEvent(MotionEvent event) {

		List<SimpleTouchEvent> touchEvents = super.processMotionEvent(event);
		List<SimpleTouchEvent> filteredEvents = new LinkedList<SimpleTouchEvent>();

		for (SimpleTouchEvent touchEvent : touchEvents) {
			List<SimpleTouchEvent> otherEvents = new LinkedList<SimpleTouchEvent>(touchEvents);
			otherEvents.remove(touchEvent);
			
			for (SimpleTouchEventFilter filter : filters) {
				touchEvent = filter
						.filter(touchEvent, otherEvents, filteredHistory, history);
			}
			filteredEvents.add(touchEvent);
		}

		for (SimpleTouchEvent filteredEvent : filteredEvents) {
			pushFilteredHistory(filteredEvent);
		}

		return filteredEvents;
	}
	
	protected void pushFilteredHistory(SimpleTouchEvent event) {
		int index = event.getPointerIndex();
		LinkedList<SimpleTouchEvent> history = filteredHistory.get(index);
		if (history == null)
			filteredHistory.put(event.getPointerIndex(),
					history = new LinkedList<SimpleTouchEvent>());

		history.addFirst(event);

		if (history.size() > 5)
			history.removeLast();
	}

	public FilteringTouchEventProcessor addFilter(SimpleTouchEventFilter filter) {
		this.filters.add(filter);
		return this;
	}

	public void remove(SimpleTouchEventFilter filter) {
		this.filters.remove(filter);
	}

}
