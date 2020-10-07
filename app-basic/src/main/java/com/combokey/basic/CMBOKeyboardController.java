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

import android.content.SharedPreferences;

import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.combokey.basic.view.CMBOButton;

// Note: This class is a very sensitive part of touch management, please avoid editing!
// This deals with combos, swipes, chords, taps, distinguishing between overlapping taps
// and combos etc. This is a result of a lot of testing and typing.
//
// typeMethod 1: Taps Swipes AND Combos (check max allowed overlapping percentage)
// typeMethod 2: Just Swipes, no Combos
// typeMethod 3: Just Combos, no Swipes


public class CMBOKeyboardController {

	public static String TAG = "-CONTR";

	private final static String[] PROBLEMATIC_MODELS = new String[] {
			"Nexus One", "HTC Desire", "HTC Aria", "HTC Hero", "ZTE Blade" };

	private boolean[] down = new boolean[2];
	private CMBOKeyboardMode mode = CMBOKeyboardMode.UNDEFINED;

	public boolean peekPress = false; // a press just to peek characters
	private int onehandCombo = 2; // Settings: two taps instead of a (long) swipe is allowed, 0 = off, 1 = hold shorter, 2 = hold longer
	private boolean holdFirstKey = false; // (two taps) holding first key a while after release

	private boolean skipRelease = false;

	private long stateMillis = 0;
	private long keyMillis = 0;
	private long pressMillis = 0;

	private int state = CMBOKey.NONE;

	private int state0 = CMBOKey.NONE;
	private int state1 = CMBOKey.NONE;

	public int getState() {
		// TODO check
		return this.state;
	}

	public int getState0() {
		// TODO check
		return this.state0;
	}
	public int getState1() {
		// TODO check
		return this.state1;
	}

	public int getStateIndex() {
		// TODO check
		return CMBOKey.getIndexForChord(this.state);
	}

	public int getState0Index() {
		// TODO check
		return CMBOKey.getIndexForChord(this.state0);
	}
	public int getState1Index() {
		// TODO check
		return CMBOKey.getIndexForChord(this.state1);
	}

	CountDownTimer holdTimer = new CountDownTimer(1500, 400) {
		// (time in ms to onFinish(), Tick interval while counting)
		@Override
		public void onTick(long l) {
			Log.d("-TIMER", "- Hold timer onTick");
		}
		@Override
		public void onFinish() {
			Log.d("-TIMER", "- Hold timer done, stop holding first press.");
			resetHoldKey();
		}
	};


	// The timer will allow some (50%) overlap of two short consecutive key
	// presses. If they overlap more they are considered simultaneous key
	// presses (i.e. a combo), if Combo typing method is not disabled.

	private void resetStateTimer() {
		stateMillis = SystemClock.uptimeMillis();
	} // overlap
	private void resetKeyTimer() {
		keyMillis = SystemClock.uptimeMillis();
	} // overlap
	private void resetPressTimer() { // peek
		peekPress = true; // default is a long tap! It will be changed to false in due course
		pressMillis = SystemClock.uptimeMillis();
		Log.d("-TOUCH-OV", "==== RESET pressTimer ===");
	} // detect a check of hidden characters: long press without swiping or second key


	private int getStateTimer() {
		return (int) (SystemClock.uptimeMillis() - stateMillis);
	} // keyTimer must be much larger (4 x) than stateTimer if keys only overlap (do not form a chord)
	private int getKeyTimer() {
		return (int) (SystemClock.uptimeMillis() - keyMillis);
	}
	private int getPressTimer() {  // update pressTimer to read time elapsed from press to release
		return (int) (SystemClock.uptimeMillis() - pressMillis);
	} // If pressTimer > 750, it will be a peek press (peekPress = true)

	private int previousKey = CMBOKey.NONE;
	private int previousState = CMBOKey.NONE;

	private int previousKey0 = CMBOKey.NONE;
	private int previousState0 = CMBOKey.NONE;
	private int previousKey1 = CMBOKey.NONE;
	private int previousState1 = CMBOKey.NONE;



	public boolean showTempHelp = false; // show Tips/help a while after a peek press (peekPress)
	private boolean enableOutput = true;

	/**
	 * If the state is not NONE, holds the other key pressed, if any
	 */
	private int key = CMBOKey.NONE;

	private int key0 = CMBOKey.NONE;
	private int key1 = CMBOKey.NONE;

