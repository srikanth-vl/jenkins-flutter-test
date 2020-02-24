import 'dart:async';

import 'package:flutter/material.dart';
import 'app_translations.dart';
import '../ua_app_context.dart';

class AppTranslationsDelegate extends LocalizationsDelegate<AppTranslations> {
  final Locale newLocale;

  const AppTranslationsDelegate({this.newLocale});

  @override
  bool isSupported(Locale locale) {
    List<String> supportedLanguagesCodes = List();
    if(UAAppContext.getInstance().appMDConfig != null && UAAppContext.getInstance().appMDConfig.enabledLanguages != null) {
      supportedLanguagesCodes = UAAppContext.getInstance().appMDConfig.enabledLanguages.map((a) => a.locale).toList();
    } else {
      supportedLanguagesCodes = UAAppContext.getInstance().supportedLanguagesCodes;
    }
    return supportedLanguagesCodes.contains(locale.languageCode);
//    return UAAppContext.getInstance().supportedLanguagesCodes.contains(locale.languageCode);
  }

  @override
  Future<AppTranslations> load(Locale locale) {
    return AppTranslations.load(newLocale ?? locale);
  }

  @override
  bool shouldReload(LocalizationsDelegate<AppTranslations> old) {
    return true;
  }
}