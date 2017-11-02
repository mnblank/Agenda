package com.mnblank.agenda;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Created by mnblank on 9/20/17.
 */

public class TaskListActivity extends SingleFragmentActivity
        implements TaskListFragment.Callbacks, TaskFragment.Callbacks{

    @Override
    protected Fragment createFragment(){
        return new TaskListFragment();
    }
    @Override
    protected int getLayoutRedId() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.d("ApplicationTagName", "Display width in px is " + metrics.widthPixels);
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onTaskSelected(Task task) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = TaskPagerActivity.newIntent(this, task.getId());
            startActivity(intent);
        } else {
            Fragment newDetail = TaskFragment.newInstance(task.getId());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail)
                    .commit();
        }
    }

    @Override
    public void boxChecked(Task task) {

    }


    @Override
    public void onTaskUpdated(Task task) {
        Log.d("TaskListActivity", "onTaskUpated");
        TaskListFragment listFragment = (TaskListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }

    @Override
    public void onTaskDeleted(Task task){
        /*
        TaskListFragment listFragment = (TaskListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
        */

        Log.e("TaskListActivity", "onTASKDELETED");
        TaskFragment taskFragment = (TaskFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detail_fragment_container);
        getSupportFragmentManager().beginTransaction().remove(taskFragment).commit();
        TaskListFragment listFragment = (TaskListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }
}