	public int getKey() {
		return key;
	}
	public int getKeyIndex() { return CMBOKey.getIndexForChord(key); }

	public int getKey0() {
		return key0;
	}
	public int getKey0Index() { return CMBOKey.getIndexForChord(key0); }
	public int getKey1() {
		return key1;
	}
	public int getKey1Index() { return CMBOKey.getIndexForChord(key1); }

	private void initialize() {

		// TODO refactor

		SharedPreferences prefs = CMBOKeyboardApplication.getApplication()
				.getPreferences().get();

	}

	private boolean isCombo() {

		long stateTimer = getStateTimer();
		long keyTimer = getKeyTimer();

		if (keyTimer - stateTimer > 5) return false; // (just one key down, don't bother)

		Log.d("-TOUCH-OVERLAP", "Overlap counters (stateTimer/keyTimer): " + stateTimer + "/" + keyTimer);

		if ((keyTimer > stateTimer/3) || (keyTimer + stateTimer < 150)){
			Log.d("-TOUCH-OVERLAP", "- COMBO **** (Large overlap) -A-");
			return true;
		} else {
			Log.d("-TOUCH-OVERLAP", "- OVERLAPPING SINGLE TAPS -B-");
			return false;
		}
	}


	private int typeMethod = 1; //CMBOKeyboardApplication.getApplication().getPreferences().getTypeMethod();

	public boolean processKeyPress(int key, int pointerIndex) { //
		// typeMethods: 1 = Swipes and Combos, 2 = Swipes only, 3 = Combos only
		// ALWAYS NEW METHODS IN USE (typeMethods 1, 2, 3)
		Log.d("-TOUCH", "=== Swipes only or Combos only (new).");
		return processKeyPressNew(key, pointerIndex);
	}

	public void release(int pointerIndex) {
		// typeMethods: 1 = Swipes and Combos, 2 = Swipes only, 3 = Combos only
		// ALWAYS USE NEW METHODS (typeMethods 1, 2, 3)
		// set typeMethod here (is this the best place?):
		previousKeyDown = 0; // bug fix for v4.8
		typeMethod = CMBOKeyboardApplication.getApplication().getPreferences().getTypeMethod();
		releaseNew(pointerIndex); // Taps/Swipes/Combos, Taps
	}

	private boolean swiping0 = false;
	private boolean swiping1 = false;
	private boolean readyForOutput = false;
	private int previousKeyDown = 0;


