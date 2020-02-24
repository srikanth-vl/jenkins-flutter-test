package com.vassar.unifiedapp.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.vassar.unifiedapp.R;

public class OpenCameraImageThumbnail extends FrameLayout {

    private View mRoot;
    private ImageView mImgPhoto;
    private ImageButton mBtnClose;
    private Uri imageUri;

    private Context mContext;

    public OpenCameraImageThumbnail(final Context context) {
        this(context, null);
    }

    public OpenCameraImageThumbnail(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OpenCameraImageThumbnail(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        if (isInEditMode())
            return;

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View customView = null;

        if (inflater != null)
            customView = inflater.inflate(R.layout.open_camera_preview_thumbnail, this);

        if (customView == null)
            return;

        mRoot = customView.findViewById(R.id.root);
        mImgPhoto = (ImageView) customView.findViewById(R.id.img_photo);
        mBtnClose = (ImageButton) customView.findViewById(R.id.btn_close);
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public Uri getImageUri()
    {
        return imageUri;
    }

    public View getRoot() {
        return mRoot;
    }

    public ImageView getImgPhoto() {
        return mImgPhoto;
    }

    public ImageButton getBtnClose() {
        return mBtnClose;
    }
}
