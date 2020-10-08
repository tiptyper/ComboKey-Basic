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

import java.util.HashMap;
import java.util.Map;
import android.os.Handler;
import android.view.KeyEvent;
import android.util.Log;

import com.combokey.basic.layout.Layout;
import com.combokey.basic.view.CMBOKeyboardView;

import static android.view.KeyEvent.keyCodeFromString;
import static android.view.KeyEvent.keyCodeToString;
import static com.combokey.basic.KeyboardOutput.KEYCODE_INSERT;

/***
 * 
 * Responsible for mapping the key combinations to actual characters/keycodes,
 * taking into account the current settings
 * 
 */

public class CMBOKeyboard {

	public boolean devanagariOn = false; // default
	public boolean koreanOn = false; // default
	public boolean hystOn = false; // hysteresis on/off
	public int previousListButton = 0; // hysteresis, selected button

	private int offset = CMBOKey.SHIFT_MODIFIER; // (< start in upper case) was: 0; // n x 64, n = 0...4: abc/Abc/ABC/123/SYMB/한글



	// TODO REFACTOR TO PROPER STATE OBJECT
	private boolean numberShift = false; // Shift has been pressed in number
											// mode
	public boolean numberMode = false;
	public boolean numberModeTemp = false; // temporary for symbols thru keyM
	public boolean symbolModeTemp = false;

	private boolean emojiMode = false;
	private boolean fnMode = false;
	private boolean auxMode = false;
	private boolean controlDown = false;
	private boolean controlWasDown = false;
	private boolean altDown = false; // State of kbd
	private boolean altMode = false; // If action is done with Alt being pressed (AltDown may be already cleared)

	private boolean emojiModeSaved = emojiMode;
	private boolean fnModeSaved = fnMode;
	private boolean numberShiftSaved = numberShift;
	private boolean numberModeSaved = numberMode;
	private int offsetSaved = offset;

	private String previousChar = " ";
	private String previousAutoCapsChar = " ";
	private KeyboardOutput output;

	public static String TAG = "CMBOKeyboard";
	private static final Map<String, Integer> keyCodes = new HashMap<String, Integer>();

	public static final int CUSTOM_KEYCODE_END = 1000000;
	public static final int CUSTOM_KEYCODE_HOME = 1000001;
	public static final int CUSTOM_KEYCODE_WLEFT = 1000002;
	public static final int CUSTOM_KEYCODE_WRIGHT = 1000003;
	public static final int CUSTOM_KEYCODE_DEL = 1000004;
	public static final int CUSTOM_KEYCODE_TAB = 1000005;
	public static final int CUSTOM_KEYCODE_SELECT_ALL = 1000006;
	public static final int CUSTOM_KEYCODE_COPY = 1000007;
	public static final int CUSTOM_KEYCODE_PASTE = 1000008;
	public static final int CUSTOM_KEYCODE_CLEAR = 1000009;
	public static final int CUSTOM_KEYCODE_PGDN = 1000010;
	public static final int CUSTOM_KEYCODE_PGUP = 1000011;

	public static final int CUSTOM_KEYCODE_RECEIVED = 1000012;
	public static final int CUSTOM_KEYCODE_MENU = 1000013;
	public static final int CUSTOM_KEYCODE_SEND = 1000014;
	public static final int CUSTOM_KEYCODE_TRANSLATE = 1000015;
	public static final int CUSTOM_KEYCODE_CALLBACK = 1000016;
	public static final int CUSTOM_KEYCODE_SEARCH = 1000017;
	public static final int CUSTOM_KEYCODE_LANG = 1000018;
	public static final int CUSTOM_KEYCODE_ALT = 1000019;
	public static final int CUSTOM_KEYCODE_CTRL = 1000020;
	public static final int CUSTOM_KEYCODE_EMOJI = 1000021;
	public static final int CUSTOM_KEYCODE_USERSTRINGS = 1000022;
	public static final int CUSTOM_KEYCODE_INPUT = 1000023;

	public static final int CUSTOM_KEYCODE_123 = 1000024;
	public static final int CUSTOM_KEYCODE_ABC = 1000025;
	public static final int CUSTOM_KEYCODE_SHIFTDOWN = 1000026;

	public static final int CUSTOM_KEYCODE_CALL = 1000027;
	public static final int CUSTOM_KEYCODE_NOTES = 1000028;
	public static final int CUSTOM_KEYCODE_LAUNCH = 1000029;
	public static final int CUSTOM_KEYCODE_REPEAT = 1000030;
	public static final int CUSTOM_KEYCODE_SETTINGS = 1000031;
	public static final int CUSTOM_KEYCODE_KEYPAD_RIGHT = 1000032;
	public static final int CUSTOM_KEYCODE_KEYPAD_LEFT = 1000033;
	public static final int CUSTOM_KEYCODE_KEYPAD_CENTER = 1000034;
	public static final int CUSTOM_KEYCODE_KEYPAD_WIDE = 1000035;
	public static final int CUSTOM_KEYCODE_KEYPAD_HIDE = 1000036;
	public static final int CUSTOM_KEYCODE_SPEAK_PLAY = 1000037;
	public static final int CUSTOM_KEYCODE_SPEAK_STOP = 1000038;
	public static final int CUSTOM_KEYCODE_VOICE_REC = 1000039;

	public static final int CUSTOM_KEYCODE_FN = 1000040;
	public static final int CUSTOM_KEYCODE_F1 = 1000041;
	public static final int CUSTOM_KEYCODE_F2 = 1000042;
	public static final int CUSTOM_KEYCODE_F3 = 1000043;
	public static final int CUSTOM_KEYCODE_F4 = 1000044;
	public static final int CUSTOM_KEYCODE_F5 = 1000045;
	public static final int CUSTOM_KEYCODE_F6 = 1000046;
	public static final int CUSTOM_KEYCODE_F7 = 1000047;
	public static final int CUSTOM_KEYCODE_F8 = 1000048;
	public static final int CUSTOM_KEYCODE_F9 = 1000049;
	public static final int CUSTOM_KEYCODE_F10 = 1000050;
	public static final int CUSTOM_KEYCODE_F11 = 1000051;
	public static final int CUSTOM_KEYCODE_F12 = 1000052;
	public static final int CUSTOM_KEYCODE_META = 1000053;
	public static final int CUSTOM_KEYCODE_OPEN = 1000054;
	public static final int CUSTOM_KEYCODE_SAVE = 1000055;
	public static final int CUSTOM_KEYCODE_MORE = 1000056;

		public static final int CUSTOM_KEYCODE_PAD_UL = 1000057;
		public static final int CUSTOM_KEYCODE_PAD_CW = 1000058;
		public static final int CUSTOM_KEYCODE_PAD_UR = 1000059;
		public static final int CUSTOM_KEYCODE_PAD_B = 1000060;
		public static final int CUSTOM_KEYCODE_PAD_U = 1000061;
		public static final int CUSTOM_KEYCODE_PAD_S = 1000062;
		public static final int CUSTOM_KEYCODE_PAD_L = 1000063;
		public static final int CUSTOM_KEYCODE_PAD_R = 1000064;
		public static final int CUSTOM_KEYCODE_PAD_5R = 1000065;
		public static final int CUSTOM_KEYCODE_PAD_3R = 1000066;
		public static final int CUSTOM_KEYCODE_PAD_LL = 1000067;
		public static final int CUSTOM_KEYCODE_PAD_CN = 1000068;
		public static final int CUSTOM_KEYCODE_PAD_D = 1000069;
		public static final int CUSTOM_KEYCODE_PAD_LR = 1000070;

	public static final int CUSTOM_KEYCODE_FILE = 1000071;
	public static final int CUSTOM_KEYCODE_APPEND = 1000072;
	public static final int CUSTOM_KEYCODE_GET = 1000073;


