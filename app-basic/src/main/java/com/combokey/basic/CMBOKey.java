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

import android.util.Log;

public class CMBOKey {
	private static final int chordToIndex[] = { 0, // index/chord of single buttons
			1, 3, 2, 5, 6,  4, 7, 11, 0, 0, // 10
			0, 0, 0, 0, 0,  13, 0, 0, 0, 0, // 20
			0, 0, 0, 12, 0, 0, 0, 0, 0, 0,  // 30
			0, 15, 0, 0, 0, 0, 0, 0, 0, 8,  // 40
			0, 0, 0, 0, 0,  0, 0, 14, 0, 0, // 50
			0, 0, 0, 0, 0,  9, 0, 0, 0, 0,  // 60
			0, 0, 0, 10, 0, 0, 0, 0, 0, 0,  // 70
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  // 80
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  // 90
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  // 100
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0   // 110
	};
	private static final int indexToChord[] = { 0, // index/chord of single buttons
			1,  3,  2,  6,  4,   // Left column   (a  o  u  s  c)
			5,  7, 40, 56, 64,   // Center column (d  BS l  SP m)
			8, 24, 16, 48, 32    // Right column  (i  t  e  n  r)
	};
	// 128: two more aux sets added (123 and SYMB) later
	private static final int chordToRef[] = {
			0, 1, 2, 15, 3, 27, 19, 46, 4, 42, 36, //10
			16, 33, 28, 20,	46, 5, 35, 59, 17, 32, // 20
			29, 21, 46, 7, 8, 9, 44, 10, 41, 38,   // 30
			57, 6, 34, 31, 18, 43, 30, 22, 46, 23, // 40
			24, 25, 58, 26,	60,	40, 46, 11, 12, 13, // 50 // Ctrl = 62, 46 = Backspace, 50 = space, 27 = TH/saksal.U, 58 = Old Shift (SP + u)
			37, 14, 39, 45, 63, 50, 48, 55, 60, 49, // 60 // the first and the two last item on this line edited due to combokey
			50, 47, 61,	64,  // spare 50 (space) was 55 (Tab) // Combokey: added key m chord 64, ref 64
			528+128, 531+128, 537+128, 534+128, 529+128, 540+128, // 70  // extra symbols (emojis not included, just Key M as modifieer)
			538+128, 530+128, 9, 10, 1, 2, 3, 4, 5, 533+128, // 80
			7, 8, 9, 10, 1, 2, 3, 539+128, 5, 6, // 90 // 539+128
			7, 8, 9, 10, 1, 536+128, 3, 4, 5, 6, // 100
			7, 8, 9, 532+128, 1, 2, 3, 4, 5, 6, 7, // 110 // 532+128
			542+128, 9, 10, 1, 2, 3, 4, 5, 541+128, 7, // 120
			8, 9, 10, 1, 2, 3, 4, 535+128 // 128
			};
	// 128: two more aux sets added (123 and SYMB) later
	private static final int chordToRefKeyBackspace[] = { 0, // Key Backspace as a modifier (chord = state + key, except for key 64: chord = key)
			1, 2, 3, 4, 5, 6, 7, 543+128, 546+128, 552+128, //
			549+128, 544+128, 555+128, 46, 545+128, 6, 7, 8, 9, 10, // ref 46 = Backspace
			1, 2, 548+128, 4, 5, 6, 7, 8, 9, 10,
			554+128, 2, 3, 4, 5, 6, 7, 8, 551+128, 10, // 40
			1, 2, 3, 4, 5, 6, 547+128, 8, 9, 10,
			1, 2, 3, 4, 557+128, 6, 7, 8, 9, 10,
			1, 2, 556+128, 550+128, 5, 6, 7, 8, 9, 10, // 64 & 71 = Ctrl (550)
			550+128, 2, 3, 4, 5, 6, 7, 8, 9, 10,
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10
	};
	// 128: two more aux sets added (123 and SYMB) later
	private static final int chordToRefKeySpace[] = { 0, // Key Space as a modifier (chord = state + key, except for key 64: chord = key)
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
			1, 2, 3, 4, 5, 571+128, 7, 8, 9, 10, // 50
			1, 2, 3, 4, 5, 6, 558+128, 561+128, 567+128, 564+128, // ...60 //
			559+128, 570+128, 568+128, 560+128, 5, 6, 7, 8, 9, 10, // 565 < 560
			1, 563+128, 3, 4, 5, 6, 7, 8, 9, 569+128, // 80
			1, 2, 3, 4, 5, 6, 7, 566+128, 9, 10, // 90
			1, 2, 3, 4, 5, 562+128, 7, 8, 9, 10, // 100
			1, 2, 3, 572+128, 5, 6, 7, 8, 9, 10, // 110
			1, 50, 3, 4, 5, 6, 7, 8, 9, 565+128 // 120 // 64 & 120 = _Down (565) // // ref 50 = Space

	};
			// Swipe from Key TH (D)
	private static final int chordToRefKey6[] = { 0, // Key TH ('D') as a modifier (chord = state + key, except for key 64: chord = key)
			1, 2, 3, 4, 5, 28, 29, 41, 30, 10, // 10 // these first values are due to Magic Other Hand Off use to give the letter from the other side
			39, 59, 28, 4, 5, 6, 7, 8, 9, 10, // 20 // 61 = 123abc, 59 = Shift (shift+shift here to have Abc > abc directly)
			29, 2, 3, 4, 5, 6, 7, 8, 41, 10, // 30
			1, 2, 3, 4, 5, 6, 30, 8, 9, 10, // 40
			1, 2, 3, 4, 60, 6, 7, 8, 9, 10, // 50 // 60 = user-defined string
			1, 2, 39, 4, 5, 6, 7, 8, 9, 10, // 60
			50, 2, 61, 64, 5, 6, 7, 8, 9, 10, // 70  // 50 = space, 61 = 123abc
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10, // 100
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10  // 120

	};
			// Swipe from key W (L)
	private static final int chordToRefKey8[] = { 0, // Key W ('L') as a modifier (chord = state + key, except for key 64: chord = key)
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10, // 10 // TBD
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10, // 20
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10, // 30
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10, // 40
			24, 25, 58, 26, 60, 40, 61, 24, 9, 10, // 50 // 60 = user-defined string
			1, 2, 3, 4, 5, 25, 7, 8, 9, 10, // 60
			1, 2, 3, 58, 5, 6, 7, 8, 9, 10, // 70
			1, 26, 3, 4, 5, 6, 7, 8, 9, 10, // 80
			1, 2, 3, 4, 5, 6, 7, 40, 9, 10, // 90
			1, 2, 3, 4, 5, 50, 7, 8, 9, 10, // 100
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10, // 110
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10  // 120

	};


