package com.techbeloved.travelmantics4odife;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class DealViewModel extends ViewModel {

    private static final String TAG = "DealViewModel";

    private DatabaseReference travelDealReference;

    private final MutableLiveData<Status> dealSavingStatus = new MutableLiveData<>();
    private final MutableLiveData<UploadStatus> imageUploadStatus = new MutableLiveData<>();
    private String imageUri;
    // Gotten when the file is about to be uploaded
    private String imageName;
    private StorageReference storageRef;
    private ExecutorService executor;

    private DealViewModel(DatabaseReference database, StorageReference storageReference, ExecutorService executor) {
        storageRef = storageReference;
        this.executor = executor;
        this.travelDealReference = database.child("travelDeals");
    }

    void saveDeal(TravelDeal deal) {
        if (TextUtils.isEmpty(deal.getId())) {
            String key = travelDealReference.push().getKey();
            deal.setId(key);
        }
        // Change only if new image is uploaded
        if (imageUri != null) {
            deal.setImageUrl(imageUri);
        }
        if (imageName != null) {
            deal.setImageName(imageName);
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
                .removeValue()
                .addOnCompleteListener(executor, task -> {
                    // Delete picture also
                    try {
                        if (!TextUtils.isEmpty(currentDeal.getImageName())) {
                            Log.i(TAG, "deleteDeal: Image name: " + currentDeal.getImageName());
                            Tasks.await(storageRef.child("images/" + currentDeal.getImageName()).delete());
                        }
                        dealSavingStatus.postValue(Status.savedSuccess());
                    } catch (ExecutionException e) {
                        Log.w(TAG, "deleteDeal: Delete picture failed", e);

                    } catch (InterruptedException e) {
                        Log.w(TAG, "deleteDeal: Delete picture failed", e);

                    } catch (Exception e) {
                        Log.w(TAG, "deleteDeal: Delete picture failed", e);
                    }
                })
                .addOnFailureListener(executor, e -> {
                    Log.w(TAG, "deleteDeal: Error deleting deal!", e);
                    dealSavingStatus.postValue(Status.errorSaving(e));
                });
    }

    void uploadFile(Uri file) {
        imageName = file.getLastPathSegment();
        StorageReference reference = storageRef.child("images/" + imageName);
        UploadTask uploadTask = reference.putFile(file);
        uploadTask.addOnSuccessListener(executor, taskSnapshot -> {

            Uri downloadUri;
            try {
                downloadUri = Tasks.await(taskSnapshot.getStorage().getDownloadUrl());
                Log.i(TAG, "uploadFile: downloadUri: " + downloadUri);
                imageUploadStatus.postValue(new UploadStatus.Success(downloadUri.toString()));
                imageUri = downloadUri.toString();
            } catch (ExecutionException e) {
                Log.w(TAG, "uploadFile: Error", e);
                ;
            } catch (InterruptedException e) {
                Log.w(TAG, "uploadFile: Error", e);
                ;
            }

        })
                .addOnFailureListener(executor, error -> imageUploadStatus.postValue(new UploadStatus.Failure(error)))
                .addOnProgressListener(executor, progress ->
                        imageUploadStatus.postValue(new UploadStatus.Progress((int) (((float) progress.getBytesTransferred() / progress.getTotalByteCount()) * 100))));

    }

    LiveData<Status> dealStatus() {
        return dealSavingStatus;
    }

    public LiveData<UploadStatus> imageUploadStatus() {
        return imageUploadStatus;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    static class Factory extends ViewModelProvider.NewInstanceFactory {
        private DatabaseReference database;
        private ExecutorService executor;
        private StorageReference storageRef;

        Factory(DatabaseReference database, ExecutorService executor, StorageReference storageRef) {
            this.database = database;
            this.executor = executor;
            this.storageRef = storageRef;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new DealViewModel(database, storageRef, executor);
        }
    }

    interface UploadStatus {
        class Success implements UploadStatus {
            private String downloadUri;

            Success(String downloadUri) {
                this.downloadUri = downloadUri;
            }

            public String getDownloadUri() {
                return downloadUri;
            }
        }

        class Progress implements UploadStatus {
            private int percent;

            Progress(int percent) {

                this.percent = percent;
            }

            public int getPercent() {
                return percent;
            }
        }

        class Failure implements UploadStatus {
            private Throwable error;

            Failure(Throwable throwable) {
                error = throwable;
            }

            public Throwable getError() {
                return error;
            }
        }

        class None implements UploadStatus {

        }
    }
}