	static {

		keyCodes.put("_BS", KeyEvent.KEYCODE_DEL);
		keyCodes.put("_Enter", KeyEvent.KEYCODE_ENTER);
		keyCodes.put("_Up", KeyEvent.KEYCODE_DPAD_UP);
		keyCodes.put("_Down", KeyEvent.KEYCODE_DPAD_DOWN);
		keyCodes.put("_Left", KeyEvent.KEYCODE_DPAD_LEFT);
		keyCodes.put("_Right", KeyEvent.KEYCODE_DPAD_RIGHT);

		keyCodes.put("_Tab", CUSTOM_KEYCODE_TAB); // This is CUSTOM due to special terminal use
		keyCodes.put("_End", CUSTOM_KEYCODE_END);
		keyCodes.put("_Home", CUSTOM_KEYCODE_HOME);
		keyCodes.put("_Del", CUSTOM_KEYCODE_DEL);
		keyCodes.put("_WRight", CUSTOM_KEYCODE_WRIGHT);
		keyCodes.put("_WLeft", CUSTOM_KEYCODE_WLEFT);
		keyCodes.put("_PgDn", CUSTOM_KEYCODE_PGDN);
		keyCodes.put("_PgUp", CUSTOM_KEYCODE_PGUP);
		keyCodes.put("_Clear", CUSTOM_KEYCODE_CLEAR);
		keyCodes.put("_Copy", CUSTOM_KEYCODE_COPY);
		keyCodes.put("_Paste", CUSTOM_KEYCODE_PASTE);


		keyCodes.put("_Received", CUSTOM_KEYCODE_RECEIVED); // ComboKey
		keyCodes.put("_Menu", CUSTOM_KEYCODE_MENU);
		keyCodes.put("_Send", CUSTOM_KEYCODE_SEND);
		keyCodes.put("_Play", CUSTOM_KEYCODE_SPEAK_PLAY);
		keyCodes.put("_Record", CUSTOM_KEYCODE_VOICE_REC);
		keyCodes.put("_Stop", CUSTOM_KEYCODE_SPEAK_STOP);
		keyCodes.put("_Translate", CUSTOM_KEYCODE_TRANSLATE);
		keyCodes.put("_Callback", CUSTOM_KEYCODE_CALLBACK);
		keyCodes.put("_Call", CUSTOM_KEYCODE_CALL);
		keyCodes.put("_Search", CUSTOM_KEYCODE_SEARCH);
		keyCodes.put("_Lang", CUSTOM_KEYCODE_LANG);
		keyCodes.put("_Alt", CUSTOM_KEYCODE_ALT);
		keyCodes.put("_Ctrl", CUSTOM_KEYCODE_CTRL);
		keyCodes.put("_Emoji", CUSTOM_KEYCODE_EMOJI);
		keyCodes.put("_User", CUSTOM_KEYCODE_USERSTRINGS);
		keyCodes.put("_Keyboard", CUSTOM_KEYCODE_INPUT);
		keyCodes.put("_Hide", CUSTOM_KEYCODE_KEYPAD_HIDE);
		keyCodes.put("_Meta", CUSTOM_KEYCODE_META);
		keyCodes.put("_Save", CUSTOM_KEYCODE_SAVE);
		keyCodes.put("_Add", CUSTOM_KEYCODE_APPEND);
		keyCodes.put("_Open", CUSTOM_KEYCODE_OPEN);
		keyCodes.put("_Get", CUSTOM_KEYCODE_GET);
		keyCodes.put("_More", CUSTOM_KEYCODE_MORE);

		keyCodes.put("_Esc", KeyboardOutput.KEYCODE_ESCAPE);
		keyCodes.put("_Ins", KEYCODE_INSERT);

		keyCodes.put("_123", CUSTOM_KEYCODE_123);
		keyCodes.put("_abc", CUSTOM_KEYCODE_ABC);
		keyCodes.put("_ShiftDown", CUSTOM_KEYCODE_SHIFTDOWN);
		keyCodes.put("_Notes", CUSTOM_KEYCODE_NOTES);
		keyCodes.put("_Launch", CUSTOM_KEYCODE_LAUNCH);
		keyCodes.put("_Repeat", CUSTOM_KEYCODE_REPEAT);
		keyCodes.put("_Settings", CUSTOM_KEYCODE_SETTINGS);
		keyCodes.put("_Pad-R", CUSTOM_KEYCODE_KEYPAD_RIGHT);
		keyCodes.put("_Pad-L", CUSTOM_KEYCODE_KEYPAD_LEFT);
		keyCodes.put("_Pad-C", CUSTOM_KEYCODE_KEYPAD_CENTER);
		keyCodes.put("_Pad-W", CUSTOM_KEYCODE_KEYPAD_WIDE);

		keyCodes.put("_Fn", CUSTOM_KEYCODE_FN);
		keyCodes.put("_F1", CUSTOM_KEYCODE_F1);
		keyCodes.put("_F2", CUSTOM_KEYCODE_F2);
		keyCodes.put("_F3", CUSTOM_KEYCODE_F3);
		keyCodes.put("_F4", CUSTOM_KEYCODE_F4);
		keyCodes.put("_F5", CUSTOM_KEYCODE_F5);
		keyCodes.put("_F6", CUSTOM_KEYCODE_F6);
		keyCodes.put("_F7", CUSTOM_KEYCODE_F7);
		keyCodes.put("_F8", CUSTOM_KEYCODE_F8);
		keyCodes.put("_F9", CUSTOM_KEYCODE_F9);
		keyCodes.put("_F10", CUSTOM_KEYCODE_F10);
		keyCodes.put("_F11", CUSTOM_KEYCODE_F11);
		keyCodes.put("_F12", CUSTOM_KEYCODE_F12);
		keyCodes.put("_File", CUSTOM_KEYCODE_FILE);

		keyCodes.put("_PadUL", CUSTOM_KEYCODE_PAD_UL); // upper left
		keyCodes.put("_PadCW", CUSTOM_KEYCODE_PAD_CW); // center wide
		keyCodes.put("_PadUR", CUSTOM_KEYCODE_PAD_UR); // upper right
		keyCodes.put("_PadB", CUSTOM_KEYCODE_PAD_B); // big
		keyCodes.put("_PadU", CUSTOM_KEYCODE_PAD_U); // up
		keyCodes.put("_PadS", CUSTOM_KEYCODE_PAD_S); // small
		keyCodes.put("_PadL", CUSTOM_KEYCODE_PAD_L); // left
		keyCodes.put("_PadR", CUSTOM_KEYCODE_PAD_R); // right
		keyCodes.put("_Pad5R", CUSTOM_KEYCODE_PAD_5R); // 5-row
		keyCodes.put("_Pad3R", CUSTOM_KEYCODE_PAD_3R); // 3-row
		keyCodes.put("_PadLL", CUSTOM_KEYCODE_PAD_LL); // lower left
		keyCodes.put("_PadCN", CUSTOM_KEYCODE_PAD_CN); // center narrow
		keyCodes.put("_PadD", CUSTOM_KEYCODE_PAD_D); // down
		keyCodes.put("_PadLR", CUSTOM_KEYCODE_PAD_LR); // lower right

		// Missing
		// "_Shift", _acb123

	}

	public boolean isKoreanOn() {
		String extra = "";
		extra = CMBOKeyboardApplication.getApplication()
				.getLayoutManager().getCurrentLayout().getValue(Layout.Metadata.LAYOUT_EXTRA); //
		Log.d("CMBO", "*** LAYOUT_EXTRA string = " + extra);
		// string = ["-","korean","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-"]
		if (extra == null) {
			Log.d("CMBO", "*** Devanagari/Korean not defined in JSON (no extra json data available)");
			return false;
		}

		if (extra.toLowerCase().contains("korean")) { // This is a bit hasty solution !
			Log.d("CMBO", "*** Korean is set in JSON (extra json data is available: use korean offset logic)");
			return true;
		} else {
			Log.d("CMBO", "*** Korean not set in JSON (extra json data is available: use normal offset logic)");
			return false;
		}

	}

