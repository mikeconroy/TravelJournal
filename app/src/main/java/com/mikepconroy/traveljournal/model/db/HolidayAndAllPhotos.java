package com.mikepconroy.traveljournal.model.db;

import android.arch.persistence.room.Relation;

import java.util.List;

//https://developer.android.com/reference/android/arch/persistence/room/Relation.html
public class HolidayAndAllPhotos {
    public int id;
    public String name;
    @Relation(parentColumn="id", entityColumn="holiday_id")
    public List<Photo> photos;
}
