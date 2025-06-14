package com.example.closetapp.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.closetapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

public class MatchActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<MatchItem> matchList = new ArrayList<>();
    private MatchAdapter adapter;
    private FirebaseFirestore db;
    private String currentUserId;
    private boolean showOnlyFavorite = false;
    private MenuItem favoriteFilterMenuItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MatchAdapter(matchList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadMatches();

        FloatingActionButton fab = findViewById(R.id.fabAddMatch);
        fab.setOnClickListener(v -> startActivity(new Intent(this, MatchRegisterActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_closet); // 예시: closet 탭과 동일하게 표시
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_closet) {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_match, menu);
        favoriteFilterMenuItem = menu.findItem(R.id.action_favorite_filter);
        updateFavoriteFilterIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_favorite_filter) {
            showOnlyFavorite = !showOnlyFavorite;
            updateFavoriteFilterIcon();
            loadMatches();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateFavoriteFilterIcon() {
        if (favoriteFilterMenuItem != null) {
            favoriteFilterMenuItem.setIcon(showOnlyFavorite ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        }
    }

    private void loadMatches() {
        db.collection("matches")
          .whereEqualTo("userId", currentUserId)
          .get()
          .addOnSuccessListener(queryDocumentSnapshots -> {
              matchList.clear();
              for (var doc : queryDocumentSnapshots.getDocuments()) {
                  String topImageUrl = doc.getString("topImageUrl");
                  String bottomImageUrl = doc.getString("bottomImageUrl");
                  List<String> extraImageUrls = (List<String>) doc.get("extraImageUrls");
                  String description = doc.getString("description");
                  boolean favorite = doc.contains("favorite") && Boolean.TRUE.equals(doc.getBoolean("favorite"));
                  if (showOnlyFavorite && !favorite) continue;
                  matchList.add(new MatchItem(topImageUrl, bottomImageUrl, extraImageUrls, description, favorite, doc.getId()));
              }
              if (!showOnlyFavorite) {
                  // 즐겨찾기 먼저 정렬
                  matchList.sort((a, b) -> Boolean.compare(!a.favorite, !b.favorite));
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
            // 이미지 최대 3개
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
            ImageButton favoriteButton;
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
