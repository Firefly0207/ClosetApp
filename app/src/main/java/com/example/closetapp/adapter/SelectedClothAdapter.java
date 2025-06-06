package com.example.closetapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.closetapp.model.Cloth;
import com.example.closetapp.R;

import java.util.List;

public class SelectedClothAdapter extends RecyclerView.Adapter<SelectedClothAdapter.ViewHolder> {
    private Context context;
    private List<Cloth> clothes;
    private OnRemoveClickListener onRemoveClickListener;

    public SelectedClothAdapter(Context context, List<Cloth> clothes) {
        this.context = context;
        this.clothes = clothes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_selected_cloth, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cloth cloth = clothes.get(position);
        holder.categoryTextView.setText(cloth.getCategory());
        Glide.with(context).load(cloth.getImageUrl()).into(holder.clothImageView);
        holder.removeButton.setOnClickListener(v -> {
            if (onRemoveClickListener != null) {
                onRemoveClickListener.onRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clothes.size();
    }

    public void updateClothes(List<Cloth> newClothes) {
        this.clothes = newClothes;
        notifyDataSetChanged();
    }

    public void setOnRemoveClickListener(OnRemoveClickListener listener) {
        this.onRemoveClickListener = listener;
    }

    public interface OnRemoveClickListener {
        void onRemove(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView clothImageView;
        TextView categoryTextView;
        ImageButton removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            clothImageView = itemView.findViewById(R.id.clothImageView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
} 