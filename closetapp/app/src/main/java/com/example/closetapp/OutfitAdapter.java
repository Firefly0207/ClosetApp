package com.example.closetapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.ViewHolder> {
    private Context context;
    private List<Outfit> outfitList;
    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat;

    public OutfitAdapter(Context context, List<Outfit> outfitList) {
        this.context = context;
        this.outfitList = outfitList;
        this.db = FirebaseFirestore.getInstance();
        this.dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_outfit_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Outfit outfit = outfitList.get(position);
        
        // 날짜 표시
        holder.dateTextView.setText(dateFormat.format(outfit.getDate()));
        
        // 설명 표시
        holder.descriptionTextView.setText(outfit.getDescription());
        
        // 옷 이미지 로드 (RecyclerView 사용)
        List<String> clothIds = outfit.getClothIds();
        List<String> imageUrls = new ArrayList<>();
        ClothesImageAdapter imageAdapter = new ClothesImageAdapter(context, imageUrls);
        holder.clothesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.clothesRecyclerView.setAdapter(imageAdapter);

        if (clothIds != null && !clothIds.isEmpty()) {
            for (String clothId : clothIds) {
                db.collection("clothes").document(clothId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                imageUrls.add(imageUrl);
                                imageAdapter.notifyDataSetChanged();
                            }
                        }
                    });
            }
        }

        // 좋아요 수 표시
        holder.likeCountTextView.setText(String.valueOf(outfit.getLikeCount()));

        // 아이템 클릭 시 상세 화면으로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OutfitDetailActivity.class);
            intent.putExtra("outfitId", outfit.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return outfitList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView clothesRecyclerView;
        TextView dateTextView;
        TextView descriptionTextView;
        TextView likeCountTextView;

        ViewHolder(View itemView) {
            super(itemView);
            clothesRecyclerView = itemView.findViewById(R.id.clothesRecyclerView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            likeCountTextView = itemView.findViewById(R.id.likeCountTextView);
        }
    }
} 