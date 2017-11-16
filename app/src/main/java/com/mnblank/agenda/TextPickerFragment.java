package com.mnblank.agenda;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class TextPickerFragment extends DialogFragment{

    private static final String ARG_TEXT = "text";

    public static final String EXTRA_TEXT_MESSAGE = "text_to_be_sent";

    private String mText;
    private EditText mEditText;

    public static TextPickerFragment newInstance(){
        return new TextPickerFragment();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_text, null);
        mEditText = v.findViewById(R.id.message_text);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.text_picker_title)
                .setPositiveButton(android.R.string.ok,
                        //La funcionalidad del PositiveButton
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mText = mEditText.getText().toString();

                                //Inicia el Asistente para mandar el texto con otra app
                                ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder
                                        .from(getActivity());
                                intentBuilder.setType("text/plain");
                                intentBuilder.setText(mText);
                                intentBuilder.setSubject(getString(R.string.task_report_subject));
                                intentBuilder.startChooser();
                                startActivity(intentBuilder.getIntent());

                                /* Para mandar el texto a otra activity
                                sendResult(Activity.RESULT_OK, mText);
                                */

                            }
                        })
                .create();
    }

    private void sendResult(int resultCode, String text){
        if(getTargetFragment() == null){
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_TEXT_MESSAGE, text);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
