package com.vassar.unifiedapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.api.LatestProjectDataService;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.model.LatestFieldValue;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectIconInfo;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.utils.ImageUtils;
import com.vassar.unifiedapp.utils.StringUtils;
import com.vassar.unifiedapp.utils.Utils;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ProjectListAdapter extends RecyclerView.Adapter<ProjectListAdapter.ProjectListViewHolder> implements SectionTitleProvider{

    private List<Project> mProjectList = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private long mListCreationTimestamp;
    private  String mAppId;
    private ProjectTypeModel mProjectTypeModel = null;
    public ProjectListAdapter(Activity context, List<Project> projectList, long listCreationTimestamp, String appId) {
        this.mContext = context;
        this.mProjectList.addAll(projectList);
        this.mInflater = LayoutInflater.from(mContext);
        mAppId = appId;
        mProjectTypeModel = UAAppContext.getInstance().getProjectTypeModel(mAppId);

    }

    @NonNull
    @Override
    public ProjectListAdapter.ProjectListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.project_list_item, parent, false);
        return new ProjectListAdapter.ProjectListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectListAdapter.ProjectListViewHolder holder, int position) {

        if (mProjectList.get(position).mExpired ) {
            ((ProjectListViewHolder) holder).itemView.setAlpha(0.3f);
        }

//        ArrayList<ProjectListFieldModel> projectListFields = mProjectList.get(position).mFields;
//        for (ProjectListFieldModel model : projectListFields) {
//            if (model.mIdentifier.equals("event_name"))
////                holder.mSubtext.setText(model.mProjectListFieldValue.mValue);
//            if (model.mIdentifier.equals("school_name"))
//                holder.mName.setText("School Name - " + model.mProjectListFieldValue.mValue);
//        }

        holder.mName.setText(mProjectList.get(position).mProjectName);

        long timeStamp = mProjectList.get(position).mLastSyncTimestamp;

        if (timeStamp == 0) {
            holder.mLastUpdate.setText("Last Updated :   -");
        } else {
            GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("Asia/Kolkata"));
            c.setTimeInMillis(timeStamp);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);

            String dateStr = sdf.format(c.getTime());

            holder.mLastUpdate.setText("Last Updated : " + dateStr);
        }
        if(mProjectTypeModel == null || mProjectTypeModel.mDisplayProjectIcon == null || !mProjectTypeModel.mDisplayProjectIcon ) {
            holder.projectIconLayout.setVisibility(View.GONE);
        } else {
            ProjectIconInfo projectIconInfo = null;
            if(mProjectTypeModel!= null && mProjectTypeModel.mProjectIconInfo != null) {
                projectIconInfo = mProjectTypeModel.mProjectIconInfo;
            }
            String projectIconUrl = Utils.getInstance().getProjectIcon(mProjectList.get(position), mAppId, projectIconInfo );
            ImageUtils.setImage(projectIconUrl, holder.mProjectIcon, R.drawable.splash_app_logo);
        }
    }

    @Override
    public int getItemCount() {
        return mProjectList.size();
    }

    public class ProjectListViewHolder extends RecyclerView.ViewHolder {

        public TextView mName;
        public TextView mLastUpdate;
//        public TextView mSubtext;
        public ImageView mProjectIcon;
        public RelativeLayout projectIconLayout;
        public ProjectListViewHolder(View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.project_list_item_name);
            mProjectIcon = itemView.findViewById(R.id.project_icon);
            mLastUpdate = itemView.findViewById(R.id.project_list_item_count);
            projectIconLayout = itemView.findViewById(R.id.project_image_layout);
//            mSubtext = itemView.findViewById(R.id.project_list_subtext);
        }
    }

    @Override
    public String getSectionTitle(int position) {
        return String.valueOf(position + 1);
    }
}
