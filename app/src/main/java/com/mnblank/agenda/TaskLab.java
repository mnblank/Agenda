package com.mnblank.agenda;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import database.TaskBaseHelper;
import database.TaskDbSchema;
import database.TaskDbSchema.TaskTable;


public class TaskLab {
    private static TaskLab sTaskLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;


    private boolean mIsEtwDeleted;
    private int mLastPositionAccesed;

    // Al mismo tiempo crea devuelve el task lab y lo crea si no existe

    public static TaskLab get(Context context){
        if(sTaskLab == null){
            sTaskLab = new TaskLab(context);
        }
        return sTaskLab;
    }

    private TaskLab(Context context){
        mContext = context.getApplicationContext();   // El garbage collector nunca borra TaskLab porque esta vinculado al contexto
        mDatabase = new TaskBaseHelper(mContext).getWritableDatabase();
    }

    public void deleteTask(UUID id){
        mDatabase.delete(
                TaskTable.NAME,
                TaskTable.Cols.UUID + " = ?",
                new String[] { id.toString() });
        setEtwDeleted(true);
    }
    public boolean isEtwDeleted(){
        return mIsEtwDeleted;
    }

    public void setEtwDeleted(boolean etwDeleted) {
        mIsEtwDeleted = etwDeleted;
    }

    public int getLastPositionAccesed() {
        return mLastPositionAccesed;
    }

    public void setLastPositionAccesed(int lastPositionAccesed) {
        mLastPositionAccesed = lastPositionAccesed;
    }


    // public int getDeletedPosition(){
    //    return mDeletedPosition;
    // }



    public void addTask(Task c){
        ContentValues values = getContentValues(c);

        mDatabase.insert(TaskTable.NAME, null, values);
    }

    public List<Task> getTasks(){
        List<Task> tasks = new ArrayList<>();

        TaskCursorWrapper cursor = queryTasks(null, null);

        try{
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){     // cursor.isAfterLast() es true cuando esta en la ultima linea
                tasks.add(cursor.getTask()); // agrega la fila de SQLite al ArrayList
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return tasks;
    }

    // devuelve le task pedido por medio de su UUID

    public Task getTask(UUID id){

        TaskCursorWrapper cursor = queryTasks(
                TaskTable.Cols.UUID + " = ?",
                new String[] { id.toString() }
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getTask();
        } finally {
            cursor.close();
        }
    }

    public File getPhotoFile(Task task) {
        File externalFilesDir = mContext
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if(externalFilesDir == null){
            return null;
        }

        return new File(externalFilesDir, task.getPhotoFilename());
    }

    public void updateTask(Task task) {
        task.setRecentlyChanged();
        String uuidString = task.getId().toString();
        ContentValues values = getContentValues(task);

        mDatabase.update(TaskTable.NAME, values,
                TaskTable.Cols.UUID + " = ?",
                new String[] { uuidString });
    }

    // Agarra los datos del Task para ser insertados a SQLite
    private static ContentValues getContentValues(Task task){
        ContentValues values = new ContentValues();
        values.put(TaskTable.Cols.UUID, task.getId().toString());
        values.put(TaskTable.Cols.TITLE, task.getTitle());
        values.put(TaskTable.Cols.DATE, task.getDate().getTime());
        values.put(TaskTable.Cols.DONE, task.isDone() ? 1 : 0);
        values.put(TaskTable.Cols.CONTACT, task.getContact());

        return values;
    }


    /* whereClause: dice que parametro se busca
       whereArgs: dice que valor del parametro de whereClause se busca
     */
    private TaskCursorWrapper queryTasks(String whereClause, String[] whereArgs) {

        // Ingresa el codigo SQLite
        Cursor cursor = mDatabase.query(
                TaskTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null //orderBy
        );

        // Se deja listo el TaskCursorWrapper, solo hace falta llamar el metodo para sacarle los valores
        return new TaskCursorWrapper(cursor);
    }
}
