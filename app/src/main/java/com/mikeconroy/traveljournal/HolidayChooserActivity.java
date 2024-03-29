package com.mikeconroy.traveljournal;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.mikeconroy.traveljournal.fragments.holidays.HolidayListFragment;
import com.mikeconroy.traveljournal.db.Holiday;

/**
 * Created by mikecon on 01/03/2018.
 */

public class HolidayChooserActivity extends AppCompatActivity implements HolidayListFragment.HolidayListInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holiday_chooser);

        HolidayListFragment holidayListFragment = new HolidayListFragment();

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, holidayListFragment).commit();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onHolidayListItemInteraction(Holiday item) {
        Intent i = new Intent();
        i.putExtra(Configuration.ITEM_ID, item.getId());
        i.putExtra(Configuration.ITEM_TITLE, item.getTitle());
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onFragmentOpened(String title, boolean navDrawerActive) {
        //Nothing to do.
    }
}
