package com.vassar.unifiedapp.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.model.FormMedia;
import com.vassar.unifiedapp.model.TransactionLogObject;
import com.vassar.unifiedapp.ui.ImagePreviewActivity;
import com.vassar.unifiedapp.ui.VideoPreviewActivity;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.MediaType;
import com.vassar.unifiedapp.utils.Utils;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TransactionLogAdapter extends RecyclerView.Adapter<TransactionLogAdapter.TransactionLogViewHolder> {

    private String appId;
    private List<TransactionLogObject> transactionLogList;
    private Activity activity;

    public TransactionLogAdapter(String appId, List<TransactionLogObject> projectSubmissionList, Activity activity) {
        this.transactionLogList = projectSubmissionList;
        this.activity = activity;
        this.appId = appId;
    }

    @NonNull
    @Override
    public TransactionLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = ((LayoutInflater) UAAppContext.getInstance().getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.transaction_log_item_layout, parent, false);
        return new TransactionLogAdapter.TransactionLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionLogViewHolder holder, int position) {
        if(transactionLogList.isEmpty() || transactionLogList.isEmpty()) {
            return;
        }
        TransactionLogObject transactionLogObject = transactionLogList.get(position);
        if(transactionLogObject == null) {
            Utils.logError("TRANSACTION_LOG", "Error getting element at position " + position + " from transaction log " +
                    "list - " + transactionLogList);
            return;
        }

        holder.getProjectName().setText(transactionLogObject.getProjectName());
        Date date = new Date(Long.parseLong(transactionLogObject.getSubmissionTimestamp()));
        holder.getTimestamp().setText(String.valueOf(date));
        holder.getTextSubmissions().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialogForTextSubmission(transactionLogObject);
            }
        });
        Map<Integer, List<String>> mediaTypeToUUIDs = transactionLogObject.getMediaUUIDs();
        if(mediaTypeToUUIDs == null || mediaTypeToUUIDs.isEmpty()) {
            holder.getMediaLayout().setVisibility(View.GONE);
        } else {
            loadMedia(mediaTypeToUUIDs, holder.getMediaSubmissions());
            
        }
    }

    private void loadMedia(Map<Integer, List<String>> mediaTypeToUUIDs, LinearLayout layout) {

        int i = 0;
        for(Integer mediaType : mediaTypeToUUIDs.keySet()) {
            List<String> mediaUUIDs = mediaTypeToUUIDs.get(mediaType);
            for(String uuid : mediaUUIDs) {
                FormMedia formMedia = UAAppContext.getInstance().getDBHelper().getFormMedia(uuid, appId, UAAppContext.getInstance().getUserID());
                if (formMedia != null) {
                    ImageView imageView = new ImageView(activity);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) activity.getResources().getDimension(R.dimen.dimen_60dp), ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(8, 0, 8, 0);
                    imageView.setLayoutParams(params);

                    if (mediaType == MediaType.IMAGE.getValue()) {
                        File imgFile = new File(formMedia.getLocalPath());
                        if (imgFile != null) {
                            Picasso.get().load(imgFile).resize(150, 150).into(imageView);
                        }

                        imageView.setAdjustViewBounds(true);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Opening activity that shows the image
                                Intent intent = new Intent(activity, ImagePreviewActivity.class);
                                intent.putExtra("image_path", formMedia.getLocalPath());
                                activity.startActivity(intent);
                            }
                        });
                    } else if (mediaType == MediaType.VIDEO.getValue()) {
                        imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.play_video));
                        imageView.setPadding(16, 16, 16, 16);
                        imageView.setAdjustViewBounds(true);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Opening activity that shows the image
                                Intent intent = new Intent(activity, VideoPreviewActivity.class);
                                intent.putExtra("video_path", formMedia.getLocalPath());
                                activity.startActivity(intent);
                            }
                        });
                    }
                    layout.addView(imageView, i++);
                }
            }
        }
    }


    private void createDialogForTextSubmission(TransactionLogObject transactionLogObject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(activity.getResources().getString(R.string.text_submission_title));

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        View view = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.transaction_log_text_submission_dialog_layout, null, false);

        LinearLayout listOfSubmissions = (LinearLayout) view.findViewById(R.id.transaction_log_text_submission_layout);

        populateSubmissionList(listOfSubmissions, transactionLogObject.getFields());

        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void populateSubmissionList(LinearLayout linearLayout, Map<String, String> keyToValueObject) {

        for (String key : keyToValueObject.keySet()) {
            LinearLayout innerLinearLayout = new LinearLayout(activity);
            innerLinearLayout.setWeightSum(2);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            innerLinearLayout.setLayoutParams(params);

            LinearLayout.LayoutParams textviewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            TextView keyView = new TextView(activity);
            keyView.setLayoutParams(textviewParams);
            key.replaceAll("_", " ");
            keyView.setText(Constants.capitalize(key) + " : ");

            TextView valueView = new TextView(activity);
            valueView.setLayoutParams(textviewParams);
            valueView.setText(keyToValueObject.get(key));

            innerLinearLayout.addView(keyView);
            innerLinearLayout.addView(valueView);

            linearLayout.addView(innerLinearLayout);
        }
    }

    @Override
    public int getItemCount() {
        return transactionLogList.size();
    }

    public class TransactionLogViewHolder
            extends RecyclerView.ViewHolder {

        private TextView mProjectName;
        private TextView mTimestamp;
        private Button mTextSubmissions;
        private LinearLayout mMediaSubmissions;
        private RelativeLayout mMediaLayout;

        public TransactionLogViewHolder(View itemView) {
            super(itemView);
            mProjectName = itemView.findViewById(R.id.transaction_log_project_name);
            mTimestamp = itemView.findViewById(R.id.transaction_log_project_submission_ts);
            mTextSubmissions = itemView.findViewById(R.id.transaction_log_show_text_values);
            mMediaSubmissions = itemView.findViewById(R.id.transaction_log_media_scrollview);
            mMediaLayout = itemView.findViewById(R.id.transaction_log_media_layout);
        }

        public TextView getProjectName() {
            return mProjectName;
        }

        public void setProjectName(TextView mProjectName) {
            this.mProjectName = mProjectName;
        }

        public TextView getTimestamp() {
            return mTimestamp;
        }

        public void setTimestamp(TextView mTimestamp) {
            this.mTimestamp = mTimestamp;
        }

        public Button getTextSubmissions() {
            return mTextSubmissions;
        }

        public void setTextSubmissions(Button mTextSubmissions) {
            this.mTextSubmissions = mTextSubmissions;
        }

        public LinearLayout getMediaSubmissions() {
            return mMediaSubmissions;
        }

        public void setMediaSubmissions(LinearLayout mMediaSubmissions) {
            this.mMediaSubmissions = mMediaSubmissions;
        }

        public RelativeLayout getMediaLayout() {
            return mMediaLayout;
        }

        public void setMediaLayout(RelativeLayout mMediaLayout) {
            this.mMediaLayout = mMediaLayout;
        }
    }
}
