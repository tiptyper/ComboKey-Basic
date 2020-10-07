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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
//import android.os.VibrationEffect;
import android.os.CountDownTimer;
import android.provider.UserDictionary;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class CMBOWordCandidates implements CMBOWordCandidatesOverride {

    private String wordHint = "";
    private String previousWordShown = "";
    private String[] wordCandidates = {"", "", "", "", "", "", "", "", "", ""};
    private int[] wordFrequencies = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private String[] wordIdStrings = {"", "", "", "", "", "", "", "", "", ""};
    private int maxN = 9; // 5; // Max number of candidates to browse on topBar, maxN allowed values: 2...10

    private int frequencyOfSelectedWord = 1;
    private ContentResolver resolver = null; // getContentResolver();
    private Locale locale = Locale.getDefault();
    private Context thisContext;
    //final boolean hasShortcutColumn = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    private int itemsFound = 0;
    private boolean reloadRequired = true;

    private String localeString = "en_US";
    private boolean isTopBar = false;
    private boolean isKeypadVisible = false;
    private boolean isUserActive = true;
    private boolean isPasswordMode = false;
    private int itemSelected = 0; // Which one of the (three) matches
    private boolean debugOn = false; // true; // For TEST ONLY !!!

    void initializeWords(Context context) {

        thisContext = context;
        itemsFound = 0;
        if (locale == null) {
            locale = Locale.getDefault();
            localeString = locale.toString();
        }
        resolver = context.getContentResolver();
        if (debugOn) logWords(); // for debug only
        isTopBar = CMBOKeyboardApplication.getApplication().getPreferences().getDisplayWordCandidates();
        Log.d("-CAND", "> initializeWords(), localeString = " + localeString);
        // loadDictionary(); // This is too early (in v5.3) and causes java.lang.SecurityException in
	    //  old devices, like Android 5
    }

    private int activityCounter = 10;

    private void setUserActive(boolean active) {
        if (active) {
            Log.d("-CAND", "> [User set Active, activityCounter = 10]");
            activityCounter = 10;
            resetReloadDictTimer();
        } else {
            activityCounter = 0;
            Log.d("-CAND", "> [User became INactive, activityCounter = 0]");
        }
        this.isUserActive = active;
    }
    private boolean isUserActive() { // checked when dictReloadTimer up
        activityCounter -= 1;
        Log.d("-CAND", "> [User activityCounter = " + activityCounter + "]");
        if (activityCounter < 1) {
            activityCounter = 0;
            setUserActive(false);
            return false;
        } else {
            return this.isUserActive;
        }
    }

    void setKeypadVisible(boolean on) {
        if (on) {
            setUserActive(true);
            this.isKeypadVisible = on;
            checkDictionary();
        } else {
            activityCounter = 0;
        }
        Log.d("-CAND", "*** keypadVisible changed ==> " + this.isKeypadVisible + " ***");
    }

    public void setIsTopBar(boolean on) { // View will also update isTopBar value when starting
        this.isTopBar = on;
        Log.d("-CAND", "- update isTopBar (started by view) ************** ");
    }

    //------------------------------

    private CountDownTimer dictReloadTimer = new CountDownTimer(120000, 1000) { // 120000, 1000
        // (time in ms to onFinish(), Tick interval while counting)
        @Override
        public void onTick(long l) { //
        }
        @Override
        public void onFinish() {
            Log.d("-CAND", "- Dictionary reload timer done. Reload required?");
            dictReloadTimerUp(); // Reload?
        }
    };

    private CountDownTimer rotateTimer = new CountDownTimer(30, 10) {
        // (time in ms to onFinish(), Tick interval while counting)
        @Override
        public void onTick(long l) { // to
        }
        @Override
        public void onFinish() {
            //browseCandidates(1);
            Log.d("-CAND-ROT", "Candidates rotated. itemSelected = " + itemSelected
                    + ", * word selected = " + wordCandidates[itemSelected] + ", itemsFound = " + itemsFound);
            rotateTimer.cancel();
        }
    };


    public void checkDictionary() {
        Log.d("-CAND", "  - Check Dictionary. Reload if required.");
        if (reloadRequired) {
            loadDictionary(); // load to memory
            reloadRequired = false;
            return;
        }
        Log.d("-CAND", "  - Dictionary reload NOT required.");
    }

    private void resetReloadDictTimer() { // User is active, no reload for a while needed
        dictReloadTimer.cancel();
        dictReloadTimer.start(); // no reload required for a while
        reloadRequired = false;
    }

    private void dictReloadTimerUp() { // User has not been active, consider reloading dictionary using timer
        //clearHint(); // indication to user that time is up
        Log.d("-CAND", " DictReloadTimerUp(). keypad visible = " + isKeypadVisible + ", isTopBar = " + isTopBar);
        reloadRequired = true; // Reload required before use next time
        dictReloadTimer.cancel(); // Stop counter
        if (isKeypadVisible && isTopBar && isUserActive()) {
            loadDictionary();
            reloadRequired = false;
            // keep Dictionary loaded while keypad visible after timer up
            dictReloadTimer.start();
        }
    }

        // Keep dictionary available/loaded with correct locale
    private void loadDictionary() { // run e.g. when language selected, Opt 2
        Log.d("-CAND", "*** loadDictionary() - " + localeString);
        resetReloadDictTimer();
        String [] tmp = findWords("-xqxx-", localeString, false);
    }

    //--------------------------

    void setPasswordMode(boolean on) { this.isPasswordMode = on;} // (package-private)
    private boolean isPasswordMode() {return this.isPasswordMode;}
    public int itemsFound() {return this.itemsFound;}  // 0 = not found any (max 3)
    public int itemSelected() {return this.itemSelected;}

    public void setLocaleString(String locString) { // "en_US" etc.
        if (!locString.equals(localeString)){ // Reload dictionary when language set in View
            this.localeString = locString;
            Log.d("-CAND", "localeString set to " + locString);
            loadDictionary(); // Is this really needed here?!!! May cause problems with Android 5.1 and earlier.
        }
    }

    // Add word
    public void addWordToDictionary(String localeString) { // left end part of topBar pressed (green '+')
        setUserActive(true);
        if ((wordHint.length() > 3) && (!isWordInDictionary(wordHint, localeString))) { // to add, min 4 letters required!
            Log.d("-CAND", "> addWordToDictionary(): " + wordHint);
            addWordToDict(wordHint, localeString);
        } else {
            Log.d("-CAND", "> addWordToDictionary(). Did NOT add: " + wordHint);
        }
        if (debugOn) logWords();
    }

    // Remove word based on Id
    private void removeWordById(String idString) {
        // delete
        if (idString.equals("")) return;
        resolver.delete(UserDictionary.Words.CONTENT_URI,
                UserDictionary.Words._ID + "=?", new String[] { idString });
        if (debugOn) logWords();
    }

    private void updateFrequencyByIdString(String idString, int newFrequency) {
        // increment frequency
        if (idString.equals("")) return;
        ContentValues values = new ContentValues(4);
        values.put(UserDictionary.Words.FREQUENCY, newFrequency);
        //values.put(UserDictionary.Words.APP_ID, "com.combokey.basic");
        // This will finally update the data:
        int result = resolver.update(UserDictionary.Words.CONTENT_URI, values, UserDictionary.Words._ID + "=?", new String[] { idString } );
        if (debugOn) logWords();
    }

    // Remove word shown on text and on topBar
    public void removeWordFromDictionary(String locString) { // right end part of topBar pressed (red 'X')
        setUserActive(true);
        Log.d("-CAND", "> removeWordFromDictionary: " + wordCandidates[this.itemSelected] + ", hint: " + wordHint
                + ", locale: " + localeString + ", itemSelected = " + itemSelected);
        if ((wordHint.length() > 2)
                && (wordCandidates[this.itemSelected].length() > 3)
                && (localeString.equals(locString))) {
            removeWordById(wordIdStrings[this.itemSelected]);
            showToast("X " + wordCandidates[this.itemSelected], "#FFCCCC");
            if (debugOn) logWords(); // TEST
            wordCandidates[this.itemSelected] = ""; // remove from topBar, too
        }
    }

    // Remove word just based on word
    private void removeWordFromDict(String word, String localeString) // not used in this app version
    {
        if (TextUtils.isEmpty(word)) {
            return;
        }
        resolver.delete(UserDictionary.Words.CONTENT_URI,
                UserDictionary.Words.WORD + "=?", new String[] { word });
        // TODO: check locale before deleting
        showToast("X " + word, "#FFAAAA");
    }

    // "it_IT" to "it-IT" to Locale
    private Locale convertLocaleStringToLocale(String localeString) {
        Locale loc = Locale.getDefault();
        String tag = loc.toString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // forLanguageTag requires API 21
            try {
                //String tag = localeString.substring(0,2) + "-" + localeString.substring(3, 5);
                tag = localeString.replace("_", "-"); // "it_IT" to "it-IT"
                //loc = Locale.forLanguageTag("it-IT");
                loc = Locale.forLanguageTag(tag); // requires API 21 (now 14 is min)
                Log.d("-CAND", ">>> tag = " + tag + " <<< Locale.forLanguageTag(tag) ==>" + loc.toString());
            } catch (Exception e) {
                loc = Locale.getDefault();
            }
        } else { // Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
            // Cannot do! Using default Locale
            Log.d("-CAND", ">>> Cannot covert " + localeString + " to Locale! Using: " + tag);
        }
        return loc;
    }

    private void addWordToDict(String word, String localeString) {

        locale = Locale.getDefault();
        int columnCount = 3;
        int frequency = 1; // new word, not frequently used yet
        String shortcut = "";
        if (wordHint.length() > 2) shortcut = wordHint.substring(0, 2); // (0, 2) two(!) characters (end index is exclusive)

        if (localeString.equals("")) { // TEMP !!!!
            localeString = locale.toString();
        }
        resolver = thisContext.getContentResolver();

        // Two options to add word
        // - recent devices:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 'forLanguageTag' requires API 21 (convertLoc...)
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // API 16 is minimum for 'addWord'
            locale = convertLocaleStringToLocale(localeString);
            UserDictionary.Words.addWord(thisContext, word, frequency, shortcut, locale); // locale is not a string here
            // - older devices:
        } else {
            ContentValues values = new ContentValues(columnCount);
            values.put(UserDictionary.Words.LOCALE, localeString);
            values.put(UserDictionary.Words.WORD, word);
            values.put(UserDictionary.Words.FREQUENCY, 1);
            //values.put(UserDictionary.Words.APP_ID, "com.combokey.basic");
            // This will finally add the data:
            Uri result = resolver.insert(UserDictionary.Words.CONTENT_URI, values);
        }
        showToast("+ " + word, "#AAFFAA");
        Log.d("-CAND", "> addWordToDict(word, localeString/locale): " + word + ", "
                + localeString + "/" + locale.toString());
    }

    private boolean isWordInDictionary (String wordString, String localeString) {

        boolean wordFoundInDict = false;
        StringBuilder data = new StringBuilder();
        String[] projection = 	{
                UserDictionary.Words._ID,
                UserDictionary.Words.LOCALE,
                UserDictionary.Words.WORD,
                UserDictionary.Words.FREQUENCY
        };
        String sortOrder = UserDictionary.Words.LOCALE;
        Cursor cursor = resolver.query(UserDictionary.Words.CONTENT_URI
                , projection
                , UserDictionary.Words.WORD +  "=?"
                , new String[] { wordString }, sortOrder);    // Find word
        if (cursor == null) {
            return false;
        }
        int idColumn = cursor.getColumnIndex(UserDictionary.Words._ID);
        int localeColumn = cursor.getColumnIndex(UserDictionary.Words.LOCALE);
        int wordColumn = cursor.getColumnIndex(UserDictionary.Words.WORD);
        try {
            while (cursor.moveToNext()) {
                if (cursor.getString(localeColumn).equals(localeString)) {
                    wordFoundInDict = true;
                    Log.d("-CAND", "> Word " + wordString + " is in the Dictionary (" + localeString + ")");
                }
            }
        } catch (Exception e) {
            cursor.close();
        }
        cursor.close();
        if (!wordFoundInDict) {
            Log.d("-CAND", "> Word " + wordString + " IS NOT in the Dictionary! (" + localeString + ")");
        }
        return wordFoundInDict;
    }

    private String[] findWords(String hintString, String localeString, boolean doRotate) {

        boolean debugWordlist = false; // for testing
        int minNumberOfLetters = 2; // letters required for starting word prediction
        previousWordShown = wordCandidates[this.itemSelected];

        String[] wordsFound = {hintString, "", "", "", "", "", "", "", "", ""}; // maxN: 2...10 allowed
        int[] frequencies = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        itemsFound = 0; // e.g. for setting green/yellow text color if word found
        if (hintString.length() < minNumberOfLetters) {
            return wordsFound;
        }
        int item = 0;
        int frequency = 0;
        String word = "";
        String wordLocale = "";

        StringBuilder data = new StringBuilder();

        String[] projection = 	{
                UserDictionary.Words._ID,
                UserDictionary.Words.LOCALE,
                UserDictionary.Words.WORD,
                UserDictionary.Words.FREQUENCY
        };

        String sortOrder = UserDictionary.Words.LOCALE;
        Cursor cursor; // = null; // << null Gave Security Exception in Android 5.1 and earlier (fixed in Manifest):
        try {  // when onDraw(View) > loadDictionary > > findWords("-xqxx-"...) > java.lang.SecurityException is possible
            cursor = resolver.query(UserDictionary.Words.CONTENT_URI
                    , projection //
                    , UserDictionary.Words.WORD + " LIKE ?"
                    , new String[]{hintString + "%"}, sortOrder);    // Find word
        } catch (Exception e) {
            Log.d("-CAND", "error: " + e.toString());
            return wordsFound; // error, return value: 'no words found'
        }
        if ((cursor == null) || (cursor.getCount() == 0)) return wordsFound; // return value: 'no words found'
        // << quit if cursor is null or there are no lines
        int idColumn = cursor.getColumnIndex(UserDictionary.Words._ID);
        int localeColumn = cursor.getColumnIndex(UserDictionary.Words.LOCALE);
        int wordColumn = cursor.getColumnIndex(UserDictionary.Words.WORD);
        int frequencyColumn = cursor.getColumnIndex(UserDictionary.Words.FREQUENCY);
        int appIdColumn = cursor.getColumnIndex(UserDictionary.Words.APP_ID);

        try {
            while (cursor.moveToNext()) {

                wordLocale = cursor.getString(localeColumn);
                if (wordLocale.equals(localeString)) {    // When word found, check locale
                    wordsFound[item] = cursor.getString(wordColumn);
                    frequencies[item] = Integer.parseInt(cursor.getString(frequencyColumn));
                    wordFrequencies[item] = frequencies[item];
                    wordIdStrings[item] = cursor.getString(idColumn);
                    item += 1;
                    itemsFound = item;
                    if (item > maxN - 1) {
                        item = maxN - 1;
                        //break;
                    }
                }

                if (debugWordlist) {
                    data.append(cursor.getString(0)); // _ID
                    data.append(", ");
                    data.append(cursor.getString(1)); // LOCALE
                    data.append(", ");
                    data.append(cursor.getString(2)); // WORD
                    data.append(", ");
                    data.append(cursor.getString(3)); // FREQUENCY
                    data.append(", ");
                    data.append("\n");
                }

            }
            cursor.close();
        } catch (Exception e) {
            cursor.close();
        }
        // Sound when found:
        if (itemsFound != 0) { // API 26 (Oreo) required, amplitude 1...255 or DEFAULT_AMPLITUDE
            // Does not seem to work yet
            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE);
        }
        // debug words found:
        if(debugWordlist) Log.d("-CAND", "Contents matching:\n" + data);
        Log.d("-CAND-ORDER", "*Candidates found: " + wordsFound[0] + ", " + wordsFound[1] + ", " + wordsFound[2] + ", "
                + wordsFound[3] + ", " + wordsFound[4] + "..., itemsFound = " + itemsFound);
        Log.d("-CAND-ORDER", "     - frequencies: " + frequencies[0] + ", " + frequencies[1] + ", " + frequencies[2] + ", "
                + frequencies[3] + ", " + frequencies[4] );

        // -------- Set word order according to frequency: ---------
        int tempFreq = 0; // Sort the freq array in descending order
        String tempId = "";
        String tempWord = "";
        for (int i = 0; i < itemsFound; i++) {
            for (int j = i+1; j < itemsFound; j++) {
                if(frequencies[i] < frequencies[j]) {
                    tempFreq = frequencies[i]; tempWord = wordsFound[i]; tempId = wordIdStrings[i];
                    frequencies[i] = frequencies[j]; wordsFound[i] = wordsFound[j]; wordIdStrings[i] = wordIdStrings[j];
                    frequencies[j] = tempFreq; wordsFound[j] = tempWord; wordIdStrings[j] = tempId;
                }
            }
        }
        // -----------------------------------------------------
        // debug words after re-ordering:
        if(debugWordlist) {
            Log.d("-CAND-ORDER", "*Frequency order: " + wordsFound[0] + ", " + wordsFound[1] + ", " + wordsFound[2] + ", "
                    + wordsFound[3] + ", " + wordsFound[4] + "..., itemsFound = " + itemsFound);
            Log.d("-CAND-ORDER", "- freqs in order: " + frequencies[0] + ", " + frequencies[1] + ", " + frequencies[2] + ", "
                    + frequencies[3] + ", " + frequencies[4] + ", itemSelected = " + itemSelected);
        }
        // -----------------------------------------------------

        if (previousWordShown.equalsIgnoreCase(wordsFound[this.itemSelected])) { // same word?
            if (doRotate) { // do not rotate when just reloading dictionary
                rotate(); // keep changing words on top bar display even if potential candidates are the same
                Log.d("-CAND-ORDER", "Candidates rotated. itemSelected = " + itemSelected + ", * word selected = " + wordsFound[itemSelected] + ", itemsFound = " + itemsFound);
            } else {
                Log.d("-CAND-ORDER", "Candidates NOT rotated. itemSelected = " + itemSelected + ", * word selected = " + wordsFound[itemSelected] + ", itemsFound = " + itemsFound);
            }
        } else if (wordsFound[this.itemSelected].equals("")){ // nothing more on list?
            this.itemSelected = 0;
        }
        if(debugWordlist) {
            Log.d("-CAND-ORDER", "* Rotated order: " + wordsFound[0] + ", " + wordsFound[1] + ", " + wordsFound[2] + ", "
                    + wordsFound[3] + ", " + wordsFound[4] + "..., itemsFound = " + itemsFound);
            Log.d("-CAND-ORDER", "- freqs rotated: " + frequencies[0] + ", " + frequencies[1] + ", " + frequencies[2] + ", "
                    + frequencies[3] + ", " + frequencies[4] + ", itemSelected = " + itemSelected);
        }

        this.wordFrequencies = frequencies;

        return wordsFound;
    }

    private void logWords() { // for debug only: list all words in dictionary
        StringBuilder data = new StringBuilder();
        String[] projection = 	{
                UserDictionary.Words._ID,
                UserDictionary.Words.LOCALE,
                UserDictionary.Words.WORD,
                UserDictionary.Words.FREQUENCY,
                UserDictionary.Words.APP_ID
        };
        String sortOrder = UserDictionary.Words.LOCALE;
        Cursor cursor = resolver.query(UserDictionary.Words.CONTENT_URI, projection, null, null, sortOrder);
        if (cursor == null) return;
        try {
            while (cursor.moveToNext()) {
                data.append(cursor.getString(0)); // _ID
                data.append(", ");
                data.append(cursor.getString(1)); // LOCALE
                data.append(", ");
                data.append(cursor.getString(2)); // WORD
                data.append(", ");
                data.append(cursor.getString(3)); // FREQUENCY
                //data.append(", "); // app id not used
                //data.append(cursor.getString(4)); // APP_ID
                //data.append(", ");
                data.append("\n");
            }
            cursor.close();
        } catch (Exception e) {
            cursor.close();
        }
        Log.d("-CAND", "Contents(id, locale, word, freq):\n" + data);
    }

    public boolean autorepeatOn = false;

    void updateWordAtCursor(String word) { // (keyboard -> service -> here)
        setUserActive(true); // someone is typing
        Log.d("-CAND", "   * Prepare for predict() at updateWordAtCursor(), isTopBar = " + isTopBar + ", word = " + word);
        if (!isTopBar || this.autorepeatOn) return;
        if (isPasswordMode() || word.equals("")) {
            clearHint();
            return;
        }
        if (!word.matches(".*\\d.*")) { // cannot include digits (passwords often do)
            this.wordHint = word;
            predict(this.wordHint);
        } else {
            clearHint();
        }
    }

    void addLetter(String letter) { // For Service, adding typically a space to clear hint+candidates

        if ((!isTopBar) || (letter.equals(""))) return;
        if (isPasswordMode()) {
            clearHint();
            return;
        }
        if (letter.substring(letter.length() - 1).equals(" ")) { // next word
            clearHint();
            Log.d("-CAND", "> Cleared word candidates (next word coming).");
        }
    }

    public void clearHint() { // hint word is cleared as well as the word candidates
        if (!isTopBar) return;
        int i = 0;
        while (i < maxN) { // maxN = array length (e.g. 3 gives three items in array)
            this.wordCandidates[i] = "";
            i++;
        }
        this.wordHint = "";
        this.itemSelected = 0;
        this.itemsFound = 0; // clears ellipsis on topBar and sidePad
        Log.d("-CAND", "> Cleared word candidates (C). localeString = " + localeString);
    }

    public String getWordCandidate() { // For View to show candidate words on topBar
        Log.d("-CAND", "> Get wordCandidate[" + itemSelected + "] for TopBar = " + this.wordCandidates[itemSelected]);
        return  this.wordCandidates[itemSelected];
    }

    private void rotate() { // to show different matching candidate for each letter typed
        browseCandidates(1); // Option 1, no delay
        //rotateTimer.start(); // Option 2 with delay
    }

    public void browseCandidates(int step) { // Browse candidates forwards or backwards (step = 1/-1)

        Log.d("-CAND-ORDER", "=== browse === step: " + step);

        if (this.wordCandidates[this.itemSelected].equals("")) {
            this.itemSelected = 0;
            Log.d("-CAND-ORDER", "Empty: ==> itemSelected = 0" + ", "
                    + this.wordFrequencies[0] + " " + this.wordCandidates[0]);
        }

        int itemSel = this.itemSelected;
        itemSel += step;

        if ((step == 1) && ((itemSel > maxN - 1) || (itemSel > this.itemsFound - 1))) { // end reached?
            itemSel = 0;
        } else if ((step == -1) && (itemSel < 0) && (itemsFound > 0)) {
            itemSel = this.itemsFound - 1;
        }

        if (itemSel > -1) this.itemSelected = itemSel;
        resetReloadDictTimer(); // avoid candidates from disappearing

        Log.d("-CAND-ORDER", "Not empty ==> itemSelected = " + this.itemSelected + " "
                + this.wordFrequencies[this.itemSelected] + " " + this.wordCandidates[this.itemSelected]);
    }

    public String acceptWordCandidate(int swipeSpanX) { // swipeSpanX switches between candidates if deviates a lot from 0
        // Browse (swipe) candidate words or accept (tap) candidate word shown
        String result = "";
        if (swipeSpanX > 50) { // swipe left on topBar to see next word
            browseCandidates(-1);
            return "";
        } else if (swipeSpanX < -50) { // swipe right on topBar to see previous word
            browseCandidates(1);
            return "";
        } else { // tap on word on topBar to select the candidate word shown
            Log.d("-CAND-ORDER", "> Accept wordCandidate[" + itemSelected + "] = " + this.wordCandidates[itemSelected] + ".");
            Log.d("-CAND-ORDER", "> - with word Id/Frequency: " + this.wordIdStrings[itemSelected] + "/" + this.wordFrequencies[itemSelected]);
            if (this.wordCandidates[itemSelected].length() >= this.wordHint.length()) { // v5.4: '>' to '>='

                // TODO: add dealing with selected word's frequency, Then deal with max values of frequency!!!
                int oldFreq = this.wordFrequencies[itemSelected];
                int newFreq = oldFreq + 1;
                String wrdId = this.wordIdStrings[itemSelected];
                if (newFreq > 128) {
                    newFreq = 100;  // First approximation of dealing with ever
                }                   // increasing values of frequency (max 255).
                updateFrequencyByIdString(wrdId, newFreq);

                Log.d("-CAND-ORDER", "> Updated wrdId = " + wrdId + " (" + this.wordCandidates[itemSelected]
                        + ") freq " + oldFreq + " to " + newFreq);
                // Space policy
                result = this.wordCandidates[itemSelected].substring(this.wordHint.length());
                itemSelected = 0;
                itemsFound = 0; // this will clear ellipsis on topBar and sidePad
                // replace "_" with Space ("New_York" becomes "New York" etc.)
                result = result.replace("_", " ");
                // Optionally add space after a shortcut word or a candidate word
                boolean noTrailingSpace = CMBOKeyboardApplication.getApplication().getPreferences().isTrailingSpaceRemoved();
                if (noTrailingSpace) { // Keep trailing space in shortcut words?
                    return  result;
                }

                return result + " ";
            }
            return this.wordHint;
        }

    }

    private void predict(String hint) { // For each typed letter, find matching words
        Log.d("-CAND", " * predict()");
        this.wordCandidates = findWords(hint, this.localeString, true);
    }

    private String [] predictedWords(String hint) { // not used momentarily
        if (reloadRequired) {
            predictAsync(hint);
            reloadRequired = false;
            return findWords(hint, this.localeString, true);
        }
        Log.d("-CAND", " * predict()");
        return findWords(hint, this.localeString, true);
    }


    private void predictAsync(String hint) { // not used momentarily
        wordHint = hint;
        resetReloadDictTimer();
        Log.d("-CAND", "> * predictAsync()");
        AsyncTask.execute(new Runnable() {
            //@Override
            public void run() {
                // background code
                wordCandidates = findWords(wordHint, localeString, true);
            }
        });

    }

    private void showToast(String message, String colorStr) { // Response to user for Adding/Removing words in Dictionary
        if (colorStr.equals("")) colorStr = "#FFFFFF";
        Toast toast = Toast.makeText(thisContext, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
        View toastView = toast.getView();
        toastView.setBackgroundResource(R.drawable.toast_background);
        TextView text = (TextView) toastView.findViewById(android.R.id. message);
        text.setTextColor(Color.parseColor(colorStr));
        toast.show();
    }

}
