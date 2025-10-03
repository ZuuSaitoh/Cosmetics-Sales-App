package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Banner;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    List<Banner> banners;
    public BannerAdapter(List<Banner> banners) {
        this.banners = banners;
    }


    @NonNull
    @Override
    public BannerAdapter.BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.banner_product, parent, false);
        return new BannerAdapter.BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        // Lấy dữ liệu của banner tại vị trí "position"
        Banner banner = banners.get(position);
        // Gán dữ liệu vào các View trong holder
        holder.imgBanner.setImageResource(banner.getImageResId());

    }

    @Override
    public int getItemCount() {
        if (banners != null) {
            return banners.size();
        }
        return 0;
    }

    public class BannerViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgBanner;
        public BannerViewHolder(@NonNull View view) {
            super(view);
            imgBanner = view.findViewById(R.id.imgBanner);
        }

    }
}
