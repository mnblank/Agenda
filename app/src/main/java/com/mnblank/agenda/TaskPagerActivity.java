package com.mnblank.agenda;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;

import java.util.List;
import java.util.UUID;


public class TaskPagerActivity extends AppCompatActivity implements TaskFragment.Callbacks{

    private static final String EXTRA_TASK_ID =
            "com.bigneardranch.android.criminalintent.task_id";

    private ViewPager mViewPager;
    private List<Task> mTasks;

    // Intent que se comunica con la lista de TaskListActivity
    public static Intent newIntent(Context packageContext, UUID taskId) {
        Intent intent = new Intent(packageContext, TaskPagerActivity.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        return intent;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_pager);

        // Recibe el Id del task seleccionado
        UUID taskId = (UUID) getIntent().getSerializableExtra(EXTRA_TASK_ID);

        // Busca el ViewPager con el que funciona
        mViewPager = (ViewPager) findViewById(R.id.activity_task_pager_view_pager);



        mTasks = TaskLab.get(this).getTasks(); // El context this hace que se busque el TaskLab ya hecho en la app
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager){

            @Override
            public int getCount() {
                return mTasks.size();
            }

            // Abre el fragmento que se pide
            @Override
            public Fragment getItem(int position) {
                Task task = mTasks.get(position);
                return TaskFragment.newInstance(task.getId());
            }

        });

        // Selecciona cual es el Task que se mostrara, a partir del id mandado
        for (int i = 0; i < mTasks.size(); i++){
            if (mTasks.get(i).getId().equals(taskId)) {
                mViewPager.setCurrentItem(i); // Metodo que inicia la vista del Fragment
                break;
            }
        }
    }

    @Override
    public void onTaskUpdated(Task task) {

    }

    @Override
    public void onTaskDeleted(Task task) {

    }
}