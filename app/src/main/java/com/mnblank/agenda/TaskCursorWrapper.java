package com.mnblank.agenda;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Date;
import java.util.UUID;

import database.TaskDbSchema;
import database.TaskDbSchema.TaskTable;


public class TaskCursorWrapper extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public TaskCursorWrapper(Cursor cursor) {
        super(cursor);
    }


    //Al haber obtenido un cursor esta clase puede extraer los datos de manera inmediata al llamar este metodo
    public Task getTask() {
        String uuidString = getString(getColumnIndex(TaskTable.Cols.UUID));
        String title = getString(getColumnIndex(TaskTable.Cols.TITLE));
        long date = getLong(getColumnIndex((TaskTable.Cols.DATE)));
        int isSolved = getInt(getColumnIndex(TaskTable.Cols.DONE));
        String suspect = getString(getColumnIndex(TaskTable.Cols.CONTACT));

        Task task = new Task(UUID.fromString(uuidString));
        task.setTitle(title);
        task.setDate(new Date(date));
        task.setDone(isSolved != 0);
        task.setContact(suspect);

        return task;
    }

}
