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

package com.combokey.basic.view;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Rect;
import android.util.Log;

import com.combokey.basic.CMBOKeyboard;
import com.combokey.basic.CMBOKeyboardApplication;


public class CMBOKeyboardButtonLayout {

	private List<CMBOButton> buttons = new ArrayList<CMBOButton>();
	private int width, height;
	private int gridWidth, gridHeight;

	//private CMBOKeyboardController controller;
	private CMBOKeyboard keyboard = CMBOKeyboardApplication.getApplication().getCMBOManager()
				.getKeyboard();

	public CMBOKeyboardButtonLayout(int width, int height, int gridWidth,
									int gridHeight) { // gridWidth = number of buttons per line
		this.width = width;
		this.height = height;
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
	}

	public void addButton(CMBOButton button) {
		this.buttons.add(button);
	}

	public CMBOButton addButton(int gridX, int gridY, int xSpan, int ySpan) {
		int xPos = this.width / gridWidth * gridX + 20; // + 10
		int yPos = this.height / gridHeight * gridY + 10; // + 10
		int hSize = this.width / gridWidth * xSpan;
		int vSize = this.height / gridHeight * ySpan;

		buttonHeight = vSize; // for hysteresis
		buttonWidth = hSize;

		Rect hitBox = new Rect(xPos, yPos, xPos + hSize, yPos + vSize);

		CMBOButton button = new CMBOButton(new Rect(hitBox), hitBox);

		this.buttons.add(button);
		//Log.d("CMBO", "**** Button added: " + button.getHitBox() + " = " + button);
		return button;
	}


	public List<CMBOButton> getButtons() {
		return this.buttons;
	}


	public CMBOButton getHitKeyORIG(int x, int y) { // original (no hysteresis)

		Log.d("CMBO", "**** Buttons on the list (size): " + buttons.size() + " = " + buttons);
		int i = 0;
		for (CMBOButton button : buttons) {
			// Log.d("CMBO", "**** Button checked (hitBox/Id/visibleRect/button): " + button.getHitBox() + " / " + button.getId() + " / " + button.getVisibleRect() + " / " + buttons.get(i));
			i += 1;
			Log.d("CMBO", "**** Button checked " + i);
			if (button.getHitBox().contains(x, y))
				return button;
		}
		return null;
	}

	// ------ hysteresis: ------------- take this into use when ready

	// hysteresis, check if x, y within same button area
	private int buttonHeight;
	private int buttonWidth;
	private boolean overDelta = true;
	private boolean overDeltaReached = false;

	//private double hystAmount = 0.3; // amount of hysteresis (part of button height)

