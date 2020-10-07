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

public class CoordinateThresholdFilter implements SimpleTouchEventFilter {

	private float threshold;
	private int yChange, yDiff;

	public CoordinateThresholdFilter(float threshold, int yChange, int yDiff) {
		this.threshold = threshold;
		this.yChange = yChange;
		this.yDiff = yDiff;
	}

	public SimpleTouchEvent filter(SimpleTouchEvent event,
			List<SimpleTouchEvent> otherEvents,
			Map<Integer, LinkedList<SimpleTouchEvent>> history,
			Map<Integer, LinkedList<SimpleTouchEvent>> unfilteredHistory) {

		if (event.getType() != SimpleTouchEvent.Type.POINTER_MOVE) {
			return event;
		}

		if (otherEvents.isEmpty()) {
			return event;
		}

		float x = event.getX();
		float y = event.getY();

		int deltaX = event.getDeltaX();
		int deltaY = event.getDeltaY();

		LinkedList<SimpleTouchEvent> previous = history.get(event
				.getPointerIndex());

		if (previous != null) {

			float previousX = previous.getFirst().getX();
			float previousY = previous.getFirst().getY();

			for (SimpleTouchEvent e : otherEvents) {

				float otherX = e.getX();
				float otherY = e.getY();

				// Filter X

				if ((Math.abs(x - otherX) < threshold)
						&& Math.abs(x - previousX) > 100) {
					x = previousX;
				}

				//x = 100; // TEST, no effect

				// Filter Y

				if (Math.abs(y - previousY) > yChange
						&& Math.abs(y - otherY) < yDiff) {
					y = previousY;
				}

				//y = 100; // TEST, no effect

				event = new SimpleTouchEvent(event.getPointerIndex(), event
						.getType(), x, y, deltaX, deltaY);
			}
		}

		return event;

	}
}