	private boolean processKeyPressNew(int key, int pointerIndex) {
		// Return true if not repeating the same key area

		if (pointerIndex > 1) {
			stateMillis = 0; keyMillis = 0; readyForOutput = false;
			return false;
		}
		down[pointerIndex] = true;

		Log.d("-TOUCH", ">>>> KeyPress: key " + key + ", pointerIndex " + pointerIndex);

		if (pointerIndex == 0) { // ------ key0 / state0 ----------- POINTER 0 ------

			Log.d("-TOUCH-OV", "==== previousKeyDown/key: " + previousKeyDown + "/" + key);
			Log.d("-TOUCH", "previousKey0:      " + previousKey0);
			if (previousKey0 == key) {
				return false;	// E.g. Enables: hold 'O' and press 'P' several
								// times without typing 'O' after last release .
			}


			if (previousKeyDown != key) {
				holdTimer.cancel(); // bug fix for v4.8
				Log.d("-TOUCH-OV", "==== Hold Timer cancel ===");
			}
			previousKeyDown = key;

			Log.d("-TOUCH-OV", "================= DOWN 0 ================");

			if (typeMethod == 3) { // ---------- No swipes, just Combos (POINTER 0)

				readyForOutput = true; // No char output if no key pressed down after previous output
				Log.d("-TOUCH", "=== Just Combos");

				if ((previousState0 == 0) && oneDown()) { // Lock this key and state
					Log.d("-TOUCH", "=== Just Combos (State A, lock state)");
					this.state = key; // get first location as the state and keep it
					//this.key = 0;
					this.state0 = key;
					this.key0 = key;

					resetStateTimer(); // New Timer Code: state starts
					resetPressTimer(); // New press starts, monitor long press on a single key (peek hidden letters)
				}

				showKeysAndStates();

				previousKey0 = key0;
				previousState0 = state0;

				return true;

			} else if (typeMethod == 2) { // --------- Just Swipes, no Combos (POINTER 0)

				readyForOutput = true; // No char output if no key pressed down after previous output
				Log.d("-TOUCH", "=== Just Swipes");

				if ((previousState0 == 0) && oneDown()) {
					Log.d("-TOUCH", "=== Just Swipes (state A, keep state)");
					this.state = key; // get first location as the state and keep it
					//this.key = 0;
					//this.key0 = key;
					this.state0 = key;
					this.key0 = 0;
					resetStateTimer(); // New Timer Code: state starts
					resetPressTimer(); // New press starts, monitor long press on a single key (peek hidden letters)

				} else if ((previousState0 != key) && oneDown()) { // a hack to prevent faulty index

					Log.d("-TOUCH", "=== Just Swipes (state B, swiping ON)");
					if (!swiping0) { // no swipe occurred yet with this pointer index
						//this.state0 = previousKey0; // lock state
						swiping0 = true;
						peekPress = false;
					}

					this.key0 = key; // safer have this here
					this.key = key;
				}

				//this.key = key0; // highlight
				int offset = getKeyboardOffset();
				this.key0 = CMBOKey.mirrorIfOnSameSideColumn(this.state0, key0, offset);
				if (swiping0) this.key = key0; // highlight only if index 0 has been sliding to a new key

				showKeysAndStates();

				previousKey0 = key0;
				previousState0 = state0;

				return true;
				// this will output the letter as defined by state and key:
				// getKeyboard().handleKeyPress(this.state, this.key);


			} else if (typeMethod == 1) { // --------- Taps Swipes AND Combos (POINTER 0)

				readyForOutput = true; // No char output if no key pressed down after previous output
				Log.d("-TOUCH", "=== Swipes AND Combos");

				if ((previousState0 == 0) && oneDown()) { // Lock this key and state
					Log.d("-TOUCH", "=== Swipes AND Combos (State A, lock state)");
					this.state = key; // get first location as the state and keep it
					//this.key = 0;
					this.state0 = key;
					this.key0 = key;

					resetStateTimer(); // New Timer Code: state starts
					resetPressTimer(); // New press starts, monitor long press on a single key (peek hidden letters)

				} else if ((previousState0 != key) && oneDown()) { // a hack to prevent faulty index

					Log.d("-TOUCH", "=== Just Swipes (state B, swiping ON)");
					if (!swiping0) { // no swipe occurred yet with this pointer index
						//this.state0 = previousKey0; // lock state
						swiping0 = true;
						peekPress = false;
					}

					this.key0 = key; // safer have this here
					//this.key = key;

					int offset = getKeyboardOffset();
					this.key0 = CMBOKey.mirrorIfOnSameSideColumn(this.state0, key0, offset);
					this.key = key0; // highlight as well

				}

				showKeysAndStates();

				previousKey0 = key0;
				previousState0 = state0;

				return true;

			}


			//  another case of typeMethod later? Taps, Swipes and Combos
			return true;


		} else {  // pointerIndex == 1 //  ------ key1 / state1 ------------- POINTER 1 ----------

			Log.d("-TOUCH", "previousKey1:      " + previousKey1);
			if (previousKey1 == key) return false;

			Log.d("-TOUCH-OV", "================= DOWN 1 ================") ;


			if (typeMethod == 3) { // --------------- just Combos, no Swipes (POINTER 1)

				readyForOutput = true; // No char output if no key pressed down after previous output
				Log.d("-TOUCH", "=== Just Combos") ;

				if (previousKey1 == 0) { // Lock this key
					Log.d("-TOUCH", "=== Just Combos (State A, lock key)") ;
					this.key = key;
					this.key1 = key;

					resetKeyTimer(); // Second key pressed down
					peekPress = false;
				}

				showKeysAndStates();

				previousKey1 = key;
				previousState1 = state1;

				return true;

			} else if (typeMethod == 2) { // ---- Just Swipes, no Combos (POINTER 1)

				readyForOutput = true; // No char output if no key pressed down after previous output
				Log.d("-TOUCH", "=== Just Swipes");
				// This case is not possible because no second hand swiping is allowed here

				if ((previousKey1 != 0) && (previousKey1 != key) && (key != this.state0)) {
					Log.d("-TOUCH", "=== Just Swipes (State A, set swiping1 true") ;
					// if swiping the second hand, and hack off any wrong pointer value
					if (!swiping1) { // no swipe occurred yet with this pointer index
						swiping1 = true;
						peekPress = false;
					}
				}


				if ((key != this.key0) && (!swiping1)){ // if swipes in use (3 = Combos only, no Swipes)
					Log.d("-TOUCH", "=== Just Swipes (State B, keep key)");

					this.key1 = key; // get first location as the key and keep it. This is only
					// for the case of having the next key pressed before the previous is released.
					this.key = key; // to highlight

					resetKeyTimer(); // Second key pressed down
					peekPress = false;
				}

				showKeysAndStates();

				previousKey1 = key1;
				previousState1 = state1;

				return true;

			} else if (typeMethod == 1) { // ---- Taps, Swipes AND Combos (POINTER 1)

				readyForOutput = true; // No char output if no key pressed down after previous output
				Log.d("-TOUCH", "=== Swipes AND Combos") ;

				if ((previousKey1 == 0) && (key != this.state0)) { // Lock this key
					Log.d("-TOUCH", "=== Swipes AND Combos (State A, lock key)") ;
					this.key = key;
					this.key1 = key;

					resetKeyTimer(); // Second key pressed down
					peekPress = false;
				}

				showKeysAndStates();

				previousKey1 = key;
				previousState1 = state1;

				return true;

			}

			// another case of typeMethod later? Taps, Swipes and Combos
			return true;

		}
	}


