package com.mikeconroy.traveljournal.fragments.photos;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikeconroy.traveljournal.Configuration;
import com.mikeconroy.traveljournal.OnFragmentUpdateListener;
import com.mikeconroy.traveljournal.R;
import com.mikeconroy.traveljournal.fragments.holidays.HolidayDetailsFragment;
import com.mikeconroy.traveljournal.fragments.places.PlaceDetailsFragment;
import com.mikeconroy.traveljournal.db.AppDatabase;
import com.mikeconroy.traveljournal.db.Holiday;
import com.mikeconroy.traveljournal.db.Photo;
import com.mikeconroy.traveljournal.db.Place;

import java.io.File;
import java.util.List;

public class PhotoDetailsFragment extends Fragment {

    private static final String PHOTO_ID = "photoId";
    private String imagePath;
    private int photoId;

    private OnFragmentUpdateListener mListener;

    public PhotoDetailsFragment() {}

    public static PhotoDetailsFragment newInstance(int photoId) {
        Log.i(Configuration.TAG, "PhotoDetailsFragment#newInstance: Creating new instance.");
        PhotoDetailsFragment fragment = new PhotoDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(PHOTO_ID, photoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            photoId = getArguments().getInt(PHOTO_ID);
            Log.i(Configuration.TAG, "PhotoDetailsFragment#onCreate: Photo ID: " + photoId);
        } else {
            Log.e(Configuration.TAG, "Photo Details opened without Photo ID");
            getFragmentManager().popBackStack();
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(Configuration.TAG, "PhotoDetailsFragment#onCreateView: Creating View.");
        View view = inflater.inflate(R.layout.fragment_photo_details, container, false);

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_edit_white_24dp);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(Configuration.TAG, "PhotoDetailsFragment: FAB Clicked.");
                //Start the Edit Holiday Fragment.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, EditPhotoFragment.newInstance(photoId));
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        new PhotoDetailsFragment.LoadPhoto().execute(photoId);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_share:
                shareImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareImage(){
        Log.i(Configuration.TAG, "PhotoDetailsFragment: Sharing photo with path: " + imagePath);

        if(imagePath.indexOf("file://") > -1){
//          imagePath = "file://" + imagePath;
            imagePath = imagePath.replace("file://", "");
        }

        Log.i(Configuration.TAG, "PhotoDetailsFragment: Creating Photo File: " + imagePath);

        File imageFile = new File(imagePath);
        Uri photoURI = FileProvider.getUriForFile(
                getContext(),
                getContext().getApplicationContext().getPackageName() + ".fileprovider",
                imageFile);
        Log.i(Configuration.TAG, "PhotoDetailsFragment: Photo URI Created.");
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, photoURI);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(intent, "Share Image");
        List<ResolveInfo> resInfoList = getActivity().getPackageManager()
                .queryIntentActivities(chooser,
                        PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            getActivity().grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        Log.i(Configuration.TAG, "PhotoDetailsFragment: Starting Share Image Chooser.");
        startActivity(chooser);
//        startActivity(intent);
    }

    private void updatePhotoDetailsDisplay(Photo photo){
        if(photo == null){
            Log.e(Configuration.TAG, "PhotoDetailsFragment: Photo not found.");
            Toast.makeText(getContext(), "Photo not found :(.", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        } else {
            Log.i(Configuration.TAG, "PhotoDetailsFragment: Displaying photo with path: " + photo.getImagePath());
            mListener.onFragmentOpened("Photo Details", false);

            ImageView imageView = getActivity().findViewById(R.id.photo_image);
            imagePath = photo.getImagePath();
            imageView.setImageURI(Uri.parse(photo.getImagePath()));

            TextView tags = getActivity().findViewById(R.id.image_tags);
            if(!photo.getTags().equals("")) {
                Log.i(Configuration.TAG, "PhotoDetailsFragment: Tags on photo.");
                tags.setText(photo.getTags());
            } else {
                Log.i(Configuration.TAG, "PhotoDetailsFragment: No tags on photo.");
                tags.setTextColor(getResources().getColor(R.color.noTagsMessage));
                tags.setHint("No tags set.");
            }

            Button viewAssociatedTrip = getView().findViewById(R.id.view_associated_trip);
            if(photo.getHolidayId() != 0){
                Log.i(Configuration.TAG, "PhotoDetailsFragment: Photo is associated with a holiday: " + photo.getHolidayId());
                viewAssociatedTrip.setText("Loading Associated Holiday");
                new LoadHoliday().execute(photo.getHolidayId());
            } else if (photo.getPlaceId() != 0){
                Log.i(Configuration.TAG, "PhotoDetailsFragment: Photo is associated with a place." + photo.getPlaceId());
                viewAssociatedTrip.setText("Loading Associated Place");
                //TODO Load Place.
                new LoadPlaceTask().execute(photo.getPlaceId());
            } else {
                //TODO Maybe remove the button if there is no associated trip.
                viewAssociatedTrip.setText("No associated trip");
                viewAssociatedTrip.setClickable(false);
            }

            if(photo.getLatitude() != 0 && photo.getLongitude() != 0) {
                Log.i(Configuration.TAG, "PhotoDetailsFragment: Displaying MapView.");
                getActivity().findViewById(R.id.map_view).setVisibility(View.VISIBLE);
                LatLng latLng = new LatLng(photo.getLatitude(), photo.getLongitude());
                createMap(getView(), latLng);
            } else {
                Log.i(Configuration.TAG, "PhotoDetailsFragment: Latitude or Longitude set to 0");
            }
        }

    }

    private void updateAssociatedTripButton(final Holiday holiday){
        Button viewAssociatedTrip = getView().findViewById(R.id.view_associated_trip);
        viewAssociatedTrip.setText("View Holiday: " + holiday.getTitle());
        viewAssociatedTrip.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.i(Configuration.TAG, "PhotoDetailsFragment: Opening Holiday Details Fragment with ID: " + holiday.getId());
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, HolidayDetailsFragment.newInstance(holiday.getId()));
                ft.addToBackStack(null);
                ft.commit();
            }
        });
    }

    private void updateAssociatedTripButton(final Place place){
        Button viewAssociatedTrip = getView().findViewById(R.id.view_associated_trip);
        viewAssociatedTrip.setText("View Place: " + place.getTitle());
        viewAssociatedTrip.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.i(Configuration.TAG, "PhotoDetailsFragment: Opening Place Details Fragment with ID: " + place.getId());
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, PlaceDetailsFragment.newInstance(place.getId()));
                ft.addToBackStack(null);
                ft.commit();
            }
        });
    }

    private void createMap(View view, final LatLng location){

        final MapView mMapView = view.findViewById(R.id.map_view);
        mMapView.onCreate(Bundle.EMPTY);

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                MapsInitializer.initialize(getContext());
                googleMap.getUiSettings().setAllGesturesEnabled(false);

                googleMap.addMarker(new MarkerOptions().position(location));
                CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(location, 17.0f);
                googleMap.animateCamera(camUpdate);

                mMapView.onResume();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentUpdateListener) {
            mListener = (OnFragmentUpdateListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentUpdateListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class LoadPhoto extends AsyncTask<Integer, Void, Photo> {
        @Override
        protected Photo doInBackground(Integer... photoId) {
            Log.i(Configuration.TAG, "PhotoDetailsFragment#doInBackground: Finding photo with ID: " + photoId[0]);
            return AppDatabase.getInstance(getContext()).photoDao().findPhotoById(photoId[0]);
        }

        @Override
        protected void onPostExecute(Photo photo) {
            updatePhotoDetailsDisplay(photo);
        }
    }

    //TODO Add a loader for places.
    private class LoadPlaceTask extends AsyncTask<Integer, Void, Place> {
        @Override
        protected Place doInBackground(Integer... placeId) {
            return AppDatabase.getInstance(getContext()).placeDao().findPlaceById(placeId[0]);
        }

        @Override
        protected void onPostExecute(Place place) {
            updateAssociatedTripButton(place);
        }
    }

    private class LoadHoliday extends AsyncTask<Integer, Void, Holiday> {
        @Override
        protected Holiday doInBackground(Integer... holidayId) {
            Log.i(Configuration.TAG, "PhotoDetailsFragment#doInBackground: Finding holiday with ID: " + holidayId[0]);
            return AppDatabase.getInstance(getContext()).holidayDao().findHolidayById(holidayId[0]);
        }

        @Override
        protected void onPostExecute(Holiday holiday) {
            updateAssociatedTripButton(holiday);
        }
    }
}
