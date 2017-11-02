package com.mnblank.agenda;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;


// Clase abstracta que define una actividad que usa un fragmento que llena toda la pantalla


public abstract class SingleFragmentActivity extends AppCompatActivity{

    protected abstract Fragment createFragment();

    @LayoutRes
    protected int getLayoutRedId() {
        return R.layout.activity_fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRedId());
        if(getResources().getBoolean(R.bool.tablet)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        // Se hace un FragmentManager y con el se crea un nuevo fragmento

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if(fragment == null){

            // si no existe el fragmento que se pide se usa el FragmentManager para crearlo

            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)  // le añade el .xml al fragmento que se le pide
                    .commit();
        }
    }
}
