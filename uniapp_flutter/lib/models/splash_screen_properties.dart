class SplashScreenproperties {
  String splashicon;
  String splashduration;
  String splashbackground;
  String loginicon;

  SplashScreenproperties(
      {this.splashicon,
        this.splashduration,
        this.splashbackground,
        this.loginicon});

  SplashScreenproperties.fromJson(Map<String, dynamic> json) {
    splashicon = json['splashicon'];
    splashduration = json['splashduration'];
    splashbackground = json['splashbackground'];
    loginicon = json['loginicon'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['splashicon'] = this.splashicon;
    data['splashduration'] = this.splashduration;
    data['splashbackground'] = this.splashbackground;
    data['loginicon'] = this.loginicon;
    return data;
  }
}
