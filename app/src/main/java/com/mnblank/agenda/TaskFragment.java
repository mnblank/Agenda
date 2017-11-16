package com.mnblank.agenda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.Date;
import java.util.UUID;

public class TaskFragment extends Fragment {

    private static final String ARG_TASK_ID = "task_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final String DIALOG_TEXT = "DialogText";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;
    private static final int REQUEST_TEXT = 4;

    private Task mTask;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mHourButton;
    private CheckBox mDoneCheckBox;
    private Button mMessageButton;
    private Button mContactButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private Callbacks mCallbacks;

    /**
     * Required interface for hosting activities.
     */
    public interface Callbacks {
        void onTaskUpdated(Task task);
        void onTaskDeleted(Task task);
    }

    /* Para luego pasar la informacion a otra Activity sin usar Intents, asi el fragmento funciona
    * independientemente de la Activity que lo active */

    public static TaskFragment newInstance(UUID taskId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TASK_ID, taskId);


        TaskFragment fragment = new TaskFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
        //Log.d("TaskFragment", "onAttach act");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_task, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_item_delete:
                TaskLab.get(getActivity()).deleteTask(mTask.getId());
                if(getResources().getBoolean(R.bool.tablet)) {
                    //getFragmentManager().beginTransaction().remove(this).commit();
                    mCallbacks.onTaskDeleted(mTask);
                    Log.e("TaskFragment", "DELETE SELECTED");
                }
                if(!getResources().getBoolean(R.bool.tablet))
                    getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Agarra el UUID que pide la Activity que activo el fragmento

        UUID TaskId = (UUID) getArguments().getSerializable(ARG_TASK_ID);

        // Agarra el id del task a partir del UUID que se tomo en newInstance

        mTask = TaskLab.get(getActivity()).getTask(TaskId);
        mPhotoFile = TaskLab.get(getActivity()).getPhotoFile(mTask);

