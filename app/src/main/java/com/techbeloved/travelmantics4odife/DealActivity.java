package com.techbeloved.travelmantics4odife;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.techbeloved.travelmantics4odife.databinding.ActivityDealBinding;

import java.util.concurrent.Executors;

public class DealActivity extends AppCompatActivity {

    private static final String TAG = "DealActivity";

    public static final String ARG_DEAL = "selectedDeal";
    private static final String SAVED_DEAL = "savedDeal";
    private static final int RC_INSERT_PICS = 45;

    private ActivityDealBinding binding;
    private DealViewModel viewModel;
    @Nullable
    private TravelDeal currentDeal;

    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_deal);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(getString(R.string.storage_reference_deals_pictures));
        DealViewModel.Factory factory = new DealViewModel.Factory(FirebaseDatabase.getInstance().getReference(), Executors.newCachedThreadPool(), storageRef);
        viewModel = ViewModelProviders.of(this, factory).get(DealViewModel.class);

        viewModel.dealStatus().observe(this, status -> {
            if (status.error()) {
                Log.w(TAG, "onCreate: Status has error", status.getError());
                Toast.makeText(this, "Error saving deal!", Toast.LENGTH_SHORT).show();
            } else if (status.saved()) {
                Log.i(TAG, "onCreate: Deal saved!");
                finish();
            }
        });

        viewModel.imageUploadStatus().observe(this, status -> {
            if (status instanceof DealViewModel.UploadStatus.Progress) {
                int progress = ((DealViewModel.UploadStatus.Progress) status).getPercent();
                if (binding.progressBar.getVisibility() != View.VISIBLE) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                }
                binding.progressBar.setProgress(progress);
            } else if (status instanceof DealViewModel.UploadStatus.Success) {
                Log.i(TAG, "onCreate: Success upload");
                String downloadUri = ((DealViewModel.UploadStatus.Success) status).getDownloadUri();
                Glide.with(this)
                        .load(downloadUri)
                        .placeholder(R.drawable.ic_power)
                        .error(R.drawable.ic_error)
                        .centerCrop()
                        .into(binding.imageViewDealPhoto);
                binding.progressBar.setVisibility(View.GONE);
            } else if (status instanceof DealViewModel.UploadStatus.Failure) {
                Toast.makeText(this, "Image Upload Failed!", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            }
        });

        if (savedInstanceState != null) {
            currentDeal = savedInstanceState.getParcelable(SAVED_DEAL);
        } else {
            currentDeal = getIntent().getParcelableExtra(ARG_DEAL);
            if (currentDeal != null) populateDeal(currentDeal);
        }

        adminChecks();

        binding.buttonUploadImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Insert Picture"), RC_INSERT_PICS);
        });
    }

    private void adminChecks() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getUid();
        if (userId != null) {
            FirebaseDatabase.getInstance().getReference().child("administrators")
                    .child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                enableAdminFeatures(true);
                            } else {
                                enableAdminFeatures(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            enableAdminFeatures(false);
                        }
                    });
        }
    }

    private void enableAdminFeatures(boolean enable) {
        if (!enable) {
            binding.editTextDealPrice.setKeyListener(null);
            binding.editTextDealDescription.setKeyListener(null);
            binding.editTextDealTitle.setKeyListener(null);
        }
        if (enable) {
            binding.buttonUploadImage.setVisibility(View.VISIBLE);
        } else {
            binding.buttonUploadImage.setVisibility(View.INVISIBLE);
        }
        isAdmin = enable;
        invalidateOptionsMenu();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(SAVED_DEAL, currentDeal);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.deal, menu);
        menu.findItem(R.id.menu_delete_deal).setVisible(isAdmin);
        menu.findItem(R.id.menu_save_deal).setVisible(isAdmin);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_save_deal) {
            checkDealDetailsAndSave();
            return true;
        } else if (itemId == R.id.menu_delete_deal) {
            if (currentDeal != null) {
                viewModel.deleteDeal(currentDeal);
            } else {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_INSERT_PICS && resultCode == RESULT_OK) {
            if (data != null) {
                Uri file = data.getData();
                viewModel.uploadFile(file);
            }
        }
    }

    /**
     * Verifies that details are entered correctly after which it saves the deal.
     */
    private void checkDealDetailsAndSave() {
        if (!TextUtils.isEmpty(binding.editTextDealTitle.getText())) {
            String title = binding.editTextDealTitle.getText().toString();
            String description = null;
            if (!TextUtils.isEmpty(binding.editTextDealDescription.getText())) {
                description = binding.editTextDealDescription.getText().toString();
            }

            double price = 0.00;
            if (!TextUtils.isEmpty(binding.editTextDealPrice.getText())) {
                try {
                    price = Double.parseDouble(binding.editTextDealPrice.getText().toString());
                } catch (NumberFormatException e) {
                    Log.w(TAG, "checkDealDetailsAndSave: Incorrect number: "
                            + binding.editTextDealPrice.getText().toString(), e);
                }
            }

            TravelDeal travelDeal = new TravelDeal(title, description, String.valueOf(price),
                    null, null);
            if (currentDeal != null) {
                travelDeal.setId(currentDeal.getId());
                travelDeal.setImageName(currentDeal.getImageName());
                travelDeal.setImageUrl(currentDeal.getImageUrl()); // DONE in ViewModel: Change this when upload image is done
            }
            viewModel.saveDeal(travelDeal);
        } else {
            Toast.makeText(this, "Deal Title cannot be empty!", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateDeal(@NonNull TravelDeal deal) {
        binding.editTextDealTitle.setText(deal.getTitle());
        binding.editTextDealDescription.setText(deal.getDescription());
        binding.editTextDealPrice.setText(deal.getPrice());


        String imageUrl = TextUtils.isEmpty(deal.getImageUrl()) ? null : deal.getImageUrl();
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_power)
                .error(R.drawable.ic_error)
                .fitCenter()
                .into(binding.imageViewDealPhoto);
    }

}
