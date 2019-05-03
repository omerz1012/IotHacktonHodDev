package com.app_name.Fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.app_name.R;
import com.app_name.Services.NotificationService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

interface OnDataReceiveCallback {
    void onDataReceived(Integer doorState);
}

public class Manager extends Fragment {

    private static final String TAG = "MANAGER";

    View mView;
    Button openDoorBtn;
    Button closeDoorBtn;
    TextView tvDoorStatus;
    ImageButton btnSpeak;
    private TextView txtSpeechInput;

    TextToSpeech textToSpeech;

    DatabaseReference myRef;
    DatabaseReference databaseReadData;
    DatabaseReference intruderDetectedRef;

    private final int REQ_CODE_SPEECH_INPUT = 100;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    public Manager() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_manager, container, false);
        constructViews();
        getFromFirebase(new OnDataReceiveCallback() {
            @Override
            public void onDataReceived(Integer doorState) {
                if (doorState == 1){
                    tvDoorStatus.setText("Door is opened");
                } else {
                    tvDoorStatus.setText("Door is closed");
                }

                //Toast.makeText(getContext(),doorState.toString(),Toast.LENGTH_LONG);
            }
        });

        handleIntruderDetected(new OnDataReceiveCallback() {
            @Override
            public void onDataReceived(Integer intruderDetected) {
                if (intruderDetected == 1){
                    textToSpeech.speak("someone is in your house", TextToSpeech.QUEUE_FLUSH, null,"SPEAK");
                    intruderDetectedRef.setValue(0);
                }
        }
        });




        openDoorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnLed();
            }
        });
        closeDoorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffLed();
            }
        });


        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();

            }
        });

        textToSpeech=new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

//        Intent i= new Intent(getContext(), NotificationService.class);
//// potentially add data to the intent
//        i.putExtra("KEY1", "Value to be used by the service");
//        getContext().startService(i);
        return mView;
    }

    public void turnOnLed() {
        myRef.setValue(1);
    }

    public void turnOffLed(){
        myRef.setValue(0);
    }

    private void getFromFirebase(final OnDataReceiveCallback callback){
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object data = dataSnapshot.getValue();
                if (data instanceof Long){
                    callback.onDataReceived(((Long) data).intValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        databaseReadData = database.getReference("Homes/GuyHome/doorState");
        databaseReadData.addValueEventListener(postListener);
    }

    private void handleIntruderDetected(final OnDataReceiveCallback callback){
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object data = dataSnapshot.getValue();
                if (data instanceof Long){
                    callback.onDataReceived(((Long) data).intValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        intruderDetectedRef.addValueEventListener(postListener);
    }

    private void constructViews() {        // initialize all the variables in an organized way

        openDoorBtn = mView.findViewById(R.id.open_door_btn);
        closeDoorBtn = mView.findViewById(R.id.close_door_btn);
        tvDoorStatus = mView.findViewById(R.id.tv_door_status);

        btnSpeak = mView.findViewById(R.id.btnSpeak);

        txtSpeechInput = mView.findViewById(R.id.txtSpeechInput);

        myRef = database.getReference("/Homes/GuyHome/openDoor");
        intruderDetectedRef = database.getReference("Homes/GuyHome/intruderDetected");
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(mView.getContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String reasultStr = result.get(0);
                    txtSpeechInput.setText(reasultStr);
                    if (reasultStr.toLowerCase().contains("shazam".toLowerCase())){
                        turnOnLed();
                    }
                    else if (reasultStr.toLowerCase().contains("close".toLowerCase())){
                        turnOffLed();
                    }

                }
                break;
            }

        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

}