	public boolean statusDevanagariOn() {
		Log.d("-DEV", "*** Checking status: devanagriOn: " + devanagariOn);
		return devanagariOn;
	}


	public boolean isDevanagariOn() {

		Log.d("-DEV", "*** Updating: updateDevanagariOn()... ");

		String extra = ""; String name = "";

	try {
			extra = CMBOKeyboardApplication.getApplication()
					.getLayoutManager().getCurrentLayout().getValue(Layout.Metadata.LAYOUT_EXTRA); //
			name = CMBOKeyboardApplication.getApplication()
					.getLayoutManager().getCurrentLayout().getValue(Layout.Metadata.NAME);

		} catch (Exception e) {
			extra = ""; name = "";
		}

		Log.d("-DEV", "*** " + name + " LAYOUT_EXTRA string = " + extra);
		// string = ["-","dev","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-"]

		if ((extra == null) || (extra.equals(""))) {
			Log.d("-DEV", "*** Devanagari/Korean not defined in JSON (no extra json data available)");
			devanagariOn = false;
			return false;
		}
		// Get "dev" from JSON extra by splitting string to array items (split points are: ",")
		// Metadata extra = ["-","dev","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-"]

		String[] aryX = extra.split("\",\"");
		// Note: First and last item missed! Not allowed to have any space between items? Just a comma?
		if (aryX[1].toLowerCase().equals("dev")) { // Option 1. This is better but is it fast enough?
		//if (extra.toLowerCase().contains("dev")) { // Option 2. This is a bit hasty solution !
			Log.d("-DEV", "*** Devanagari is set in JSON (extra json data is available: use dev. offset logic)");
			devanagariOn = true;
			return true;
		} else {
			Log.d("-DEV", "*** Devanagari not set in JSON (extra json data is available: use normal offset logic)");
			devanagariOn = false;
			return false; //
		}


	}


	public void checkDevanagariEtc() {

		//devanagariOn = false; // default

		if (isDevanagariOn()) {
			offset = auxMode ? CMBOKey.AUXSET_MODIFIER : CMBOKey.NONE; // start in lower case in this case
			devanagariOn = true;
			koreanOn = false;
		} else if (isKoreanOn()) {
			offset = auxMode ? CMBOKey.AUXSET_MODIFIER : CMBOKey.NONE; // start in lower case in this case
			devanagariOn = true; // Note: Use same offset logic as with debanagari!
			koreanOn = true;
		} else {
			devanagariOn = false;
			koreanOn = false;
		}
		Log.d("-DEV", "*** Check if Devanagari/Korean offset logic needed: " + devanagariOn + "/" + koreanOn );

	}


	public void setOutput(KeyboardOutput output) {
		checkDevanagariEtc();
		this.output = output;

	}


	public int repeatState = 0;
	public int repeatKey = 0;


	private void sendAsciiCharSequence(String outString) { // For sending Esc sequences when pressing F1...F12 keys

		if (outString.equals("")) return;
		// if (!this.output.isTerminalMode()) return; // To send Esc sequences only in terminalMode.
		// The line above was commented in ordet to send always because some terminal emulators use normal
		// text input type (inputEditor's inputType != 0) - ConnectBot would have worked fine with this check.

	Log.d("-Fn", "*** Sending characters Ctrl+[ followed by: ");
		this.output.sendKeyChar((char) 27); // Esc always first
		for(int n = 0; n < outString.length(); n++) {
			char c = outString.toUpperCase().charAt(n);
			int asciiChar = CharToASCII(c);
			if (asciiChar > 255) return;
			int code = ASCIIToChar(asciiChar);
			this.output.sendKeyChar((char) code);
			Log.d("-Fn", "char: " + (char) code);
		}
	}

	private boolean anyFnPressed(int state, int key, String keyString) {
		Log.d("-Fn", "*** Handle Fn map keypress...");
		// beginning with Ctrl+[ [ or Ctrl+[ O  (= letter O, not zero!)
		// [ = 91, Ctrl+[ = 91- 64 = 27 (char) = Esc
		// Ctrl+[ is sent always first in sendAsciiCharSequence()
		if (keyString.equals("F1")) {// F1 ^[OP // or key code 112
			sendAsciiCharSequence("OP");
			return true;
		} else
		if (keyString.equals("F2")) {// F2 ^[OQ // or key code 113
			sendAsciiCharSequence("OQ");
			return true;
		} else
		if (keyString.equals("F3")) {// F3 ^[OR // or key code 114
			sendAsciiCharSequence("OR");
			return true;
		} else
		if (keyString.equals("F4")) {// F4 ^[OS // or key code 115
			sendAsciiCharSequence("OS");
			return true;
		} else
		if (keyString.equals("F5")) {// F5 ^[[15~ // or key code 116
			sendAsciiCharSequence("[15~");
			return true;
		} else
		if (keyString.equals("F6")) {// F6 ^[[17~ // or key code 117
			sendAsciiCharSequence("[17~");
			return true;
		} else
		if (keyString.equals("F7")) {// F7 ^[[18~ // or key code 118
			sendAsciiCharSequence("[18~");
			return true;
		} else
		if (keyString.equals("F8")) {// F8 ^[[19~ // or key code 119
			sendAsciiCharSequence("[19~");
			return true;
		} else
		if (keyString.equals("F9")) {// F9 ^[[20~ // or key code 120
			sendAsciiCharSequence("[20~");
			return true;
		} else
		if (keyString.equals("F10")) {// F10 ^[[21~ // or key code 121
			sendAsciiCharSequence("[21~");
			return true;
		} else
		if (keyString.equals("F11")) {// F11 ^[[23~ // or key code 122
			sendAsciiCharSequence("[23~");
			return true;
		} else
		if (keyString.equals("F12")) {// F12 ^[[24~ // or key code 123
			sendAsciiCharSequence("[24~");
			return true;
		}

		return false;
	}