	public static final int NONE = 0;

	public static final int A = 0x01; // 1
	public static final int B = 0x02; // 2
	public static final int C = 0x04; // 4
	public static final int D = 0x08; // 8
	public static final int E = 0x10; // 16
	public static final int F = 0x20; // 32
	public static final int M = 0x40; // 64 = key m for ComboKey extra button

	public static final int O = A + B; // 3
	public static final int S = B + C; // 6
	public static final int G = D + E; // 24
	public static final int K = E + F; // 48
	public static final int TH = A + C;// 5
	public static final int W = D + F; // 40

	public static final int BACKSPACE = A + B + C; // 7
	public static final int SPACE = D + E + F;     // 56

	// ========== ComboKey Index approach: =======

	// Key pad indices: e.g. CMBOKey.I4 = CMBOKey.S

	// I1  I6  I11  =  A  TH  D
	// I2  I7  I12  =  O  BS  G
	// I3  I8  I13  =  B   W  E
	// I4  I9  I14  =  S  SP  K
	// I5 I10  I15  =  C   M  F

	public static final int I1 = A; // Left column
	public static final int I2 = O;
	public static final int I3 = B;
	public static final int I4 = S;
	public static final int I5 = C;

	public static final int I6 = TH; // Center column
	public static final int I7 = BACKSPACE;
	public static final int I8 = W;
	public static final int I9 = SPACE;
	public static final int I10 = M;

	public static final int I11 = D; // Right column
	public static final int I12 = G;
	public static final int I13 = E;
	public static final int I14 = K;
	public static final int I15 = F;

	// ============================================


	public static final int SHIFT_MODIFIER = 0x40; // TODO: same value as key m, is there a problem?
	public static final int CAPS_MODIFIER = 0x80;
	public static final int NUMBER_MODIFIER = 0xc0;
	public static final int SYMBOL_MODIFIER = 0x100;
	public static final int AUXSET_MODIFIER = 0x140;
	//public static final int AUXSET2_MODIFIER = 0x180;
	public static final int AUXSET_SHIFT_MODIFIER = 0x180;
	//public static final int AUXSET3_MODIFIER = 0x1c0;
	public static final int AUXSET_CAPS_MODIFIER = 0x1c0;
	public static final int AUXSET_NUMBER_MODIFIER = 512;
	public static final int AUXSET_SYMBOL_MODIFIER = 576;






