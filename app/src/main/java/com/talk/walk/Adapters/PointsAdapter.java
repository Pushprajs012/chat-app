package com.talk.walk.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.talk.walk.Models.Points;
import com.talk.walk.R;

import java.util.List;

public class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.ViewHolder> {

    private Context mContext;
    private List<Points> pointsList;
    private OnItemClickListener onItemClickListener;

    public PointsAdapter(Context mContext, List<Points> pointsList, OnItemClickListener onItemClickListener) {
        this.mContext = mContext;
        this.pointsList = pointsList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_purchase_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Points points = pointsList.get(position);

        holder.tvPoints.setText(String.valueOf(points.getPoints()));
        holder.tvPointsCost.setText(mContext.getResources().getString(R.string.currency_symbol) + String.valueOf(points.getPoints_cost()));

        holder.cvPointCost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(points, pointsList, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pointsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvPoints, tvPointsCost;
        private CardView cvPointCost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvPointsCost = itemView.findViewById(R.id.tvPointsCost);
            cvPointCost = itemView.findViewById(R.id.cvPointCost);
        }
    }
    
    public interface OnItemClickListener {
        void onItemClick(Points points, List<Points> pointsList, int position);
    }
}
