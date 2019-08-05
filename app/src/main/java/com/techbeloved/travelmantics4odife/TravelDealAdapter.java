package com.techbeloved.travelmantics4odife;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.techbeloved.travelmantics4odife.databinding.ItemDealBinding;

import java.util.Objects;

public class TravelDealAdapter extends ListAdapter<TravelDeal, TravelDealAdapter.ViewHolder> {

    private static DiffUtil.ItemCallback<TravelDeal> sDealItemCallback = new DiffUtil.ItemCallback<TravelDeal>() {
        @Override
        public boolean areItemsTheSame(@NonNull TravelDeal oldItem, @NonNull TravelDeal newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TravelDeal oldItem, @NonNull TravelDeal newItem) {
            return Objects.deepEquals(oldItem, newItem);
        }
    };
    private ClickListener<TravelDeal> clickListener;


    TravelDealAdapter(ClickListener<TravelDeal> clickListener) {
        super(sDealItemCallback);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemDealBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_deal, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TravelDeal deal = getItem(position);
        holder.binding.textViewDealTitle.setText(deal.getTitle());
        holder.binding.textViewDealDescription.setText(deal.getDescription());
        holder.binding.textViewDealPrice.setText(deal.getPrice());

        Glide.with(holder.binding.getRoot().getContext())
                .load(deal.getImageUrl())
                .placeholder(R.drawable.ic_power)
                .error(R.drawable.ic_error)
                .centerCrop()
                .into(holder.binding.imageViewDealPhoto);
        holder.binding.getRoot().setOnClickListener(view -> clickListener.onItemClick(deal));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemDealBinding binding;

        ViewHolder(@NonNull ItemDealBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
