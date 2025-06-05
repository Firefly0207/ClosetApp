package com.example.closetapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ClothesImageAdapter extends RecyclerView.Adapter<ClothesImageAdapter.ViewHolder> {
    private List<String> imageUrls;
    private Context context;
    private boolean isGridView;
    private OnItemClickListener onItemClickListener;

    public ClothesImageAdapter(Context context, List<String> imageUrls, boolean isGridView) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.isGridView = isGridView;
    }

    public ClothesImageAdapter(Context context, List<String> imageUrls) {
        this(context, imageUrls, false);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_thumbnail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = imageUrls.get(position);
        if (url != null && !url.isEmpty()) {
            Glide.with(context).load(url).centerCrop().into(holder.imageView);
        }
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(position, url));
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, String imageUrl);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_thumb);
        }
    }
} 