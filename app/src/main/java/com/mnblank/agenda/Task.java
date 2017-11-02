package com.mnblank.agenda;


import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;


public class Task {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mDone;
    private String mContact;

    private boolean mRecentlyChanged = false;  // para actualizar los fragment en CrimeListFragment
    private int mCurrentPosition;



    public boolean isRecentlyChanged() {
        return mRecentlyChanged;
    }

    public void setRecentlyChanged() {
        //Log.d("Crime", "setRecentlyChanged");
        mRecentlyChanged = true;
    }
    public void updated(){
        mRecentlyChanged = false;
    }


    public UUID getId() {
        return mId;
    }


    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public boolean isDone() {
        return mDone;
    }

    public void setDone(boolean solved) {
        mDone = solved;
        if(solved)
            Log.e("TaskError", "Set Done");
    }

    public String getContact() {
        return mContact;
    }

    public void setContact(String suspect) {
        mContact = suspect;
    }





    public String getDateString() {
        SimpleDateFormat format = new SimpleDateFormat("EEEE d MMMM yyyy");
        String date = format.format(mDate);
        return date;
    }

    public String getHourString(){
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        String date = format.format(mDate);
        return date;
    }

    public String getFullDateString(){
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a EEEE d MMMM yyyy");
        String date = format.format(mDate);
        return date;
    }
    public Date getDate(){
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public void setCurrentPosition(int i){
        mCurrentPosition = i;
    }
    public int getCurrentPosition(){
        return mCurrentPosition;
    }


    public Task(){
        //Generate unique identifier
        this(UUID.randomUUID());
    }

    public Task(UUID id){
        mId = id;
        mDate = new Date();
    }

    public String getPhotoFilename() {
        return "IMG_" + getId().toString() + ".jpg";
    }
}
