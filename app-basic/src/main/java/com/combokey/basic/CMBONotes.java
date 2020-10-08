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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
//import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
//import android.widget.Toast;

public class CMBONotes extends Activity implements View.OnClickListener {

    private EditText notesText;
    private boolean dataArrived = false; // Have previous contents been received?
    private String notesHeader = ""; // getString(R.string.flashtext_notes);

    final float dispDensity = CMBOKeyboardApplication.getApplication().getResources().getDisplayMetrics().density;
    final float scale = (float) (0.42 * dispDensity + 0.44/dispDensity);

    //BroadcastReceiver broadcastReceiver2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        //this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        dataArrived = false;
        ImageButton notesCloseButton = (ImageButton) findViewById(R.id.notes_close_button);
        notesText = (EditText) findViewById(R.id.notes_text);

        notesCloseButton.setOnClickListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("sending_notes_data"));
        Log.d("-NOTES", "onCreate ready?");

    }


    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            //notesHeader = getString(R.string.flashtext_notes);
            String message = intent.getStringExtra("value");
            String msg = "\n" + message;
            String fontsize = intent.getStringExtra("fontsize");
            int fontSize = Integer.parseInt(fontsize);
            notesText.setTextSize(fontSize * scale);
            Log.d("-NOTES", "Got message in Notes activity: " + message);
            notesText.setText(msg);
            dataArrived = true;
        }
    };

    private void sendNotesBackToService(String notesText) { // OK button pressed => broadcast the text to Service
        String status = "valid";
        if (!dataArrived) status = "invalid"; // do not send if old contents still missing
        Intent intent = new Intent("getting_notes_data");
        //broadcast1.putExtra("value", txtSpeechInput.getText() + "");
        String notesTxt = notesText;
        if (notesText.length() > 1) notesTxt = notesText.substring(1); // remove linefeed
        intent.putExtra("value", notesTxt);
        intent.putExtra("status", status);
        sendBroadcast(intent);
        Log.d("-NOTES", "*** Notes text broadcasted to service");
    }


    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        sendNotesBackToService("" + notesText.getText().toString() + "");
        dataArrived = false;
        super.onDestroy();
    }


    public void onClick(View v) {
        if (v.getId() == R.id.notes_close_button) { // OK pressed
            sendNotesBackToService("" + notesText.getText().toString() + "");
            // Broadcast this text to app
            dataArrived = false; // prepare for next session
            finish();
        }
    }

}