	// JSON area:
	public static final int NUMBERS_EXTRA = 513+128; // 641 delta = 65// smileys area (15 pieces) at 513...527
	public static final int SYMBOLS_EXTRA = 528+128; // 656 delta = 15 // Key M set of symbols (15 pieces) at 528...542

	// fixed area:
	public static final int SYMBOLS_BACKSPACE = 543+128; // 671 delta = 15 // shown by Backspace as modifier (15 pieces) at 543...557
	public static final int SYMBOLS_SPACE = 558+128; // 686	delta = 15 // shown by Space as modifier (15 pieces) at 558...572
	public static final int EMOJI_MODIFIER = 602+128; // 730, delta from above = 44
	public static final int FN_MODIFIER = 602+128+128; // 858 // delta = 128 // F1...F12 (special treatment)

	public static final int MODIFIER_MASK = 0x1c0;
	public static final int ALL_BUTTONS = SPACE + BACKSPACE;

	//int offs = keyboard.getOffset();
	//if (controller.isSingleHandEnabled() && !(offs == CMBOKey.EMOJI_MODIFIER))

	public static int mirror(int chord, int offset) { // only used for preventMirrorSlippage

		if (offset == CMBOKey.EMOJI_MODIFIER) return chord; // use Emojis on all buttons

		Log.d("-INDEX", "mirror(int chord)");
		int index = chordToIndex[chord];
		//index = mirrorIndex(index);
		return indexToChord[mirrorIndex(index)];
		/*
		if (chord < D)
			return chord << 3; // signed shift left
		else
			return chord >> 3;
		*/
	}

	public static int mirrorIndex(int index) { // only mirror between Right and Left columns
		if (index < 6) { // if Left column
			Log.d("-INDEX", "mirrorIndex(int index) Mirror from Left column. Index => " + (index + 10) + ".");
			return index + 10; // jump to right column
		}
		if ((index > 10) && (index < 16)) { // if Right column
			Log.d("-INDEX", "mirrorIndex(int index) Mirror from Right column. Index => " + (index - 10) + ".");
			return index - 10; // jump to left column
		}
			Log.d("-INDEX", "mirrorIndex(int index) No mirror action if Center column. Index = " + index + ".");
			return index; // no mirror action if Center column
	}

	
	public static boolean areMirrorKeys(int key1, int key2, int offs) {return mirror(key1, offs) == key2;}
	public static boolean areMirrorIndices(int index1, int index2) {return mirrorIndex(index1) == index2;}

	public static int getIndexForChord(int chord) { return chordToIndex[chord]; } // single keys only
	public static int getChordForIndex(int index) { return indexToChord[index]; } // single keys only
	public static int getRefForChord(int chord) {
		return chordToRef[chord];
	} // combos as well

	public static int getRefForChordKeyBackspace(int chord) {
		Log.d("CMBO", "Swipe from Backspace. Chord " + chord + ".");
		return chordToRefKeyBackspace[chord];
	}

	public static int getRefForChordKeySpace(int chord) {
		Log.d("CMBO", "Swipe from Space key. Chord " + chord + ".");
		return chordToRefKeySpace[chord];
	}

	public static int getRefForChordKeyTH(int chord) {
		Log.d("CMBO", "Swipe from Key TH. Chord " + chord + ".");
		return chordToRefKey6[chord];
	}

	public static int getRefForChordKeyW(int chord) {
		Log.d("CMBO", "Swipe from Key W. Chord " + chord + ".");
		return chordToRefKey8[chord];
	}

	public static int getRefForChordExtended(int offset, int state, int key) {
		int chord = state + key;
		Log.d("CMBO", "getRefForChordExtended. Chord " + chord + ".");
		// Add logic here:

		//chord = 1;

		// ------
		return chordToRef[chord]; // TEMP
	}

	// ----- 2018: ----
	public static boolean isOnLeftColumn(int index) {
		if (index == 0) return false;
		if (index <= 5) return true;
		return false;
	}
	public static boolean isOnRightColumn(int index) {
		if (index == 0) return false;
		if (index >= 11) return true;
		return false;
	}
	public static boolean isOnCenterColumn(int index) {
		if (index == 0) return false;
		if ((index >= 6) && (index <= 10)) return true;
		return false;
	}
	public static boolean areOnSameColumn(int index1, int index2) {
		if ((index1 == 0) || (index2 == 0)) return false;
		if ((index1 <= 5) && (index2 <= 5)) return true; // both Left
		if ((index1 >= 11) && (index2 >= 11)) return true; // both Right
		if ((index1 >= 6) && (index2 >= 6) && (index1 <= 10) && (index2 <= 10)) return true; // both Center
		return false;
	}
	// -----------------

