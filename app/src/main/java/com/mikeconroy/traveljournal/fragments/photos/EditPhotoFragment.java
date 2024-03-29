package com.mikeconroy.traveljournal.fragments.photos;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.mikeconroy.traveljournal.Configuration;
import com.mikeconroy.traveljournal.R;
import com.mikeconroy.traveljournal.db.AppDatabase;
import com.mikeconroy.traveljournal.db.Holiday;
import com.mikeconroy.traveljournal.db.Photo;
import com.mikeconroy.traveljournal.db.Place;

public class EditPhotoFragment extends PhotoEditableBaseFragment {

    public static EditPhotoFragment newInstance(int photoId){
        EditPhotoFragment fragment = new EditPhotoFragment();
        Bundle args = new Bundle();
        args.putInt("photoId", photoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.findViewById(R.id.map_view).setVisibility(View.GONE);
        ((CheckBox) view.findViewById(R.id.location_enabled)).setChecked(false);
        new EditPhotoFragment.LoadPhotoTask().execute(photoId);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.onFragmentOpened("Edit Photo", false);
    }

    private void updatePhotoDetailsDisplay(Photo photo){
        if(photo == null) {
            Log.i(Configuration.TAG, "EditPhoto#updatePhotoDetailsDisplay: Photo not found.");
            Toast.makeText(getContext(), "Photo not found :(.", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        } else {
            imagePath = photo.getImagePath();
            photoId = photo.getId();

            ImageView imageView = getActivity().findViewById(R.id.photo_image);
            imageView.setImageURI(Uri.parse(photo.getImagePath()));

            if(!photo.getTags().equals("")){
                EditText tags = getActivity().findViewById(R.id.image_tags);
                tags.setText(photo.getTags());
            }

            Button viewAssociatedTrip = getView().findViewById(R.id.associate_image_button);
            if(photo.getHolidayId() != 0){
                viewAssociatedTrip.setText("Loading Associated Photo");
                holidayId = photo.getHolidayId();
                new LoadHoliday().execute(photo.getHolidayId());
            } else if (photo.getPlaceId() != 0){
                viewAssociatedTrip.setText("Loading Associated Place");
                tripId = photo.getPlaceId();
                new LoadPlace().execute(photo.getPlaceId());
            }

            if(photo.getLatitude() != 0 && photo.getLongitude() != 0){
                Log.i(Configuration.TAG, "EditPhotoFragment#updatePhotoDetailsDisplay: LatLng are set.");
                getView().findViewById(R.id.map_view).setVisibility(View.VISIBLE);
                ((CheckBox) getView().findViewById(R.id.location_enabled)).setChecked(true);
                mapViewWrapper.placeMarkerAndZoom(new LatLng(photo.getLatitude(), photo.getLongitude()));
            }
        }
    }

    private void updateAssociatedTripButton(final String title){
        Button viewAssociatedTrip = getView().findViewById(R.id.associate_image_button);
        viewAssociatedTrip.setText(title);
    }

    @Override
    protected void savePhotoToDatabase(Photo photo) {
        new EditPhotoTask().execute(photo);
    }

    private class LoadPhotoTask extends AsyncTask<Integer, Void, Photo> {
        @Override
        protected Photo doInBackground(Integer... photoId) {
            Log.i(Configuration.TAG, "Loading photo: "+ photoId[0]);
            return AppDatabase.getInstance(getContext()).photoDao().findPhotoById(photoId[0]);
        }

        @Override
        protected void onPostExecute(Photo photo) {
            updatePhotoDetailsDisplay(photo);
        }
    }

    private class EditPhotoTask extends AsyncTask<Photo, Void, Void>{
        @Override
        protected Void doInBackground(Photo... photos) {
            AppDatabase.getInstance(getContext()).photoDao().updatePhoto(photos[0]);
            return null;
        }
    }

    private class LoadPlace extends AsyncTask<Integer, Void, Place> {
        @Override
        protected Place doInBackground(Integer... placeId) {
            Log.i(Configuration.TAG, "PhotoDetailsFragment#doInBackground: Finding place with ID: " + placeId[0]);
            return AppDatabase.getInstance(getContext()).placeDao().findPlaceById(placeId[0]);
        }

        @Override
        protected void onPostExecute(Place place) {
            updateAssociatedTripButton("Place: " + place.getTitle());
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
            updateAssociatedTripButton("Holiday: " + holiday.getTitle());
        }
    }
}
