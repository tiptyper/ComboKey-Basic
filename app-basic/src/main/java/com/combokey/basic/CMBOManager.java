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
package com.combokey.basic;

import com.combokey.basic.layout.LayoutManager;
import com.combokey.basic.preferences.Preferences;
import com.combokey.basic.preferences.PreferencesModule;
import com.combokey.basic.view.theme.ThemeManager;
import com.combokey.basic.view.touchevent.SimpleTouchEventProcessor;

public class CMBOManager {
	private Preferences preferences = new Preferences();
	private CMBOKeyboardController controller = new CMBOKeyboardController();
	private CMBOKeyboard keyboard = new CMBOKeyboard();
	private CMBOWordCandidates candidates = new CMBOWordCandidates();
	private LayoutManager layoutManager;
	private ThemeManager themeManager = new ThemeManager();
	private SimpleTouchEventProcessor simpleTouchEventProcessor = PreferencesModule
			.getSimpleTouchEventProcessor();
	private SimpleTouchEventProcessor simpleTouchEventProcessorForSmallLandscape = PreferencesModule
			.getSimpleTouchEventProcessorForSmallLandscape();
	public SimpleTouchEventProcessor getTouchEventProcessor(
			boolean smallLandscape) {
		return smallLandscape ? simpleTouchEventProcessorForSmallLandscape
				: simpleTouchEventProcessor;
	}
	public CMBOKeyboardController getController() {
		return this.controller;
	}
	public Preferences getPreferences() {
		return this.preferences;
	}
	public CMBOKeyboard getKeyboard() {
		return this.keyboard;
	}
	public CMBOWordCandidates getCandidates() {
		return this.candidates;
	}
	public LayoutManager getLayoutManager() {
		if (layoutManager == null) {
			layoutManager = new LayoutManager();
		}
		return layoutManager;
	}
	public ThemeManager getThemeManager() {
		return themeManager;
	}
}
