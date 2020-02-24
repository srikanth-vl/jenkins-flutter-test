package com.vassar.unifiedapp.asynctask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.vassar.unifiedapp.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.widget.Toast.LENGTH_SHORT;

public class TimestampImageTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private File mFile;

    public TimestampImageTask(Context context, File file) {
        this.mContext = context;
        this.mFile = file;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        timestampItAndSave();
        return null;
    }

    private File timestampItAndSave() {

        if (mFile == null) {
            Log.e("FILE PATH", "NO file is there");
            return null;
        }
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        try (InputStream is = new FileInputStream(mFile)) {

            bitmap = BitmapFactory.decodeStream(is);

        } catch (FileNotFoundException e) {
//            Toast.makeText(this, "file not found", LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(bitmap == null) {
            return null;
        }
        Bitmap dest = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime = sdf.format(Calendar.getInstance().getTime()); // reading local time in the system

        int spSize = 25;//your sp size
        // Convert the sp to pixels
        float scaledTextSize = spSize * mContext.getResources().getDisplayMetrics().scaledDensity;

        Canvas cs = new Canvas(dest);
        Paint tPaint = new Paint();
        tPaint.setTextSize(scaledTextSize);
        tPaint.setColor(Color.BLUE);
        tPaint.setStyle(Paint.Style.FILL);
        tPaint.setTextAlign(Paint.Align.LEFT);
        cs.drawBitmap(bitmap, 0f, 0f, null);
        float height = tPaint.measureText("yY");
        cs.drawText(dateTime, 20f, height + 20f, tPaint);
        try {

            FileOutputStream fist = new FileOutputStream(new File(mFile.getAbsolutePath()));
            dest.compress(Bitmap.CompressFormat.JPEG, 100, fist);
            fist.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Utils.logInfo("ImageSizeAFterTimeStamp", String.valueOf(mFile.length()/Math.pow(1024,2)));
        return mFile;
    }
}
