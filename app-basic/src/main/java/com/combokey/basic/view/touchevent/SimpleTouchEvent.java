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

public class SimpleTouchEvent {

	private final int pointerIndex;

	private final Type type;
	private final long time;
	private final float x;
	private final float y;
	private final int deltaX; // how long was the swipe
	private final int deltaY;

	public enum Type {
		POINTER_DOWN, POINTER_UP, POINTER_MOVE
	}

	public SimpleTouchEvent(int pointerIndex, Type type, float x, float y, int deltaX, int deltaY) {
		this.time = System.nanoTime();
		this.pointerIndex = pointerIndex;
		this.type = type;
		this.x = x;
		this.y = y;
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}

	public int getPointerIndex() {
		return pointerIndex;
	}

	public Type getType() {
		return type;
	}

	public float getX() {
		return x;
	}
	public float getY() {
		return y;
	}

	public int getDeltaX() {
		return deltaX;
	}
	public int getDeltaY() {
		return deltaY;
	}

	public long getTime() {
		return this.time;
	}

}
