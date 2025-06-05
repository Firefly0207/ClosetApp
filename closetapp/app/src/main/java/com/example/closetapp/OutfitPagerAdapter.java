package com.example.closetapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class OutfitPagerAdapter extends RecyclerView.Adapter<OutfitPagerAdapter.ViewHolder> {
    private Context context;
    private List<Outfit> outfitList;
    private FirebaseFirestore db;

    public OutfitPagerAdapter(Context context, List<Outfit> outfitList) {
        this.context = context;
        this.outfitList = outfitList;
        this.db = FirebaseFirestore.getInstance();
    }

    public void updateOutfits(List<Outfit> newOutfitList) {
        this.outfitList = newOutfitList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_outfit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Outfit outfit = outfitList.get(position);
        holder.descriptionTextView.setText(outfit.getDescription());

        // 여러 옷 이미지 horizontal로 표시
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
    }

    @Override
    public int getItemCount() {
        return outfitList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView clothesRecyclerView;
        TextView descriptionTextView;

        ViewHolder(View itemView) {
            super(itemView);
            clothesRecyclerView = itemView.findViewById(R.id.clothesRecyclerView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        }
    }
}
