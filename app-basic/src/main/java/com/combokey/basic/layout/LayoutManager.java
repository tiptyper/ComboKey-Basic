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

package com.combokey.basic.layout;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

import com.combokey.basic.CMBOKey;
import com.combokey.basic.CMBOKeyboardApplication;
import com.combokey.basic.R;

public class LayoutManager implements OnSharedPreferenceChangeListener {

	private final Map<String, Layout> layouts = new LinkedHashMap<String, Layout>();

	//private Layout currentLayout; // either one, keeps changing
	private Layout currentLayout1; // main lang
	private Layout currentLayout2; // aux lang

	private boolean mainLangActive = true; // are we now using main or aux language?
	//private String previousLayoutName;
	private String previousLayout1Name;
	private String previousLayout2Name;

	public void setMainLanguageActive(boolean on) { // keep in sync with the view
		if (on) {
			mainLangActive = true;
		} else {
			mainLangActive = false;
		}
	}

	public boolean getMainLanguageActive(){
		return mainLangActive;
	}


	public void changeCharacterMapItem(int item, String string) {
		Layout current = getCurrentLayout(); // current layout
		current.changeMapItem(item, string); // current layout
	}


	public void switchLanguage() {
		if (mainLangActive) {
			mainLangActive = false;

		} else {
			mainLangActive = true;

		}
	}

	//private CMBOKeyboardView view; // only needed for one thing

	private final LayoutParser layoutParser = new LayoutParser(); // can be private?

	private String abcIndicator = "-";

	public String getAbcIndicator(int offs) {

		Layout current = getCurrentLayout();

		switch (offs) {
			case CMBOKey.NONE:
			case CMBOKey.AUXSET_MODIFIER:
				//result = current.getMapLowerCase()[mapIndex];
				abcIndicator = current.getMapLowerCase()[241];
				//if (result.equals("_abc123")) result = "_123"; // May also be set correctly in key maps
				break;
			case CMBOKey.SHIFT_MODIFIER:
			case CMBOKey.AUXSET_SHIFT_MODIFIER:
				//result = current.getMapUpperCase()[mapIndex];
				abcIndicator = current.getMapUpperCase()[241];
				//if (result.equals("_abc123")) result = "_123";
				break;
			case CMBOKey.CAPS_MODIFIER:
			case CMBOKey.AUXSET_CAPS_MODIFIER:
				//result = current.getMapCapsLock()[mapIndex];
				abcIndicator = current.getMapCapsLock()[241];
				//if (result.equals("_abc123")) result = "_abc";
				break;
			case CMBOKey.NUMBER_MODIFIER:
			case CMBOKey.AUXSET_NUMBER_MODIFIER:
				//result = current.getMapNumbers()[mapIndex];
				abcIndicator = current.getMapNumbers()[241];
				//if (result.equals("_abc123")) result = "_abc";
				break;
			case CMBOKey.SYMBOL_MODIFIER:
			case CMBOKey.AUXSET_SYMBOL_MODIFIER:
				//result = current.getMapSymbols()[mapIndex];
				abcIndicator = current.getMapSymbols()[241];
				//if (result.equals("_abc123")) result = "_123";
				break;
			case CMBOKey.EMOJI_MODIFIER:
				//result = current.getMapEmojis()[mapIndex];
				abcIndicator = current.getMapEmojis()[241];
				//if (result.equals("_abc123")) result = "_abc";
				break;
			case CMBOKey.FN_MODIFIER:
				//result = current.getMapEmojis()[mapIndex];
				abcIndicator = current.getMapMore()[241];;
				//if (result.equals("_abc123")) result = "_abc";
				break;
			default:
				abcIndicator = current.getMapLowerCase()[241];
				//result = current.getMapLowerCase()[mapIndex];
		}


		return abcIndicator;
	}

	/*
	public String getLowerCaseIndicator(boolean aux) {
		return aux ? currentLayout2.getMapLowerCase()[241] : currentLayout1.getMapLowerCase()[241];
	}
	public String getUpperCaseIndicator(boolean aux) {
		//return layoutParser.getUpperCaseIndicator()[241];
		return aux ? currentLayout2.getMapUpperCase()[241] : currentLayout1.getMapUpperCase()[241];
	}
	public String getCapsIndicator(boolean aux) {
		//return layoutParser.getCapsIndicator()[241];
		return aux ? currentLayout2.getMapCapsLock()[241] : currentLayout1.getMapCapsLock()[241];
	}
	public String getNumbersIndicator(boolean aux) {
		//return layoutParser.getNumbersIndicator()[241];
		return aux ? currentLayout2.getMapNumbers()[241] : currentLayout1.getMapNumbers()[241];
	}
	public String getSymbolsIndicator(boolean aux) {
		//return layoutParser.getSymbolsIndicator()[241];
		return aux ? currentLayout2.getMapSymbols()[241] : currentLayout1.getMapSymbols()[241];
	}

	*/

