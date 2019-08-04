package com.techbeloved.travelmantics4odife;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;
import java.util.List;

public class DealListingViewModel extends ViewModel {
    private FirebaseDatabase database;

    private MutableLiveData<List<TravelDeal>> travelDeals = new MutableLiveData<>();

    private DealListingViewModel(FirebaseDatabase database) {
        this.database = database;
        getTravelDeals();

    }

    public LiveData<List<TravelDeal>> travelDeals() {
        return travelDeals;
    }

    private void getTravelDeals() {
        database.getReference("travelDeals")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    static class Factory extends ViewModelProvider.NewInstanceFactory {
        private FirebaseDatabase database;

        Factory(FirebaseDatabase database) {
            this.database = database;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new DealListingViewModel(database);
        }
    }
}
