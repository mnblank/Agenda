package com.mnblank.agenda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class TaskListFragment extends Fragment{

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView mTaskRecyclerView;
    private TaskAdapter mAdapter;
    private boolean mSubtitleVisible;
    private Callbacks mCallbacks;
    private int mAccesedItem;

    /**
     * Required interface for hosting activities.
     */
    public interface Callbacks {
        void onTaskSelected(Task task);
        void boxChecked(Task task);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
        //Log.d("TaskListFragment", "onAttach act");
    }

    // se hace este onCreate solo para indicar que hay un options menu
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        /*if(TaskLab.get(getActivity()).getTasks().isEmpty()){
            View view = inflater.inflate(R.layout.empty_task_list, container, false);
            Button emptyList = view.findViewById(R.id.empty_tasks_button);
            emptyList.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Task task = new Task();
                    TaskLab.get(getActivity()).addTask(task);
                    Intent intent = TaskPagerActivity
                            .newIntent(getActivity(), task.getId());
                    startActivity(intent);
                }
            });
            return view;
        }*/


        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        mTaskRecyclerView = view.findViewById(R.id.task_recycler_view);
        mTaskRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Button mEmptyButton = view.findViewById(R.id.empty_tasks_button);
        mEmptyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Task task = new Task();
                TaskLab.get(getActivity()).addTask(task);
                Intent intent = TaskPagerActivity
                        .newIntent(getActivity(), task.getId());
                startActivity(intent);
            }
        });
        mEmptyButton.setVisibility(View.GONE);

        /*
        if(TaskLab.get(getActivity()).getTasks().isEmpty()){
            mEmptyButton.setVisibility(View.VISIBLE);
        }
        */

        if(savedInstanceState != null){
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();


        return view;
    }

    // Actualiza los fragment de los taskn cambiados en TaskActivity

    @Override
    public void onResume(){
        super.onResume();
        Log.d("Resume", "onResume() activated");
        updateUI();
    }

    // para que se mantenga el subtitle despues de la rotacion
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mCallbacks = null;
    }

    // Despues de que la activity recibe la informacion de que hay un optionsMenu, este metodo lo infla
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_task_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.menu_item_show_subtitle);
        if(mSubtitleVisible){
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    // igual que onClick de onClickListener
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_item_new_task:
                Task task = new Task();
                TaskLab.get(getActivity()).addTask(task);
                updateUI();
                mCallbacks.onTaskSelected(task);

                /* Cuando no se hizo el uso para tablets
                Intent intent = TaskPagerActivity                  // para agregar los datos del nuevo task
                        .newIntent(getActivity(), task.getId());
                startActivity(intent);
                */
                return true;
            case R.id.menu_item_show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle(){
        TaskLab taskLab = TaskLab.get(getActivity());
        int taskCount = taskLab.getTasks().size();
        String subtitle = getResources()
                .getQuantityString(R.plurals.subtitle_plural, taskCount, taskCount);

        if(!mSubtitleVisible){
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    public void updateUI(){
        TaskLab taskLab = TaskLab.get(getActivity());
        List<Task> tasks = taskLab.getTasks();
        //Log.d("TaskListFragment", "UpdateUI act");

        if(mAdapter == null) {
            mAdapter = new TaskAdapter(tasks);
            mTaskRecyclerView.setAdapter(mAdapter);
        } else {

            mAdapter.setTasks(tasks);

            if(TaskLab.get(getActivity()).isEtwDeleted()){
                if(getResources().getBoolean(R.bool.tablet)) {
                    Log.e("TaskListFragment:", "DELETED UPDATE");
                    mAdapter.notifyItemRemoved(mAccesedItem);
                    updateSubtitle();
                    TaskLab.get(getActivity()).setEtwDeleted(false);
                    return;

                } else {
                    mAdapter.notifyDataSetChanged();
                    updateSubtitle();
                    return;
                }
            }
            mAdapter.notifyItemChanged(TaskLab.get(getActivity()).getLastPositionAccesed());
            //agarra la posicion de los fragment que se actualizaron y los actualiza
            for(Integer i : mAdapter.searchUpdated()){
                //mAdapter.notifyDataSetChanged();
                mAdapter.notifyItemChanged(i);
                Log.d("UpdateUI", "UpdateUI act2");
            }

        }

        updateSubtitle();
    }

    // se crea la clase que se encargara de manejar los fragmentos de cada task. En MVC este seria
    // el Model

    private class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mCheckImage;
        private CheckBox mDoneCheckBox;
        private Task mTask;

        // se crea la clase, agarrando una vista que terminaria siendo la de la Activity

        public TaskHolder(View itemView){
            super(itemView);
            itemView.setOnClickListener(this);

            // Se activan cada uno de los elementos que contiene el fragmento de cada taskn (list_item_task.xlm)

            mTitleTextView = itemView.findViewById(R.id.list_item_task_title_text_view);
            mDateTextView = itemView.findViewById(R.id.list_item_task_date_text_view);
            mCheckImage = itemView.findViewById(R.id.list_item_task_done_check_box);

            //mDoneCheckBox = itemView.findViewById(R.id.list_item_task_done_check_box);
        }


        //indica las instrucciones de como sera cada task de la lista

        public void bindTask(Task task){
            mTask = task;
            mTitleTextView.setText(mTask.getTitle());
            mDateTextView.setText(mTask.getFullDateString());


            if(mTask.isDone())
                mCheckImage.setVisibility(View.VISIBLE);
            if(!mTask.isDone())
                mCheckImage.setVisibility(View.INVISIBLE);
            /*
            mDoneCheckBox.setChecked(mTask.isDone());
            mDoneCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // Set the task's done property
                    mTask.setDone(isChecked);
                }
            });*/
        }


        // Inicia TaskPagerActivity con el Id del crimen seleccionado
        @Override
        public void onClick(View view) {
            TaskLab.get(getActivity()).setLastPositionAccesed(getAdapterPosition());
            /* Cuando era solo para telefonos
            Intent intent = TaskPagerActivity.newIntent(getActivity(), mTask.getId());
            startActivity(intent);
            */
            if(getResources().getBoolean(R.bool.tablet))
                mAccesedItem = getAdapterPosition();
            mCallbacks.onTaskSelected(mTask);
        }
    }

    // se crea una clase que se encargara de manejar cada fragmento. En MVC este seria el Controller

    private class TaskAdapter extends RecyclerView.Adapter<TaskHolder> {

        private List<Task> mTasks;

        // Constructor simple en el que se le meten los tasks al adapter

        public TaskAdapter(List<Task> tasks) {
            mTasks = tasks;
        }


        // Este metodo se encarga de crear la vista en la que estara el fragmento
        @Override
        public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            // Se crea dicha vista
            View view = layoutInflater.inflate(R.layout.list_item_task, parent, false);
            Drawable background = view.getBackground();
            background.setAlpha(50);
            return new TaskHolder(view);
        }

        // Vincula el ViewHolder con la vista hecha
        @Override
        public void onBindViewHolder(TaskHolder holder, int position) {
            Task task = mTasks.get(position);
            holder.bindTask(task);
        }


        @Override
        public int getItemCount() {
            return mTasks.size();
        }

        public void setTasks(List<Task> tasks){
            mTasks = tasks;
        }


        // Metodo para obtener las posiciones que deben actualizarses

        public List<Integer> searchUpdated() {

            //Log.d("TaskListFragment", "searchUpdated");

            List<Integer> toUpdate = new ArrayList<>();

            for (int i = 0; i < mTasks.size(); i++) {
                if (mTasks.get(i).isRecentlyChanged()) {
                    toUpdate.add(i);
                    mTasks.get(i).updated();
                }
            }
            return toUpdate;
        }
    }


}