	public LayoutManager() {
		Context context = CMBOKeyboardApplication.getApplication()
				.getApplicationContext();

		PreferenceManager.getDefaultSharedPreferences(context)
				.registerOnSharedPreferenceChangeListener(this);
	}

	public void loadAvailableLayouts() {
		// TODO Not here
		Context context = CMBOKeyboardApplication.getApplication()
				.getApplicationContext();

		Log.i("CMBO", "layoutParser " + this.layoutParser);
		Log.i("-MEDIA-TIMER", "Loading available layouts");

		try {
			String[] layoutAssets = context.getAssets().list("layouts");
			Log.d("-CMBO-TIMER", "Start loading " + layoutAssets.length + " assets of layouts");

			for (String layoutAsset : layoutAssets) {
				Log.i("CMBO", "Loading layouts from " + layoutAsset); // layoutAsset = e.g. gkos_layout_english_optimized.json
				Layout layout = layoutParser.getLayout(context, "layouts/"
						+ layoutAsset);
				if (layout != null) {
					Log.d("CMBO", "Storing layout " + layout);
					layouts.put(layout.toString(), layout);
				}
			}
			Log.d("-CMBO-TIMER", "Finished loading " + layoutAssets.length + " assets of layouts");

			File storageDirectory = CMBOKeyboardApplication.getApplication()
					.getStorageDirectory();
			if (storageDirectory != null) {
				File layoutDir = new File(storageDirectory, "layouts/");
				if (layoutDir.mkdirs() || layoutDir.isDirectory()) {
					Log.d("-MEDIA-TIMER", "Looking for external language layout files in "
							+ layoutDir.getAbsolutePath());
					String[] files = layoutDir.list(new FilenameFilter() {
						public boolean accept(File file, String name) {
							return name.matches(".*\\.json");
						}
					});
					if (files != null) { // 4/2020
						for (String fileName : files) { // NullPointerException may occur here 4/2020 (did in Plus version v5.1)
							File layoutFile = new File(layoutDir, fileName);
							
							Log.d("-MEDIA-TIMER", "Loading layouts from " + layoutFile.getAbsolutePath());
							
							Layout layout = layoutParser.getLayout(context, layoutFile);
							if (layout != null) {
								Log.d("CMBO", "Storing layout " + layout);
								layouts.put(layout.toString(), layout);
							}
						}
					} else { // 4/2020
						Log.e("-MEDIA-TIMER", "Language JSON files not accessible yet!");
					}	
				}
			} else {
				Log.d("-MEDIA-TIMER", "No layout external storage directory available.");
			}
		} catch (IOException e) {
			Log.e("-MEDIA-TIMER", "Error accessing layouts directory!");
			// TODO notification?
		}
	}


