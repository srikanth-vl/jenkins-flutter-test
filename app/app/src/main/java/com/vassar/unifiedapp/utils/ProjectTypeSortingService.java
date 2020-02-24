package com.vassar.unifiedapp.utils;

import com.vassar.unifiedapp.model.ProjectTypeModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ProjectTypeSortingService {

    public static ArrayList<ProjectTypeModel> mList;
    public static String mSortType;

    public static void sort() {
        switch (mSortType) {
            case Constants.PROJECT_TYPE_ALPHABETICAL_SORTING:
                // The list is shown in alphabetical order
                if (mList.size() > 0) {
                    Collections.sort(mList, new Comparator<ProjectTypeModel>() {
                        @Override
                        public int compare(ProjectTypeModel o1, ProjectTypeModel o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                }
                break;

            case Constants.PROJECT_TYPE_CUSTOM_SORTING:
                if (mList.size() > 0) {
                    Collections.sort(mList, new Comparator<ProjectTypeModel>() {
                        @Override
                        public int compare(ProjectTypeModel o1, ProjectTypeModel o2) {
                            return o1.getOrder().compareTo(o2.getOrder());
                        }
                    });
                }
                break;
        }
    }
}
