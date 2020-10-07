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

public class LayoutFactory {

	// ==============================================================================
	// ======= Code pages (keymaps) for the keypad in all five modes ================
	// ==============================================================================

	//	--- Note: PHP 'JSON keymap file generation tool' may overwrite any of these items!!! ---

	// This shows what symbols are shown on other keys when a certain key '[X]' is pressed.
	// Each row below corresponds to the key pad's three columns,
	// indices (keys on columns 1, 2, 3): 1 2 3 4 5     6 7 8 9 10 	   11 12 13 14 15
	// Note regarding JSON layout files: In legacy case (JSON format < 10), slash and
	// backslash are only taken from primary set (not punctuation set)

	public static String[] getMapLowerCase() { // One of 15 keys pressed down = X
		return new String[] { "NULL", // indexed keys // [X] = the key pressed,
				// [] = clear this (intended for legacy JSON remapping, formats < 3.0)
				// [X = is functionally X but is not visible on button

				// Left column				Center column					Right column
				 "a", "o", "u", "s", "c", 	"d", "_BS", "l", " ", "m",		"i", "t", "e", "n", "r",

				"[X]", "", "", "", "", 		"[]", "_", "", "\u02c6", "¨",	"_Up", "h", "-", "ght", "?",
				"", "[X]", "", "", "", 		"[]", "_SYMB", "", "", "",		"p", "_PgUp", "f", "\\", "q",
				"", "", "[X]", "", "_Left",	"[]", "´", "", ":", "",			"'", "g", "_Shift", "b", ".",
				"", "", "", "[X]", "", 		"[]", "", "", "", "",	 		"sh", "/", "w", "_PgDn", "v",
				"", "", "_Left", "", "[X]", "[]", "|", "y", ";", "",		"!", "j", ",", "k", "_Down",

				"", "", "", "", "",
				"[X]", "_More", "_ShiftDown", "", "_Hide",
				"", "", "", "", "",

				"_Record", "_Clear", "_Translate", "_Esc", "_Search",
				"_Shift", "[X]", "_Lang", "_abc123", "_Ctrl",
				"_Play", "_Del", "_Copy", "_Alt", "_Paste",

				"[]", "[]", "[]", "[]", "[]",
				"_Shift", "_abc123", "[X]", "_Repeat", "_User",
				"x", "", "y", "", "z",

				"_WLeft", "_SYMB", "_Tab", "_Left", "_Home",
				"_More", "_abc123", "_Up", "[X]", "_Down",
				"_WRight", "_Emoji", "_Enter", "_Right", "_End",

				"&", "+", "/", "*", "(",
				"$", "£", "€", ":", "[X]",
				"%", "=", "@", "#", ")",

				"_Up", "p", "'", "sh", "!", 	"[]", "\"", "x", "ˇ", "~",		"[X]", "", "", "", "",
				"h", "_PgUp", "g", "/", "j", 	"[]", "_Emoji", "the ", "", "",	"", "[X]", "", "", "",
				"-", "f", "_Shift", "w", ",", 	"[]", "`", "y", ";", "",		"", "", "[X]", "", "_Right",
				"ght", "\\", "b", "_PgDn", "k", "[]", "", "wh", "", "",			"", "", "", "[X]", "",
				"?", "q", ".", "v", "_Down", 	"[]", "~", "z", ":", ".com",	"", "", "_Right", "", "[X]",
				// abc + spare:
				"abc", "", "", "", "",  "_", ":", "|", "\"", ";",    "~", "´", "`", "", ""
		};
	}

	// Note: The symbols added here (_Up, :, _Ctrl, ; etc.) can be overwritten by
	//       JSON layouts format v10.0 and up, but not by legacy JSON format < v10.0.
	//       So, please edit also the keymap template in the separate PHP layout generator.
	//		 Still SYMB/Numbers M-key characters are defined on index.php!

