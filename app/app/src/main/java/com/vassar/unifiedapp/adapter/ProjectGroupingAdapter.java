package com.vassar.unifiedapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.model.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;


public class ProjectGroupingAdapter extends RecyclerView.Adapter<ProjectGroupingAdapter.ProjectGroupingViewHolder> implements SectionTitleProvider{

    private List<String> mGroups;
    private Context mContext;
    private LayoutInflater mInflater;

    public ProjectGroupingAdapter(Activity context, List<String> projectGroups) {
        this.mContext = context;
        this.mGroups = new ArrayList<>();
        this.mGroups.addAll(projectGroups);
        this.mInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public ProjectGroupingAdapter.ProjectGroupingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.project_grouping_item, parent, false);
        return new ProjectGroupingAdapter.ProjectGroupingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectGroupingAdapter.ProjectGroupingViewHolder holder, int position) {
        holder.mName.setText(mGroups.get(position));
    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }

    @Override
    public String getSectionTitle(int position) {
        return String.valueOf(position + 1);
    }

    public class ProjectGroupingViewHolder extends RecyclerView.ViewHolder {

        public TextView mName;

        public ProjectGroupingViewHolder(View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.project_grouping_item_name);
        }
    }
}
