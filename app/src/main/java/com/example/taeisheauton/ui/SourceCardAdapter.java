package com.example.taeisheauton.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taeisheauton.R;
import com.example.taeisheauton.data.SourceEntity;

import java.util.List;

public class SourceCardAdapter extends RecyclerView.Adapter<SourceCardAdapter.CardViewHolder> {

    public interface OnSourceClickListener {
        void onSourceClick(SourceEntity source);
    }

    private List<SourceEntity> sources;
    private OnSourceClickListener listener;

    public SourceCardAdapter(List<SourceEntity> sources, OnSourceClickListener listener) {
        this.sources = sources;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_source_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        SourceEntity source = sources.get(position);
        holder.sourceNameText.setText(source.name);
        holder.itemView.setAlpha(source.isActive ? 1.0f : 0.5f);
        holder.itemView.setOnClickListener(v -> listener.onSourceClick(source));
    }

    @Override
    public int getItemCount() {
        return sources.size();
    }

    public void setSources(List<SourceEntity> newSources) {
        this.sources = newSources;
        notifyDataSetChanged();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView sourceNameText;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            sourceNameText = itemView.findViewById(R.id.sourceNameText);
        }
    }
}
