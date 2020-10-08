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

import com.combokey.basic.CMBOKeyboardController;

public class CMBOButton {

	private Rect visibleRect;
	private Rect hitBox;
	//private Rect hitBoxPlus; // scaled hitbox for hysteresis
	private int id;

	private final boolean highliteAdjacentButtons = false; // 2017-09


	private final List<CMBOButton> adjacentButtons = new ArrayList<CMBOButton>();

	public CMBOButton(Rect visibleRect, Rect hitBox) {
		this.visibleRect = visibleRect;
		this.hitBox = hitBox;
	}

	public Rect getHitBox() {
		return this.hitBox;
	}

	public Rect getHitBoxPlus(double scale) {
		// get enlarged hitBox for hysteresis management (e.g. 1.2 = lager)
		int boxHeight = (this.hitBox).bottom - (this.hitBox).top;
		int boxHeightDelta = (int) ((scale - 1) * boxHeight);

		return new Rect((int) (this.hitBox).left - boxHeightDelta,
						(int) (this.hitBox).top - boxHeightDelta,
						(int) (this.hitBox).right + boxHeightDelta,
						(int) (this.hitBox).bottom + boxHeightDelta);
	}


	public Rect getVisibleRect() {
		return this.visibleRect;
	}

	public int getId() {
		return this.id;
	}

	public CMBOButton setId(int id) {
		this.id = id;
		return this;
	}

	public CMBOButton modifyVisibleSize(double top, double bottom, double left,
										double right) {
		visibleRect = new Rect(hitBox);
		visibleRect.top -= (int) (top * visibleRect.height());
		visibleRect.bottom += (int) (bottom * visibleRect.height());
		visibleRect.left -= (int) (left * visibleRect.width());
		visibleRect.right += (int) (right * visibleRect.width());
		return this;
	}

	public CMBOButton extendHitBoxBottom(double extension) { // 2020 Needed in some devices (e.g. Nokia 6)
		hitBox = new Rect(visibleRect); // the bottom keys k/m/r need more hit box area at the bottom line
		hitBox.bottom += (int) extension;
		return this;
	}


	public CMBOButton clearButton() { // 2017
		this.visibleRect = new Rect(0,0,0,0);
		this.hitBox = new Rect(0,0,0,0);
		this.id = 0;
        visibleRect = new Rect(0,0,0,0);
        visibleRect.top = 0;
        visibleRect.bottom = 0;
        visibleRect.left = 0;
        visibleRect.right = 0;
        hitBox = visibleRect;
        return this;
	}

	public CMBOButton modifyVisibleSize(double top, double bottom) {
		return this.modifyVisibleSize(top, bottom, 0, 0);
	}

	public CMBOButton modifyVisibleSizePixels(int top, int bottom, int left,
											  int right) {
		visibleRect.top -= top;
		visibleRect.bottom += bottom;
		visibleRect.left -= left;
		visibleRect.right += right;
		return this;
	}

	public CMBOButton modifyVisibleSizePixels(int top, int bottom) {
		return this.modifyVisibleSizePixels(top, bottom, 0, 0);
	}

	public List<CMBOButton> getAdjacentButtons() {
		return adjacentButtons;
	}

	private CMBOKeyboardController controller;


	public CMBOButton addAdjacent(CMBOButton... buttons) {

		//if (!( (state == 7) || (state == 56) )) { // for slides not from space and backspace
		if (highliteAdjacentButtons) { // for slides not from space and backspace

			for (CMBOButton b : buttons) {
				this.adjacentButtons.add(b);
			}
		}

		return this;
	}


	public CMBOButton addAdjacent(CMBOButton adjacentButton) {
		this.adjacentButtons.add(adjacentButton);
		return this;
	}

}