	private void showKeysAndStates() {
		Log.d("-TOUCH-OV", "state0 = " + state0 + ", key0 = " + key0 + ", swiping0 = " + swiping0) ;
		Log.d("-TOUCH-OV", "state1 = " + state1 + ", key1 = " + key1 + ", swiping1 = " + swiping1) ;
		Log.d("-TOUCH-OV", "state  = " + state + ", key  = " + key + ", readyForOutput = " + readyForOutput) ;
		Log.d("-TOUCH-OV", "=================================================") ;
	}


	/**
	 * Should be called when the keypress should be sent forward.
	 * This is done using the state and key variables.
	 *
	 */
	private boolean isValidPressDuration() { // Note: Check combos only!
		// Minimum time for accepting an Up/PgUp combo
		if (((key0 == 1 || key0 == 8) && (key0 + key1 == 9)) || (key0 + key1 == 27)) {
			Log.d("-TOUCH-OVERLAP", "=== UpArrow or PageUp === getKeyTimer() = " + getKeyTimer()) ;
			if (getKeyTimer() > 200) {
				Log.d("-TOUCH-OVERLAP", "=== more than 200 ms");
				return true; // accepted
			} else {
				Log.d("-TOUCH-OVERLAP", "=== less than 200 ms");
				showToast(CMBOKeyboardApplication.getApplication().getResources()
						.getString(R.string.press_longer));
				return false; // not long enough press of PgUp or UpArrow
			}
		}	// note: 9 = 1+8 (= Up) or 4+5 (= key indices 5 and 6); 27 = 3 + 24
		Log.d("-TOUCH-OVERLAP", "=== any other combo ===");
		return true; // Any other combo is valid anyway
	}

