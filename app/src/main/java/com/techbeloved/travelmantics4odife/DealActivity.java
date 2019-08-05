package com.techbeloved.travelmantics4odife;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.techbeloved.travelmantics4odife.databinding.ActivityDealBinding;

import java.util.concurrent.Executors;

public class DealActivity extends AppCompatActivity {

    private static final String TAG = "DealActivity";

    public static final String ARG_DEAL = "selectedDeal";
    private static final String SAVED_DEAL = "savedDeal";

    private ActivityDealBinding binding;
    private DealViewModel viewModel;
    @Nullable
    private TravelDeal currentDeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_deal);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        DealViewModel.Factory factory = new DealViewModel.Factory(FirebaseDatabase.getInstance().getReference(), Executors.newCachedThreadPool());
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

        if (savedInstanceState != null) {
            currentDeal = savedInstanceState.getParcelable(SAVED_DEAL);
        } else {
            currentDeal = getIntent().getParcelableExtra(ARG_DEAL);
            if (currentDeal != null) populateDeal(currentDeal);
        }
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_save_deal) {
            checkDealDetailsAndSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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
                    null, "Deal Image");
            if (currentDeal != null) {
                travelDeal.setId(currentDeal.getId());
                travelDeal.setImageName(currentDeal.getImageName());
                travelDeal.setImageUrl(currentDeal.getImageUrl()); // TODO: Change this when upload image is done
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
        Picasso.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_power)
                .error(R.drawable.ic_error)
                .centerCrop()
                .resize(binding.imageViewDealPhoto.getMaxWidth(), binding.imageViewDealPhoto.getMaxHeight())
                .into(binding.imageViewDealPhoto);
    }
}