	public static String[] getMapUpperCase() { // One of 15 keys pressed down = X
		return new String[] { "NULL", // indexed keys

				"A", "O", "U", "S", "C", 	"D", "_BS", "L", " ", "M",		"I", "T", "E", "N", "R",

				"[X]", "", "", "", "", 		"[]", "-", "", "\u02c6", "¨",	"_Up", "H", "_", "Ght", "?",
				"", "[X]", "", "", "", 		"[]", "_SYMB", "", "", "",	 	"P", "_PgUp", "F", "\\", "Q",
				"", "", "[X]", "", "_Left",	"[]", "´", "", ".", "",			"\"", "G", "_Shift", "B", ":",
				"", "", "", "[X]", "", 		"[]", "", "", "", "",	 		"Sh", "/", "W", "_PgDn", "V",
				"", "", "_Left", "", "[X]",	"[]", "!", "Y", ",", "",	 		"|", "J", ",", "K", "_Down",

				"", "", "", "", "",
				"[X]", "_More", "_ShiftDown", "", "_Hide",
				"", "", "", "", "",

				"_Record", "_Clear", "_Translate", "_Esc", "_Search",
				"_Shift", "[X]", "_Lang", "_abc123", "_Ctrl",
				"_Play", "_Del", "_Copy", "_Alt", "_Paste",

				"", "", "", "", "",
				"_Shift", "_abc123", "[X]", "_Repeat", "_User",
				"X", "", "Y", "", "Z",

				"_WLeft", "_SYMB", "_Tab", "_Left", "_Home",
				"_More", "_abc123", "_Up", "[X]", "_Down",
				"_WRight", "_Emoji", "_Enter", "_Right", "_End",

				"&", "+", "/", "*", "(",
				"$", "£", "€", ":", "[X]",
				"%", "=", "@", "#", ")",

				"_Up", "P", "\"", "Sh", "!", 	"[]", "'", "X", "ˇ", "~",		"[X]", "", "", "", "",
				"H", "_PgUp", "G", "/", "J", 	"[]", "_Emoji", "The ", "", "",	"", "[X]", "", "", "",
				"_", "F", "_Shift", "W", ";", 	"[]", "`", "Y", ",", "",		"", "", "[X]", "", "_Right",
				"Ght", "\\", "B", "_PgDn", "K",	"[]", "", "Wh", "", "",			"", "", "", "[X]", "",
				"~", "Q", ":", "V", "_Down", 	"[]", "?", "Z", ".", ".com",	"", "", "_Right", "", "[X]",
				// Abc + spare:
				"Abc", "", "", "", "",  "-", ".", "!", "'", ",",   "?", "/", "\\", "", ""
		};
	}

	public static String[] getMapCapsLock() { // One of 15 keys pressed down = X
		return new String[] { "NULL", // indexed keys

				"A", "O", "U", "S", "C", 	"D", "_BS", "L", " ", "M",		"I", "T", "E", "N", "R",

				"[X]", "", "", "", "", 		"[]", "_", "", "\u02c6", "¨",	"_Up", "H", "-", "GHT", "?",
				"", "[X]", "", "", "", 		"[]", "_SYMB", "", "", "",	 	"P", "_PgUp", "F", "\\", "Q",
				"", "", "[X]", "", "_Left",	"[]", "´", "", ":", "", 		"'", "G", "_Shift", "B", ".",
				"", "", "", "[X]", "", 		"[]", "", "", "", "",	 		"SH", "/", "W", "_PgDn", "V",
				"", "", "_Left", "", "[X]", "[]", "|", "Y", ";", "",	 	"!", "J", ",", "K", "_Down",

				"", "", "", "", "",
				"[X]", "_More", "_ShiftDown", "", "_Hide",
				"", "", "", "", "",

				"_Record", "_Clear", "_Translate", "_Esc", "_Search",
				"_Shift", "[X]", "_Lang", "_abc123", "_Ctrl",
				"_Play", "_Del", "_Copy", "_Alt", "_Paste",

				"", "", "", "", "",
				"_Shift", "_abc123", "[X]", "_Repeat", "_User",
				"X", "", "Y", "", "Z",

				"_WLeft", "_SYMB", "_Tab", "_Left", "_Home",
				"_More", "_abc123", "_Up", "[X]", "_Down",
				"_WRight", "_Emoji", "_Enter", "_Right", "_End",

				"&", "+", "/", "*", "(",
				"$", "£", "€", ":", "[X]",
				"%", "=", "@", "#", ")",

				"_Up", "P", "'", "SH", "!", 	"[]", "\"", "X", "ˇ", "~",		"[X]", "", "", "", "",
				"H", "_PgUp", "G", "/", "J", 	"[]", "_Emoji", "THE ", "", "",	"", "[X]", "", "", "",
				"-", "F", "_Shift", "W", ",", 	"[]", "`", "Y", ";", "",		"", "", "[X]", "", "_Right",
				"GHT", "\\", "B", "_PgDn", "K", "[]", "", "WH", "", "",			"", "", "", "[X]", "",
				"?", "Q", ".", "V", "_Down", 	"[]", "~", "Z", ":", ".COM",	"", "", "_Right", "", "[X]",

				// ABC + spare:
				"ABC", "", "", "", "",  "_", ":", "|", "\"", ";",    "~", "´", "`", "", ""
		};
	}

