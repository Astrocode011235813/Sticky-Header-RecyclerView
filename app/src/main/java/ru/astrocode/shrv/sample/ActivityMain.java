package ru.astrocode.shrv.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import ru.astrocode.shrv.library.SHRVLinearLayoutManager;

/**
 * Created by Astrocode on 25.03.2017.
 */

public class ActivityMain extends AppCompatActivity {
    RecyclerView mRecyclerView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int orientation;

        if(getResources().getBoolean(R.bool.isPort)){
            orientation = SHRVLinearLayoutManager.VERTICAL;
        }else{
            orientation = SHRVLinearLayoutManager.HORIZONTAL;
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new SHRVLinearLayoutManager(orientation));
        mRecyclerView.setAdapter(new AdapterMain(this,orientation));
/*
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.scrollToPosition(10);
            }
        },10000);
*/
    }

}
