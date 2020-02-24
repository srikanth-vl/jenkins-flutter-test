package com.vassar.unifiedapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.api.NewImageDownloaderService;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.IncomingImage;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import java.io.File;
import java.util.ArrayList;

public class GridFragmentAdapter extends RecyclerView.Adapter<GridFragmentAdapter.GridViewHolder> {

    private ArrayList<ProjectTypeModel> mAllApplications = new ArrayList<>();
    private ArrayList<ProjectTypeModel> mApplications = new ArrayList<>();

    public GridFragmentAdapter(ArrayList<ProjectTypeModel> allApplications, ArrayList<ProjectTypeModel> applications) {
        this.mAllApplications.addAll(allApplications);
        this.mApplications.addAll(applications);
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = ((LayoutInflater) UAAppContext.getInstance().getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.grid_fragment_item_layout, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        if (mApplications != null && mApplications.size() > 0) {
            ProjectTypeModel applicationModel = mApplications.get(position);
            holder.mTextview.setText(applicationModel.mName);

            if (mApplications.get(position).mIcon != null) {
                IncomingImage image = UAAppContext.getInstance().getDBHelper()
                        .getIncomingImageWithUrl(mApplications.get(position).mIcon);
                if (image != null) {
                    if (image.getImageLocalPath() == null || image.getImageLocalPath().isEmpty()) {
                        // Setting the default image, while the icon downloads
                        holder.mImageView.setImageResource(R.drawable.splash_app_logo);
                        // Image download failed the first time, download again
                        NewImageDownloaderService newImageDownloaderService = new NewImageDownloaderService(image);
                        newImageDownloaderService.execute();
                    } else {
                        File imgFile = new  File(image.getImageLocalPath());
                        if(imgFile.exists()){
                            if (imgFile.length() == 0) {
                                // Setting the default image, while the icon downloads
                                holder.mImageView.setImageResource(R.drawable.splash_app_logo);
                                // Image download failed the first time, download again
                                NewImageDownloaderService newImageDownloaderService = new NewImageDownloaderService(image);
                                newImageDownloaderService.execute();
                            } else {
                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                holder.mImageView.setImageBitmap(myBitmap);
                            }
                        } else {
                            // Image does not exist
                            holder.mImageView.setImageResource(R.drawable.splash_app_logo);
                        }
                    }
                } else {
                    // Image does not exist
                    holder.mImageView.setImageResource(R.drawable.splash_app_logo);
                }
            } else {
                holder.mImageView.setImageResource(R.drawable.splash_app_logo);
            }

            // Showing unsynced project count
            if (mAllApplications.size() > 0) {
                int flag = 1;
                for (ProjectTypeModel projectTypeModel : mAllApplications) {
                    if (projectTypeModel.mParentAppId.equals(applicationModel.mAppId))
                        flag = 0;
                }

                if (flag == 1) {
                    // This project type doesn't have sub project types, show project count
                    int unsyncedMediaCount = Utils.getInstance().getUnsyncedMediaCount(applicationModel.mAppId);
                    int unsyncedProjectCount = Utils.getInstance().getUnsyncedProjectCount(applicationModel.mAppId);

                    Utils.logInfo(LogTags.UPDATE_UNSYNCED_COUNT, "updated unSynced count for APP :: " + applicationModel.mAppId);
                    holder.mProjectCount.setText(UAAppContext.getInstance().getContext().getResources()
                            .getString(R.string.UNSYNCED_RECORDS) + " : " + String.valueOf(unsyncedMediaCount + unsyncedProjectCount));
                }
            }

            // Assigning ProjectType status
            if(mApplications != null && !mApplications.isEmpty()) {
                ProjectTypeModel appModel = mApplications.get(position);
                if (appModel != null && appModel.mStatus != null && !appModel.mStatus.isEmpty()) {
                    // Has a status value
                    switch (appModel.mStatus) {
                        case Constants.ROOT_CONFIG_APPLICATION_STATUS_INACTIVE :
                            holder.mRootView.setAlpha(0.3f);
                            break;
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mApplications.size();
    }

    public class GridViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout mRootView;
        public ImageView mImageView;
        public TextView mTextview;
        public TextView mProjectCount;

        public GridViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView.findViewById(R.id.grid_item_root_view);
            mImageView = itemView.findViewById(R.id.grid_image);
            mTextview = itemView.findViewById(R.id.grid_text);
            mProjectCount = itemView.findViewById(R.id.grid_project_count);
        }
    }
}
