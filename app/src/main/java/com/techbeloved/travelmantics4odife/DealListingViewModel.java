package com.techbeloved.travelmantics4odife;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DealListingViewModel extends ViewModel {
    private DatabaseReference database;

    public static final String TAG = "DealListingViewModel";

    private MutableLiveData<List<TravelDeal>> travelDeals = new MutableLiveData<>();

    private DealListingViewModel(DatabaseReference database) {
        this.database = database;
        getTravelDeals();

    }

    public LiveData<List<TravelDeal>> travelDeals() {
        return travelDeals;
    }

    private void getTravelDeals() {
        database.child("travelDeals")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<TravelDeal> deals = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TravelDeal deal = snapshot.getValue(TravelDeal.class);
                    deals.add(deal);
                }
                // Sort by date added descending
                Collections.sort(deals, (d1, d2) -> d2.getId().compareTo(d1.getId()));
                travelDeals.postValue(deals);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: " + "code: " + databaseError.getCode() + ", message: " +
                        databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    static class Factory extends ViewModelProvider.NewInstanceFactory {
        private DatabaseReference database;

        Factory(DatabaseReference database) {
            this.database = database;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new DealListingViewModel(database);
        }
    }
}
