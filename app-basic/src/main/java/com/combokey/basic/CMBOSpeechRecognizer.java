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

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.Locale;

import android.util.Log;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import android.widget.Toast;

import com.combokey.basic.layout.Layout;


public class CMBOSpeechRecognizer extends Activity implements OnClickListener
{

    final int USER_PERMISSON_REQUEST_RECORD_AUDIO = 100208;
    final int USER_PERMISSON_REQUEST_INTERNET = 100209;
    boolean recordAudioPermissionGranted = false;
    boolean internetPermissionGranted = false;

    private EditText editText;
    private TextView langInfoText;
    private TextView tapMicText;
    private SpeechRecognizer sr;
    private static final String TAG = "-STT";
    // STILL A DRAFT SETTING:
    private boolean startDirectly = CMBOKeyboardApplication.getApplication().getPreferences().isTtsDirect();
    private String startText = "Start";
    //private String startText = this.getResources().getString(R.string.tap_on_mic); // CRASH!!!
    private String stopText = "Stop";
    private boolean isRunning = false;
    private boolean resultsHandled = false;
    // private String stopText = this.getResources().getString(R.string.tap_on_mic_to_stop); // CRASH!!!
    final float dispDensity = CMBOKeyboardApplication.getApplication().getResources().getDisplayMetrics().density;
    final float scale = (float) (0.42 * dispDensity + 0.44/dispDensity);


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_input);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ImageButton speakButton = (ImageButton) findViewById(R.id.btnSpeak);
        //ImageButton stopButton = (ImageButton) findViewById(R.id.speak_stop);
        ImageButton okButton = (ImageButton) findViewById(R.id.btnOk);
        ImageButton clearButton = (ImageButton) findViewById(R.id.btnClear);

        editText = (EditText) findViewById(R.id.txtSpeechInput);
        tapMicText = (TextView) findViewById(R.id.tapMic);
        langInfoText = (TextView) findViewById(R.id.langInfo);

        editText.setTextSize(CMBOKeyboardApplication.getApplication().getPreferences().getFontSize() * scale);

        startText = CMBOKeyboardApplication.getApplication().getResources().getString(R.string.tap_on_mic);
        stopText = CMBOKeyboardApplication.getApplication().getResources().getString(R.string.tap_on_mic_to_stop);
        tapMicText.setText(startText);

        speakButton.setOnClickListener(this);
        okButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);

        checkRecordAudioPermission();
        checkInternetPermission();

        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());

        setLanguageTag();

        if (startDirectly) {
            showToast(CMBOKeyboardApplication.getApplication()
                    .getResources().getString(R.string.speak_or_tap_on_mic));
            resultsHandled = false;
            startMic();
        }

    }

    /*
    private boolean isXLarge() { // resource 'isTablet' in attr.xml in folder sw600dp.xml
        return getResources().getBoolean(R.bool.isTablet); // 2019-11-28
    }
    */

    class listener implements RecognitionListener
    {

        public void onReadyForSpeech(Bundle params)
        {
            Log.d("-SPEECH", "> onReadyForSpeech");
            Log.d("-SPEECH", "- Called when the endpointer is ready for the user to start speaking.");
        }
        public void onBeginningOfSpeech()
        {
            Log.d("-SPEECH", "> onBeginningOfSpeech");
            Log.d("-SPEECH", "- The user has started to speak.");
        }
        public void onRmsChanged(float rmsdB)
        {   // This is a repeating message, do not show on log
            //Log.d("-SPEECH", "> onRmsChanged");
            //Log.d("-SPEECH", "- The sound level in the audio stream has changed.");
        }
        public void onBufferReceived(byte[] buffer)
        {
            Log.d("-SPEECH", "> onBufferReceived");
            Log.d("-SPEECH", "- More sound has been received. The purpose of this function " +
                    "is to allow giving feedback to the user regarding the captured audio. There is " +
                    "no guarantee that this method will be called.");
        }

        public void onEndOfSpeech()
        {
            Log.d("-SPEECH", "> onEndOfSpeech");
            Log.d("-SPEECH", "- Called after the user stops speaking.");
            tapMicText.setText(startText);
        }

        public void onError(int error)
        {
            Log.d("-SPEECH",  "**** Error " +  error + ". A network or recognition error occured:");
            //Log.d("-SPEECH", "onError:\n- A network or recognition error occured.");
            //String nText = editText.getText() + " Error " + error + " = ";
            isRunning = false;
            tapMicText.setText(startText);

            //String nText = "Error " + error + " = ";
            String nText = "Error: ";
            nText = nText + errorMessage(error);
            langInfoText.setText(nText);

            if (startDirectly) okPressed();
        }

        public void onResults(Bundle results)
        {
            if (resultsHandled) return;

            String str = ""; // new String();
            Log.d("-SPEECH", "> onResults() " + results);
            tapMicText.setText(startText);
            isRunning = false;
            //Log.d("-SPEECH", "onResults");

            ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++)
            {
                Log.d("-SPEECH", "result " + data.get(i));
                str += "\n" + data.get(i);
            }
            String nText = data.size() + " results: " + str;
            String resultText = data.get(0).toString(); // first option of results only

            String fullText = (editText.getText().toString().equals("")
                    || editText.getText().toString().equals(" ")) ? resultText
                     : editText.getText().toString() + " " + resultText;
            editText.setText(fullText);
            editText.setSelection(editText.getText().length()); // put cursor to end of text
            // Broadcast this text to app when OK button is pressed

            if (startDirectly) {
                startDirectly = false;
                resultsHandled = true;
                okPressed();
            }
            resultsHandled = true;
        }

        public void onPartialResults(Bundle partialResults)
        {
            Log.d("-SPEECH", " > onPartialResults");
            Log.d("-SPEECH", "- Called when partial recognition results are available. " +
                    "The callback might be called at any time between onBeginningOfSpeech() and " +
                    "onResults(android.os.Bundle) when partial results are ready. This method may " +
                    "be called zero, one or multiple times for each call to SpeechRecognizer#" +
                    "startListening(Intent), depending on the speech recognition service implementation. " +
                    "To request partial results, use RecognizerIntent#EXTRA_PARTIAL_RESULTS");

        }

        public void onEvent(int eventType, Bundle params)
        {
            Log.d("-SPEECH", "> onEvent " + eventType);
            Log.d("-SPEECH", "- Reserved for future events.");
        }

    } // End of recognitionListener

    /**
     * The edited speech input text is ready for input to the app
     * */
    private void sendSpeechInput(String txtSpeechInput) { // OK button pressed => broadcast the text to Service
        Intent broadcast1 = new Intent("getting_speech_data");
        //broadcast1.putExtra("value", txtSpeechInput.getText() + "");
        broadcast1.putExtra("value", txtSpeechInput);
        sendBroadcast(broadcast1);
        Log.d("-SPEECH", "*** Text broadcasted to app: " + txtSpeechInput);
    }


    // ----------------------- permissions ----------------------------------

    private void checkRecordAudioPermission() {

        // Dealing with the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("-SPEECH", "* Record audio permission not granted.");

            // Permission not granted
            // Show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Log.d("-SPEECH", "* Reason explained for permission RECORD_AUDIO");
                Toast toast = Toast.makeText(this,
                        "Voice-to-text requires access to your microphone.",
                        Toast.LENGTH_LONG);
                toast.show();
            } else {
                // request permission
                Log.d("-SPEECH", "* Record audio permission requested.");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        USER_PERMISSON_REQUEST_RECORD_AUDIO);

                // MY_PERMISSIONS_REQUEST_RECORD_AUDIO is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            Log.d("-SPEECH", "* Record audio permission has been granted.");

        }

    }

    private void setLanguageTag() {

        String extra = CMBOKeyboardApplication.getApplication()
                .getLayoutManager().getCurrentLayout().getValue(Layout.Metadata.LAYOUT_EXTRA);
        String lang = CMBOKeyboardApplication.getApplication()
                .getLayoutManager().getCurrentLayout().getValue(Layout.Metadata.DESCRIPTION);
        String langISO = CMBOKeyboardApplication.getApplication()
                .getLayoutManager().getCurrentLayout().getValue(Layout.Metadata.ISO_639_1);

        String[] ary = extra.split("\",\""); // Note: Not allowed to have any space between items? Just a comma?
        String languageTag = " - " + ary[2] + "-" + ary[3]; // "en-US"

        String languageInfo = "USA"; // "USA"
        if (ary[4].equals(ary[5])) {
            languageInfo = "(" + ary[4] + ")";
        } else {
            languageInfo = "(" + ary[4] + "/" + ary[5] + ")";
        }

        String languageInformation = "";

        if (languageTag.length() < 3+5) {
            languageTag = "";
            languageInfo= "";
            languageInformation = lang + " (" + langISO + ")";
        } else {
            languageInformation = lang + " " + languageInfo + languageTag;
        }

        //languageInformation = lang + " - " + languageTag + " - " + languageInfo;
        langInfoText.setText(languageInformation);

        Log.i("-SPEECH", "(setLanguageTag() for Voice Input) - languageTag = " + languageTag);
    }



    // ...............

    private void checkInternetPermission() {

        // Dealing with the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("-SPEECH", "* Internet permission not granted.");
            // Permission is not granted
            // Show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {
                // Show an explanation to the user *asynchronously* -- do not block
                // this thread waiting for the user's response. After the user
                // sees the explanation, try again to request the permission.
                Log.d("-SPEECH", "* Reason explained for permission INTERNET");
                Toast toast = Toast.makeText(this,
                        "Voice-to-text requires internet access to Google servers.",
                        Toast.LENGTH_LONG);
                toast.show();
            } else {
                // No explanation needed; request the permission
                Log.d("-SPEECH", "* Internet permission requested.");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.INTERNET},
                        USER_PERMISSON_REQUEST_INTERNET);
                // This is an app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Log.d("-SPEECH", "* Internet permission has been granted.");
            // Permission has already been granted
        }

    }

    // --------

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case USER_PERMISSON_REQUEST_RECORD_AUDIO:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, so do the
                    // contacts-related task.
                } else {
                    langInfoText.setText(getString(R.string.no_audio_recording_permission));
                    // Permission denied, so disable the
                    // functionality that depends on this permission.
                }
                break;


            case USER_PERMISSON_REQUEST_INTERNET:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, so do the
                    // contacts-related task.
                } else {
                    langInfoText.setText(getString(R.string.no_internet_permission));
                    // Permission was denied. Disable the
                    // functionality that depends on this permission.
                }
                break;

            // Other cases to check for other
            // permissions...

            default:
                break;
        }
    }




    // -------------------- end of permissions ------------------------------------

    private String errorMessage(int errorCode) {
        String msg = "";
        switch (errorCode) {
            case 1:
                msg = getString(R.string.network_operation_timed_out);
                break;
            case 2:
                msg = getString(R.string.other_network_related_errors);
                break;
            case 3:
                msg = getString(R.string.audio_recording_error);
                break;
            case 4:
                msg = getString(R.string.server_error_or_no_internet);
                break;
            case 5:
                msg = getString(R.string.other_client_side_errors);
                break;
            case 6:
                msg = getString(R.string.no_speech_input);
                break;
            case 7:
                msg = getString(R.string.no_recognition_result_matched);
                break;
            case 8:
                msg = getString(R.string.recognitionservice_busy);
                break;
            case 9:
                msg = getString(R.string.insufficient_permissions);
                break;
            default:
                msg = getString(R.string.other_error);
                break;
        }
        Log.d("-SPEECH", "   - error message is: " + msg);
        showToast(msg);
        return msg;

    }

    public void onClick(View v) { // Mic pressed
        if (v.getId() == R.id.btnSpeak)
        {
            startDirectly = false; // Switch to manual mode if any button pressed
            if (isRunning) {
                stopMic();
                return;
            }
            startMic();
        }

        if (v.getId() == R.id.btnClear) { // Clear pressed
            startDirectly = false; // Switch to manual mode if any button pressed
            clearPressed();
        }

        if (v.getId() == R.id.btnOk) { // OK pressed
            startDirectly = false; // Switch to manual mode if any button pressed
            okPressed();
        }

    }

    public void okPressed() {

        Log.d("-SPEECH", "*** OK Pressed");

        isRunning = false;
        tapMicText.setText(startText);
        //sr.stopListening();
        sendSpeechInput("" + editText.getText().toString() + ""); // Broadcast this text to app
        finish();
    }

    public void clearPressed() {

        Log.d("-SPEECH", "*** Clear Pressed");

        editText.setText("");
        setLanguageTag();
    }

    public void startMic() {

        Log.d("-SPEECH", "*** Start Mic Pressed");

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getApplication().getPackageName());

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
        //startActivity(intent);
        sr.startListening(intent);
        isRunning = true;
        tapMicText.setText(stopText);
        setLanguageTag();

        Log.i("-SPEECH","Listening...");
    }

    public void stopMic() {

        Log.d("-SPEECH", "*** Stop Mic Pressed");

        isRunning = false;
        sr.stopListening();
        tapMicText.setText(startText);
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
