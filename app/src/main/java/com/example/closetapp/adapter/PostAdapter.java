// java/com/example/closetapp/adapter/PostAdapter.java
package com.example.closetapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.closetapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import com.example.closetapp.model.PostItem;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<PostItem> postList;
    private Context context;

    public PostAdapter(Context context, List<PostItem> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostItem item = postList.get(position);
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .into(holder.imagePost);
        holder.textCaption.setText(item.getCaption());
        holder.textLikes.setText(item.getLikes() + " likes");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference postRef = FirebaseFirestore.getInstance().collection("posts").document(item.getDocumentId());
        postRef.get().addOnSuccessListener(documentSnapshot -> {
            List<String> likedUserIds = (List<String>) documentSnapshot.get("likedUserIds");
            if (likedUserIds == null) likedUserIds = new java.util.ArrayList<>();
            boolean alreadyLiked = likedUserIds.contains(userId);
            holder.likeIcon.setImageResource(alreadyLiked ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        });
        holder.likeIcon.setOnClickListener(v -> {
            postRef.get().addOnSuccessListener(documentSnapshot -> {
                List<String> likedUserIds = (List<String>) documentSnapshot.get("likedUserIds");
                if (likedUserIds == null) likedUserIds = new java.util.ArrayList<>();
                boolean alreadyLiked = likedUserIds.contains(userId);
                if (alreadyLiked) {
                    postRef.update(
                        "likes", FieldValue.increment(-1),
                        "likedUserIds", FieldValue.arrayRemove(userId)
                    );
                    item.setLikes(item.getLikes() - 1);
                    holder.textLikes.setText(item.getLikes() + " likes");
                    holder.likeIcon.setImageResource(R.drawable.ic_favorite_border);
                } else {
                    postRef.update(
                        "likes", FieldValue.increment(1),
                        "likedUserIds", FieldValue.arrayUnion(userId)
                    );
                    item.setLikes(item.getLikes() + 1);
                    holder.textLikes.setText(item.getLikes() + " likes");
                    holder.likeIcon.setImageResource(R.drawable.ic_favorite_filled);
                }
            });
        });
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("삭제 확인")
                    .setMessage("정말 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        FirebaseFirestore.getInstance().collection("posts")
                                .document(item.getDocumentId())
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    postList.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "삭제됨", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("취소", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imagePost, likeIcon;
        TextView textCaption, textLikes;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePost = itemView.findViewById(R.id.image_post);
            textCaption = itemView.findViewById(R.id.text_caption);
            textLikes = itemView.findViewById(R.id.text_likes);
            likeIcon = itemView.findViewById(R.id.like_icon);
        }
    }
}