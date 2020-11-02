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

import com.combokey.basic.CMBOKeyboardApplication;

import com.combokey.basic.R;
import com.combokey.basic.view.CMBOKeyboardView;
import com.combokey.basic.view.theme.ThemeManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.preference.PreferenceManager;
import android.util.Log;

public class Preferences implements OnSharedPreferenceChangeListener {

	// Remember: You need to have same defaults for preferences in file preferences.xml as here below!

	private Context context;

	private final String PREFERENCES_STT_DIRECT = "PREFERENCES_STT_DIRECT";

	public SharedPreferences get() {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void register(Context context) {
		PreferenceManager.getDefaultSharedPreferences(context)
				.registerOnSharedPreferenceChangeListener(this);
		this.context = context;
	}

	public int getHighlightOption() {
		String PREFERENCES_HIGHLIGHT_OPTION = "PREFERENCES_HIGHLIGHT_OPTION";
		return Integer.parseInt(get().getString(PREFERENCES_HIGHLIGHT_OPTION, "1")); // 0 = legacy, 1 = strong highlight
	}

	public int getFontSize() {
		String PREFERENCES_FONT_SIZE = "PREFERENCES_FONT_SIZE";
		return Integer.parseInt(get().getString(PREFERENCES_FONT_SIZE, "28")); // 12 20 28 36 = Small Medium Large Huge
	}

	public int getFontType() {
		String PREFERENCES_FONT_TYPE = "PREFERENCES_FONT_TYPE";
		return Integer.parseInt(get().getString(PREFERENCES_FONT_TYPE, "0")); // 0 = default, 1 = OpenDyslexic
	}

	public int getSoundVolume() {
		String PREFERENCES_SOUND_VOLUME = "PREFERENCES_SOUND_VOLUME";
		return Integer.parseInt(get().getString(PREFERENCES_SOUND_VOLUME, "15")); // 0 to 100
	}

	public int getHystAmount() {
		String PREFERENCES_HYST_AMOUNT = "PREFERENCES_HYST_AMOUNT";
		return Integer.parseInt(get().getString(PREFERENCES_HYST_AMOUNT, "15")); // 0, 15, 20, 30 = hysteresis percent
	}
	public int getTerminalModeSetting() {
		String PREFERENCES_TERMINAL_MODE = "PREFERENCES_TERMINAL_MODE";
		return Integer.parseInt(get().getString(PREFERENCES_TERMINAL_MODE, "0")); // 0, 1, 2 = auto, ON, OFF
	}
	public int getLandscapeColumns() { // padType in landscape orientation: 5 = 3-row, 3 = 5-row, 6 = double 5-row
		// Changed to: 3 = Normal single pad,  6 = double (=split) pad
		String PREFERENCES_LANDSCAPECOLUMNS = "PREFERENCES_LANDSCAPECOLUMNS";
		return Integer.parseInt(get().getString(PREFERENCES_LANDSCAPECOLUMNS, "3"));
	}
	public int getPadPosition() {
		String PREFERENCES_PADPOSITION = "PREFERENCES_PADPOSITION";
		return Integer.parseInt(get().getString(PREFERENCES_PADPOSITION, "2")); // 0 = left, 1/3 = middle (wide/narrow), 2 = right
	}
	public void setPadPosition(String position) {
		Editor e = get().edit();
		e.putString("PREFERENCES_PADPOSITION", position);
		//e.commit();// write into persistent memory immediately
		e.apply(); // write handled in the background (2017-10)
	}
	public void setPadType(String padtype) {
		Editor e = get().edit();
		e.putString("PREFERENCES_PADTYPE", padtype);
		e.apply();
	}
	public void setPadHeight(String padheight) {
		Editor e = get().edit();
		e.putString("PREFERENCES_PADHEIGHT", padheight);
		e.apply();
	}
	public void setLandscapeColumns(String columns) {
		Editor e = get().edit();
		e.putString("PREFERENCES_LANDSCAPECOLUMNS", columns);
		e.apply();
	}

	public void setBooleanPreference(String key, boolean value) {
		Editor e = get().edit();
		e.putBoolean(key, value); e.apply();
	}

	public void setStringPreference(String key, String value) {
		Editor e = get().edit();
		e.putString(key, value); e.apply();
	}

	public int getPadHeight() {
		try {
			// 10 = Higher small, 0 = Normal, 20 = Lower small, [30 = 3-row special (portrait for now)]
			String PREFERENCES_PADHEIGHT = "PREFERENCES_PADHEIGHT";
			return Integer.parseInt(get().getString(PREFERENCES_PADHEIGHT, "10")); // 310dp = Higher, 280dp = Normal, 250 = Lower
		} catch (Exception e) { // saved values may be old ones with "dp", cannot parseInt
			Log.d("-PADTYPE", "Could not read padHeight in Preferences.java (at getPadHeight())");
			return 0; // in case no value stored in previous app version
		}
	}


	public int getPadType() {
		try {
			// 10 = Higher small, 0 = Normal, 20 = Lower small, 30 = 3-row special (portrait for now)
			String PREFERENCES_PADTYPE = "PREFERENCES_PADTYPE";
			return Integer.parseInt(get().getString(PREFERENCES_PADTYPE, "0")); // 0 = 5-row, 10 = 3-row
		} catch (Exception e) { // saved values may be old ones with "dp", cannot parseInt
			Log.d("-PADTYPE", "Could not read padType in Preferences.java (at getPadType())");
			return 0; // in case no value stored in previous app version
		}

	}
	public int getSpeakType() {
		try {
			// 0 = No speaking, 1 = speak each letter, 2 = speak each word, 3 = both
			String PREFERENCES_SPEAK = "PREFERENCES_SPEAK";
			return Integer.parseInt(get().getString(PREFERENCES_SPEAK, "2")); //
		} catch (Exception e) { // saved values may be old ones with "dp", cannot parseInt
			Log.d("-SPEAK", "Could not read speakType in Preferences.java (at getSpeakType())");
			return 0; // in case no value stored in previous app version
		}

	}

	public boolean isTtsDirect() {
		return true; // get().getBoolean("PREFERENCES_TTS_DIRECT", true);	// TODO: add into settings
	}

	public boolean isSidePanelHidden() { // No side panel (No Abc and Language indicators)
		return get().getBoolean("PREFERENCES_SIDE_PANEL_HIDDEN", false);
	}

	public boolean isSingleHandEnabled() {
		//return get().getBoolean("PREFERENCES_SINGLE_HAND", true);
		return (getTypeMethod() != 3); // Swipes+Combos (1) or Swipes only (2)  (3 = Combos only)
	}

	// PREFERENCES_DISABLE_LANGUAGE_COMBO 2/2018
	public boolean isLangComboDisabled() {
		return get().getBoolean("PREFERENCES_DISABLE_LANGUAGE_COMBO", false);
	}

	// PREFERENCES_REMOVE_TRAILING_SPACE 3/2018
	public boolean isTrailingSpaceRemoved() {
		return get().getBoolean("PREFERENCES_REMOVE_TRAILING_SPACE", false);
	}

	// PREFERENCES_DISABLE_MODECHANGE_COMBO 2/2018
	public boolean isEasyModeChangeDisabled() {
		return get().getBoolean("PREFERENCES_DISABLE_MODECHANGE_COMBO", false);
	}


	public boolean isKeysDisabled() { // not used any more in preferences, problematic multitouch screens do not exist any more
		return false; // get().getBoolean("PREFERENCES_DISABLE_KEYS", false);
	}

	public boolean isComboFlashOn() { // Combo symbol flash next to the keypad (not used any more)
		return get().getBoolean("PREFERENCES_COMBO_FLASH_ON", true);
	}

	public boolean isSymbolFlashOn() { // Tapped symbol flash next to the keypad
		return get().getBoolean("PREFERENCES_SYMBOL_FLASH_ON", true);
	}

	public boolean isXLargeForced() { // This used to be in preferences, not any more

		return false;//get().getBoolean("PREFERENCES_XLARGE_FORCED", false);
	}

	public boolean isSoundEnabled() {
		return get().getBoolean("PREFERENCES_SOUND_ON", true) && ((getSpeakType() == 0) || (getSpeakType() == 2));
		// No button click Sound TOGETHER WITH Speak 'by letter' (1) or both 'by letter' and 'play text' (3) settings on!
	}

	public String getTheme() {
		return get().getString("PREFERENCES_THEME", "Basic");
	}

	public String getUserStrings() {
		//return get().getString("PREFERENCES_USERSTRINGS", " You can define this text in Settings!");
		return get().getString("PREFERENCES_USERSTRINGS", context.getString(R.string.settings_usertext_default));
	}

	public String getAppToLaunch() { // removed in versions 4.3 up
		return "";
		//return get().getString("PREFERENCES_LAUNCH", context.getString(R.string.settings_launch_default));
	}

	public boolean getDisplayHelpCharacters() {
		return get().getBoolean("PREFERENCES_DISPLAY_HELP_CHARACTERS", true);
	}


	public int getTipType() {
		try {
			// 0 = no Tips, 1 = Tips on Middle buttons, 2 = Tips on Main buttons, 3 = various locations
			String PREFERENCES_TIPTYPE = "PREFERENCES_TIPTYPE";
			return Integer.parseInt(get().getString(PREFERENCES_TIPTYPE, "2")); // Tiptype = type of showing help characters
		} catch (Exception e) {
			return 2; // in case no value stored in previous app version
		}
	}

	public boolean isSwipesOnly(){
		return getTypeMethod() == 2; // (Swipes only)
	}

	public boolean isCombosOnly(){
		return getTypeMethod() == 3; // (Combos only)
	}


	public int getTypeMethod() {
		try {
			// 1 = Swipes and Combos, 2 = Swipes only, 3 = Combos only
			String PREFERENCES_TYPE_METHOD = "PREFERENCES_TYPE_METHOD";
			return Integer.parseInt(get().getString(PREFERENCES_TYPE_METHOD, "1")); // Type methos = Swipes+Combos/Swipes/Combos
		} catch (Exception e) {
			return 1; // in case no value stored in previous app version
		}
	}

	public void saveNotes(String notes) {
		Editor e = get().edit();
		e.putString("PREFERENCES_NOTES", notes);
		//e.commit();// write into persistent memory immediately
		e.apply(); // write handled in the background (2017-10)
	}
	public String getNotes() {
		try {
			String PREFERENCES_NOTES = "PREFERENCES_NOTES";
			return (get().getString(PREFERENCES_NOTES, ""));
		} catch (Exception e) { // saved values may be old ones with "dp", cannot parseInt
			Log.d("-NOTES", "Could not read Notes in Preferences.java (at getNotes())");
			return ""; // in case no value stored in previous app version
		}
	}

	public void saveTempString(String tempString) {
		Editor e = get().edit();
		e.putString("PREFERENCES_TEMP_STRING", tempString);
		//e.commit();// write into persistent memory immediately
		e.apply(); // write handled in the background (2017-10)
	}
	public String getTempString() {
		try {
			String PREFERENCES_TEMP_STRING = "PREFERENCES_TEMP_STRING";
			return (get().getString(PREFERENCES_TEMP_STRING, ""));
		} catch (Exception e) { // saved values may be old ones with "dp", cannot parseInt
			Log.d("-NOTES", "Could not read Temp String in Preferences.java (at getNotes())");
			return ""; // in case no value stored in previous app version
		}
	}


	public boolean getDisplayWordCandidates() {
		return get().getBoolean("PREFERENCES_DISPLAY_WORD_CANDIDATES", false);
	}

	public boolean getDisplayMirrorCharacters() {
		return get().getBoolean("PREFERENCES_DISPLAY_MIRROR_CHARACTERS", true);
	}

	public int onehandCombo() {
		try { // 0 = not in use, 1 = hold shorter, 2 = hold longer
			String PREFERENCES_ONEHAND_COMBO = "PREFERENCES_ONEHAND_COMBO";
			return Integer.parseInt(get().getString(PREFERENCES_ONEHAND_COMBO, "0"));
		} catch (Exception e) {
			Log.d("-TIMER", "*** Could not read onehandCombo in Preferences.java (at onehandCombo())");
			return 0; // in case boolean or no value stored in previous app version
		}
	}

	public boolean getDisplayNoCharacters() {
		return get().getBoolean("PREFERENCES_DISPLAY_NO_CHARACTERS", false);
	}
	
	public boolean isTransparentBackground() {
		return get().getBoolean("PREFERENCES_TRANSPARENT_BACKGROUND", false);
	}
	
	public boolean isThisFirstStart() { // First run done, First start done
		boolean firstStartDone =  get().getBoolean("PREFERENCES_FIRST_START_DONE", false);
		if (!firstStartDone) {
			Editor e = get().edit();
			e.putBoolean("PREFERENCES_FIRST_START_DONE", true);
			//e.commit();// write into persistant memory immediately
			e.apply(); // write handled in the background (2017-10)
			Log.d("-FIRST_START", "This IS first start.");
			return true;
		}
		Log.d("-FIRST_START", "This is NOT first start.");
		return false;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d("-VIEW", "onSharedPreferenceChanged().");
		if ("PREFERENCES_THEME".equals(key)) {
			String theme = sharedPreferences.getString(key, "Basic");

			// TODO set current theme, maybe here maybe not
			ThemeManager themeManager = CMBOKeyboardApplication
					.getApplication().getThemeManager();
			themeManager.setActiveTheme(themeManager.getThemeByName(theme));
		}

		if (("PREFERENCES_PADHEIGHT".equals(key))
				|| ("PREFERENCES_PADTYPE".equals(key))
				|| ("PREFERENCES_LANDSCAPECOLUMNS".equals(key)) ) {
			CMBOKeyboardView.regenerateViewKludge = true;
		}

		// is current layout a devanagari or korean set (= dev. offset logic used)
		CMBOKeyboardApplication.getApplication()
				.getKeyboard().checkDevanagariEtc();

		// update input keyboard view:
		CMBOKeyboardView.regenerateViewKludge = true;




	}



	// TODO re-enable?
	public void onSharedPreferenceChangedView( // also found on javas: Preferences, CMBOKeyboardView, LayoutManager
			SharedPreferences sharedPreferences, String key) {

		/*
		 * else if ("PREFERENCES_OPTIMIZED_LAYOUTS".equals(key)) { //
		 * this.useOptimizedLayout = sharedPreferences.getBoolean(key, // true);
		 * CMBOKey.setLanguage(sharedPreferences.getString(
		 * "PREFERENCES_LANGUAGE", "English"), sharedPreferences
		 * .getString("PREFERENCES_OPTIMIZED_LAYOUTS", "Optimized_2")); String
		 * lang = sharedPreferences.getString("PREFERENCES_LANGUAGE",
		 * CMBOKey.Language.English.name());
		 *
		 * if (CMBOKey.Language.Korean.name().equals(lang)) {
		 * controller.setKeyboardOffsetOverride(CMBOKey.AUXSET_MODIFIER); } else
		 * { controller.setKeyboardOffsetOverride(CMBOKey.NONE); } } else if
		 * 
		 * 
		 * ("PREFERENCES_XLARGE_FORCED".equals(key)) { this.xLargeForced =
		 * sharedPreferences.getBoolean(key, false); xLarge = xLargeForced ||
		 * ((getResources().getConfiguration().screenLayout &
		 * Configuration.SCREENLAYOUT_SIZE_MASK) ==
		 * Configuration.SCREENLAYOUT_SIZE_XLARGE); buttonLayout = null;
		 */
		/*
		 * if (this.getContext().getClass() == CMBOKeyboardActivity.class) {
		 * updateKludge = true; }
		 *
		 * else if (this.getContext().getClass() == CMBOKeyboardService.class) {
		 * CMBOKeyboardService service = (CMBOKeyboardService) this
		 * .getContext(); service.setInputView(service.onCreateInputView()); } }
		 *
		 * else if ("PREFERENCES_VIBRATOR_ON".equals(key)) { this.useVibrator =
		 * sharedPreferences.getBoolean(key, true); } else if
		 * 
		 * ("PREFERENCES_AUTOCAPS".equals(key)) { this.autoCaps =
		 * sharedPreferences.getBoolean(key, false); } else if
		 * 
		 * ("PREFERENCES_LANGUAGE".equals(key)) {
		 * 
		 * // after changing the language in the preferences, clear aux flag
		 * nativeLang = true;
		 * 
		 * String lang = sharedPreferences.getString(key,
		 * CMBOKey.Language.English.name());
		 * 
		 * CMBOKey.setLanguage(sharedPreferences.getString(
		 * "PREFERENCES_LANGUAGE", CMBOKey.Language.English.name()),
		 * sharedPreferences.getString( "PREFERENCES_OPTIMIZED_LAYOUTS",
		 * "Optimized_2"));
		 * 
		 * if (CMBOKey.Language.Korean.name().equals(lang)) {
		 * controller.setKeyboardOffsetOverride(CMBOKey.AUXSET_MODIFIER); } else
		 * { controller.setKeyboardOffsetOverride(CMBOKey.NONE); } }
		 */
	}

	public boolean isKorean() {

		return CMBOKeyboardApplication.getApplication()
				.getKeyboard().koreanOn; //checkDevanagariEtc();
	}

	public boolean isAutoCaps() {
		return get().getBoolean("PREFERENCES_AUTOCAPS", true);
	}

}