	private void releaseNew(int pointerIndex) {

		onehandCombo = CMBOKeyboardApplication.getApplication().getPreferences().onehandCombo();

		if (pointerIndex > 1) {
			Log.d("-TOUCH-OV", "===  pointerIndex > 1  !!! (third finger) ===");
			stateMillis = 0; keyMillis = 0; readyForOutput = false;
			return; // not a valid touch
		}
		Log.d("-TOUCH", "<<<< KeyRelease. pointerIndex " + pointerIndex);
		down[pointerIndex] = false; // valid touch up
		//this.state = 0; // just to redraw, no more highlight

		if ((getPressTimer() > onehandCombo * 200) && (peekPress)) { // Hold first key down after key release
			// 0 * 200 ms, 1 * 200 ms or 2 * 200 ms
			if (getPressTimer() > 750) { // Subjective experimental value = 750 ms
				peekPress = true; // Finally, a long press on a single key
				// Note! Default is a long tap! It can be changed to false below and in counter reset
				checkTempHelp(); // Show tips/help, too, for a while
				holdFirstKey = false;
				Log.d("-TOUCH-OV", "==== LONG press (peekPress true) ==== " + getPressTimer() + " ms ====");
				//holdTimer.start();
				//resetHoldKey();
			} else {

				holdFirstKey = (onehandCombo != 0) && (typeMethod != 3); // no hold if just 2-hand combos in use (=3)
				if (holdFirstKey) {
					holdTimer.cancel(); // after v4.8 to fix bug
					holdTimer.start();
					Log.d("-TOUCH-OV", "==== Hold Timer start ====");
				} else { // after v4.8 to fix bug
					holdTimer.cancel();
					Log.d("-TOUCH-OV", "==== Hold Timer cancelled ====");
				}
				peekPress = false; // after v4.8 to fix bug
				showTempHelp = false; // after v4.8 to fix bug
				Log.d("-TOUCH-OV", "==== MEDIUM long press (holdFirstKey " + holdFirstKey + ") ==== " + getPressTimer() + " ms ====");
				Log.d("-TIMER", "holdFirstKey, onehandCombo = " + holdFirstKey + ", " + onehandCombo);
			}

		} else {

			peekPress = false; // Due to low timer value or swipe or second press
			showTempHelp = false; // No temporary show of tips/help
			Log.d("-TOUCH-OV", "==== SHORT/OTHER press (peekPress false) ==== (tap) " + getPressTimer() + " ms ====");
		}

		if (pointerIndex == 0) { // ----- key0 / state0 ------------

			Log.d("-TOUCH-OV", "================= UP 0 ================== ready/swipe/peek/hold: " + readyForOutput + "/" + swiping0 + "/" + peekPress + "/" + holdFirstKey);


			if (typeMethod == 3) { // Just Combos and Taps
				//if (readyForOutput && !swiping0) getKeyboard().handleKeyPress(key0, key1);
				if (readyForOutput && !swiping0 && !peekPress && !holdFirstKey) {
					if (isCombo()) {
						if (isValidPressDuration()) {
							getKeyboard().handleKeyPress(state0, key1);
							setPreviousState(state0); setPreviousKey(key1);
						}
					} else if (!peekPress && !holdFirstKey) {
						getKeyboard().handleKeyPress(state0,0);
						getKeyboard().handleKeyPress(key1, 0);
						setPreviousState(key1); setPreviousKey(0);
					}
				}

			readyForOutput = false; // new key down required for next output

			} else if (typeMethod == 2) { // Just Swipes and Taps
				if (!peekPress && !holdFirstKey) {
					getKeyboard().handleKeyPress(state0, key0);
					setPreviousState(state0); setPreviousKey(key0); // Needed for autorepeat
				}
				//Note: leave out "readyForOutput = false" to have independent overlapping taps

			} else if (typeMethod == 1) { // All (legacy)

				if (readyForOutput && !swiping0 && !peekPress && !holdFirstKey) {
					if (isCombo()) {
						if (isValidPressDuration()) getKeyboard().handleKeyPress(state0, key1);
						setPreviousState(state0); setPreviousKey(key1);
					} else {
						getKeyboard().handleKeyPress(state0,0);
						getKeyboard().handleKeyPress(key1, 0);
						setPreviousState(state0); setPreviousKey(0);
					}

				} else if (readyForOutput && swiping0 && !peekPress && !holdFirstKey) {

					getKeyboard().handleKeyPress(state0, key0);
					setPreviousState(state0); setPreviousKey(key0);
				}

				readyForOutput = false; // new key down required for next output
			}

			key0 = 0;
			if (!holdFirstKey) state0 = 0;
			swiping0 = false;
			if (!holdFirstKey) this.state = 0; // to display no highlight
			this.key = 0;
			holdFirstKey = false;

		} else {				// ------ key1 / state1 --------------

			holdFirstKey = false;

			Log.d("-TOUCH-OV", "================= UP 1 ================== ready/swipe/peek/hold: " + readyForOutput + "/" + swiping0 + "/" + peekPress + "/" + holdFirstKey) ;

			if (typeMethod == 3) { // Just Combos and Taps
				//if (readyForOutput && !swiping1) getKeyboard().handleKeyPress(key0, key1);
				if (readyForOutput && !swiping1 && !peekPress) {
					// Note: Do not check overlap when second finger is taken up first!
					// It is always a Combo!
					if (isValidPressDuration()) {
						getKeyboard().handleKeyPress(state0, key1);
						setPreviousState(state0); setPreviousKey(key1); // Needed for autorepeat
					}
				}
				readyForOutput = false;

			} else if (typeMethod == 2) { // Just Swipes and Taps

				if (readyForOutput && !swiping1 && !peekPress) {
					getKeyboard().handleKeyPress(state1, key1);
					setPreviousState(state1); setPreviousKey(key1); // Needed for autorepeat
				}
				//Note: leave out "readyForOutput = false" to have independent overlapping taps

			} else if (typeMethod == 1) { // Taps, Swipes AND Combos (recommended way)

				if (readyForOutput && !swiping1 && !peekPress) {
					// Note: Do not check overlap when second finger is taken up first!
					// It is always a Combo!
					if (isValidPressDuration()) {
						getKeyboard().handleKeyPress(state0, key1);
						setPreviousState(state0); setPreviousKey(key1); // Needed for autorepeat
					}
				}
				readyForOutput = false;

			}


			key1 = 0;
			state1 = 0;
			swiping1 = false;
			this.key = 0;
		}

		previousKey0 = key0;
		previousState0 = state0;
		previousKey1 = key1;
		previousState1 = state1;

		showKeysAndStates();

	}