        setHasOptionsMenu(true);
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d("if pause", "onPause() activated");
        TaskLab.get(getActivity()).updateTask(mTask);
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_task, container, false);

        // Edit Text donde se pone el titulo del task

        mTitleField = v.findViewById(R.id.task_title);
        mTitleField.setText(mTask.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //This space intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                mTask.setTitle(s.toString());
                mTask.setRecentlyChanged();
                updateTask();
            }

            @Override
            public void afterTextChanged(Editable s) {
                mTask.setRecentlyChanged();
                updateTask();
            }
        });
        mTitleField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Do whatever you want here
                    return true;
                }
                return false;
            }
        });



        mDateButton = v.findViewById(R.id.task_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Para hacer que el dialogo salga en un fragment (mayor utilidad)
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mTask.getDate());
                /* Se crea una relacion entre dos fragmentos para mandarse informacion entre si.
                Este metodo se encarga de establecer cual es el fragmento que recibira la informacion
                 */
                dialog.setTargetFragment(TaskFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE); // Muestra el DataPickerFragment
                mTask.setRecentlyChanged();
            }
        });

        mHourButton = v.findViewById(R.id.task_hour);
        mHourButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment
                        .newInstance(mTask.getDate());
                dialog.setTargetFragment(TaskFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
                mTask.setRecentlyChanged();
            }
        });
        updateHour();


        mDoneCheckBox = v.findViewById(R.id.task_done);
        //mDoneCheckBox.getBackground().setAlpha(100);
        mDoneCheckBox.setChecked(mTask.isDone());
        mDoneCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Set the task's solved property
                mTask.setDone(isChecked);
                mTask.setRecentlyChanged(); // Para que despues se cambie en la primera Activity
                updateTask();
            }
        });


        mMessageButton = v.findViewById(R.id.task_message);

        // Para el Challenge: ShareCompat
        //mMessageButton.setEnabled(false);

        mMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder
                        .from(getActivity());
                intentBuilder.setType("text/plain");
                intentBuilder.setText(getTaskReport());
                intentBuilder.setSubject(getString(R.string.task_report_subject));
                intentBuilder.startChooser();
                startActivity(intentBuilder.getIntent());*/

                getTaskReport();

            }
        });

        /*  El que se hace primero
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND); // intent implicito que busca una app con la funcion send
                i.setType("text/plain");  // el tipo de data que se quiere enviar, en este caso un texto simple
                i.putExtra(Intent.EXTRA_TEXT, getTaskReport()); //Extras normales
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.task_report_subject));
                // se asegura que siempre salga un chooser (si hay mas de 1 app que cumpla con la funcion que se pide)
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });*/

        /* mCallButton = v.findViewById(R.id.call_contact);
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        */


        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mContactButton = v.findViewById(R.id.task_contact);
        mContactButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        if (mTask.getContact() != null) {
            mContactButton.setText(mTask.getContact());
        }

        // PackageManager se encarga de buscar informacion sobre las app
        PackageManager packageManager = getActivity().getPackageManager();
        // Se le pregunta al PackageManager si hay una app con "pickContact", si no la hay desactiva mContactButton
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mContactButton.setEnabled(false);
        }

        mPhotoButton = v.findViewById(R.id.task_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // es true solo si hay un mPhotoFile y hay una app que tome fotos
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        //mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setEnabled(false);

        if(canTakePhoto) {
            // un uri que da la direccion del archivo de la foto
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });
        //mPhotoView = v.findViewById(R.id.task_photo);
        //updatePhotoView();


        return v;
    }


    /* Se hace el onActivityResult de TaskFragment para que este pueda recibir informacion
    requestCode: pregunta que es lo que se esta pidiendo
    resultCode: si la otra activity funciono sin problemas
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK){
            return;
        }

        if (requestCode == REQUEST_DATE){
            Date date = (Date) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mTask.setDate(date);
            updateTask();
            //Actualiza el DateButton
            updateDate();
        }else if (requestCode == REQUEST_TIME){
            Date date = (Date) data
                    .getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mTask.setDate(date);
            updateHour();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            //Specify which fields you want your query to return values for.
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            //Perform your query - the contactUri is like a "where" clause here
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);

            try {
                // Double-check that you actually got results
                if (c.getCount() == 0) {
                    return;
                }

                // Pull out the first column of the first row of data - that is your contact's name.
                c.moveToFirst();
                String contact = c.getString(0);
                mTask.setContact(contact);
                updateTask();
                mContactButton.setText(contact);
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            updateTask();
            updatePhotoView();
        }
    }

    private void updateTask() {
        TaskLab.get(getActivity()).updateTask(mTask);
        mCallbacks.onTaskUpdated(mTask);
    }

    private void updateDate() {
        mDateButton.setText(mTask.getDateString());
    }
    private void updateHour(){
        mHourButton.setText(mTask.getHourString());
    }

    private String getTaskReport(){

        /* En este se usaba el texto predefinido de CriminalIntent

        String doneString = null;
        if(mTask.isDone()){
            doneString = getString(R.string.task_report_done);
        } else {
            doneString = getString(R.string.task_report_undone);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mTask.getDate()).toString();

        String contact = mTask.getContact();
        if (contact == null) {
            contact = getString(R.string.task_report_no_contact);
        } else {
            contact = getString(R.string.task_report_contact, contact);
        }

        String report = getString(R.string.task_report,
                mTask.getTitle(), dateString, doneString, contact);

        return report;

        */

        // Se inicia el TextPicker para agarrar el texto que se enviara

        String message = null;

        FragmentManager manager = getFragmentManager();
        TextPickerFragment dialog = TextPickerFragment
                .newInstance();
                /* Se crea una relacion entre dos fragmentos para mandarse informacion entre si.
                Este metodo se encarga de establecer cual es el fragmento que recibira la informacion
                 */
        dialog.setTargetFragment(TaskFragment.this, REQUEST_TEXT);
        dialog.show(manager, DIALOG_TEXT); // Muestra el DataPickerFragment

        return null;
    }


    private void updatePhotoView() {
        // si no existe el archivo o esta vacio no hace nada
        if(mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    public void updateUI(){

    }

}