	public String getStringRepresentation(int modifier, int state, int key) {

		int chord = state + key;
		String result = "";

		if (!CMBOKey.isValidChord(modifier, state, key))
			throw new IllegalArgumentException("Invalid modifier and chord: "
					+ modifier + " " + chord);

		int offset = modifier; // local variable

		Layout current = getCurrentLayout();

		//boolean aMode = CMBOKeyboardApplication.getApplication().getCMBOManager()
		//		.getKeyboard().getAuxMode();

		Log.i("CMBO", "Getting string representation extended: modifier(offset) = " + modifier + ", chord = " + chord + " (state " + state + ", key " + key + ")");
		// -------- zxcvbn

		int stateIndex = CMBOKey.getIndexForChord(state);
		int keyIndex = CMBOKey.getIndexForChord(key);
/*
		// --------------------
		// TODO: prepare for showing mirrored symbols on same column in yellow color
		if(CMBOKey.areOnSameColumn(stateIndex, keyIndex)){ // 2018-12-12
			//result = "X";
			if (CMBOKey.isOnRightColumn(stateIndex)) {
			    stateIndex =- 1;
			    keyIndex =- 0;
            }
			if (CMBOKey.isOnLeftColumn(stateIndex)) {
			    stateIndex =+ 2;
			    keyIndex =+ 0;
            }
		}
		// --------------------
*/
		int mapIndex = stateIndex * 15 + keyIndex;

		if (keyIndex == 0) mapIndex = stateIndex; // single tap, no swipe

		//if (stateIndex > 0) mapIndex = (stateIndex - 1) * 15 + keyIndex;

		Log.i("-STRING", "Got string  " + result);

		//Log.i("-MAPS", "Getting string from Maps. stateIndex = " + stateIndex + ", keyIndex = " + keyIndex + ", mapIndex = " + mapIndex);

			switch (offset) {
				case CMBOKey.NONE:
				case CMBOKey.AUXSET_MODIFIER:
					result = current.getMapLowerCase()[mapIndex];
					abcIndicator = current.getMapLowerCase()[241];
					if (result.equals("_abc123")) result = "_123"; // May also be set correctly in key maps
					break;
				case CMBOKey.SHIFT_MODIFIER:
				case CMBOKey.AUXSET_SHIFT_MODIFIER:
					result = current.getMapUpperCase()[mapIndex];
					abcIndicator = current.getMapUpperCase()[241];
					if (result.equals("_abc123")) result = "_123";
					break;
				case CMBOKey.CAPS_MODIFIER:
				case CMBOKey.AUXSET_CAPS_MODIFIER:
					result = current.getMapCapsLock()[mapIndex];
					abcIndicator = current.getMapCapsLock()[241];
					if (result.equals("_abc123")) result = "_abc";
					break;
				case CMBOKey.NUMBER_MODIFIER:
				case CMBOKey.AUXSET_NUMBER_MODIFIER:
					result = current.getMapNumbers()[mapIndex];
					abcIndicator = current.getMapNumbers()[241];
					if (result.equals("_abc123")) result = "_abc";
					break;
				case CMBOKey.SYMBOL_MODIFIER:
				case CMBOKey.AUXSET_SYMBOL_MODIFIER:
					result = current.getMapSymbols()[mapIndex];
					abcIndicator = current.getMapSymbols()[241];
					if (result.equals("_abc123")) result = "_123";
					break;
				case CMBOKey.EMOJI_MODIFIER:
					result = current.getMapEmojis()[mapIndex];
					abcIndicator = current.getMapEmojis()[241];
					if (result.equals("_abc123")) result = "_abc";
					break;
				case CMBOKey.FN_MODIFIER:
					result = current.getMapMore()[mapIndex];
					abcIndicator = current.getMapMore()[241];
					if (result.equals("_abc123")) result = "_abc";
					break;
				default:
					abcIndicator = current.getMapLowerCase()[241];
					result = current.getMapLowerCase()[mapIndex];
			}


		//Log.i("-MAPS", "Getting string from Maps. stateIndex = " + stateIndex + ", keyIndex = " + keyIndex + ", mapIndex = " + mapIndex + ", result = " + result);

		// -------

		if (result.length() == 1) return result; // just a single character, no action

		// ------- Several characters: -----------

		if (isTrailingSpaceRemoved() && result.endsWith(" ")) { // Keep trailing space in shortcut words?
			result = result.substring(0, result.length() - 1);
			return  result;
		}

		if (result.endsWith("]") && result.length() > 1) { // examples "[]", "[1]", "[ö]", "[Ö]", ...
			result = ""; // This is the way to clear or mark keymap positions on JSON layout files
			return result; // to facilitate easier finding of locations of native characters
		}

		if (isLangComboDisabled() && result.equals("_Lang")) {
			result = ""; // No quick lang change
			return result;
		}

		if (result.startsWith("_") || result.startsWith("[")) { // No close swipe to change Mode (123, abc, Symb, Emoji)

			if ( isEasyModeChangeDisabled() && (((state == 40) || (state == 3) || (state == 24)) && (key == 7))) { // No quick Mode change
				result = "";
			} else if (result.startsWith("[")) { // length is always > 1 if you end up here
				//view.setHidden(true); // not used for the moment
				//CMBOKeyboardView
				result = result.substring(1); // [X becomes X etc., [ = hide symbol but keep it functional
			}
		}

		return result;
	}

	public boolean isLangComboDisabled() { // no switch between main and aux languages by key pad combo
		return CMBOKeyboardApplication.getApplication().getPreferences().isLangComboDisabled();
	}

	public boolean isTrailingSpaceRemoved() { // no switch between main and aux languages by key pad combo
		return CMBOKeyboardApplication.getApplication().getPreferences().isTrailingSpaceRemoved();
	}