	private void resetHoldKey() {

		Log.d("-TIMER", "state0 key0 state1 key1 holdFirstKey= " + state0 + " " + key0 + " " + state1 + " " + key1 + " " + holdFirstKey);
		Log.d("-TOUCH-OV", "==== HOLD Timer up ==== Reset holdFirstKey ====");
		holdFirstKey = false;

		if (key0 == 0) { // do not change state if second key is already down (= while pressing the key)
			state0 = 0;
			this.state = 0; // to display no highlight
			previousKey0 = 0;
			previousState0 = 0;
		}
	}

	private void setPreviousState(int prevState) {
 		this.previousState = prevState;
	}
	private void setPreviousKey(int prevKey) {
		this.previousKey = prevKey;
	}

	private boolean isSwipesOnly() { // only swipes and single touches allowed so that largely
	// overlapping key presses are welcome, e.g. simultaneous 1 + r does not give '.' but 'lr' or 'rl'
		return CMBOKeyboardApplication.getApplication().getPreferences()
				.isSwipesOnly();
	}

	private boolean isCombosOnly() {
		return CMBOKeyboardApplication.getApplication().getPreferences()
				.isCombosOnly();
	}

	private boolean isSingleHandEnabled() {
		return CMBOKeyboardApplication.getApplication().getPreferences()
				.isSingleHandEnabled();
	}

	private boolean isKeysDisabled() {
		return CMBOKeyboardApplication.getApplication().getPreferences()
				.isKeysDisabled();
	}

	private void checkTempHelp() { // other keys than SP, BS, 'M' pressed
		Log.d("-TOUCH", "---- showTempHelp: state = " + getState());
		if ((getState() != CMBOKey.BACKSPACE) && (getState() != CMBOKey.SPACE && (getState() != CMBOKey.M))) {
			showTempHelp = true;
		}
	}

	private CMBOKeyboard getKeyboard() {
		return CMBOKeyboardApplication.getApplication().getCMBOManager()
				.getKeyboard();
	}

	public boolean bothDown() {
		return this.down[0] && this.down[1];
	}

	public boolean someDown() {
		return this.down[0] || this.down[1];
	}

	private boolean bothUp() {
		return !this.down[0] && !this.down[1];
	}

	public boolean oneDown() {return this.down[0] ^ this.down[1];} // ^ = xor (both bitwise and logical)

	public boolean oneDown2() {return down[0] ^ down[1];}

	public int getKeyboardOffset() {
		return getKeyboard().getOffset();
	}
	public boolean numberMode() {return getKeyboard().numberMode;}
	public boolean getKeyboardFnMode() {return getKeyboard().getFnMode();}
	public void setKeyboardFnMode(boolean on) {
		Log.d("-Fn", "(controller) setKeyboardFnMode(false)");
		getKeyboard().setFnMode(on);
	}

	public String getButtonString(int state, int key) {
		return getKeyboard().getString(getKeyboardOffset(), state, key);
	}

	public String getButtonStringUnpressed(CMBOButton button) {
		return getKeyboard().getString(getKeyboardOffset(), 0, button.getId());
	}

	public String getButtonString(CMBOButton button) {
		return getKeyboard().getString(getKeyboardOffset(), this.state, button.getId());
	}

	public enum CMBOKeyboardMode {
		INVALID, UNDEFINED, SINGLE_TOUCH, MULTI_TOUCH
	}


