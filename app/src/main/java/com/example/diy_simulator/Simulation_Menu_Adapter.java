package com.example.diy_simulator;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class Simulation_Menu_Adapter extends  RecyclerView.Adapter<com.example.diy_simulator.Simulation_Menu_Adapter.ViewHolder>{
    Context context;
    List<Simulation_Menu_Info> items;
    int item_layout;

    public Simulation_Menu_Adapter(Context context, List<Simulation_Menu_Info> items, int item_layout) {
        this.context = context;
        this.items = items;
        this.item_layout = item_layout;
    }

    @NonNull
    @Override
    public Simulation_Menu_Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simulation_menu_item, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final Simulation_Menu_Adapter.ViewHolder holder, final int position) {
        final Simulation_Menu_Info item = items.get(position);

        //제품 이름, 가격 텍스트 나타내기
        holder.name.setText(item.getName());
        holder.price.setText(item.getPrice());
        if (!TextUtils.isEmpty(item.getImg_url())) {
            //제품 이미지 url로 나타내기
            Glide.with(holder.itemView.getContext())
                    .load(item.getImg_url())
                    .into(holder.img);

        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView price;
        ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.simulation_menu_product_name);
            price = itemView.findViewById(R.id.simulation_menu_product_price);
            img = itemView.findViewById(R.id.simulation_menu_product_img);
        }
    }
}
