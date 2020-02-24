package com.vassar.unifiedapp.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.LanguageInfo;

import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends PreferenceFragment {

    public static final String PREF_LANGUAGE="pref_language";
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    List<String> name = new ArrayList<>();
    List<String> locale = new ArrayList<>();
    List<LanguageInfo> supportedLanguages = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefences);
        //method for getting dynamic language data and responsible for change language
        initLanguageSetting();
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals(PREF_LANGUAGE)) {
                        String locale = sharedPreferences.getString(key, getString(R.string.setting_lang_pref_def_values));
                        Log.i("localeFrag", locale);
                        UAAppContext.getInstance().changeLanguage(locale);
                        //prompting a dialog for restarting the application on Language selection
                        restartApplication();
                    }
                }
            };
        }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);

    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private void initLanguageSetting() {
        AppMetaData appMetaData = UAAppContext.getInstance().getAppMDConfig();
        if (appMetaData != null) {
            supportedLanguages = UAAppContext.getInstance().getAppMDConfig().getEnabledLanguages();
            for (LanguageInfo languageInfo : supportedLanguages) {
                locale.add(languageInfo.getLocale());
                name.add(languageInfo.getName());
            }
            CharSequence[] cslocale = locale.toArray(new CharSequence[locale.size()]);
            CharSequence[] csname = name.toArray(new CharSequence[name.size()]);

            Preference prefSummary = findPreference(PREF_LANGUAGE);
            prefSummary.setSummary(getString(R.string.setting_lang_pref_summary));
            ListPreference listPreference = (ListPreference) findPreference(PREF_LANGUAGE);
            listPreference.setEntries(csname);
            listPreference.setEntryValues(cslocale);
            listPreference.setDefaultValue(name.get(0));
        }
    }

    private void restartApplication(){
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(getActivity().getString(R.string.setting_lang_pref));
        alertDialog.setMessage(getActivity().getString(R.string.setting_please_restart_your_application));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getActivity().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    System.exit(0);
                } catch (Exception e) {

                }
            }
        });

        alertDialog.show();
        alertDialog.setCancelable(false);
    }

}
