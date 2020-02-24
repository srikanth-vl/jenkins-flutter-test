package com.vassar.unifiedapp.adapter;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vassar.unifiedapp.R;

import java.util.ArrayList;

public class ProjectListSeparatorAdapter extends RecyclerView
        .Adapter<ProjectListSeparatorAdapter.ProjectListSeparatorViewHolder> {

    private AppCompatActivity mContext;
    private LayoutInflater mInflater;
    private ArrayList<String> mSeparatorNames = new ArrayList<>();

    public ProjectListSeparatorAdapter(AppCompatActivity context, LayoutInflater inflater
            , ArrayList<String> separatorNames) {
        this.mContext = context;
        this.mInflater = inflater;
        this.mSeparatorNames.addAll(separatorNames);
    }

    @NonNull
    @Override
    public ProjectListSeparatorAdapter.ProjectListSeparatorViewHolder onCreateViewHolder
            (@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.project_list_item, parent, false);
        return new ProjectListSeparatorAdapter.ProjectListSeparatorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectListSeparatorAdapter.ProjectListSeparatorViewHolder holder, int position) {
//        holder.mImageView.setImageResource(R.drawable.login_image);
        holder.mName.setText(mSeparatorNames.get(position));
        holder.mSubtitle.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mSeparatorNames.size();
    }

    public class ProjectListSeparatorViewHolder extends RecyclerView.ViewHolder {

//        public ImageView mImageView;
        public TextView mName;
        public TextView mSubtitle;

        public ProjectListSeparatorViewHolder(View itemView) {
            super(itemView);
//            mImageView = itemView.findViewById(R.id.project_list_item_image);
            mName = itemView.findViewById(R.id.project_list_item_name);
            mSubtitle = itemView.findViewById(R.id.project_list_item_count);
        }
    }
}