	public static boolean isOnTheSameSide(int state, int key) {
		// int index1 = getIndexForChord(state);
		// int index2 = getIndexForChord(key);
		return areOnSameColumn(getIndexForChord(state), getIndexForChord(key));
	}

	/*
	public static boolean isOnTheSameSide(int state, int key) {
		if (state == NONE)
			return false;
		//return (state < D && key < D) || (state >= D && key >= D);
		boolean sameSide = false;
		// Clumsier but needed now for the extended character set
		if (((state < 5) || (state == 6)) &&
				((key < 5) || (key == 6)))
		{
			sameSide = true;

		} else if (((state == 8) || (state == 24) || (state == 16) || (state == 48) || (state == 32)) &&
				((key == 8) || (key == 24) || (key == 16) || (key == 48) || (key == 32)))
		{
			sameSide = true;

		} else {
			sameSide = false;
		}

		return sameSide;

	}
	*/

	public static int mirrorIfOnSameSideColumn(int state, int chord, int offset) { // just Left column or Right column

		Log.d("-INDEX", "mirrorIfOnSameSideColumn()");

		if ((state == NONE) || (offset == CMBOKey.EMOJI_MODIFIER) || (offset == CMBOKey.FN_MODIFIER)) // Use Emojis/Fn on all buttons
			return chord;

		int index1 = getIndexForChord(state);
		int index2 = getIndexForChord(chord);

		if (isOnCenterColumn(index1) && isOnCenterColumn(index2)) {
			Log.d("-INDEX", "mirrorIfOnSameSideColumn() - Both on Center column. Indices: " + index1 + " and " + index2);
			return chord; // no mirroring from Center column
		}
		if (areOnSameColumn(index1, index2)) {
			Log.d("-INDEX", "mirrorIfOnSameSideColumn() - Both on same column. Indices: " + index1 + " and " + index2);
			return getChordForIndex(mirrorIndex(index2));
		}
		Log.d("-INDEX", "mirrorIfOnSameSideColumn() - Different columns. Indices: " + index1 + " and " + index2);
		return chord;
	}

	/*
	public static int mirrorIfOnTheSameSide(int state, int chord) {
		if (state == NONE)
			return chord;

		// These two switches need more consideration!

		switch (state) { // start swipe from middle column: no mirroring
			case 5: // Key TH // added TH and W for extended set 2018-01
			case 7: // BS
			case 40: // Key W
			case 56: // SP
			case 64:
				return chord;
			default:
				break;
		}


		switch (chord) { // key, in fact
			case 5: // Key TH
			case 7: // BS
			case 40: // Key W
			case 56: // SP
			case 64: // Key M
				return chord;
			default:
				break;
		}

		//if (state == 64) return chord; // Key M as state, do not mirror

		if ((state < D && chord < D) || (state >= D && chord >= D)) {
			return mirror(chord);
		} else
			return chord;
	}
	*/

	public static boolean isAutoRepeatable(int state, int key, String character) { // do not autorepeat all entries!

		if ((character.startsWith("_")) && character.length() != 1) {

			if ((character.equals("_BS"))
					|| (character.equals("_Left")) // for selecting/painting text
					|| (character.equals("_Right")) // for selecting/painting text
					|| (character.equals("_Up")) // for selecting/painting text
					|| (character.equals("_Down")) // for selecting/painting text
					|| (character.equals("_SP"))
					|| (character.equals("_Del"))) {
				return true;
			}
			return false;

		} else {

			if (character.length() > 5) {
			    return false;
            } else {
                return true;
            }
		}

	}
		/*
		int chord = state + key;

		boolean repeatable = true;

		switch (chord) {
			case 8:
				if (key != 0) repeatable = false; // (BS + a)
				break;
			case 10: // Clear all
				if (state == 7 || key == 7)  repeatable = false;
				break;
			case 12: // new Shift BS + d/ä
				if (state == 7 || key == 7)  repeatable = false;
				break;
			case 9: // Translate
				if (state == 7 || key == 7)  repeatable = false;
				break;
			case 13: // Esc
				if (state == 7 || key == 7)  repeatable = false;
				break;
			case 11: // Search
				if (state == 7 || key == 7)  repeatable = false;
				break;
			case 15: //  BS + i
			case 63: // 123ABC
			case 18: // CMBO Shift Key.B + Key.E
			case 58: // old Shift (SP + u), now Tab
			case 59: // SYMB SP + o
			case 80: // Emojis SP + t
			case 39: // Paste
			case 23: // Copy
			case 55: // Alt
			case 71: // Ctrl
			case 47: // Lang
				repeatable = false;
				break;
			default:
				repeatable = true;
		}
		return repeatable;

	}

	*/