	public void handleKeyPress(int state, int key) {
		// Log.d(TAG, "Got an input event where the virtual key "
		// + CMBOKey.getStringRepresentation(CMBOKey.NONE, key)
		// + " was pressed in the state "
		// + CMBOKey.getStringRepresentation(CMBOKey.CMBO, state));

		// The virtual key 'key' was pressed while the key 'state' was being
		// held.

		// Figure out which character (if any) should be output based on
		// these two pieces of information and do something.

		Log.d("CMBO", "*** Handle keypress...");

		if (!CMBOKey.isValidCombination(state, key)) {
			return;
		}

		int chord = state + key;

		String keyString = getString(offset, state, key);
		int kc = getKeyCode(keyString);

		if (offset == CMBOKey.FN_MODIFIER) { // F1...F12 map active?
			// Function keys
			if (anyFnPressed(state, key, keyString)) { // && (!this.output.isTerminalMode())) {
				// if any of the keys F1 to F12 is pressed in terminal mode
				keyString = ""; // Just sent F1...F12, no more action
				kc = 0;

			} else { // Some other key in fnMode was pressed

				checkModifierPress(state, key, keyString);
			}
			// Other characters checked below as usual
		} else { // not fnMode, act normally

			checkModifierPress(state, key, keyString);
		}

		Log.d("-TOUCH", "=> keyString = " + keyString);
		Log.d("-KEYCODE", "keyString = " + keyString + ", kc = " + kc);

		if (kc != CUSTOM_KEYCODE_REPEAT) {
			repeatState = state;
			repeatKey = key;
		}

		if (kc != CUSTOM_KEYCODE_CLEAR) CMBOKeyboardService.readyToClearText = false; // readyToPlayText
		if (kc != CUSTOM_KEYCODE_SPEAK_PLAY) CMBOKeyboardService.readyToPlayText = false;
		if (kc != CUSTOM_KEYCODE_SAVE) CMBOKeyboardService.readyToSaveText = false;

		// no normal handling at all if ctrl was pressed previously
		if (!isModifierDown()) {

			setAltMode(false);
			if ((devanagariOn) && (chord == 64) && (keyString.equals("..."))) {  // the '...' key has no output
				kc = 0; keyString = "";
				checkDevOffset(state, key);
			} else {
			handleNormalKeypress(kc, keyString);
			handleAutoCaps(kc, keyString);
			checkDevOffset(state, key);
			}

		} else {
			// modifiers
			// TODO make modifiers more generic
			if (isCtrlDown()) {
				handleCtrl(kc, keyString);
			}
			if (isAltDown()) {

				altMode = true;
				if (handleAlt(kc, keyString)) {
					altMode = false;
					return; // some actions done like Alt+d

				} else { // do the standard actions (AltDown still)

					if ((devanagariOn) && (chord == 64) && (keyString.equals("..."))) {  // the '...' key has no output
						kc = 0; keyString = "";
						checkDevOffset(state, key);
					} else {
						handleNormalKeypress(kc, keyString);
						handleAutoCaps(kc, keyString);
						checkDevOffset(state, key);
					}

				}
			}
		}

		if (numberModeTemp || symbolModeTemp) {
			numberModeTemp = false; numberMode = false;
			symbolModeTemp = false;
			resumeMode();
		}

		// Keep the old previous char in buffer if the next pressed key was a modifier
		if (chord != 18 && chord != 45 && chord != 31 && chord != 63 && !(state == 7 && key == 5) && !(state == 5 && key == 7)) {
			// CMBO Shift, CMBO SYMB, Emoji, CMBO abc123, New Shift, new abc123 (swipe down from keyD), SP
			previousChar = keyString; // for adding accent to a previous char
			previousAutoCapsChar = keyString;
		}

		// Check if Ctrl or Alt pressed
		// Still need to clear Ctrl and Alt after all visible outputs
		Log.d("CMBO", "***** before playSound: offset = " + offset + ", auxMode = " + auxMode + ", devanagariOn = " + devanagariOn);

		playSound(state, key);

	}

	private void checkModifiersTurnOn(int chord) {
		switch (chord) {
		// Ctrl
		case 71: // was 47
			controlDown = true;
			break;
		// Alt
		case 55:
			altDown = true;
			break;
		}
	}

	private boolean handleAlt(int kc, String keyString) {
		Log.d("CMBO", "*** handle Alt was down");
		// Alt actions

		if (("d".equalsIgnoreCase(keyString))){
			// TODO: remove this temorary debug tool:
			String toastMessage = "";
			if (CMBOKeyboardView.debugInUse) {
				CMBOKeyboardView.debugInUse = false;
				//toastMessage = "Debug messages OFF.";
				Log.d("CMBO", "*** Debug Toast messages OFF.");
			} else {
				CMBOKeyboardView.debugInUse = false; // true; // false = disable in distribution version
				//toastMessage = "Debug messages ON.";
				Log.d("CMBO", "*** Debug Toast messages ON.");
			}
			altDown = false;
			return true; // action done
		}

		// ... add other Alt actions here
		altDown = false;
		return false;
		// Else return and do the standard output (altDown is still true)
	}

	public boolean isAltDown() {
		return altDown;
	}
	public void setAlt(boolean on) {
		altDown = on;
	}
	public boolean isAltMode() {
		return altMode;
	}
	public void setAltMode(boolean on) {
		altMode = on;
	}

	private void handleCtrl(int kc, String keyString) {
		Log.d("CMBO", "*** handle Ctrl+something, Ctrl was down");
		// a => select all
		if (("a".equalsIgnoreCase(keyString)) && (!this.output.isTerminalMode())) {
			Log.d("CMBO", "*** Select all (Ctrl + a)");
			output.onKey(CUSTOM_KEYCODE_SELECT_ALL, null);
		} else
		if (("_Ins".equalsIgnoreCase(keyString)) && (!this.output.isTerminalMode())) {
			Log.d("-KEY", "*** Select all (Ctrl + BS =Backspace)");
			output.onKey(KEYCODE_INSERT, null);
		} else
		if (("_Ins".equalsIgnoreCase(keyString)) && (this.output.isTerminalMode())) {
			Log.d("-KEY", "*** Select all (Ctrl + BS =Backspace)");
			keyString = ""; // ctrl+???? = ?????
			sendKeyWithCtrl(keyString);
		} else
		if (("_BS".equalsIgnoreCase(keyString)) && (!this.output.isTerminalMode())) {
			Log.d("-KEY", "*** Select all (Ctrl + BS =Backspace)");
			output.onKey(CUSTOM_KEYCODE_SELECT_ALL, null);
		} else
		if (("_BS".equalsIgnoreCase(keyString)) && (this.output.isTerminalMode())) {
			Log.d("-KEY", "*** Clear screen (Ctrl + BS => Ctrl+L)");
			keyString = "L"; // ctrl+L = clear screen
			sendKeyWithCtrl(keyString);
		} else
		if ("c".equalsIgnoreCase(keyString) && (!this.output.isTerminalMode())) {
			Log.d("CMBO", "*** Copy selection (Ctrl + c)");
			output.onKey(CUSTOM_KEYCODE_COPY, null);
		} else
		if ("v".equalsIgnoreCase(keyString) && (!this.output.isTerminalMode())) {
			Log.d("CMBO", "*** Paste (Ctrl + v)");
			output.onKey(CUSTOM_KEYCODE_PASTE, null);
		} else
		if ("_Home".equalsIgnoreCase(keyString)) {
			Log.d("CMBO", "*** Move to start of text (Ctrl + Home)");
			output.onKey(CUSTOM_KEYCODE_PGUP, null);
			output.onKey(CUSTOM_KEYCODE_HOME, null); // (no effect)
		} else
		if ("_End".equalsIgnoreCase(keyString)) {
			Log.d("CMBO", "*** Move to End of text (Ctrl + End)");
			output.onKey(CUSTOM_KEYCODE_PGDN, null);
			output.onKey(CUSTOM_KEYCODE_END, null);
		} else
		if (("_Lang".equalsIgnoreCase(keyString))) {
			Log.d("-SETTINGS", "*** Keyboard.java Settings (Ctrl + Lang)");
			output.onKey(CUSTOM_KEYCODE_SETTINGS, null);
		} else
		if (("_WRight".equalsIgnoreCase(keyString))) {
			Log.d("CMBO", "*** Settings (Ctrl + WRight)");
			output.onKey(CUSTOM_KEYCODE_KEYPAD_RIGHT, null);
		} else
		if (("_WLeft".equalsIgnoreCase(keyString))) {
			Log.d("CMBO", "*** Settings (Ctrl + WLeft)");
			output.onKey(CUSTOM_KEYCODE_KEYPAD_LEFT, null);
		} else
		if (("_123".equalsIgnoreCase(keyString))) {
			Log.d("CMBO", "*** Settings (Ctrl + 123)");
			output.onKey(CUSTOM_KEYCODE_KEYPAD_CENTER, null);
		} else
		if (("_Keyboard".equalsIgnoreCase(keyString))) {
			Log.d("CMBO", "*** Settings (Ctrl + Keyboard)");
			output.onKey(CUSTOM_KEYCODE_KEYPAD_WIDE, null);
		} else
		if ("s".equalsIgnoreCase(keyString) && (!this.output.isTerminalMode())) {
			//Log.d("CMBO", "*** Save text as User defined text.");
		} else {

			Log.d("-KEY", "*** Ctrl + <pressed key = " + keyString +
					">. keyCodeFromString() = " + keyCodeFromString(keyString)
					+ ", keyCodeToString() = " + keyCodeToString(kc)
					+ ", key event = " + KeyEvent.keyCodeToString(kc)
					+ ", key event value for a = " + KeyEvent.KEYCODE_A);

			char c = 0;

			if ((keyString.length() == 1) && (!this.output.isTerminalMode())){ // handle Ctrl + A-Z and a-z
				c = keyString.toLowerCase().charAt(0);

				int code = (int) c;	code += -68; // move left in the table
				Log.d("-KEY", "Key code = " + code + ", key = Ctrl+(int)" + (int) c);
				if ((code > 28) && (code < 55)) { // a, b, c...
					this.output.keyDown(KeyEvent.KEYCODE_CTRL_LEFT); // Ctrl key down
					this.output.keyUpDown(code); // code: e.g. a = 29, z = 54
					this.output.keyUp(KeyEvent.KEYCODE_CTRL_LEFT); // Ctrl key down
				}
			} else if ((keyString.length() == 1) && (this.output.isTerminalMode())) { // Terminal operation

				sendKeyWithCtrl(keyString);
			}
		}
		controlDown = false;
	}