	public static String[] getMapNumbers() { // One of 15 keys pressed down = X
		return new String[] { "NULL", // indexed keys

				"1", "", "4", "", "7", 	"2", "_BS", "5", " ", "8",		"3", "0", "6", "", "9",

				"[X]", "", "", "", "", 		"", "_", "", "\u02c6", "¨",	"_Up", "©", "-", "&", "?",
				"", "[X]", "", "", "", 		"", "_SYMB", "", "", "",	"%", "_PgUp", "=", "\\", "^",
				"", "", "[X]", "", "_Left", "", "\"", "", ":", "",		"'", "®", "_Shift", "@", ".",
				"", "", "", "[X]", "", 		"", "", "", "", "",			"", "/", "", "_PgDn", "",
				"", "", "_Left", "", "[X]", "", "|", "<", ";", "",		"!", "©", "", "½", "_Down",

				"[", "«", "{", "", "<",
				"[X]", "_More", "_ShiftDown", "", "_Hide",
				"]", "»", "}", "µ", ">", // \u00b0 (»)

				"_Record", "_Clear", "_Call", "_Esc", "_Search",
				"_Shift", "[X]", "_Lang", "_abc123", "_Ctrl",
				"_Play", "_Del", "_Copy", "_Alt", "_Paste", 	// \u00b0 degree sign // µ \00b5 // § \00a7

				"‰", "°", "{", "§", "<",
				"_Shift", "_abc123", "[X]", "_Repeat", "_User",
				"%", "", "}", "", ">",

				"_WLeft", "_SYMB", "_Tab", "_Left", "_Home",
				"_More", "_abc123", "_Up", "[X]", "_Down",
				"_WRight", "_Emoji", "_Enter", "_Right", "_End",

				"&", "+", "/", "*", "(",
				"$", "£", "€", ":", "[X]",
				"%", "=", "@", "#", ")",

				"_Up", "", "'", "", "!", 	  "", "\"", "", "ˇ", "~",		"[X]", "", "", "", "",
				"‰", "_PgUp", "®", "/", "©",  "", "_Emoji", "", "", "",		"", "[X]", "", "", "",
				"-", "=", "_Shift", "", ",",  "", "_", "", ";", "",			"",	"", "[X]", "", "_Right",
				"&", "\\", "@", "_PgDn", "½", "", "", "", "", "",			"", "", "", "[X]", "",
				"?", "^", ".", "", "_Down",   "", "~", ">", ":", "",	"", "", "_Right", "", "[X]",

				// 123 + spare:
				"123", "", "", "", "",  "_", ":", "|", "\"", ";",    "~", "´", "`", "", ""


				// \u00b0 degree sign // µ 00b5 // § 00a7
		};
	}

	public static String[] getMapSymbols() { // One of 15 keys pressed down = X
		return new String[] { "NULL", // indexed keys

				"1", "", "4", "", "7", 	"2", "_BS", "5", " ", "8",		"3", "0", "6", "", "9",
										// \u2013
				"[X]", "", "", "", "",	 	"", "-", "", "\u02c6", "¨",	"_Up", "", "_", "", "",
				"", "[X]", "", "", "", 		"", "_abc123", "", "", "",	"", "_PgUp", "", "\\", "",
				"", "", "[X]", "", "_Left",	"", "", "", ".", "",		"", "", "_Shift", "", ":",
				"", "", "", "[X]", "", 		"", "", "", "", "",			"", "/", ";", "_PgDn", "",
				"", "", "_Left", "", "[X]", "", "¡", "", ",", "",		"", "", ",", "", "_Down",

				"[", "«", "{", "", "<",
				"[X]", "_More", "_ShiftDown", "", "_Hide",
				"]", "»", "}", "µ", ">",

				"_Record", "_Clear", "_Translate", "_Esc", "_Search",
				"_Shift", "[X]", "_Lang", "_abc123", "_Ctrl",
				"_Play", "_Del", "_Copy", "_Alt", "_Paste",

				"‰", "°", "{", "§", "<",
				"_Shift", "_abc123", "[X]", "_Repeat", "_User",
				"%", "", "}", "", ">",

				"_WLeft", "_SYMB", "_Tab", "_Left", "_Home",
				"_More", "_abc123", "_Up", "[X]", "_Down",
				"_WRight", "_Emoji", "_Enter", "_Right", "_End",

				"[", "«", "{", "<", "(",
				"¤", "|", "", "", "[X]",
				"]", "»", "}", ">", ")",

				"_Up", "", "\"", "", "", 	"", "'", "", "ˇ", "~",		"[X]", "", "", "", "",
				"", "_PgUp", "", "/", "",	"", "_Emoji", "", "", "",	"", "[X]", "", "", "",
				"", "", "_Shift", "", ";", 	"", "", "", ",", "",		"", "", "[X]", "", "_Right",
				"", "\\", "", "_PgDn", "", 	"", "", "", "", "",			"", "", "", "[X]", "",
				"", "", ":", "", "_Down", 	"", "¿", "", ".", "",		"", "", "_Right", "", "[X]",

				// #@ + spare:
				"#@", "", "", "", "",  "-", ".", "!", "'", ",",   "?", "/", "\\", "", ""
		};
	}

