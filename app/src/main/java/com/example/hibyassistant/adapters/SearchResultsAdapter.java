package com.example.hibyassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hibyassistant.R;
import com.example.hibyassistant.models.SearchResult;

import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder> {
    private List<SearchResult> searchResults;
    private OnSearchResultClickListener listener;

    public interface OnSearchResultClickListener {
        void onSearchResultClick(SearchResult result);
    }

    public SearchResultsAdapter(List<SearchResult> searchResults, OnSearchResultClickListener listener) {
        this.searchResults = searchResults;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new SearchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        SearchResult result = searchResults.get(position);
        holder.categoryTextView.setText(result.getCategory());
        holder.titleTextView.setText(result.getTitle());
        holder.descriptionTextView.setText(result.getDescription());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSearchResultClick(result);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public void updateResults(List<SearchResult> newResults) {
        this.searchResults = newResults;
        notifyDataSetChanged();
    }

    static class SearchResultViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTextView;
        TextView titleTextView;
        TextView descriptionTextView;

        SearchResultViewHolder(View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        }
    }
} 