	private void sendKeyWithCtrl(String keyString) { // A => send Ctrl+A etc.

		Log.d("-KEY", "sendKeyWithCtrl(). keyString = " + keyString);
		char c = 0;
		String ctrlKey = "";

		// try: char to ascii, minus 64, ascii to char
		if (keyString.equals("")) {
			ctrlKey = this.output.getCtrlKey(); // e.g. "A", "B", ... send this as Ctrl+A, Ctrl+B...
		} else {
			ctrlKey = keyString;
		}

		// Functions converted to Ctrl+char, e.g. "A" = 'move to start of line' (= Ctrl+A)
		if (ctrlKey.length() == 1){ // only single characters are really sent with Ctrl
			c = ctrlKey.toUpperCase().charAt(0);
		} else if (ctrlKey.length() > 1) { // Others use Esc sequences etc.
			c = ctrlKey.toUpperCase().charAt(ctrlKey.length() - 1); // e.g. AltB, AltC, EscB, EscF
			// Elsewhwere:
			// Deal with Esc char and Alt+char...
			return;
		} else {
			return;
		}

		// Upper case minus 64 makes Ctrl version, any letter key (within limits: 64...127)
		int asciiChar = CharToASCII(c);
		if (asciiChar == 32) asciiChar = 64; // Ctrl+space can replace Ctrl+@ to obtain null
		if (asciiChar == 63) asciiChar = 127 + 64; // DEL (127) can be obtained by Ctrl+? (? = 63)
		if ((asciiChar > 63) && (asciiChar < 128)) asciiChar += -64;
		int code = ASCIIToChar(asciiChar);
		Log.d("-KEY", "Terminal: character code = " + code + ", key = Ctrl+(int)" + (int) c);
		this.output.sendKeyChar((char) code);

	}


	private static int CharToASCII(final char character){
		return (int) character;
	}

	private static char ASCIIToChar(final int ascii){
		return (char) ascii;
	}

	public void listChars() {

			for(int x = 0; x < 255; x = x + 1) {
				//System.out.print("value of x : " + x );
				//System.out.print("\n");
				String outString = keyCodeToString(x);
				Log.d("-KEY", "--- " + x + " " + outString + "\n");
			}
		}


	public boolean isCtrlDown() {
		return controlDown;
	}


	public void setCtrl(boolean on) {
		controlDown = on;
	}

	private void handleAutoCaps(int kc, String keyString) {
		if (devanagariOn && !auxMode) { // aux set has no devanagari and korean params
			return;
		}
		// Autocaps, automatic capitals after ". ", "! " and "? ", or after "¡" or "¿"
		if (((offset == CMBOKey.NONE) || (offset == CMBOKey.AUXSET_MODIFIER))
				&& isAutoCaps()
				&& " ".equals(keyString)
				&& (".".equals(previousAutoCapsChar) || "?".equals(previousAutoCapsChar) || "!"
						.equals(previousAutoCapsChar))) {
			offset = auxMode ? CMBOKey.AUXSET_SHIFT_MODIFIER : CMBOKey.SHIFT_MODIFIER;
		} else {
			if ("¿".equals(keyString) || "¡".equals(keyString)) { // Turn Shift back ON after Spanish inverted marks
				Log.d("-INV", "- B: ¡ or ¿ - keyString = " + keyString);
				offset = auxMode ? CMBOKey.AUXSET_SHIFT_MODIFIER : CMBOKey.SHIFT_MODIFIER;
			}
		}
	}

	public String getPreviousCharacter() {
		return previousChar;
	}

	public void repeatKeypress() {
		handleKeyPress(repeatState, repeatKey);
	}

	public void repeatStateAndKey(int state, int key) {
		handleKeyPress(state, key);
	}
	public void outputPreviousCharacter() { // used by autorepeat
		output.onText(previousChar);
	}

	public void outputCharacter(String outChar) { // used by autorepeat
		output.onText(outChar);
	}


	private void handleNormalKeypress(int kc, String keyString) {

		if (kc != Integer.MAX_VALUE) {
			this.output.onKey(kc, new int[0]);
		} else if (keyString.length() == 1 || !keyString.startsWith("_")) {
			//this.previousChar = "";
			if (!numberMode && !CMBOCombiner.isAccent(keyString)) {
				this.output.onText(keyString);
				addToCandidateHint(keyString);
				//this.previousChar = keyString;
				return;
			}
			if (!numberMode && CMBOCombiner.isAccent(keyString)) {
				final String combined = CMBOCombiner.getCombinedCharacter(previousChar,
						keyString);

				if (!CMBOCombiner.isAccent(combined)) {
					this.output.keyUpDown(KeyEvent.KEYCODE_DEL); // replace the plain character with accented one
					addToCandidateHint("_BS");
					addToCandidateHint(combined);
				}

				final Handler handler = new Handler(); // Delay required: delete first, then output new to replace,
				handler.postDelayed(new Runnable() {
					//@Override
					public void run() { // without delay the output and delete operations may overlap or reverse
						// Do something after delay
						output.onText(combined);
					}
				}, 32); // 32 ms delay

				//this.output.onText("Y");
			} else {
				this.output.onText(keyString);
				addToCandidateHint(keyString);
			}
		}

	}


	private void addToCandidateHint(String letter) {

		boolean isTopBar = CMBOKeyboardApplication.getApplication().getPreferences().getDisplayWordCandidates();
		if(isTopBar) {
			Log.d("-CAND", "Adding letter " + letter + " to word candidate hint");
			this.output.updateWordAtCursor(); // New (use inputConnection)
		}
	}

	private boolean isModifierDown() {
		return controlDown || altDown;
	}