	public static String[] getMapEmojis() { // One of 15 keys pressed down = [X]
		return new String[] { "NULL", // indexed keys

		"\uD83C\uDD97", "♥", "", "\uD83D\uDC4D", "\uD83C\uDFB9", // Nothing pressed
		"\uD83D\uDE41", "_abc", "\uD83D\uDE42", "", "\uD83D\uDE10", //"\uD83D\uDE10", // Flag position
		"☎", "\uD83C\uDF82", "✈️", "\uD83D\uDC36", "⚽",

		"[X]", "", "", "", "", 		"", "_", "", "", "",	"", "", "", "", "", // KeyA (A)

		"\uD83C\uDF29️", "[X]", "☔", "\uD83C\uDF39", "\uD83D\uDC90", // Feelings
		"\uD83D\uDC83", "\uD83D\uDD76️", "\uD83C\uDF02", "\uD83C\uDF24️", "\uD83D\uDCAB",
		"\uD83D\uDE18", "\uD83D\uDC8B", "☀️️", "\uD83D\uDC8F", "❤️", // KeyO (O)

		"", "", "[X]", "", "", "", "", "", "", "",		"", "", "", "", "", // KeyB (U)

		"\uD83E\uDD19", "\uD83D\uDC46", "\uD83D\uDC4C", "[X]", "\uD83D\uDC4E", 	// Hands
		"☝️", "✍️", "\uD83D\uDCAA", "\uD83D\uDD90️", "\uD83E\uDD1D",
		"\uD83D\uDC4C", "\uD83D\uDC4F", "✌️", "✋", "\uD83D\uDC4B️", // KeyS (S)

		"\uD83C\uDF99", "\uD83C\uDFA4", "\uD83C\uDF9B", "♩", "[X]", // Music
		"♪", "🎶", "♫", "🎼", "♬",
		"\uD83C\uDFB8", "\uD83E\uDD41", "\uD83C\uDFB7", "\uD83C\uDFBA", "\uD83C\uDFBB", // KeyC (C)

		"\uD83D\uDE32", "\uD83D\uDE35", "\uD83D\uDE1F", "\uD83D\uDE13", "\uD83D\uDE16", // sad face...
		"[X]", "", "", "", "",
		"\uD83D\uDE1E", "\uD83D\uDE16", "\uD83D\uDE22", "\uD83D\uDE27", "\uD83D\uDE20", // KeyTH (D) sad faces

		"", "", "", "", "", // Some hidden emojis.... behind abc button
		"", "[X]", "", "", "", // back to abc
		"", "", "", "", "",

		"\uD83D\uDE0B", "\uD83D\uDE0E", "\uD83D\uDE09", "\uD83D\uDE04", "\uD83D\uDE00", // smiles
		"", "", "[X]", "", "",
		"\uD83D\uDE1B", "", "", "", "",

		"", "", "", "", "", // S
		"", "", "", "[X]", "",
		"", "", "", "", "",

		":-)", "🙈", "\uD83D\uDCD6", "❤️", "\uD83D\uDE0A", //  JSON - Only these three lines are loaded from
		"\uD83D\uDE98", "\uD83D\uDE4A", "\uD83D\uDCB5", "\uD83E\uDD14", "[X]",// legacy JSON (native emojis, numbers/extra).
		"⛱️", "\uD83D\uDE49", "\uD83D\uDC8E", "\uD83D\uDE0D", "\uD83D\uDD2C",


		"\uD83D\uDC88", "\uD83C\uDFA7", "\uD83D\uDCFD️", "\uD83D\uDCF7", "\uD83D\uDCA1", // Objects
		"\uD83D\uDCFA", "\uD83D\uDCF1", "\uD83D\uDCBB", "\uD83D\uDCFB", "\uD83D\uDCE2",
		"[X]", "\uD83D\uDCFE", "\uD83D\uDDA8", "\uD83D\uDD26", "\uD83D\uDD0B", // KeyD (I)

		"\uD83C\uDF75", "\uD83C\uDF7B", "\uD83C\uDF77", "\uD83C\uDF79", "\uD83C\uDF78", // Celebrate
		"\uD83E\uDD42", "\uD83C\uDF7E", "\uD83C\uDF7C", "\uD83C\uDF7D️", "\uD83C\uDF68",
		"\uD83C\uDF70", "[X]", "\uD83C\uDF7F", "\uD83C\uDF69", "\uD83E\uDD5B", // KeyG (T)

		"⛴️", "\uD83D\uDE81", "\uD83D\uDE8B", "\uD83D\uDE82", "\uD83D\uDE97", // travel and places
		"\uD83D\uDE8C", "\uD83D\uDEB2", "\uD83D\uDE9C", "⛵", "\uD83D\uDDFA️",
		"\uD83C\uDFDD️", "\uD83C\uDFD6️", "[X]", "\uD83D\uDDFD", "\uD83C\uDFD4️",

		"\uD83D\uDC31", "\uD83D\uDC0E", "\uD83D\uDC25", "\uD83D\uDC2F", "\uD83D\uDC1D", // pets+animals
		"\uD83D\uDC27", "\uD83D\uDC15", "\uD83D\uDC22", "\uD83E\uDD89", "\uD83D\uDC0C",
		"\uD83D\uDC08", "\uD83D\uDC3C", "\uD83D\uDC27", "[X]", "\uD83D\uDC37", // KeyK (N)

		"\uD83D\uDEB4", "\uD83C\uDFC7", "⛷️", "\uD83C\uDFCA", "\uD83C\uDFCC️",
		"⛸️", "\uD83C\uDFC3", "\uD83C\uDFBF", "\uD83C\uDFD2", "\uD83C\uDFC6",
		"\uD83C\uDFBE", "⚾", "\uD83C\uDFC8", "\uD83C\uDFC0", "[X]", // Sport

		// :) + spare:
		":)", "", "", "", "",  "", "", "", "", "",   "", "", "", "", ""
		};
	}

