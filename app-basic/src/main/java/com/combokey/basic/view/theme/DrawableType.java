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

package com.combokey.basic.view.theme;

public enum DrawableType {
	
	MAIN_KEY("mainbutton"), MIDDLE_KEY("middlebutton"), LEFT_BAR_KEY("leftbarbutton"),
			RIGHT_BAR_KEY("rightbarbutton"), TOP_KEY("centerbutton"), PRESSED_KEY("pressedbutton"),
			BACKGROUND_IMAGE("backgroundimage"), TOPBAR_IMAGE("topbarimage");
			//, TEXT_BACKGROUND("textbackground"), PAD_BACKGROUND("padbackground");
	
	private final String key;
	
	DrawableType(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return this.key;
	}
}
