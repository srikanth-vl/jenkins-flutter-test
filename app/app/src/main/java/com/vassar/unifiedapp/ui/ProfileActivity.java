package com.vassar.unifiedapp.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.Header;
import com.vassar.unifiedapp.model.UserMetaData;
import com.vassar.unifiedapp.utils.StringUtils;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    public LinearLayout mProfileDetailLayout;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mProfileDetailLayout = findViewById(R.id.profile_header_layout);
        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        final Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        final CircularImageView profileCircularImage = findViewById(R.id.profile_circular_image);

        collapsingToolbarLayout.setTitle(" ");

        AppBarLayout mAppBarLayout = findViewById(R.id.app_bar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    profileCircularImage.setVisibility(View.GONE);
                    collapsingToolbarLayout.setTitle(getResources().getString(R.string.user_profile));
                    //menu.findItem(R.id.action_info).setIcon(ContextCompat.getDrawable(ScrollingActivity.this, R.drawable.photo));

                } else if (isShow) {
                    isShow = false;
                    profileCircularImage.setVisibility(View.VISIBLE);
                    Animation shake = AnimationUtils.loadAnimation(ProfileActivity.this, R.anim.scale_up_fast);
                    profileCircularImage.startAnimation(shake);
                    collapsingToolbarLayout.setTitle(" ");

                }
            }
        });
        renderProfileDetail();
    }

    private void renderProfileDetail() {
        AppMetaData appMetaData = UAAppContext.getInstance().getAppMDConfig();
        List<Header> headerList = appMetaData.getProfileConfig();
        UserMetaData userMetaData = UAAppContext.getInstance().getDBHelper().getUserMeta(UAAppContext.getInstance().getUserID());
        String userDetails = userMetaData.userDetails;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(userDetails);
        } catch (JSONException e) {
            Utils.logError("PROFILE_ACTIVITY", "Failed to parse Userdetails Json");
        }
        for (Header header : headerList) {
            String keyValue = null;
            try {
                keyValue = jsonObject.has(header.mIdentifier) ? jsonObject.getString(header.mIdentifier) : null;
            } catch (JSONException e) {
                Utils.logError("PROFILE_ACTIVITY", "Value for key " + header.mIdentifier + "not found");
            }
            loadHeaderView(mProfileDetailLayout, header, keyValue);

        }
    }


    private void loadHeaderView(LinearLayout layout, Header header, String valueText) {
        LayoutInflater mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.form_header_element, layout, false);
        TextView label = view.findViewById(R.id.form_header_label);
        TextView value = view.findViewById(R.id.form_header_value);
        String labelText = StringUtils.getTranslatedString(header.mValue);
        label.setText(labelText);
        value.setText(valueText);

        layout.addView(view);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Configuration config = newBase.getResources().getConfiguration();
            //Update your config with the Locale i. e. saved in SharedPreferences
            String locale = UAAppContext.getInstance().getLocale();
            config.setLocale(new Locale(locale));
            newBase = newBase.createConfigurationContext(config);
        }
        super.attachBaseContext(newBase);
    }
}