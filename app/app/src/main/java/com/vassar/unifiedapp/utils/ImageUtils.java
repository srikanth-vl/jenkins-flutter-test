package com.vassar.unifiedapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.api.NewImageDownloaderService;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.model.IncomingImage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageUtils {

    public static void setImage(String imageUrl, ImageView imageView, int defaultImageUrl) {

        if (imageUrl != null) {
            IncomingImage image = UAAppContext.getInstance().getDBHelper()
                    .getIncomingImageWithUrl(imageUrl);
            if (image != null) {
                if (image.getImageLocalPath() == null || image.getImageLocalPath().isEmpty()) {
                    // Setting the default image, while the icon downloads
                    imageView.setImageResource(defaultImageUrl);
                    // Image download failed the first time, download again
                    NewImageDownloaderService newImageDownloaderService = new NewImageDownloaderService(image);
                    newImageDownloaderService.execute();
                } else {
                    File imgFile = new  File(image.getImageLocalPath());
                    if(imgFile.exists()){
                        if (imgFile.length() == 0) {
                            // Setting the default image, while the icon downloads
                            imageView.setImageResource(defaultImageUrl);
                            // Image download failed the first time, download again
                            NewImageDownloaderService newImageDownloaderService = new NewImageDownloaderService(image);
                            newImageDownloaderService.execute();
                        } else {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            imageView.setImageBitmap(myBitmap);
                        }
                    } else {
                        // Image does not exist
                        imageView.setImageResource(defaultImageUrl);
                    }
                }
            } else {
                IncomingImage projectIcon = new IncomingImage(null,
                        null, imageUrl);
                NewImageDownloaderService newImageDownloaderService = new NewImageDownloaderService(projectIcon);
                newImageDownloaderService.execute();
                // Image does not exist
                imageView.setImageResource(defaultImageUrl);
            }
        } else {
            imageView.setImageResource(defaultImageUrl);
        }
    }
}
