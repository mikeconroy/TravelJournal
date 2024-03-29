package com.mikeconroy.traveljournal.fragments.holidays;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikeconroy.traveljournal.Configuration;
import com.mikeconroy.traveljournal.OnFragmentUpdateListener;
import com.mikeconroy.traveljournal.R;
import com.mikeconroy.traveljournal.db.AppDatabase;
import com.mikeconroy.traveljournal.db.Holiday;

public class HolidayDetailsFragment extends Fragment {

    private static final String HOLIDAY_ID = "holidayId";

    private int holidayId;

    private OnFragmentUpdateListener mListener;

    public HolidayDetailsFragment() {}

    public static HolidayDetailsFragment newInstance(int holidayId) {
        Log.i(Configuration.TAG, "HolidayDetailsFragment#newInstance: Creating new instance.");
        HolidayDetailsFragment fragment = new HolidayDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(HOLIDAY_ID, holidayId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Configuration.TAG, "HolidayDetailsFragment#onCreate: Creating.");
        if (getArguments() != null) {
            holidayId = getArguments().getInt(HOLIDAY_ID);
            Log.i(Configuration.TAG, "HolidayDetailsFragment#onCreate: Holiday ID: " + holidayId);
        } else {
            Log.e(Configuration.TAG, "Holiday Details Fragment opened without Holiday ID.");
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(Configuration.TAG, "HolidayDetailsFragment#onCreateView: Creating View.");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_holiday_details, container, false);
        //View view = inflater.inflate(R.layout.fragment_edit_holiday_details, container, false);

        //TODO: Add options menu here for Editing or Deleting the holiday.

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_edit_white_24dp);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(Configuration.TAG, "HolidayDetailsFragment: FAB Clicked.");
                //Start the Edit Holiday Fragment.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, EditHolidayFragment.newInstance(holidayId));
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        new LoadHoliday().execute(holidayId);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.i(Configuration.TAG, "HolidayListFragment#onAttach: Attaching.");
        if (context instanceof OnFragmentUpdateListener) {
            mListener = (OnFragmentUpdateListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement HolidayDetailsInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(Configuration.TAG, "HolidayDetailsFragment#onResume: Resuming.");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(Configuration.TAG, "HolidayListFragment#onDetach: Detaching.");

        mListener = null;
    }

    private void updateHolidayDetailsDisplay(Holiday holiday){
        if(holiday == null){
            Log.i(Configuration.TAG, "HolidayDetailsFragment#updateHolidayDetails: Holiday not found.");
            Toast.makeText(getContext(), "Holiday not found :(.", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        } else {
            Log.i(Configuration.TAG, "HolidayDetailsFragment#updateHolidayDetails: Displaying holiday with name: " + holiday.getTitle());

            //TODO: Include Updating of image here.
            mListener.onFragmentOpened(holiday.getTitle(), false);
            TextView notesField = getActivity().findViewById(R.id.holiday_notes);
            notesField.setText(holiday.getNotes());
            TextView startDateField = getActivity().findViewById(R.id.holiday_start_date);
            startDateField.setText(holiday.getStartDate());
            TextView endDateField = getActivity().findViewById(R.id.holiday_end_date);
            endDateField.setText(holiday.getEndDate());
            ImageView profilePhoto = getActivity().findViewById(R.id.holiday_image);
            if(holiday.getProfilePhotoPath() != null && !holiday.getProfilePhotoPath().equals("")) {
                profilePhoto.setImageURI(Uri.parse(holiday.getProfilePhotoPath()));
            }
        }
    }

    private class LoadHoliday extends AsyncTask<Integer, Void, Holiday> {
        @Override
        protected Holiday doInBackground(Integer... holidayId) {
            Log.i(Configuration.TAG, "HolidayDetailsFragment#doInBackground: Finding holiday with ID: " + holidayId[0]);
            return AppDatabase.getInstance(getContext()).holidayDao().findHolidayById(holidayId[0]);
        }

        @Override
        protected void onPostExecute(Holiday holiday) {
             updateHolidayDetailsDisplay(holiday);
        }
    }
}
