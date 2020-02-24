package com.vassar.unifiedapp.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.api.ChangePasswordService;
import com.vassar.unifiedapp.api.GetOtpService;
import com.vassar.unifiedapp.asynctask.DownloadImageIfFailedTask;
import com.vassar.unifiedapp.listener.ChangePasswordListener;
import com.vassar.unifiedapp.listener.GetOtpListener;
import com.vassar.unifiedapp.model.IncomingImage;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ForgotPasswordScreen extends BaseActivity {

    ImageView mForgotPasswordImage;
    Button mGetOtp, mSubmit;
    EditText mUsername, mOtp, mPassword, mConfirmPassword;
    TextView mResendOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_screen);

        initializeComponents();
        initializeViews();
        defineClickListeners();
    }

    private void initializeComponents() {
        initComponents(this);
        hideActionBar(this);
    }

    private void initializeViews() {
        IncomingImage loginIcon = mDBHelper.getImage(Constants.LOGIN_ICON_IMAGE);

        mUsername = (EditText) findViewById(R.id.activity_forgot_password_username);
        mOtp = (EditText) findViewById(R.id.activity_forgot_password_otp);
        mPassword = (EditText) findViewById(R.id.activity_forgot_password_new_password);
        mConfirmPassword = (EditText) findViewById(R.id.activity_forgot_password_confirm_password);
        mGetOtp = (Button) findViewById(R.id.activity_forgot_password_get_otp);
        mSubmit = (Button) findViewById(R.id.activity_forgot_password_submit);
        mForgotPasswordImage = (ImageView) findViewById(R.id.activity_forgot_password_imageview);
        mResendOtp = (TextView) findViewById(R.id.activity_forgot_password_resend_otp);

        if (loginIcon != null) {
            if (loginIcon.getImageLocalPath() == null || loginIcon.getImageLocalPath().isEmpty()) {
                // Setting the default image, while the icon downloads
                mForgotPasswordImage.setImageResource(R.drawable.login_image);
                // Image download failed the first time, download again
                ArrayList<IncomingImage> image = new ArrayList<>();
                image.add(loginIcon);
                showDialog(DIALOG_LOADING);
                DownloadImageIfFailedTask object = new DownloadImageIfFailedTask(this, mDBHelper, image);
                try {
                    object.execute().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dismissDialog(DIALOG_LOADING);
            } else {
                File imgFile = new  File(loginIcon.getImageLocalPath());
                if(imgFile.exists()){
                    if (imgFile.length() == 0) {
                        // Setting the default image, while the icon downloads
                        mForgotPasswordImage.setImageResource(R.drawable.login_image);
                        // Image download failed the first time, download again
                        ArrayList<IncomingImage> image = new ArrayList<>();
                        image.add(loginIcon);
                        showDialog(DIALOG_LOADING);
                        DownloadImageIfFailedTask object = new DownloadImageIfFailedTask(this, mDBHelper, image);
                        try {
                            object.execute().get();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        dismissDialog(DIALOG_LOADING);
                    } else {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        mForgotPasswordImage.setImageBitmap(myBitmap);
                    }
                } else {
                    // Default splash icon loaded
                    mForgotPasswordImage.setImageResource(R.drawable.login_image);
                }
            }
        } else {
            // Default background loaded
            mForgotPasswordImage.setImageResource(R.drawable.login_image);
        }
    }

    private void defineClickListeners() {

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Submit is clicked, change password!
                if (Utils.getInstance().isOnline(ForgotPasswordScreen.this)) {
                    changePassword();
                } else {
                    showErrorMessageAndFinishActivity(getResources().getString(R.string.CHECK_INTERNET_CONNECTION), false);
                }
            }
        });

        mGetOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call server to send OTP to user
                if(mUsername.getText().toString().isEmpty())
                    showErrorMessageAndFinishActivity(getResources().getString(R.string.MISSING_USERNAME), false);
                else
                {
                if (Utils.getInstance().isOnline(ForgotPasswordScreen.this)) {
                    sendOtp();
                } else {
                    showErrorMessageAndFinishActivity(getResources().getString(R.string.CHECK_INTERNET_CONNECTION), false);
                }
            }}
        });

        mResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Resend OTP
                if (Utils.getInstance().isOnline(ForgotPasswordScreen.this)) {
                    sendOtp();
                } else {
                    showErrorMessageAndFinishActivity(getResources().getString(R.string.CHECK_INTERNET_CONNECTION), false);
                }
            }
        });
    }

    public void sendOtp() {
        if (!mUsername.getText().toString().isEmpty()) {

            showDialog(DIALOG_LOADING);

            Map<String, String> requestParameters = prepareSendOtpParameters();

            GetOtpService getOtpService = new GetOtpService(new GetOtpListener() {

                @Override
                public void onGetOtpSuccessful() {
                    dismissDialog(DIALOG_LOADING);
                    showPostOtpSuccessfulViews();
                }

                @Override
                public void onGetOtpFailure(String message) {
                    dismissDialog(DIALOG_LOADING);
                    showErrorMessageAndFinishActivity(message, false);
                }
            });
            getOtpService.callGetOtpService(requestParameters);
        }
    }

    public void changePassword() {
        if (!mOtp.getText().toString().isEmpty()
                && !mPassword.getText().toString().isEmpty()
                && !mConfirmPassword.getText().toString().isEmpty()) {

            if (mPassword.getText().toString().equals(mConfirmPassword.getText().toString())) {
                showDialog(DIALOG_LOADING);

                Map<String, String> requestParameters = prepareChangePasswordParameters();

                ChangePasswordService changePasswordService = new ChangePasswordService(new ChangePasswordListener() {

                    @Override
                    public void onChangePasswordSuccessful() {
                        dismissDialog(DIALOG_LOADING);

                        showErrorMessageAndFinishActivity(getResources().getString(R.string.PASSWORD_CHANGED_SUCCESSFULLY), true);
                    }

                    @Override
                    public void onChangePasswordFailure(String message) {
                        dismissDialog(DIALOG_LOADING);
                        showErrorMessageAndFinishActivity(message, false);
                    }
                });
                changePasswordService.callGetOtpService(requestParameters);
            } else {
                showErrorMessageAndFinishActivity(getResources().getString(R.string.PASSWORDS_DONT_MATCH), false);
            }
        } else {
            showErrorMessageAndFinishActivity(getResources().getString(R.string.MANDATORY_FIELD_NOT_ENTERED), false);
        }
    }

    public void showPostOtpSuccessfulViews() {
        mUsername.setEnabled(false);
        mGetOtp.setVisibility(View.GONE);
        mOtp.setVisibility(View.VISIBLE);
        mPassword.setVisibility(View.VISIBLE);
        mConfirmPassword.setVisibility(View.VISIBLE);
        mResendOtp.setVisibility(View.VISIBLE);
        mSubmit.setVisibility(View.VISIBLE);
    }

    public Map<String, String> prepareSendOtpParameters() {
        HashMap<String, String> requestParameters = new HashMap<>();
        requestParameters.put(Constants.SEND_OTP_USERNAME, mUsername.getText().toString());
        requestParameters.put(Constants.SEND_OTP_SUPER_APP, Constants.SUPER_APP_ID);
        return requestParameters;
    }

    public Map<String, String> prepareChangePasswordParameters() {
        HashMap<String, String> requestParameters = new HashMap<>();
        requestParameters.put(Constants.CHANGE_PASSWORD_USERNAME, mUsername.getText().toString());
        requestParameters.put(Constants.CHANGE_PASSWORD_SUPER_APP, Constants.SUPER_APP_ID);
        requestParameters.put(Constants.CHANGE_PASSWORD_OTP, mOtp.getText().toString());
        requestParameters.put(Constants.CHANGE_PASSWORD_PASSWORD, mPassword.getText().toString());
        return requestParameters;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
