package com.mikeconroy.traveljournal.fragments.places;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikeconroy.traveljournal.Configuration;
import com.mikeconroy.traveljournal.OnFragmentUpdateListener;
import com.mikeconroy.traveljournal.R;
import com.mikeconroy.traveljournal.db.AppDatabase;
import com.mikeconroy.traveljournal.db.Place;

import java.util.List;

public class PlaceListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private PlaceListInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlaceListFragment() {}

    @SuppressWarnings("unused")
    public static PlaceListFragment newInstance(int columnCount) {
        PlaceListFragment fragment = new PlaceListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(Configuration.TAG, "PlaceListFragment#onCreateView called.");

        View view = inflater.inflate(R.layout.fragment_place_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.place_list);
        TextView emptyView = view.findViewById(R.id.empty_view);
        TextView loadingView = view.findViewById(R.id.loading_places_view);

        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);

        Context context = view.getContext();
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        new LoadPlaces().execute();

        return view;
    }

    private void updatePlaceListView(List<Place> places) {
        Log.i(Configuration.TAG, "PlaceListFragment#updatePlaceListView: Updated Place Received.");

        RecyclerView recyclerView = getActivity().findViewById(R.id.place_list);
        TextView emptyView = getActivity().findViewById(R.id.empty_view);
        TextView loadingView = getActivity().findViewById(R.id.loading_places_view);
        loadingView.setVisibility(View.GONE);

        if(places.isEmpty()){
            Log.i(Configuration.TAG, "PlaceListFragment#onCreateView: No places created" +
                    " yet. Displaying message.");
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            Log.i(Configuration.TAG, "PlaceListFragment#onCreateView: Place Exist." +
                    "Displaying RecyclerView.");
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            recyclerView.setAdapter(new PlaceRecyclerViewAdapter(places, mListener));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PlaceListInteractionListener) {
            mListener = (PlaceListInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(Configuration.TAG, "PlaceListFragment#onResume: Resuming.");
        mListener.onFragmentOpened("Place Visited", true);

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);

        if(fab != null) {
            fab.setVisibility(View.VISIBLE);
            fab.setImageResource(R.drawable.ic_add_white_24dp);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(Configuration.TAG, "PlaceListFragment: FAB Clicked.");
                    //Start the Edit Holiday Fragment.
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.fragment_container, new NewPlaceFragment());
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
        }
    }

    public interface PlaceListInteractionListener extends OnFragmentUpdateListener {
        void onPlaceListItemInteraction(Place item);
    }

    private class LoadPlaces extends AsyncTask<Void, Void, List<Place>> {
        @Override
        protected List<Place> doInBackground(Void... params) {
            Log.i(Configuration.TAG, "PlaceListFragment#doInBackground: Finding places..");
            return AppDatabase.getInstance(getContext()).placeDao().getAllPlaces();
        }

        @Override
        protected void onPostExecute(List<Place> places) {
            Log.i(Configuration.TAG, "PlaceListFragment: AsyncTask complete. No. of Places: " + places.size());
            updatePlaceListView(places);        }
    }
}
