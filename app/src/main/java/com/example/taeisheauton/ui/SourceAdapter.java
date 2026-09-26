package com.example.taeisheauton.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taeisheauton.R;

import java.util.List;

public class SourceAdapter extends RecyclerView.Adapter<SourceAdapter.SourceViewHolder> {

    private List<SourceItem> sources;

    public SourceAdapter(List<SourceItem> sources) {
        this.sources = sources;
    }

    @NonNull
    @Override
    public SourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_source, parent, false);
        return new SourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SourceViewHolder holder, int position) {
        SourceItem source = sources.get(position);
        holder.sourceNameText.setText(source.name);
        holder.activateButton.setText(source.isActive ? "Activa" : "Activar");
    }

    @Override
    public int getItemCount() {
        return sources.size();
    }

    public static class SourceViewHolder extends RecyclerView.ViewHolder {
        TextView sourceNameText;
        Button activateButton;
        Button deleteButton;

        public SourceViewHolder(@NonNull View itemView) {
            super(itemView);
            sourceNameText = itemView.findViewById(R.id.sourceNameText);
            activateButton = itemView.findViewById(R.id.activateButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}