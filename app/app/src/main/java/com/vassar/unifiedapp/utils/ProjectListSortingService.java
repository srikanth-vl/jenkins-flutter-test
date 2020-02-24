package com.vassar.unifiedapp.utils;

import com.vassar.unifiedapp.model.Project;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProjectListSortingService {

    public static List<Project> mList;
    public static String mSortType;
    public static Double mUserLat;
    public static Double mUserLong;

    public static void sort() {
        switch (mSortType) {
            case Constants.PROJECT_LIST_ALPHABETICAL_SORTING:
                // The list is shown in alphabetical order
                if (mList.size() > 0) {
                    Collections.sort(mList, new Comparator<Project>() {
                        @Override
                        public int compare(Project o1, Project o2) {
                            return o1.getProjectName().compareTo(o2.getProjectName());
                        }
                    });
                }
                break;

            case Constants.PROJECT_LIST_LAST_UPDATED_ASCENDING_SORTING:
                // The list is shown in alphabetical order
                if (mList.size() > 0) {
                    Collections.sort(mList, new Comparator<Project>() {
                        @Override
                        public int compare(Project o1, Project o2) {
                            int val =  o1.getLastSyncTimestamp().compareTo(o2.getLastSyncTimestamp());
                            if(val != 0) {
                                return val;
                            }
                            return o1.getProjectName().compareTo(o2.getProjectName());
                        }
                    });
                }
                break;

            case Constants.PROJECT_LIST_LAST_UPDATED_DESCENDING_SORTING:
                // The list is shown in alphabetical order
                if (mList.size() > 0) {
                    Collections.sort(mList, new Comparator<Project>() {
                        @Override
                        public int compare(Project o1, Project o2) {
                            int val =  o1.getLastSyncTimestamp().compareTo(o2.getLastSyncTimestamp());
                            if(val != 0) {
                                return val;
                            }
                            return o1.getProjectName().compareTo(o2.getProjectName());
                        }
                    });
                }
                Collections.reverse(mList);
                break;

            case Constants.PROJECT_LIST_NEAREST_PROJECTS_FIRST:
                if (mUserLat != null && mUserLat != 0.0 &&
                        mUserLong != null && mUserLong != 0.0) {
                    if (mList.size() > 0) {
                        Collections.sort(mList, new Comparator<Project>() {
                            @Override
                            public int compare(Project o1, Project o2) {
                                double distanceToPlace1 = 0.0;
                                double distanceToPlace2 = 0.0;

                                if (o1.getLatitude() == null || o1.getLongitude() == null || o1.getLatitude().isEmpty() || o1.getLongitude().isEmpty()) {
                                    distanceToPlace1 = distance(mUserLat, mUserLong, 0.0, 0.0);
                                } else if (o1.getLatitude() != null && !o1.getLatitude().isEmpty() && o1.getLongitude() != null && !o1.getLongitude().isEmpty()) {
                                    distanceToPlace1 = distance(mUserLat, mUserLong, Double.parseDouble(o1.getLatitude()), Double.parseDouble(o1.getLongitude()));
                                }
                                if (o2.getLatitude() == null || o2.getLongitude() == null || o2.getLatitude().isEmpty() || o2.getLongitude().isEmpty()) {
                                    distanceToPlace2 = distance(mUserLat, mUserLong, 0.0, 0.0);
                                } else if (o2.getLatitude() != null && !o2.getLatitude().isEmpty() && o2.getLongitude() != null && !o2.getLongitude().isEmpty()) {
                                    distanceToPlace2 = distance(mUserLat, mUserLong, Double.parseDouble(o2.getLatitude()), Double.parseDouble(o2.getLongitude()));
                                }
                                if (distanceToPlace1 == distanceToPlace2)
                                    return 0;
                                else if (distanceToPlace1 > distanceToPlace2)
                                    return 1;
                                else
                                    return -1;
                            }

                        });
                    }
                }
                break;
        }
    }

    private static double distance(Double fromLat, Double fromLon, Double toLat, Double toLon) {
        /*double radius = 6378137;   // approximate Earth radius, *in meters*

        double angle = 2 * Math.asin( Math.sqrt(
                Math.pow(Math.sin(deltaLat/2), 2) +
                        Math.cos(fromLat) * Math.cos(toLat) *
                                Math.pow(Math.sin(deltaLon/2), 2) ) );
        return radius * angle;*/
        double deltaLat = toLat - fromLat;
        double deltaLon = toLon - fromLon;
        return Math.sqrt(Math.pow(deltaLat,2) + Math.pow(deltaLon, 2));
    }
}