	/**
	 * Checks that two button presses do not overlap (ie. pressing a with o,
	 * which already includes a).
	 *
	 * @param chord1
	 * @param chord2
	 * @return
	 */

	public static boolean isValidCombination(int chord1, int chord2) {
		// For now, please do not edit even if warning:
		// Boolean method 'isValidCombination' is always inverted
		if (chord1 == chord2) return false; // added 2018-01
		if ((chord1 == 0) || (chord2 == 0)) return true; // Not a combination, added 2018-02

		boolean validity = false; // default

		// =========== index method ===================

		int index1 = getIndexForChord(chord1);

		int index2 = getIndexForChord(chord2);

		if (((index1 > 0) && (index1 <16)) && ((index2 > 0) && (index2 <16))) validity = true;

		/*
		if ((areOnSameColumn(index1, index2)) && (!isOnCenterColumn(index1))) {
			validity = false;
		} else {
			validity = true;
		}

		return validity;

		*/

		return validity;
	}



		// =============================================


		/*
		// chord method: =================================


		// chordX =< 7 and chodY > 7 is covered here
		validity = ((chord1 & chord2) == NONE); // opposite sides: always valid

		// extensions to that:
		switch (chord1) {
			case 64: // M
				if (chord2 <= 56) {validity = true;}
				break;
			case 56: // Space // swipe to any key
				if (chord2 <= 65) {validity = true;}
				break;
			case 24: // T // EMOJI reverse swipe to Space
				if (chord2 == 56) {validity = true;}
				break;
			case 7:  // Backspace // swipe to any key
				if (chord2 <= 65) {validity = true;}
				break;
			case 5:  // D // swipe down = reverse Shift, swipe to any key from Key TH (D)
				if (chord2 <= 65) {validity = true;}
				break;
			case 3:  // O // swipe right to backspace = symbols
				if (chord2 == 7) {validity = true;}
				break;
			case 40: // L // swipe up to backspace = abc123, and any other swipe from L
				if (chord2 <= 65) {validity = true;}
				break;
			// Special cases to add always " _ ; : (2018-02)
			case 1: // A // swipe to BS
				if (chord2 == 7) {validity = true;}
				break;
			case 8: // I // swipe to BS
				if (chord2 == 7) {validity = true;}
				break;
			case 2: // U // swipe to Space
				if (chord2 == 56) {validity = true;}
				break;
			case 16: // E // swipe to Space
				if (chord2 == 56) {validity = true;} // throws error?
				break;

			default:

				Log.d("-CHORD", "Validity of chord combination " + chord1 + " and " + chord2 + " is " + validity);
				return validity;
		}
		Log.d("-CHORD", "Validity of special/normal chord combination " + chord1 + " and " + chord2 + " is " + validity);

		// ===========================================


		return validity;

	}

	*/

	/***
	 * Checks that the modifier and chord only have the relevant bits set
	 * 
	 * @param modifier
	 * param chord used to be included here
	 * @return
	 */

	public static boolean isValidChord(int modifier, int state, int key) { // = offset, state+key

		int chord = state + key;

		if ((modifier < 859) && (chord <= 120)){  // not a proper check now, too loose
			// AUXSET_SYMBOL_MODIFIER = 576
			//return ((chord & ALL_BUTTONS) == chord && (modifier & MODIFIER_MASK) == modifier);
			// FN_MODIFIER = 858
			return true;

		} else {
			 return false;
		}

		/*
		else {  // key M added, valid if as state => state + key = 0...120
			return (((state == 64) && (key <= 56))
					|| ((state == 7) && (key <= 64))
					|| ((state == 56) && (key <= 64))
			); // key M + Space = 120
			// Swiping to and from Key M is allowed here but swiping *to* Key M is dealt with elsewhere: excluded
			// 528 is where extra symbols start (key M as state)

		}
		*/
	// Above: Some wider check needed. Now this allows swipe from Key M
	// was:
	//	if (chord == 64){return true;} else { // key M added
	//		return ((chord & ALL_BUTTONS) == chord && (modifier & MODIFIER_MASK) == modifier);}
	}

}
