package com.example.closetapp;

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
import java.util.List;

public class SelectedClothAdapter extends RecyclerView.Adapter<SelectedClothAdapter.ViewHolder> {
    private Context context;
    private List<Cloth> selectedClothes;
    private OnRemoveClickListener onRemoveClickListener;

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }

    public SelectedClothAdapter(Context context, List<Cloth> selectedClothes) {
        this.context = context;
        this.selectedClothes = selectedClothes;
    }

    public void setOnRemoveClickListener(OnRemoveClickListener listener) {
        this.onRemoveClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_selected_cloth, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cloth cloth = selectedClothes.get(position);
        
        Glide.with(context)
                .load(cloth.getImageUrl())
                .centerCrop()
                .into(holder.clothImageView);
        
        holder.categoryTextView.setText(cloth.getCategory());
        
        holder.removeButton.setOnClickListener(v -> {
            if (onRemoveClickListener != null) {
                onRemoveClickListener.onRemoveClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectedClothes.size();
    }

    public void updateClothes(List<Cloth> newClothes) {
        this.selectedClothes = newClothes;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView clothImageView;
        TextView categoryTextView;
        ImageButton removeButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            clothImageView = itemView.findViewById(R.id.clothImageView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
} 