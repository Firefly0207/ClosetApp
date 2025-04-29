package com.example.closetapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ClothAdapter extends RecyclerView.Adapter<ClothAdapter.ClothViewHolder> {

    private Context context;
    private List<Cloth> clothList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Cloth cloth);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    public ClothAdapter(Context context, List<Cloth> clothList) {
        this.context = context;
        this.clothList = clothList;
    }

    @NonNull
    @Override
    public ClothViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cloth, parent, false);
        return new ClothViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClothViewHolder holder, int position) {
        Cloth cloth = clothList.get(position);

        holder.categoryTextView.setText(cloth.getCategory());
        holder.tagTextView.setText(cloth.getTags());

        Glide.with(context).load(cloth.getImageUrl()).into(holder.clothImageView);

        // 즐겨찾기 상태 설정
        if (cloth.isFavorite()) {
            holder.favoriteButton.setImageResource(R.drawable.ic_star_filled); // 준비되면 이미지 대체
        } else {
            holder.favoriteButton.setImageResource(R.drawable.ic_star_outline);
        }

        holder.favoriteButton.setOnClickListener(v -> {
            boolean newFavorite = !cloth.isFavorite();
            cloth.setFavorite(newFavorite);
            notifyItemChanged(position);

            // ✅ Firestore 업데이트 추가
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("clothes")
                    .document(cloth.getId())
                    .update("favorite", newFavorite)
                    .addOnSuccessListener(aVoid -> {
                        // 성공했으면 필요하면 Toast 띄우거나 생략 가능
                    })
                    .addOnFailureListener(e -> {
                        // 실패했으면 다시 원래 상태로 복구
                        cloth.setFavorite(!newFavorite);
                        notifyItemChanged(position);
                    });
        });


        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(cloth);
            } else {
                Intent intent = new Intent(context, ClothDetailActivity.class);
                intent.putExtra("imageUrl", cloth.getImageUrl());
                intent.putExtra("info", cloth.getCategory() + "\n" + String.join(", ", cloth.getTagsList()));
                intent.putExtra("clothId", cloth.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clothList.size();
    }

    static class ClothViewHolder extends RecyclerView.ViewHolder {
        ImageView clothImageView;
        TextView categoryTextView, tagTextView;
        ImageButton favoriteButton;

        public ClothViewHolder(@NonNull View itemView) {
            super(itemView);
            clothImageView = itemView.findViewById(R.id.clothImageView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            tagTextView = itemView.findViewById(R.id.tagTextView);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }
}
