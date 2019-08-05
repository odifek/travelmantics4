package com.techbeloved.travelmantics4odife;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.database.DatabaseReference;

import java.util.concurrent.ExecutorService;

public class DealViewModel extends ViewModel {

    private static final String TAG = "DealViewModel";

    private DatabaseReference travelDealReference;

    private MutableLiveData<Status> dealSavingStatus = new MutableLiveData<>();
    private ExecutorService executor;

    private DealViewModel(DatabaseReference database, ExecutorService executor) {
        this.executor = executor;
        this.travelDealReference = database.child("travelDeals");
    }

    void saveDeal(TravelDeal deal) {
        if (TextUtils.isEmpty(deal.getId())) {
            String key = travelDealReference.push().getKey();
            deal.setId(key);
        }
        travelDealReference.child(deal.getId())
                .setValue(deal)
                .addOnSuccessListener(executor, aVoid -> {
                    Log.i(TAG, "saveDeal: success");
                    dealSavingStatus.postValue(Status.savedSuccess());
                })
                .addOnFailureListener(executor, e -> {
                    Log.w(TAG, "saveDeal: failed!", e);
                    dealSavingStatus.postValue(Status.errorSaving(e));
                    dealSavingStatus.postValue(Status.defaultVal());
                });

    }

    void deleteDeal(TravelDeal currentDeal) {
        travelDealReference.child(currentDeal.getId())
                .removeValue((databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        Log.w(TAG, "deleteDeal: Error deleting deal!", databaseError.toException());
                        dealSavingStatus.postValue(Status.errorSaving(databaseError.toException()));
                    } else {
                        dealSavingStatus.postValue(Status.savedSuccess());
                    }
                });
    }

    LiveData<Status> dealStatus() {
        return dealSavingStatus;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    static class Factory extends ViewModelProvider.NewInstanceFactory {
        private DatabaseReference database;
        private ExecutorService executor;

        Factory(DatabaseReference database, ExecutorService executor) {
            this.database = database;
            this.executor = executor;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new DealViewModel(database, executor);
        }
    }
}
