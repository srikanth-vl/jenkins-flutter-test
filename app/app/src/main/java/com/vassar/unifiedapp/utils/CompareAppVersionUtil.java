package com.vassar.unifiedapp.utils;

public class CompareAppVersionUtil {

    public int compareAppVersion(String currentVersion,String onlineVersion){

        int res = 0;
        String[] strOnlineVer = onlineVersion.split("\\.");
        String[] strCurrentVersion = currentVersion.split("\\.");

        // To avoid IndexOutOfBounds
        int minIndex = Math.min(strOnlineVer.length, strCurrentVersion.length);
        int maxIndex = Math.max(strOnlineVer.length, strCurrentVersion.length);

        for (int i = 0; i < minIndex; i ++) {
            int intCurrentVersion = Integer.valueOf(strCurrentVersion[i]);
            int intOnlineVersion = Integer.valueOf(strOnlineVer[i]);

            if (intCurrentVersion < intOnlineVersion) {
                res = -1;
                break;
            } else if (intCurrentVersion > intOnlineVersion) {
                res = 1;
                break;
            }
        }
        if(res == 0) {
            if(maxIndex == minIndex) {
                res = 0;
            }
            else if(strCurrentVersion.length > strOnlineVer.length && Integer.valueOf(strCurrentVersion[minIndex])>0){
                    res = 1;
            } else if(strCurrentVersion.length < strOnlineVer.length && Integer.valueOf(strOnlineVer[minIndex]) > 0) {
                res = -1;
            } else {
                res = 0;
            }
        }

        return res;
    }
}
