package com.techbeloved.travelmantics4odife;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.techbeloved.travelmantics4odife.databinding.ActivityDealListingBinding;

public class DealListingActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private FirebaseAuth.AuthStateListener authStateListener = auth -> {
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            firebaseAuth = auth;
            initializeDeals();
        }
    };

    private ActivityDealListingBinding binding;
    private DealListingViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_deal_listing);

        binding.fabAddNewDeal.setOnClickListener(view -> startActivity(new Intent(this, DealActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            firebaseAuth.signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    private void initializeDeals() {
        DealListingViewModel.Factory factory = new DealListingViewModel.Factory(FirebaseDatabase.getInstance());
        viewModel = ViewModelProviders.of(this, factory).get(DealListingViewModel.class);
    }


}
