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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.combokey.basic.view.touchevent.SimpleTouchEvent;

public class CoordinateStretchFilter implements SimpleTouchEventFilter {

	private final float min, max;
	private final int delta;

	public CoordinateStretchFilter(float min, float max, int delta) {
		this.max = max;
		this.min = min;
		this.delta = delta;
	}

	public SimpleTouchEvent filter(SimpleTouchEvent event,
			List<SimpleTouchEvent> otherEvents,
			Map<Integer, LinkedList<SimpleTouchEvent>> history,
			Map<Integer, LinkedList<SimpleTouchEvent>> unfilteredHistory) {

		if (otherEvents.isEmpty()
				|| event.getType() == SimpleTouchEvent.Type.POINTER_UP)
			return event;

		float y = event.getY();
		float x = event.getX();

		int deltaX = event.getDeltaX();
		int deltaY = event.getDeltaY();

		LinkedList<SimpleTouchEvent> previous = history.get(event
				.getPointerIndex());

		if (previous != null) {

			for (SimpleTouchEvent e : otherEvents) {
				float otherY = e.getY();
				// Original min and max values 1 and 10
				if (Math.abs(y - otherY) > min && Math.abs(y - otherY) < max) { // Original
					if (y < otherY) {

						y -= delta;
						y = Math.abs(y);
					} else if (y > otherY) {
						y += delta;
					}

					event = new SimpleTouchEvent(event.getPointerIndex(), event
							.getType(), x, y, deltaX, deltaY);
				}
			}
		}

		return event;

	}

}
