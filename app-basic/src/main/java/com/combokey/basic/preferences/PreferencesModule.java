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

package com.combokey.basic.preferences;

import com.combokey.basic.view.theme.DrawableStrategy;
import com.combokey.basic.view.theme.LegacyDrawableStrategy;
import com.combokey.basic.view.touchevent.SimpleTouchEventProcessor;
import com.combokey.basic.view.touchevent.filter.CoordinateStretchFilter;
import com.combokey.basic.view.touchevent.filter.CoordinateThresholdFilter;
import com.combokey.basic.view.touchevent.filter.FilteringTouchEventProcessor;

public class PreferencesModule {

	private static DrawableStrategy legacyDrawableStrategy;
	
	public Preferences providePreferences() {
		return new Preferences();
	}

	public static SimpleTouchEventProcessor getSimpleTouchEventProcessor() {
		return new FilteringTouchEventProcessor().addFilter(
				new CoordinateThresholdFilter(10f, 64, 10)).addFilter(
				new CoordinateStretchFilter(1f, 10f, 30));
	}

	public static SimpleTouchEventProcessor getSimpleTouchEventProcessorForSmallLandscape() {
		return new FilteringTouchEventProcessor().addFilter(
				new CoordinateThresholdFilter(10f, 8, 10)).addFilter(
				new CoordinateStretchFilter(1f, 5f, 15));
	}

	public static DrawableStrategy provideLegacyDrawableStrategy() {
		if (legacyDrawableStrategy == null)
			legacyDrawableStrategy = new LegacyDrawableStrategy();
			
		return legacyDrawableStrategy;
	}

}
