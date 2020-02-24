package com.vassar.unifiedapp.asynctask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.utils.CompareAppVersionUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * AsyncTask class for check updated version of Application  from the playstore
 */

public class ForceUpdateAsync extends AsyncTask<Void, String, String> {

    private Context context;
    private String currentVersion;
    public ForceUpdateAsync(Context context, String currentVersion) {
        this.context=context;
        this.currentVersion=currentVersion;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String newVersion = null;
        try {
            //HTML Parsing of the data coming from the url
            Document document = Jsoup.connect(context.getString(R.string.play_store_base_url) + context.getPackageName() + (context.getString(R.string.play_store_endpoint_lang)))
                    .timeout(30000)
                    .userAgent(context.getString(R.string.play_store_user_agent))
                    .referrer(context.getString(R.string.play_store_referred_url))
                    .get();
            if (document != null) {
                Elements element = document.getElementsContainingOwnText(context.getString(R.string.play_store_current_version));
                for (Element ele : element) {
                    if (ele.siblingElements() != null) {
                        Elements sibElemets = ele.siblingElements();
                        for (Element sibElemet : sibElemets) {
                            newVersion = sibElemet.text();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newVersion;
    }
    @Override
    protected void onPostExecute(String onlineVersion) {
        super.onPostExecute(onlineVersion);
        if (onlineVersion != null && !onlineVersion.isEmpty()) {
            int res=new CompareAppVersionUtil().compareAppVersion(currentVersion,onlineVersion);

            if (res==0 || res==1) {

                //check based on currentVer and onlineVer

            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle(context.getString(R.string.update));
                alertDialog.setMessage(context.getString(R.string.new_update_is_available));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.update), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.market_details_url) + context.getPackageName())));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.play_store_base_url) + context.getPackageName())));

                        }
                    }
                });

                alertDialog.show();
                alertDialog.setCancelable(false);


            }
        }
        Log.d("update", "Current version " + currentVersion + "playstore version " + onlineVersion);
    }
}