	public boolean isEasyModeChangeDisabled() { // no switch between modes by short combos/swiped
		return CMBOKeyboardApplication.getApplication().getPreferences().isEasyModeChangeDisabled();
	}


	private void logResult(String result) {
		Log.i("-STRING", "result = " + result);
	}

	//public int getGkosRef() {return this.gkosRef;}

	public Layout getCurrentLayout() { // load layout for each of the two languages

		//boolean mainLang = true;

		if (layouts.isEmpty()) { // no layouts available?
			loadAvailableLayouts();
		}

		// Not to be loaded every time asked, only when changed or empty:

			if ((currentLayout1 == null) || (!previousLayout1Name.equals(getCurrentLayout1Name()))) {
				currentLayout1 = layouts.get(getCurrentLayout1Name());
				previousLayout1Name = getCurrentLayout1Name();
				Log.i("-LANG", "Load Current layout1 = " + currentLayout1);
			}

			if ((currentLayout2 == null) || (!previousLayout2Name.equals(getCurrentLayout2Name()))) {
				currentLayout2 = layouts.get(getCurrentLayout2Name());
				previousLayout2Name = getCurrentLayout2Name();
				Log.i("-LANG", "Load Current layout2 = " + currentLayout2);
			}

		if (mainLangActive) { // Main language active

				return currentLayout1;

		} else { // Aux language active

				return currentLayout2;
		}

	}


	public Layout getCurrentLayout2() {

		if (layouts.isEmpty()) {
			loadAvailableLayouts();
		}
		if (currentLayout2 == null) {
			currentLayout2 = layouts.get(getCurrentLayout2Name());
			Log.i("-LANG", "Current layout2 " + currentLayout2);
		}
		return currentLayout2;
	}
	public Layout getCurrentLayout1() {

		if (layouts.isEmpty()) {
			loadAvailableLayouts();
		}
		if (currentLayout1 == null) {
			currentLayout1 = layouts.get(getCurrentLayout1Name());
			Log.i("-LANG", "Current layout1 " + currentLayout1);
		}
		return currentLayout1;
	}



	private String getCurrentLayout1Name() {
		String currentLayout1Name = CMBOKeyboardApplication.getApplication()
				.getPreferences().get()
				.getString("PREFERENCES_LANGUAGE"
				, CMBOKeyboardApplication.getApplication().getResources()
								.getString(R.string.main_language_defaultvalue_in_english) + "_Optimized");

		if (!layouts.keySet().contains(currentLayout1Name)) {
			currentLayout1Name = "English_Optimized";
		}

		Log.i("-LANG", "Current layout1 name = " + currentLayout1Name);
		return currentLayout1Name;
	}


	public String getMainLanguageName() {
		return currentLayout1.getValue(Layout.Metadata.NAME);
	}

	public String getAuxLanguageName() {
		return currentLayout2.getValue(Layout.Metadata.NAME);
	}

	public String getMainLanguageDescription() {
		return currentLayout1.getValue(Layout.Metadata.DESCRIPTION);
	}

	public String getAuxLanguageDescription() {
		return currentLayout2.getValue(Layout.Metadata.DESCRIPTION);
	}


	private String getCurrentLayout2Name() {
		String currentLayout2Name = CMBOKeyboardApplication.getApplication()
				.getPreferences().get()
				.getString("PREFERENCES_LANGUAGE2"
						, CMBOKeyboardApplication.getApplication().getResources()
								.getString(R.string.aux_language_defaultvalue_in_english) + "_Optimized");

		if (!layouts.keySet().contains(currentLayout2Name)) {
			currentLayout2Name = "Greek_Optimized";
		}

		Log.i("-LANG", "Current layout2 name = " + currentLayout2Name);
		return currentLayout2Name;
	}



	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

        if ("PREFERENCES_LANGUAGE".equals(key)) {
			reloadCurrentLayout();
			CMBOKeyboardApplication.getApplication().getCMBOManager()
					.getKeyboard().checkDevanagariEtc();
		}
		if ("PREFERENCES_PADPOSITION".equals(key)) {
//			CMBOKeyboardView.regenerateViewKludge = true;
		}
	}

	private void reloadCurrentLayout() {
		this.currentLayout1 = null; // main
		this.currentLayout2 = null; // aux
		getCurrentLayout(); // load both
	}

	public Collection<Layout> getLayouts() {
		return this.layouts.values();
	}

}