	private void setEmojiMode() { getKeyboard().setEmojiMode(); }
	private void setNumberMode() { getKeyboard().setNumberMode(); }
	private void setNumberModeTemp() { getKeyboard().setNumberModeTemp(); }
	private void setSymbolModeTemp() {
		this.state = 0;
		this.key = 64;
		getKeyboard().setSymbolModeTemp();
	}



	public void checkDevOffsetMode() {
		getKeyboard().checkDevanagariEtc();
	}
	public boolean statusDevanagariOn() {
		return getKeyboard().statusDevanagariOn();
	}

	public boolean isCtrlDown() {
		return getKeyboard().isCtrlDown();
	}
	public boolean isAltDown() {
		return getKeyboard().isAltDown();
	}
	public boolean isAltMode() {
		return getKeyboard().isAltMode();
	}
	public void resetKeyboardShift() {
		getKeyboard().resetShift();
	}
	public void setKeyboardShift() {
		getKeyboard().setShift();
	}
	public void setKeyboardNumberMode() {
		getKeyboard().setNumberMode();
	}
	public void setCtrl(boolean on) {
		getKeyboard().setCtrl(on);
	}
	public void setAlt(boolean on) {
		getKeyboard().setAlt(on);
	}
	public void setAltMode(boolean on) {
		getKeyboard().setAltMode(on);
	}

	public void sendAutoRepeatCombo() { // Send the key/combo currently being pressed
		getKeyboard().repeatStateAndKey(this.state, this.key);
	}

	public boolean isAutoRepeatCombo() { // Is the key/combo currently pressed autorepeatable if held down

		if ((this.state == 56) && (getKeyboardOffset() != CMBOKey.EMOJI_MODIFIER)

		){ // Swipe from Space button
			switch (this.key) {
				case 6: // left
				case 48: // right
				case 40: // up
				case 64: // down
					Log.d("-RPT", "Repeat arrow keys.");
					return true;
				default:
					return false;
			}
		}
		// only combos bring us here
		//Log.d("-RPT", "No reason to repeat the character.");
		return false;
	}


	public void autoRepeat(String rptChar) { // used when abc indicator or Repeat combo is pressed long


		String charOut = rptChar; // repeat given text/char (for: arrow combo pressed long)

		if (rptChar.equals(""))  { // rptChar = ""

			// current button press:

			if (isAutoRepeatCombo()) { // First is the pressed key/combo autorepeatable? e.g. arrow key
				sendAutoRepeatCombo(); // Sends the combo or key currently being pressed
				return;
			}

			// Sends previous button press because "_Repeat" combo is held down:

			if ((previousState == CMBOKey.NONE) && (previousKey == CMBOKey.NONE)
					|| (!CMBOKey.isValidCombination(previousState, previousKey))) {
				return;
			}

			charOut = getKeyboard().getPreviousCharacter(); // repeat previous text/char (Abc pressed)

			if (charOut.startsWith("_") && charOut.length() != 1) { // do not output e.g. '_Del' but corresponding function

				if (CMBOKey.isAutoRepeatable(previousState, previousKey, charOut)) getKeyboard().repeatKeypress();

			} else { // use the string presentation to deal with Abc => abc change etc.

				if (CMBOKey.isAutoRepeatable(previousState, previousKey, charOut)
						&& charOut.length() < 5) getKeyboard().repeatKeypress();
			}


		} else {	// rptChar != ""

			if ((state == CMBOKey.NONE) && (key == CMBOKey.NONE)
					|| (!CMBOKey.isValidCombination(state, key))) {
				return;
			}

			charOut = rptChar; // repeat given text/char (Abc not pressed)

			if (charOut.startsWith("_") && charOut.length() != 1) { // do not output e.g. '_Del' but corresponding function

				if (CMBOKey.isAutoRepeatable(state, key, charOut)) getKeyboard().handleKeyPress(state, key);

			} else { // use the string presentation

				if (CMBOKey.isAutoRepeatable(state, key, charOut)) getKeyboard().outputCharacter(charOut);
				// Note: state and key are no longer used in isAutoRepeatable()
			}


		}

		//limit autorepeat to certain characters
	}

	public void stopAutorepeat() {  // used when abc is pressed long and then released
		// May be used later
		//previousState = CMBOKey.NONE;
		//previousKey = CMBOKey.NONE;
	}

	private void showToast(String message) {
		Toast toast;
		toast = Toast.makeText(CMBOKeyboardApplication.getApplication().getApplicationContext(),
				message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER_VERTICAL| Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();
	}

}