	public static String[] getMapMore() { // One of 15 keys pressed down to select more functions like Fn (function keys)
		return new String[]{"NULL", // indexed keys, Terminal mode function keys F1...F12

				"\u2012", "", "", "_Fn", "", // figure/digit dash, , , Fn,
				"", "_BS", "_Keyboard", " ", "_Notes",
				"", "", "", "_abc", "",

				"[X]", "", "", "\u02DC", "\u301C", // [figure/digit dash pressed], , small tilde, wave dash,
				"\u2013", "_", "\u2053", "\u007E", "\u2015", // en dash, under score, swung dash, tilde, horiz.bar
				"_Up", "\u2014", "-", "\u223C", "\uFF5E", // em dash, , hyphen-minus, tilde operator, full width tilde

				"", "[X]", "", "", "",
				"", "", "", "", "",
				"", "_PgUp", "", "", "",

				"", "", "[X]", "", "_Left",
				"", "", "", "", "",
				"", "", "", "", "",

				"F1", "F10", "F4", "[X]", "F7",
				"F2", "F11", "F5", "", "F8",
				"F3", "F12", "F6", "_PgDn", "F9",

				"", "", "_Left", "", "[X]",
				"", "", "", "", "",
				"", "", "", "", "_Down",

				"", "", "", "", "",
				"[X]", "_abc", "", "_Settings", "_Hide",
				"", "", "", "", "",

				"", "_Clear", "", "_Esc", "",
				"", "[X]", "_Lang", "_Repeat", "_Ctrl",
				"", "_Del", "", "_Alt", "",

				"_PadUL", "_PadB", "_PadL", "_Pad3R", "_PadLL",
				"_PadCW", "_PadU", "[X]", "_PadD", "_PadCN",
				"_PadUR", "_PadS", "_PadR", "_Pad5R", "_PadLR",

				"", "", "_Tab", "_Left", "_Home",
				"", "", "_Up", "[X]", "_Down",
				"", "", "_Enter", "_Right", "_End",

				"", "", "_Get", "", "",
				"", "", "_Add", "", "[X]",
				"", "", "_Save", "", "",

				"_Up", "", "", "", "",
				"", "", "", "", "",
				"[X]", "", "", "", "",

				"", "_PgUp", "", "", "",
				"", "", "", "", "",
				"", "[X]", "", "", "",

				"", "", "", "", "",
				"", "", "", "", "",
				"", "", "[X]", "", "_Right",

				"", "", "", "_PgDn", "",
				"", "", "", "", "",
				"", "", "", "[X]", "",

				"", "", "", "", "_Down",
				"", "", "", "", "",
				"", "", "_Right", "", "[X]",

				"\u2190", "", "", "", "",
				"", "", "", "", "",
				"", "", "", "", ""

		};
	}


}
	


