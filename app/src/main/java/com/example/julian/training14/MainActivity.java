package com.example.julian.training14;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends ActionBarActivity implements MyDialogFragment.EditNameDialogListener, GestureOverlayView.OnGesturePerformedListener {


    private File mFile;
    private GestureLibrary mGestureLibrary;

    private GestureOverlayView mGestureView;
    private String mCurrentGestureName;
    private Gesture mCurrentGesture;

    private ArrayList<String> mNames = new ArrayList<String>();
    private ArrayList<Gesture> mGestures = new ArrayList<Gesture>();

    public static ListView sLeft_drawer;
    public static ArrayAdapter<String> sAdapter;

    private TextView mode_textView;
    private boolean mode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        //Create the file "gesture_data" in Directory if not already created. We use Internal Storage
        mFile = new File(this.getFilesDir(), "gesture_data");

        //Load Gesture Library
        mGestureLibrary = GestureLibraries.fromFile(mFile);

        Log.i("INFO", "Load Library successful?: " + mGestureLibrary.load());
        if (mGestureLibrary.getGestureEntries().isEmpty()) {
            Toast.makeText(this, "No Gestures saved in file", Toast.LENGTH_SHORT).show();
            Log.i("INFO", "Library empty?: " + mGestureLibrary.getGestureEntries().isEmpty());
        } else {
            Set<String> set = mGestureLibrary.getGestureEntries();
            for (String gesture_name : set) {
                mNames.add(gesture_name);
                mGestures.add(mGestureLibrary.getGestures(gesture_name).get(0));
            }
        }

        //Set up GestureOverlayView and add OnGesturePerformedListener
        mGestureView = (GestureOverlayView) findViewById(R.id.gestureView);
        mGestureView.addOnGesturePerformedListener(this);

        mode_textView = (TextView) findViewById(R.id.mode_textView);
        mode_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = !mode;
                if (mode) {
                    mode_textView.setText("Detection-Mode");
                    mode_textView.setBackgroundColor(Color.RED);
                } else {
                    mode_textView.setText("Add-Gesture-Mode");
                    mode_textView.setBackgroundColor(Color.argb(255, 135, 229, 61));
                }
            }
        });

        //Create a Drawer so that the user can display saved Gestures
        sLeft_drawer = (ListView) findViewById(R.id.left_drawer);
        sAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                mNames);
        sLeft_drawer.setAdapter(sAdapter);
        sLeft_drawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mGestureView.setGesture(mGestures.get(position));
            }
        });

    }

    @Override
    public void onCancel(View v) {
        mCurrentGestureName = null;
        mCurrentGesture = null;
        MyDialogFragment dialogFragment = (MyDialogFragment) getSupportFragmentManager().findFragmentByTag("start_dialog");
        dialogFragment.dismiss();
    }

    @Override
    public void onSave(View v) {
        MyDialogFragment dialogFragment = (MyDialogFragment) getSupportFragmentManager().findFragmentByTag("start_dialog");
        if (dialogFragment.sEditText.getText() != null && dialogFragment.sEditText.getText().toString() != null) {
            mCurrentGestureName = dialogFragment.sEditText.getText().toString();
            if (mNames.contains(mCurrentGestureName)) {
                Toast.makeText(this, "Name already taken, choose a different name", Toast.LENGTH_LONG).show();
                dialogFragment.dismiss();
                onGesturePerformed(mGestureView, mCurrentGesture);
                return;
            } else if (mCurrentGestureName.equals("")) {
                Toast.makeText(this, "No name entered", Toast.LENGTH_LONG).show();
                dialogFragment.dismiss();
                onGesturePerformed(mGestureView, mCurrentGesture);
                return;
            }
        }
        Log.i("INFO", "Saved as: " + mCurrentGestureName);
        mNames.add(mCurrentGestureName);
        mGestures.add(mCurrentGesture);
        mGestureLibrary.addGesture(mCurrentGestureName, mCurrentGesture);
        Log.i("INFO", "Did Library save changes?: " + mGestureLibrary.save());
        sAdapter.notifyDataSetChanged();
        dialogFragment.dismiss();
    }

    private void showDialog() {
        FragmentManager fm = getSupportFragmentManager();
        MyDialogFragment dialogFragment = new MyDialogFragment();
        dialogFragment.show(fm, "start_dialog");
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        mCurrentGesture = gesture;
        if (mode) {
            ArrayList<Prediction> predictions = mGestureLibrary.recognize(gesture);
            if (predictions.isEmpty()) {
                return;
            }
            double detection_score = 0;
            Prediction top_prediction = predictions.get(0);
            for (Prediction prediction : predictions) {
                if (prediction.score > detection_score) {
                    top_prediction = prediction;
                    detection_score = prediction.score;
                }
            }
            if (detection_score > 1.5) {
                Toast.makeText(this, "I guess you drew: " + top_prediction.name, Toast.LENGTH_SHORT).show();
                Log.i("INFO", "Prediction: " + top_prediction.name + " has score: " + detection_score);
            } else {
                Toast.makeText(this, "There's no Prediction for your gesture", Toast.LENGTH_SHORT).show();
                Log.i("INFO", "No good Prediction. Prediction: " + top_prediction + " has the highest score" +
                        "with: " + detection_score);
            }
        } else {
            showDialog();
        }
    }
}
