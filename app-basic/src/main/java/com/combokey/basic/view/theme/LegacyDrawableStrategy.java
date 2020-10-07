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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.combokey.basic.CMBOKeyboardApplication;
import com.combokey.basic.R;
/***
 * 
 * Old code copied into a strategy as part of first part of refactoring.
 * 
 * @author heikki & seppo
 *
 */
public class LegacyDrawableStrategy implements DrawableStrategy {

	private Map<String, Drawable> buttons, pressedButtons, alternateButtons,
			barLButtons, barRButtons, topButtons, images;

	private AtomicBoolean initialized = new AtomicBoolean(false);

	private void initialize() {
		Resources resources = CMBOKeyboardApplication.getApplication().getApplicationContext().getResources();

		if (buttons == null)
			buttons = new HashMap<String, Drawable>();
		if (pressedButtons == null)
			pressedButtons = new HashMap<String, Drawable>();
		if (alternateButtons == null)
			alternateButtons = new HashMap<String, Drawable>();
		if (barLButtons == null)
			barLButtons = new HashMap<String, Drawable>();
		if (barRButtons == null)
			barRButtons = new HashMap<String, Drawable>();
		if (topButtons == null)
			topButtons = new HashMap<String, Drawable>();
		if (images == null) // 2017-09
			images = new HashMap<String, Drawable>();

		buttons.put("Cool", resources.getDrawable(R.drawable.button_blue_dark3));
		alternateButtons.put("Cool",	resources.getDrawable(R.drawable.button_black_square)); //
		barLButtons.put("Cool", resources.getDrawable(R.drawable.button_black_square));
		barRButtons.put("Cool", resources.getDrawable(R.drawable.button_black_square));
		topButtons.put("Cool",
				resources.getDrawable(R.drawable.button_black_square));
		pressedButtons.put("Cool",
				resources.getDrawable(R.drawable.button_pressed_deep_yellow));
		images.put("Cool",
				resources.getDrawable(R.drawable.backgroundimage));
		images.put("Cool",
				resources.getDrawable(R.drawable.topbarimage));

		buttons.put("Basic", resources.getDrawable(R.drawable.mainbuttonbasic));
		alternateButtons.put("Basic", resources.getDrawable(R.drawable.middlebuttonbasic));
		barLButtons.put("Basic", resources.getDrawable(R.drawable.barbuttonbasic));
		barRButtons.put("Basic",	resources.getDrawable(R.drawable.barbuttonbasic));
		topButtons.put("Basic",
				resources.getDrawable(R.drawable.middlebuttonbasic));
		pressedButtons.put("Basic",
				resources.getDrawable(R.drawable.button_pressed_deep_yellow));
		images.put("Basic",
				resources.getDrawable(R.drawable.backgroundimage));
		images.put("Basic",
				resources.getDrawable(R.drawable.topbarimage));

		/* Using JSON files instead for these:
		buttons.put("Lingon", resources.getDrawable(R.drawable.button_red2));
		alternateButtons.put("Lingon",
				resources.getDrawable(R.drawable.button_red_dark2));
		barLButtons
				.put("Lingon", resources.getDrawable(R.drawable.button_red2));
		barRButtons
				.put("Lingon", resources.getDrawable(R.drawable.button_red2));
		topButtons.put("Lingon",
				resources.getDrawable(R.drawable.button_red_dark2));
		pressedButtons.put("Lingon",
				resources.getDrawable(R.drawable.button_pressed));

		buttons.put("Foliage", resources.getDrawable(R.drawable.button_green));
		alternateButtons.put("Foliage",
				resources.getDrawable(R.drawable.button_green_dark));
		barLButtons.put("Foliage",
				resources.getDrawable(R.drawable.button_green));
		barRButtons.put("Foliage",
				resources.getDrawable(R.drawable.button_green));
		topButtons.put("Foliage",
				resources.getDrawable(R.drawable.button_green_dark));
		pressedButtons.put("Foliage",
				resources.getDrawable(R.drawable.button_pressed));

		buttons.put("Healthy",
				resources.getDrawable(R.drawable.button_brown_fazer));
		alternateButtons.put("Healthy",
				resources.getDrawable(R.drawable.button_brown_fazer_dark));
		barLButtons.put("Healthy",
				resources.getDrawable(R.drawable.button_brown_fazer));
		barRButtons.put("Healthy",
				resources.getDrawable(R.drawable.button_brown_fazer));
		topButtons.put("Healthy",
				resources.getDrawable(R.drawable.button_brown_fazer_dark));
		pressedButtons.put("Healthy",
				resources.getDrawable(R.drawable.button_pressed_tin));

		buttons.put("School",
				resources.getDrawable(R.drawable.button_school_main));
		alternateButtons.put("School",
				resources.getDrawable(R.drawable.button_school_middle));
		barLButtons.put("School",
				resources.getDrawable(R.drawable.button_school_bar_left));
		barRButtons.put("School",
				resources.getDrawable(R.drawable.button_school_bar_right));
		topButtons.put("School",
				resources.getDrawable(R.drawable.button_school_top));
		pressedButtons.put("School",
				resources.getDrawable(R.drawable.button_pressed));
		*/

	}

	
	
	public Drawable getDrawable(String name, DrawableType type) {

		if (initialized.compareAndSet(false, true)) {
			initialize();
		}

		switch (type) {
		case MAIN_KEY:
			return buttons.get(name);
		case MIDDLE_KEY:
			return alternateButtons.get(name);
		case PRESSED_KEY:
			return pressedButtons.get(name);
		case LEFT_BAR_KEY:
			return barLButtons.get(name);
		case RIGHT_BAR_KEY:
			return barRButtons.get(name);
		case TOP_KEY:
			return topButtons.get(name);
		case BACKGROUND_IMAGE:
			return images.get(name);
		case TOPBAR_IMAGE:
			return images.get(name);
		default:
			return buttons.get(name);
		}
	}

}