	private void checkModifierPress(int state, int key, String keyString) {
		// Check if a Modifier pressed
		// NOTE:
		// If making changes here, you also need to check these methods:
		// 	- isValidCombination in CMBOKey.java and
		//  - getStringRepresentation in LayoutManager.java and
		//  - drawButtonSymbol in CMBOKeyboardView.java and
		//  - playSound in CMBOKey.java

		int chord = state + key;

		Log.d("-Fn", "**** Keyboard/checkModifierPressed() starting. chord = " + chord + ", pressed = " + keyString);

		if (keyString.equals("")) {
			anyOtherKeyPressed();
			return;
		}

		if (keyString.charAt(0) != "_".charAt(0)) {
			if (!keyString.equals("...")) {
				anyOtherKeyPressed();
				return;
			}
		}


		// Note: chord values 113 to 119 can be defined for special use as they do not appear as a combo
		//if ((state == 56) && (key == 24)) chord = 116; // "_Fn" might have been pressed if not "_Emoji"
		//if (((state == 56) && (key == 24)) || ((state == 24) && ((key == 56) || (key == 7)))) chord = 119;

		// Chord spare values used here:
		// 113
		// 114
		// 115 ShiftDown
		// 116 Fn
		// 117 Clear
		// 118 Shift
		// 119 Emoji


		// SYMB
		if (keyString.equals("_SYMB")) {
			chord = 59;
		} else
		// abc123
		if ((keyString.equals("_abc123")) // switch between Numbers and abc
				|| (keyString.equals("_123")) // switch to Numbers
				|| (keyString.equals("_abc"))) {
			chord = 63; // switch from Numbers
			// for using here case 63 (123abc), swipe Key TH => BS

		} else
			// Up
		if (keyString.equals("_Up")) {
			chord = 18; // Devanagari check below?
		} else
			// Shift
		if (keyString.equals("_Shift")) {
			chord = 118; // using spare (113-119) value for case 118
		} else
			// Clear
		if (keyString.equals("_Clear")) {
			chord = 117; // using spare (113-119) value for case 117
		} else
		if (keyString.equals("_ShiftDown")) {
			chord = 115; // using spare (113-119) value for case 115
		} else
			// Emoji
		if (keyString.equals("_Emoji")) {
			chord = 119; // using spare (113-119) value for case 119
			// 119 is just an invented value for Emoji selection
		} else
			// Function keys
			if (keyString.equals("_More") || keyString.equals("_Fn")) { // was _Fn
			chord = 116; // using spare (113-119) value for case 116
		}

		//
		if ((offset >= CMBOKey.AUXSET_MODIFIER) && (offset < CMBOKey.EMOJI_MODIFIER)) {
			auxMode = true;
		}

		Log.d("-Fn", "**** Keyboard/checkModifierPressed(): Pressed modifier =" + keyString);

			switch (chord) {
				case 115: // Shift Down
					shiftPressed(); // Reverse swipe Shift => CAPS, or Upper case to lower case
					shiftPressed();
					break;
				case 116:
					morePressed();
					break;
				case 117:
					clearPressed();
					break;
				// Shift
				case 118: // CMBO chord for Shift (Key.B + Key.E)
				//case 58: // Old ComboKey Shift (Swipe Space to Key B on ComboKey, not pretty) - removed
					shiftPressed(); // Might later be replaced by some other function
					break;
					/*
				case 12: // Menu button (7 + 5) taken for Shift now (swipe from backspace up, nice and easy)
					//  => set Upper case, now
					if (state == 7) { // swipe upwards: shift // Note: chord 12 is also 4 + 8 = "!"
						shiftPressed();
					} else if (state == 5) { // swipe downwards from Key W (D): F1...F12
						//abc123Pressed(); //
						morePressed();
						} else {
						anyOtherKeyPressed();
					}
					break;
					*/
				// SYMB
				case 59: //45:
					emojiMode = false; fnMode = false;
					// turn SYMB off:
					if (offset == CMBOKey.SYMBOL_MODIFIER || offset == CMBOKey.AUXSET_SYMBOL_MODIFIER) {
						if (numberShift) { // numberShift is same offset as SYMB mode
							offset = auxMode ? CMBOKey.AUXSET_NUMBER_MODIFIER : CMBOKey.NUMBER_MODIFIER;
						} else {
							offset = auxMode ? CMBOKey.AUXSET_MODIFIER : CMBOKey.NONE;
						}
					} else { // turn SYMB OFF and go back to numberMode
						if ((numberMode) && (offset == CMBOKey.AUXSET_MODIFIER || offset == CMBOKey.NONE)) { // In numberMode, SYMB switches to abc and then back
							offset = auxMode ? CMBOKey.AUXSET_NUMBER_MODIFIER : CMBOKey.NUMBER_MODIFIER;
							break;
						} // turn SYMB on:
						offset = auxMode ? CMBOKey.AUXSET_SYMBOL_MODIFIER : CMBOKey.SYMBOL_MODIFIER;
						if (numberMode) { // In numberMode, SYMB switches to abc and then back
							offset = auxMode ? CMBOKey.AUXSET_MODIFIER : CMBOKey.NONE;
						}
					}
					break;
				// 123
				case 63:
					abc123Pressed();
					break;
				case 10: // Clear all text (swipe left); (swipe right = Symb: chord forced to 59 above)
					// 10 is a bit ambiguous:
					if ((state == 2) || (state == 8)) { // leave out apostrophe (chord = 10 too, 2 + 8)
						anyOtherKeyPressed(); // 2017-12-03
						break; // 2017-12-17 (break missing was a bug!)
					}

					if (auxMode) { // remember: auxmode had no devanagari or korean params, now it can have
						offset =  auxMode ? CMBOKey.AUXSET_SHIFT_MODIFIER : CMBOKey.SHIFT_MODIFIER;
					} else {
						offset = auxMode ? CMBOKey.AUXSET_MODIFIER : CMBOKey.NONE;
					}
					numberMode = false;
					numberShift = false;
					emojiMode = false; fnMode = false;
					break;

				case 119: // Emoji set, 119 is just an invented value!
					if (offset == CMBOKey.EMOJI_MODIFIER) {
						emojiMode = false; // 2017-12
						offset = auxMode ? CMBOKey.AUXSET_MODIFIER : CMBOKey.NONE;
					} else {
						emojiMode = true; // 2017-12
						offset = CMBOKey.EMOJI_MODIFIER;
					}
					numberMode = false;
					numberShift = false;
					fnMode = false;
					break;

				case 64: // Key M (or: '...')is a modifier for Devanagari character sets
					Log.d("-DEV", "*** Devanagari on?");
					if (!statusDevanagariOn()){
						Log.d("-DEV", "*** Devanagari not on (Key 'M' pressed.");
						anyOtherKeyPressed();
						break;
					}
					if (offset == CMBOKey.AUXSET_NUMBER_MODIFIER || offset == CMBOKey.NUMBER_MODIFIER ) break;

					Log.d("-DEV", "*** Devanagari on, extra modifier (Key 'M' or '...') pressed.");

					if (offset == CMBOKey.AUXSET_MODIFIER || offset == CMBOKey.NONE ){
						offset =  auxMode ? CMBOKey.AUXSET_SHIFT_MODIFIER : CMBOKey.SHIFT_MODIFIER;
					} else {
						if (offset == CMBOKey.AUXSET_SHIFT_MODIFIER || offset == CMBOKey.SHIFT_MODIFIER ){
							offset =  auxMode ? CMBOKey.AUXSET_CAPS_MODIFIER : CMBOKey.CAPS_MODIFIER;
						} else {
							if (offset == CMBOKey.AUXSET_CAPS_MODIFIER || offset == CMBOKey.CAPS_MODIFIER ){
								offset =  auxMode ? CMBOKey.AUXSET_MODIFIER : CMBOKey.NONE;
							} else {
								offset =  auxMode ? CMBOKey.AUXSET_MODIFIER : CMBOKey.NONE;
							}
						}
					}
					Log.d("CMBO", "*** Devanagari extra modifier: offset set to " + offset);
					break;

				default: // any other key pressed (not a modifier)
					if (isOtherControl(state, key)) break; // no action here on offset if arrow, lang etc.
					anyOtherKeyPressed();

			} // switch block
		Log.d("CMBO", "***** After checkModifiers: offset = " + offset + ", auxMode = " + auxMode + ", devanagariOn = " + devanagariOn);


	}

	private void morePressed() {
		Log.d("-Fn", "**** Keyboard/checkModifierPressed(): Fn pressed.");
		if (offset == CMBOKey.FN_MODIFIER) { //  EMOJI_MODIFIER) { // TODO: change into FN_MODIFIER these all
			fnMode = false; // 2019-03
			offset = auxMode ? CMBOKey.AUXSET_MODIFIER : CMBOKey.NONE;
		} else {
			fnMode = true; // 2019-03
			offset = CMBOKey.FN_MODIFIER; // EMOJI_MODIFIER;
		}
		numberMode = false;
		numberShift = false;
		emojiMode = false;
		// to be implemented
	}

	private void clearPressed() {
		// just return from checkModifierPress() with no action,
		// Clear is handled in CMBOKeyboardService
	}

