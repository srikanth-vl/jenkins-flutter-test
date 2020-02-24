package com.vassarlabs.proj.uniapp.dashboard.service.impl;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import com.vassarlabs.proj.uniapp.constants.CommonConstants;

@Service
public class AppInstallationFromJsoup {

	public String appInstallationsCount(String packageName) throws IOException{
		if(packageName == null || packageName.isEmpty()) {
			return CommonConstants.NA;
		}
		String url = "https://play.google.com/store/apps/details?id="+packageName+"&hl=en";
		Connection.Response loginForm = Jsoup.connect(url)
				.ignoreContentType(true)
				.method(Connection.Method.GET)
				.execute();

		Document doc = loginForm.parse();
		String x = doc.select("span[class=htlgb]").text();
		String[] list = x.split(" ");
		String installations = list[9];
		return installations;
	}
}