	public CMBOButton getHitKey(int x, int y, boolean oneDown, boolean twoDown, int dX, int dY) { // to-be NEW with hysteresis

		// Only use hysteresis when sliding with one finger, not when using two fingers.
		// Using oneDown info for this.
		// There is no hysteresis when swiping with second finger but
		// could be useful *after* the second button has been pressed

		Log.d("-DELTA", ">> DELTA X and Y = " + dX + ", " + dY + ", x/y = " + x + "/" + y);

		//boolean oneDown = controller.oneDown();

		// --------------- First swipe length ---------------

		int deltaX = Math.abs(dX);
		int deltaY = Math.abs(dY);

		if (twoDown) { overDelta = true; overDeltaReached = true; } // two fingers involved, forget swipe span

		overDelta = false;

		if (deltaX < 2 && deltaY < 2) {
			overDelta = true;
			overDeltaReached = false;
			// first value will be zero
		}
		// First swipe must be long enough to be a real swipe, not just a slip to neighbouring button
		if ((deltaX > 0.2 * buttonWidth) || (deltaY > 0.25 * buttonHeight)) {
			overDelta = true;
			overDeltaReached = true;
		}

		// ----------- Hysteresis--------------

		double hystAmount = CMBOKeyboardApplication.getApplication()
				.getPreferences().getHystAmount();

		// 0, 15, 20, 30 = hysteresis percent of button measures, values in preferences/settings
		int hystY = (int) ((hystAmount/100) * buttonHeight);
		int hystX = (hystY/2);

		// Principle:
		// Turn on hysteresis when a second button area is entered
		//   either by a second finger press or by a swipe
		// In addition, the first swipe span must be at least 0,7 times the button size

		boolean hystOn = keyboard.hystOn; // restore saved value
		int previousListButton = keyboard.previousListButton; // -"-

		//Log.d("-HYST", "===== hystON = " + hystOn + " to start with. =====");
		Log.d("-HYST", ">> (0) hystON = " + hystOn + ". i = " + previousListButton + ", overDelta = " + overDelta);

		if (!hystOn) {
			// Hysteresis OFF to start with: just entering the button area from no key pressed state
			int i = 0;
			for (CMBOButton button : buttons) {
				// Log.d("CMBO", "**** Button checked (hitBox/Id/visibleRect/button): " + button.getHitBox() + " / " + button.getId() + " / " + button.getVisibleRect() + " / " + buttons.get(i));
				i += 1;
				Log.d("-HYST", "**** Button checked without hysteresis. " + button.getHitBox());
				if (button.getHitBox().contains(x, y) && (overDelta || overDeltaReached)) {
					Log.d("-HYST", ">> (1) hystON = " + hystOn + ". i = " + previousListButton);
					if ((previousListButton != i) && (previousListButton != 0)){
						keyboard.hystOn = true; // save
						hystOn = true;
						Log.d("-HYST", "==== Hysteresis turned ON ====");
					}
					previousListButton = i;
					button = buttons.get(i - 1); Log.d("-DELTA", "Button ID = " + button.getId() + " (hystOFF to ON)");

					keyboard.previousListButton = previousListButton; // save
					//Log.d("-HYST", "**** previousListButton set to: " + previousListButton + ", hystOn = " + hystOn);
					Log.d("-HYST", ">> (2) hystON = " + hystOn + ". i = " + previousListButton);
					return button;
				}
			}
			Log.d("-HYST", "**** Return null (hystOn was false)");
			//return buttons.get(keyboard.previousListButton - 1);
			Log.d("-DELTA", "Button ID = " + null + " (hystOFF)");
			return null;

		} else { // Hysteresis ON already: Moving to next button area with hysteresis

			int i = 0;
			for (CMBOButton button : buttons) { // found with hysteresis?
				i += 1;
				//Log.d("_HYST", "**** Button checked with hysteresis. hysteresis (y) = " + hystY);
				Log.d("-HYST", ">> (3) hystON = " + hystOn + ". i = " + previousListButton);
				if ((button.getHitBox().contains(x + hystX, y + hystY))
						&& (button.getHitBox().contains(x - hystX, y - hystY))
						&& (overDelta || overDeltaReached)) {
					previousListButton = i;
					keyboard.previousListButton = i; // save
					button = buttons.get(i - 1); Log.d("-DELTA", "Button ID = " + button.getId() + " (hystON)");
					Log.d("-HYST", "**** (hystON) Found.****");
					Log.d("-HYST", ">> (4) hystON = " + hystOn + ". i = " + previousListButton);
					return button;
				}
			}
			// if not found with hysteresis, use previous button
			CMBOButton button = buttons.get(keyboard.previousListButton - 1); Log.d("-DELTA", "Button ID = " + button.getId() + " (previous button)");
			previousListButton = keyboard.previousListButton;
			Log.d("-HYST", "**** (hystOn) Not found.");
			Log.d("-HYST", ">> (5) hystON = " + hystOn + ". i = " + previousListButton);
			Log.d("-DELTA", "Button ID = " + button.getId() + " (previous button)");
			return button;
		}

	}

	public void clearPreviousListButton() {
		overDeltaReached = false;
		overDelta = false;
		keyboard.hystOn = false; // save
		keyboard.previousListButton = 0;
		Log.d("-HYST", "==== Hysteresis turned OFF ====");
	}

	// ---------- end of hysteresis ----


	public void listButtons() {
		int i = 0;
		for (CMBOButton button : buttons) {
			Log.d("CMBO", "**** Button on the list (hitBox/Id/visibleRect): " + button.getHitBox() + " / " + button.getId() + " / " + button.getVisibleRect() + " / " + buttons.get(i));
		i += 1;
		}
	}

	// -------------



    // -----------------

	public void clearButtons() { // 2017
		int i = 0;
		for (CMBOButton button : buttons) { // toimii kyllä mutta ei auta entisten nappuloiden poistoon!
			Log.d("CMBO", "**** Button to clear (hitBox/Id/visibleRect/button): " + button.getHitBox() + " / " + button.getId() + " / " + button.getVisibleRect() + " / " + buttons.get(i));
			button.clearButton();
			i += 1;
			//Log.d("CMBO", "**** Button set to NONE: " + button);
		}
	}

	public void clearHitBoxes() { // 2017
		for (CMBOButton button : buttons) {
			button.getHitBox().set(0, 0, 0, 0);
		}
	}
	
	public CMBOButton getButton(int key) {
		for (CMBOButton button : buttons) {
			if (button.getId() == key)
				return button;
		}
		
		return null;
	}

}