	private void abc123Pressed(){
		Log.d("CMBO", "***** abc123 pressed.");

		if (offset == CMBOKey.AUXSET_NUMBER_MODIFIER || offset == CMBOKey.NUMBER_MODIFIER || numberMode || emojiMode || fnMode) {
			// Default: Return to abc from 123 mode...
			offset = auxMode ? CMBOKey.AUXSET_MODIFIER : CMBOKey.NONE;
			numberMode = false;
			numberShift = false;
		} else {
			offset = auxMode ? CMBOKey.AUXSET_NUMBER_MODIFIER : CMBOKey.NUMBER_MODIFIER;
			numberMode = true;
		}
		emojiMode = false; fnMode = false;
	}

	private void anyOtherKeyPressed() {
		if (CMBOKeyboardView.autoRepeat){
			return;
		}

		if (offset != CMBOKey.CAPS_MODIFIER // 'ABC'
				&& offset != CMBOKey.AUXSET_CAPS_MODIFIER // 'AUX'
				&& offset != CMBOKey.FN_MODIFIER // 'Fn' F1...F12 mode
				&& offset != CMBOKey.NUMBER_MODIFIER) { // '123'
			if (((offset == CMBOKey.SYMBOL_MODIFIER) || (offset == CMBOKey.AUXSET_SYMBOL_MODIFIER)) && numberShift) {
				offset = auxMode ? CMBOKey.AUXSET_NUMBER_MODIFIER : CMBOKey.NUMBER_MODIFIER;
				numberShift = false;
			} else {
				offset = auxMode ? CMBOKey.AUXSET_NUMBER_MODIFIER : CMBOKey.NUMBER_MODIFIER;
				numberShift = false;
				if (!numberMode) {
					offset = auxMode ? CMBOKey.AUXSET_MODIFIER : CMBOKey.NONE;
				}
			}
		}
	}



	public void abcPressed() { // This goes to Numbers after CAPS, unlike ShiftPressed()

		controlDown = false;
		altDown = false;

		Log.d("CMBO", "***** Shift pressed: offset = " + offset + ", auxMode = " + auxMode + ", devanagariOn = " + devanagariOn);
		emojiMode = false; fnMode = false;
		if (offset == CMBOKey.SHIFT_MODIFIER) {
			offset = CMBOKey.CAPS_MODIFIER;
		} else if (offset == CMBOKey.NONE) { // abc => Abc
			if (statusDevanagariOn()){ //  auxmode was never devanagari, now it can be
				offset = CMBOKey.CAPS_MODIFIER; // devanagari: abc -> ABC
			} else {
				offset = CMBOKey.SHIFT_MODIFIER; // aux -> Aux
			}
		} else if (offset == CMBOKey.CAPS_MODIFIER) { // ABC => abc
			//abc123Pressed(); return; // just a test
			offset = CMBOKey.NUMBER_MODIFIER;
			numberMode = true;
		} else if (offset == CMBOKey.NUMBER_MODIFIER || offset == CMBOKey.AUXSET_NUMBER_MODIFIER) { // 123 => SYMB
			offset = auxMode ? CMBOKey.AUXSET_MODIFIER : CMBOKey.NONE;
			numberMode = false;
			//numberShift = true;
		} else if (offset == CMBOKey.AUXSET_MODIFIER) {
			if (statusDevanagariOn()){ // auxmode wass never devanagari, now it can be
				offset = CMBOKey.AUXSET_CAPS_MODIFIER; // devanagari: aux -> AUX
			} else {
				offset = CMBOKey.AUXSET_SHIFT_MODIFIER; // aux -> Aux
			}
		} else if (offset == CMBOKey.AUXSET_SHIFT_MODIFIER) { // Aux => AUX
			// ->
			// AUX
			offset = CMBOKey.AUXSET_CAPS_MODIFIER; // AUX, (AuxSet3 = 'aux
			// caps')
		} else if (offset == CMBOKey.AUXSET_CAPS_MODIFIER) { // AUX
			offset = CMBOKey.AUXSET_NUMBER_MODIFIER;
			numberMode = true;
		} else {
			offset = auxMode ? CMBOKey.AUXSET_SHIFT_MODIFIER : CMBOKey.SHIFT_MODIFIER;  // abc -> Abc
		}
		Log.d("CMBO", "***** After shift: offset = " + offset + ", auxMode = " + auxMode + ", devanagariOn = " + devanagariOn);


	}


	public void shiftPressed() { // used in two places

		Log.d("CMBO", "***** Shift pressed: offset = " + offset + ", auxMode = " + auxMode + ", devanagariOn = " + devanagariOn);
		emojiMode = false; fnMode = false;
		if (offset == CMBOKey.SHIFT_MODIFIER) {
			offset = CMBOKey.CAPS_MODIFIER;
		} else if (offset == CMBOKey.NONE) { // abc => Abc
			if (statusDevanagariOn()){ //  auxmode was never devanagari, now it can be
				offset = CMBOKey.CAPS_MODIFIER; // devanagari: abc -> ABC
			} else {
				offset = CMBOKey.SHIFT_MODIFIER; // aux -> Aux
			}
		} else if (offset == CMBOKey.CAPS_MODIFIER) { // ABC => abc
			offset = CMBOKey.NONE;
		} else if (offset == CMBOKey.NUMBER_MODIFIER || offset == CMBOKey.AUXSET_NUMBER_MODIFIER) { // 123 => SYMB
			offset = auxMode ? CMBOKey.AUXSET_SYMBOL_MODIFIER : CMBOKey.SYMBOL_MODIFIER;
			numberShift = true;
		} else if (offset == CMBOKey.AUXSET_MODIFIER) {
			if (statusDevanagariOn()){ // auxmode wass never devanagari, now it can be
				offset = CMBOKey.AUXSET_CAPS_MODIFIER; // devanagari: aux -> AUX
			} else {
				offset = CMBOKey.AUXSET_SHIFT_MODIFIER; // aux -> Aux
			}
		} else if (offset == CMBOKey.AUXSET_SHIFT_MODIFIER) { // Aux => AUX
			// ->
			// AUX
			offset = CMBOKey.AUXSET_CAPS_MODIFIER; // AUX, (AuxSet3 = 'aux
			// caps')
		} else if (offset == CMBOKey.AUXSET_CAPS_MODIFIER) { // AUX
			offset = CMBOKey.AUXSET_MODIFIER;
		} else {
				offset = auxMode ? CMBOKey.AUXSET_SHIFT_MODIFIER : CMBOKey.SHIFT_MODIFIER;  // abc -> Abc
		}
		Log.d("CMBO", "***** After shift: offset = " + offset + ", auxMode = " + auxMode + ", devanagariOn = " + devanagariOn);


	}


	// if (!isOtherControl(state, key))
	private boolean isOtherControl(int state, int key) { // no change of offset if arrow, lang etc.

		if (((state == 7) || (state == 56)) && (key != 0)) return true; // swipe from backspace or space


		return false;
	}

	private boolean isMiddleButton(int key) {

		switch (key) { // Devanagari only, for consonant detection
			case 3: // O
			case 6: // S
			case 5: // W
			case 24: // G
			case 48: // K
			case 64: // M
			case 40: //  TH

				return true;

			default:
				return false;
		}

	}

