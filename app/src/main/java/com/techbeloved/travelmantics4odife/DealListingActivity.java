package com.techbeloved.travelmantics4odife;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private TravelDealAdapter dealAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_deal_listing);

        binding.fabAddNewDeal.setOnClickListener(view -> startActivity(new Intent(this, DealActivity.class)));


        dealAdapter = new TravelDealAdapter(deal -> {
            Intent intent = new Intent(this, DealActivity.class);
            intent.putExtra(DealActivity.ARG_DEAL, deal);
            startActivity(intent);
        });
        binding.recyclerviewDeals.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerviewDeals.setAdapter(dealAdapter);
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
        DealListingViewModel.Factory factory = new DealListingViewModel.Factory(FirebaseDatabase.getInstance().getReference());
        viewModel = ViewModelProviders.of(this, factory).get(DealListingViewModel.class);
        viewModel.travelDeals().observe(this, travelDeals -> dealAdapter.submitList(travelDeals));

        String userId = firebaseAuth.getUid();
        if (userId != null) {
            FirebaseDatabase.getInstance().getReference().child("administrators")
                    .child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                binding.fabAddNewDeal.setVisibility(View.VISIBLE);
                            } else {
                                binding.fabAddNewDeal.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            binding.fabAddNewDeal.setVisibility(View.GONE);
                        }
                    });
        }
    }


}
