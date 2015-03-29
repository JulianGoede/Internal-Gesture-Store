package com.example.julian.training14;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Julian on 25.03.2015.
 */
public class MyDialogFragment extends DialogFragment {

    public interface EditNameDialogListener {
        void onCancel(View v);

        void onSave(View v);
    }

    public static EditText sEditText;

    private Button mSaveButton, mCancelButton;

    public MyDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_dialog, container);
        sEditText = (EditText) view.findViewById(R.id.txt_your_name);
        getDialog().setTitle("Save");

        mSaveButton = (Button) view.findViewById(R.id.safeButton);
        mCancelButton = (Button) view.findViewById(R.id.cancelButton);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EditNameDialogListener) getActivity()).onSave(v);
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EditNameDialogListener) getActivity()).onCancel(v);
            }
        });
        // Show soft keyboard automatically
        sEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return view;
    }

}