	private void checkDevOffset(int state, int key) { // After outputting a character (char !== ""), not including _Shift etc. (gChar = "")

		Log.d("-DEV", "***** Before middleButton check: offset = " + offset + ", auxMode = " + auxMode + ", devanagariOn = " + devanagariOn);

		boolean middleButtonIncluded = true; // default

		if (!devanagariOn) { // This is no devanagari (or korean) alphabet? => normal offset logic
			return;
		}

		//if ((state == 7 || key == 7) || (state == 56 || key == 56) || (state + key == 18) || (state + key == 64)) { // leave out Shift etc. functions (SP or BS included)
		if ((state == 7 || key == 7) || (state == 56 || key == 56) || (state + key == 18) || (state + key == 64)) { // leave out Shift etc. functions (SP or BS included)
		//if ((state == 7 || key == 7) || (state == 56 || key == 56) || (state + key == 18) || ((state == 56) && (key == 6))) { // leave out Shift etc. functions (SP or BS included)
			Log.d("-DEV", "*** Devanagari - Function detected: offset remains " + offset);
			return;
		}

		Log.d("-DEV", "*** Devanagari/Korean on. Dev. shift logic in use.");
		if (isMiddleButton(state) || isMiddleButton(key)){ // Middlebutton is a *dark* key! (not Center!)
			middleButtonIncluded = true;
			//if ((state == 7) || (state == 56) || (state == 64)) middleButtonIncluded = false; // Swipe from Del/Space/M
		} else middleButtonIncluded = false; // ((gStateN === 8) && (gKeyN !== 8));  // false; // count in special symbols (keyM held down)

		if(middleButtonIncluded){ // Sanskrit/Hindi
			switch (offset) {  // Use caps set after consonant
				case 0: // abc
					offset = 128; // abc > ABC
					break;
				case 128: // ABC
					offset = 128; // ABC > ABC
					break;
				case 320: // aux
					offset = 448; // aux > AUX
					break;
				case 448: // AUX
					offset = 448; // AUX > aux
					break;
				default:
					break;
			}
			Log.d("-DEV", "*** Devanagari consonant detected: offset set to " + offset);
		} else {
			switch (offset) {
				case 0: // abc
					offset = 0; // abc > abc
					break;
				case 128: // ABC
					offset = 0; // ABC > abc
					break;
				case 320: // aux
					offset = 320; // aux > aux
					break;
				case 448: // AUX
					offset = 320; // AUX > aux
					break;
				default:
					break;
			}
			Log.d("-DEV", "*** No devanagari consonant detected: offset set to " + offset);

		}
		Log.d("-DEV", "***** After middleButton check: offset = " + offset + ", auxMode = " + auxMode + ", devanagariOn = " + devanagariOn);

	}


	public void playSound(int state, int key) {
		switch (state+key) {
		//case 56: // Space
		//case 7: // Backspace
		case 18: // Shift
		//case 58: // Shift (Swipe Space to Key B)
		case 45: // SYMB
		//case 63: // abc123
		//case 62: // Delete
		//case 59: // Enter (now combokey SYMB)
		case 27: // PgUp
		//case 72: // combokey enter
		case 54: // PgDn
		//case 9: // Up
		//case 36: // Down
		//case 15: // Left
		//case 57: // Right
		case 119: // 119 is an invented value for statusArea, settingsArea and languageArea
			CMBOKeyboardView.soundIndex = 6;

			break;
		//case 10: // same chord: apostrophe and Clear

		default: //

			if ((state == 7) || (state == 56)) { // all slides from space and backspace
				if (state + key == 10) {
					CMBOKeyboardView.soundIndex = 3; // Clear
				} else if ((state + key == 7) || (state + key == 31)) { // BS or Delete
					CMBOKeyboardView.soundIndex = 2;
				} else if (state + key == 56){
					if (key == 0) {  // 56+0
						CMBOKeyboardView.soundIndex = 1; // spacebar
					} else { //
						CMBOKeyboardView.soundIndex = 5; // i+n 7+49 (?)
					}
				} else {
					CMBOKeyboardView.soundIndex = 6; // functions
				}

			} else {

				if ((key == 7) && ((state == 3) || (state == 40) || (state == 24) || (state == 5))) {
					// Swipes towards Backspace from Key O, W ot G (index 2, 8 or 12)
					// possible functions to be added
					CMBOKeyboardView.soundIndex = 6;

				} else {
					CMBOKeyboardView.soundIndex = 4; // letters
				}
			}
		}
	}

	private boolean isAutoCaps() {
		return CMBOKeyboardApplication.getApplication().getPreferences()
				.isAutoCaps();
	}

	public int getOffset() {
		return this.offset;
	}
	public boolean getEmojiMode() {
		return this.emojiMode;
	}
	public boolean getFnMode() {

		return this.fnMode;
	}
	public boolean getAuxMode() {
		return this.auxMode;
	}

	public void setLangOffset(int offs, boolean aMode) {

		Log.d("CMBO", "LangOffset = " + offs);

		offset = offs;
		auxMode = aMode;

		emojiMode = false;
		fnMode = false;
		numberShift = false;
		numberMode = false;

		Log.d("-Fn", "(KEYBOARD) main/aux languages: offset = " + offset + ", aux mode (auxMode) = " + auxMode);

	}


	public void saveMode() {
		fnModeSaved = fnMode;
		emojiModeSaved = emojiMode;
		numberShiftSaved = numberShift;
		numberModeSaved = numberMode;
		offsetSaved = offset;
	}

	public void resumeMode() {
		emojiMode = emojiModeSaved;
		fnMode = fnModeSaved;
		numberShift = numberShiftSaved;
		numberMode = numberModeSaved;
		offset = offsetSaved;
		numberModeTemp = false;
		symbolModeTemp = false;
	}


	public void setEmojiMode() {
		Log.d("CMBO", "Emoji mode set.");
		emojiMode = true;
		fnMode = false;
		numberShift = false;
		numberMode = false;
		offset = auxMode ? CMBOKey.EMOJI_MODIFIER : CMBOKey.EMOJI_MODIFIER;
	}

	public void setFnMode(boolean on) {
		if (on) {
			Log.d("-Fn", "(keyboard) Fn mode set ON.");
			fnMode = true;
		}
		Log.d("-Fn", "(keyboard) Fn mode set OFF.");
		fnMode = false;
	}


	public void setNumberModeTemp() {
		Log.d("CMBO", "Number mode set.");
		saveMode();
		emojiMode = false; fnMode = false;
		numberShift = false;
		numberMode = true;
		numberModeTemp = true;
		offset = auxMode ? CMBOKey.AUXSET_NUMBER_MODIFIER : CMBOKey.NUMBER_MODIFIER;
	}

	public void setNumberMode() {
		Log.d("CMBO", "Number mode set.");
		emojiMode = false; fnMode = false;
		numberShift = false;
		numberMode = true;
		numberModeTemp = false;
		offset = auxMode ? CMBOKey.AUXSET_NUMBER_MODIFIER : CMBOKey.NUMBER_MODIFIER;
	}

	public void setSymbolModeTemp() {
		Log.d("CMBO", "Symbol mode set.");
		saveMode();
		emojiMode = false; fnMode = false;
		numberShift = false;
		numberMode = false;
		symbolModeTemp = true;
		offset = auxMode ? CMBOKey.AUXSET_SYMBOL_MODIFIER : CMBOKey.SYMBOL_MODIFIER;
	}

	public void setShift() {
		Log.d("CMBO", "Upper Case set.");
		emojiMode = false; fnMode = false;
		numberShift = false;
		numberMode = false;
		offset = auxMode ? CMBOKey.AUXSET_SHIFT_MODIFIER : CMBOKey.SHIFT_MODIFIER;
	}

	public void resetShift() {
		Log.d("CMBO", "Lower Case set.");
		emojiMode = false; fnMode = false;
		numberShift = false;
		numberMode = false;
		offset = auxMode ? CMBOKey.AUXSET_MODIFIER : CMBOKey.NONE;
	}

	public int getKeyCode(String c) {
		Integer code = keyCodes.get(c);
		if (code != null)
			return code;
		return Integer.MAX_VALUE;
	}


	//public String getStringExtended(int offset, int state, int key) {
	public String getString(int offset, int state, int key) {

		if (!CMBOKey.isValidCombination(state, key)
				|| !CMBOKey.isValidChord(offset, state, key))
			return "";

		return CMBOKeyboardApplication.getApplication().getLayoutManager()
				.getStringRepresentation(offset, state, key);

		//return "#";

	}


}
