package com.example.closetapp.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.tabs.TabLayout;
import com.example.closetapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import android.view.ViewGroup;

public class FavoriteMatchesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<MatchItem> favoriteMatches = new ArrayList<>();
    private MatchAdapter adapter;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_matches);
        
        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        // 하단 네비게이션 설정
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_mypage);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_closet) {
                startActivity(new Intent(this, ClosetActivity.class));
                return true;
            } else if (id == R.id.nav_daily) {
                startActivity(new Intent(this, DailyFitActivity.class));
                return true;
            } else if (id == R.id.nav_community) {
                startActivity(new Intent(this, CommunityActivity.class));
                return true;
            } else if (id == R.id.nav_mypage) {
                startActivity(new Intent(this, MyPageActivity.class));
                return true;
            }
            return false;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MatchAdapter(favoriteMatches);
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadFavoriteMatches();
        setupTabs();
    }
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.getTabAt(1).select();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        startActivity(new Intent(FavoriteMatchesActivity.this, FavoriteClothesActivity.class));
                        break;
                    case 1:
                        // 현재 탭
                        break;
                    case 2:
                        startActivity(new Intent(FavoriteMatchesActivity.this, FavoriteDailyFitsActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(FavoriteMatchesActivity.this, FavoritePostsActivity.class));
                        break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    private void loadFavoriteMatches() {
        db.collection("matches")
          .whereEqualTo("userId", currentUserId)
          .whereEqualTo("favorite", true)
          .get()
          .addOnSuccessListener(queryDocumentSnapshots -> {
              favoriteMatches.clear();
              for (var doc : queryDocumentSnapshots.getDocuments()) {
                  String topImageUrl = doc.getString("topImageUrl");
                  String bottomImageUrl = doc.getString("bottomImageUrl");
                  List<String> extraImageUrls = (List<String>) doc.get("extraImageUrls");
                  String description = doc.getString("description");
                  boolean favorite = doc.contains("favorite") && Boolean.TRUE.equals(doc.getBoolean("favorite"));
                  favoriteMatches.add(new MatchItem(topImageUrl, bottomImageUrl, extraImageUrls, description, favorite, doc.getId()));
              }
              adapter.notifyDataSetChanged();
          });
    }
    public static class MatchItem {
        public String topImageUrl, bottomImageUrl, description, id;
        public List<String> extraImageUrls;
        public boolean favorite;
        public MatchItem(String t, String b, List<String> extra, String desc, boolean fav, String id) {
            topImageUrl = t; bottomImageUrl = b; extraImageUrls = extra; description = desc; favorite = fav; this.id = id;
        }
    }
    public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.ViewHolder> {
        private List<MatchItem> list;
        public MatchAdapter(List<MatchItem> list) { this.list = list; }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_match, parent, false);
            return new ViewHolder(v);
        }
        @Override
        public void onBindViewHolder(ViewHolder holder, int pos) {
            MatchItem item = list.get(pos);
            Glide.with(holder.itemView.getContext()).load(item.topImageUrl).into(holder.image1);
            Glide.with(holder.itemView.getContext()).load(item.bottomImageUrl).into(holder.image2);
            if (item.extraImageUrls != null && !item.extraImageUrls.isEmpty()) {
                Glide.with(holder.itemView.getContext()).load(item.extraImageUrls.get(0)).into(holder.image3);
                holder.image3.setVisibility(View.VISIBLE);
            } else {
                holder.image3.setImageDrawable(null);
                holder.image3.setVisibility(View.INVISIBLE);
            }
            holder.description.setText(item.description != null ? item.description : "");
            holder.favoriteButton.setImageResource(item.favorite ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
            holder.favoriteButton.setOnClickListener(v -> {
                boolean newFavorite = !item.favorite;
                db.collection("matches").document(item.id).update("favorite", newFavorite)
                    .addOnSuccessListener(aVoid -> {
                        item.favorite = newFavorite;
                        notifyItemChanged(pos);
                    });
            });
        }
        @Override
        public int getItemCount() { return list.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image1, image2, image3;
            TextView description;
            android.widget.ImageButton favoriteButton;
            ViewHolder(View v) {
                super(v);
                image1 = v.findViewById(R.id.image1);
                image2 = v.findViewById(R.id.image2);
                image3 = v.findViewById(R.id.image3);
                description = v.findViewById(R.id.description);
                favoriteButton = v.findViewById(R.id.favoriteButton);
            }
        }
    }
} 