package com.vassar.unifiedapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.ConfigFile;
import com.vassar.unifiedapp.model.DBProjectForm;
import com.vassar.unifiedapp.model.Entity;
import com.vassar.unifiedapp.model.FormMedia;
import com.vassar.unifiedapp.model.IncomingImage;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectIconInfo;
import com.vassar.unifiedapp.model.ProjectList;
import com.vassar.unifiedapp.model.ProjectListFieldModel;
import com.vassar.unifiedapp.model.ProjectSpecificForms;
import com.vassar.unifiedapp.model.ProjectSubmission;
import com.vassar.unifiedapp.model.ProjectTypeConfiguration;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.model.TransactionLogObject;
import com.vassar.unifiedapp.model.UserMetaData;
import com.vassar.unifiedapp.model.Validation;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.MediaType;
import com.vassar.unifiedapp.utils.ProjectSubmissionUploadStatus;
import com.vassar.unifiedapp.utils.ServerConstants;
import com.vassar.unifiedapp.utils.StringUtils;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UnifiedAppDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = Constants.APP_DATABASE_VERSION;
    private static final String DATABASE_NAME = Constants.APP_DATABASE_NAME;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SQL_CREATE_CONFIG_FILE_TABLE =
            "CREATE TABLE " + UnifiedAppDbContract.ConfigFilesEntry.TABLE_CONFIG + " (" +
                    UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_NAME + " TEXT," +
                    UnifiedAppDbContract.ConfigFilesEntry.COLUMN_USER_ID + " TEXT," +
                    UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_VERSION + " INTEGER," +
                    UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_FILE_CONTENT + " TEXT," +
                    UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_LAST_SYNC_TS + " LONG," +
                    " PRIMARY KEY (" + UnifiedAppDbContract.ConfigFilesEntry.COLUMN_USER_ID + ", " +
                    UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_NAME + ")" +
                    ")";
    private static final String SQL_DELETE_CONFIG_FILE_TABLE =
            "DROP TABLE IF EXISTS " + UnifiedAppDbContract.ConfigFilesEntry.TABLE_CONFIG;
    private static final String SQL_CREATE_PROJECT_FORM_TABLE =
            "CREATE TABLE " + UnifiedAppDbContract.ProjectFormTableEntry.TABLE_PROJECT_FORM + " (" +
                    UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_USER_ID + " TEXT," +
                    UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_APP_ID + " TEXT," +
                    UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_PROJECT_ID + " TEXT," +
                    UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_FORM_TYPE + " TEXT," +
                    UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_FORM_DATA + " TEXT," +
                    UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_VERSION + " INTEGER," +
                    UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_MD_INSTANCE_ID + " TEXT," +
                    " PRIMARY KEY (" + UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_USER_ID + ", " +
                    UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_APP_ID + "," +
                    UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_PROJECT_ID + "," +
                    UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_FORM_TYPE + "," +
                    UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_VERSION + ")" +
                    ")";
    private static final String SQL_DELETE_PROJECT_FORM_TABLE =
            "DROP TABLE IF EXISTS " + UnifiedAppDbContract.ProjectFormTableEntry.TABLE_PROJECT_FORM;
    private static final String SQL_CREATE_PROJECT_TABLE =
            "CREATE TABLE " + UnifiedAppDbContract.ProjectTableEntry.TABLE_PROJECT + " (" +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_APP_ID + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_USER_ID + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ID + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_NAME + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_LAT + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_LON + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_BBOX + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_CIRCLE_VALIDATION + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_LAST_SUB_DATE + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_STATE + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_EXTERNAL_PROJECT_ID + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_FIELDS + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_USER_TYPE + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_VALIDATIONS + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_SERVER_SYNC_TS + " LONG," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_SHOW_MAP + " BOOLEAN," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ASSINGED_STATUS + " BOOLEAN," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_GROUPING_DIMENSION_VALUES + " TEXT," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_LAST_UPDATED_TS + " LONG," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ICON + " TEXT," +
                    " PRIMARY KEY (" + UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_APP_ID + ", " +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_USER_ID + "," +
                    UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ID + ")" +
                    ")";
    private static final String SQL_CREATE_PROJECT_SUBMISSION_TABLE =
            "CREATE TABLE " + UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION + " (" +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID + " TEXT," +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID + " TEXT," +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_TYPE + " TEXT," +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID + " TEXT," +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FORM_ID + " TEXT," +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP + " LONG," +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS + " TEXT," +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_API + " TEXT," +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_MD_INSTANCE_ID + " TEXT," +
                    // TODO : The Upload Error - 1) Validation Error 2) Upload Error
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS + " INTEGER," +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RESPONSE + " TEXT," +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_SERVER_SYNC_TS + " LONG," +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RETRY_COUNT + " INTEGER," +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_ADDITIONAL_PROPERTIES + " TEXT," +
                    " PRIMARY KEY (" + UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID + ", " +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID + "," +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID + "," +
                    UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP + ")" +
                    ")";

    // TODO : Pick the latest submission among both the tables
    private static final String SQL_DELETE_PROJECT_TABLE =
            "DROP TABLE IF EXISTS " + UnifiedAppDbContract.ProjectTableEntry.TABLE_PROJECT;
    private static final String SQL_CREATE_USER_META_TABLE =
            "CREATE TABLE " + UnifiedAppDbContract.UserMetaEntry.TABLE_USER + " (" +
                    UnifiedAppDbContract.UserMetaEntry.COLUMN_USER_ID + " TEXT," +
                    UnifiedAppDbContract.UserMetaEntry.COLUMN_PASSWORD + " TEXT," +
                    UnifiedAppDbContract.UserMetaEntry.COLUMN_TOKEN + " TEXT," +
                    UnifiedAppDbContract.UserMetaEntry.COLUMN_LAST_NETWORK_SYNC_TIME + " TEXT," +
                    UnifiedAppDbContract.UserMetaEntry.COLUMN_LAST_LOGIN_TS + " LONG," +
                    UnifiedAppDbContract.UserMetaEntry.COLUMN_IS_LOGGEN_IN + " BOOLEAN," +
                    UnifiedAppDbContract.UserMetaEntry.COLUMN_USER_DETAILS + " TEXT," +
                    " PRIMARY KEY (" + UnifiedAppDbContract.UserMetaEntry.COLUMN_USER_ID + ")" +
                    ")";
    private static final String SQL_DELETE_USER_META_TABLE =
            "DROP TABLE IF EXISTS " + UnifiedAppDbContract.UserMetaEntry.TABLE_USER;
    private static final String SQL_CREATE_INCOMING_IMAGES_TABLE =
            "CREATE TABLE " + UnifiedAppDbContract.IncomingImagesEntry.TABLE_IMAGES + " (" +
                    UnifiedAppDbContract.IncomingImagesEntry._ID + " INTEGER PRIMARY KEY," +
                    UnifiedAppDbContract.IncomingImagesEntry.COLUMN_IMAGE_TYPE + " TEXT," +
                    UnifiedAppDbContract.IncomingImagesEntry.COLUMN_IMAGE_URL + " TEXT," +
                    UnifiedAppDbContract.IncomingImagesEntry.COLUMN_IMAGE_LOCAL_PATH + " TEXT)";
    private static final String SQL_DELETE_INCOMING_IMAGES_TABLE =
            "DROP TABLE IF EXISTS " + UnifiedAppDbContract.IncomingImagesEntry.TABLE_IMAGES;
    private static final String SQL_DELETE_PROJECT_SUBMISSION_TABLE =
            "DROP TABLE IF EXISTS " + UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION;
    private static final String SQL_CREATE_FORM_MEDIA_TABLE =
            "CREATE TABLE " + UnifiedAppDbContract.FormMediaEntry.TABLE_FORM_MEDIA + " (" +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_APP_ID + " TEXT," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_USER_ID + " TEXT," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_UUID + " TEXT," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_SUBMISSION_TIMESTAMP + " LONG," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_PROJECT_ID + " TEXT," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_LOCAL_PATH + " TEXT," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_BITMAP + " BLOB," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_HAS_GEOTAG + " TEXT," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_LATITUDE + " DOUBLE," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_LONGITUDE + " DOUBLE," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_GPS_ACCURACY + " DOUBLE," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_TYPE + " INTEGER," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_REQUEST_STATUS + " INTEGER," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_SUBTYPE + " INTEGER," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_EXTENSION + " TEXT," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_CLICK_TS + " LONG," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_UPLOAD_RETRIES + " INTEGER," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_ACTION_TYPE + " INTEGER, " +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_UPLOAD_TIMESTAMP + " LONG," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_ADDITIONAL_PROPERTIES + " TEXT," +
                    " PRIMARY KEY (" + UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_APP_ID + "," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_USER_ID + "," +
                    UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_UUID + "))";

    private static final String SQL_CREATE_ENTITY_META_TABLE =
            "CREATE TABLE " + UnifiedAppDbContract.EntityMetaEntry.TABLE_ENTITY_METADATA + " (" +
                    UnifiedAppDbContract.EntityMetaEntry.COLUMN_SUPER_APP_ID + " TEXT," +
                    UnifiedAppDbContract.EntityMetaEntry.COLUMN_APP_ID + " TEXT," +
                    UnifiedAppDbContract.EntityMetaEntry.COLUMN_PROJECT_ID + " TEXT," +
                    UnifiedAppDbContract.EntityMetaEntry.COLUMN_USER_ID + " TEXT," +
                    UnifiedAppDbContract.EntityMetaEntry.COLUMN_PARENT_ENTITY + " TEXT," +
                    UnifiedAppDbContract.EntityMetaEntry.COLUMN_ENTITY_NAME + " TEXT," +
                    UnifiedAppDbContract.EntityMetaEntry.COLUMN_ELEMENTS + " TEXT," +
                    UnifiedAppDbContract.EntityMetaEntry.COLUMN_INSERT_TIMESTAMP + " LONG," +
                    " PRIMARY KEY (" + UnifiedAppDbContract.EntityMetaEntry.COLUMN_SUPER_APP_ID + "," +
                    UnifiedAppDbContract.EntityMetaEntry.COLUMN_APP_ID + "," +
                    UnifiedAppDbContract.EntityMetaEntry.COLUMN_USER_ID + "," +
                    UnifiedAppDbContract.EntityMetaEntry.COLUMN_PROJECT_ID + "," +
                    UnifiedAppDbContract.EntityMetaEntry.COLUMN_PARENT_ENTITY + "," +
                    UnifiedAppDbContract.EntityMetaEntry.COLUMN_ENTITY_NAME + "))";

    private static final String SQL_DELETE_ENTITY_META_TABLE =
            "DROP TABLE IF EXISTS " + UnifiedAppDbContract.EntityMetaEntry.TABLE_ENTITY_METADATA;


    private static final String SQL_DELETE_MEDIA_IMAGES_TABLE =
            "DROP TABLE IF EXISTS " + UnifiedAppDbContract.FormMediaEntry.TABLE_FORM_MEDIA;

    private Context context;

    public UnifiedAppDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Creating required tables
        sqLiteDatabase.execSQL(SQL_CREATE_CONFIG_FILE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_USER_META_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_INCOMING_IMAGES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PROJECT_SUBMISSION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PROJECT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PROJECT_FORM_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FORM_MEDIA_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_ENTITY_META_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        sqLiteDatabase.execSQL(SQL_DELETE_USER_META_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_CONFIG_FILE_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_INCOMING_IMAGES_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_PROJECT_SUBMISSION_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_PROJECT_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_PROJECT_FORM_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_MEDIA_IMAGES_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_ENTITY_META_TABLE);
        // Create new tables
        onCreate(sqLiteDatabase);

        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.commit();
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public int getTableCount(String TABLE_NAME) {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public long addToDatabase(String tableName, ContentValues values) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Insert the new row, returning the primary key value of the new row
        Utils.getInstance().showLog("DB :", "Added");
        return db.insert(tableName, null, values);
    }

    public int addOrUpdateToUserMDTable(ContentValues values) {

        String userId = String.valueOf(values.get(UnifiedAppDbContract.UserMetaEntry.COLUMN_USER_ID));

        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        int id = -1;
        String whereClause = UnifiedAppDbContract.UserMetaEntry.COLUMN_USER_ID + " = ?";
        id = db.update(UnifiedAppDbContract.UserMetaEntry.TABLE_USER, values, whereClause,
                new String[]{userId});
        if (id == 0) {
            id = (int) db.insert(UnifiedAppDbContract.UserMetaEntry.TABLE_USER, null, values);
        }
        return id;
    }

    public long addOrUpdateToProjectTableWithTS(String tableName, ContentValues values) {

        String appId = String.valueOf(values.get(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_APP_ID));
        String userId = String.valueOf(values.get(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_USER_ID));
        String projectId = String.valueOf(values.get(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ID));

        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        int id = -1;
        String whereClause = UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_USER_ID
                + " = ? AND " + UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_APP_ID + " = ? AND "
                + UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ID + " = ?";
        id = db.update(tableName, values, whereClause,
                new String[]{userId, appId, projectId});
        if (id == 0) {
            id = (int) db.insert(tableName, null, values);
        }
        return id;
    }

    /**
     * Returns map of Project Id -> Last Sync Timestamp of that project
     *
     * @param userId
     * @param appId
     * @param assignedStatus
     * @return
     */
    public Map<String, Long> getProjectIdToLastSyncTsMap(String userId, String appId, boolean assignedStatus) {
        System.out.println("assignedStatus:: " + assignedStatus);
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String, Long> projectIdToTs = new HashMap<>();

        String[] projection = {
                UnifiedAppDbContract.ProjectTableEntry.COLUMN_SERVER_SYNC_TS,
                UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ID
        };

        String selection = UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_PROJECT_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_PROJECT_APP_ID + " = ?" +
                (assignedStatus ? " AND "
                        + UnifiedAppDbContract.ProjectTableEntry
                        .COLUMN_PROJECT_ASSINGED_STATUS + " = 1" : "");
        System.out.println("QUERY:: " + selection);
        String[] selectionArgs = {userId, appId};

        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectTableEntry.TABLE_PROJECT,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            String projectId = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectTableEntry.COLUMN_PROJECT_ID));
            Long timestamp = cursor.getLong(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectTableEntry.COLUMN_SERVER_SYNC_TS));
            projectIdToTs.put(projectId, timestamp);
        }

        cursor.close();
        return projectIdToTs;
    }

    /**
     * Returns all projects assigned to the user
     * @param userId
     * @param appId
     * @return
     */
    public ProjectList getProjectsForUser(String userId, String appId) {
        ProjectList projectList = new ProjectList();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_PROJECT_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_PROJECT_APP_ID + " = ?";
        String[] selectionArgs = {userId, appId};

        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectTableEntry.TABLE_PROJECT,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {

            String userType = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectTableEntry.COLUMN_PROJECT_USER_TYPE));

            if (!projectList.mUserTypes.contains(userType))
                projectList.mUserTypes.add(userType);

            Project project = getProjectObjectFromDB(cursor, appId);
            project.setUserType(userType);
            projectList.mLastSyncTime = cursor.getLong(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectTableEntry.COLUMN_SERVER_SYNC_TS));
            projectList.mShouldShowMap = (cursor.getInt(cursor.getColumnIndex(UnifiedAppDbContract
                    .ProjectTableEntry.COLUMN_SHOW_MAP)) == 1);
            projectList.mProjects.add(project);
        }
        cursor.close();

        return projectList;
    }

    /**
     * Utility method to create @{@link Project} object from DB row
     *
     * @param cursor
     * @param appId
     * @return
     */
    private Project getProjectObjectFromDB(Cursor cursor, String appId) {

        Project project = new Project();

        RootConfig rootConfig = UAAppContext.getInstance().getRootConfig();

        project.setProjectId(cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_PROJECT_ID)));
        project.setProjectName(cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_PROJECT_NAME)));
        project.setLatitude(cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_PROJECT_LAT)));
        project.setLongitude(cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_PROJECT_LON)));
        project.setBBoxValidation(cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_PROJECT_BBOX)));
        project.setCentroidValidation(cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_PROJECT_CIRCLE_VALIDATION)));
        project.setLastSubDate(cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_PROJECT_LAST_SUB_DATE)));
        project.setLastSyncTimestamp(cursor.getLong(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_PROJECT_LAST_UPDATED_TS)));
        project.setLastSyncTimestamp(cursor.getLong(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_PROJECT_LAST_UPDATED_TS)));
        project.setState(cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_PROJECT_STATE)));
        project.setExtProjectId(cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_PROJECT_EXTERNAL_PROJECT_ID)));

        String filteringDimensionValue = cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES));

        String groupingDimensionValue = cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_GROUPING_DIMENSION_VALUES));

        if (rootConfig != null && rootConfig.mApplications != null && !rootConfig.mApplications.isEmpty()) {
            for (ProjectTypeModel projectType : rootConfig.mApplications) {
                if (projectType.mAppId.equalsIgnoreCase(appId)) {

                    Map<String, String> attributes = new HashMap<>();

                    if (projectType.mFilteringAttributes != null && !projectType.mFilteringAttributes.isEmpty()
                            && filteringDimensionValue != null && !filteringDimensionValue.isEmpty()) {

                        String[] dimArr = filteringDimensionValue.split("#");
                        for (int i = 0; i < projectType.mFilteringAttributes.size(); i++) {
                            attributes.put(projectType.mFilteringAttributes.get(i), dimArr[i]);
                        }
                    }
                    if (projectType.mGroupingAttributes != null && !projectType.mGroupingAttributes.isEmpty()
                            && groupingDimensionValue != null && !groupingDimensionValue.isEmpty()) {
                        String[] dimArr = groupingDimensionValue.split("#");
                        for (int i = 0; i < projectType.mGroupingAttributes.size(); i++) {
                            if (i < dimArr.length)
                                attributes.put(projectType.mGroupingAttributes.get(i), dimArr[i]);
                        }
                    }
                    project.setAttributes(attributes);
                    break;
                }
            }
        }

        String projectFieldsString = cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_PROJECT_FIELDS));

        project.setAssigned(cursor.getInt(cursor.getColumnIndex(UnifiedAppDbContract
                .ProjectTableEntry.COLUMN_PROJECT_ASSINGED_STATUS)) == 1);

        ArrayList<ProjectListFieldModel> fieldList;
        if (projectFieldsString != null) {
            try {
//                Type type = new TypeToken<List<ProjectListFieldModel>>() {
//                }.getType();
//                fieldList = gson.fromJson(projectFieldsString, type);
                fieldList =  objectMapper.readValue(projectFieldsString, new TypeReference<List<ProjectListFieldModel>>() {});
//
            } catch (IOException e) {
                fieldList = new ArrayList<>();
                Utils.logError(LogTags.UNIFIED_DB_HELPER,"failed to parse json string for ProjectFields :: " + projectFieldsString);
                e.printStackTrace();
            }
        } else {
            fieldList = new ArrayList<>();
        }

        project.setFields(fieldList);

        String validations = cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_PROJECT_VALIDATIONS));
        if (validations != null && !validations.isEmpty()) {
            try {
                Validation validation = objectMapper.readValue(validations, Validation.class);
                project.setValidation(validation);
            } catch (IOException e) {
                Utils.logError(LogTags.UNIFIED_DB_HELPER,"failed to parse json string for ProjectValidations :: " + validations);
                e.printStackTrace();
            }
        }

        String iconInfo = cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .ProjectTableEntry.COLUMN_PROJECT_ICON));

        if (iconInfo != null && !iconInfo.isEmpty()) {
            try{
                ProjectIconInfo icon = objectMapper.readValue(iconInfo, ProjectIconInfo.class);
                project.setProjectIcon(icon);
            } catch (IOException e) {
                Utils.logError(LogTags.UNIFIED_DB_HELPER,"failed to parse json string for ProjectIconInfo :: " + iconInfo);
                e.printStackTrace();
            }
        }

        return project;
    }

    /**
     * Gets all projects for a userId, appId and grouping value selected
     * @param userId
     * @param appId
     * @param groupingAttribute
     * @return
     */
    public ProjectList getProjectsForUserForGroupingAttribute(String userId, String appId, String groupingAttribute) {
        ProjectList projectList = new ProjectList();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_PROJECT_USER_ID + " LIKE ?" + " AND "
                + UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_PROJECT_APP_ID + " LIKE ?" + " AND "
                + UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_GROUPING_DIMENSION_VALUES + " LIKE ?";
        String[] selectionArgs = {userId, appId, "%" + groupingAttribute + "%"};

        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectTableEntry.TABLE_PROJECT,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        while (cursor.moveToNext()) {

            String userType = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectTableEntry.COLUMN_PROJECT_USER_TYPE));

            Project project = getProjectObjectFromDB(cursor, appId);
            project.setUserType(userType);
            
            if (!projectList.mUserTypes.contains(userType))
                projectList.mUserTypes.add(userType);
            projectList.mLastSyncTime = cursor.getLong(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectTableEntry.COLUMN_SERVER_SYNC_TS));
            projectList.mShouldShowMap = (cursor.getInt(cursor.getColumnIndex(UnifiedAppDbContract
                    .ProjectTableEntry.COLUMN_SHOW_MAP)) == 1);
            if(groupingAttribute != null && project.getAttributes() != null) {
                Collection<String> attributeValues = project.getAttributes().values();
                if(!attributeValues.contains(groupingAttribute)) {
                    continue;
                }
            }
            projectList.mProjects.add(project);

        }
        cursor.close();

        return projectList;
    }

    public int getProjectCountForUser(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ID
        };
        String selection = UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_PROJECT_USER_ID + " = ?";
        String[] selectionArgs = {userId};
        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectTableEntry.TABLE_PROJECT,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int getProjectTypeCountForUser(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_PROJECT_ID
        };
        String selection = UnifiedAppDbContract.ProjectFormTableEntry
                .COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {userId};
        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectFormTableEntry.TABLE_PROJECT_FORM,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public ProjectTypeConfiguration getProjectFormForApp(String userId, String appId) {

        Map<String, Map<String, ProjectSpecificForms>> projectIdToFormMap = new HashMap<String, Map<String, ProjectSpecificForms>>();
        ProjectTypeConfiguration projectTypeConfiguration = new ProjectTypeConfiguration();

        List<DBProjectForm> dBProjectFormlist = new ArrayList<DBProjectForm>();
        projectTypeConfiguration.mUserId = userId;
        projectTypeConfiguration.mProjectTypeId = appId;
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = UnifiedAppDbContract.ProjectFormTableEntry
                .COLUMN_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectFormTableEntry
                .COLUMN_APP_ID + " = ?";
        String[] selectionArgs = {userId, appId};

        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectFormTableEntry.TABLE_PROJECT_FORM,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        while (cursor.moveToNext()) {

            DBProjectForm dBProjectForm = new DBProjectForm();

            dBProjectForm.setUserId(cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectFormTableEntry.COLUMN_USER_ID)));
            dBProjectForm.setProjectId(cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectFormTableEntry.COLUMN_PROJECT_ID)));
            dBProjectForm.setAppId(cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectFormTableEntry.COLUMN_APP_ID)));
            dBProjectForm.setFormData(cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectFormTableEntry.COLUMN_FORM_DATA)));
            dBProjectForm.setformType(cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectFormTableEntry.COLUMN_FORM_TYPE)));
            dBProjectForm.setFormVersion(cursor.getInt(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectFormTableEntry.COLUMN_VERSION)));
            dBProjectForm.setMdInstanceId(cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectFormTableEntry.COLUMN_MD_INSTANCE_ID)));

            dBProjectFormlist.add(dBProjectForm);
            ProjectSpecificForms form;
//            Gson gson = new Gson();
//            form = gson.fromJson(dBProjectForm.getFormData(), ProjectSpecificForms.class);
            try{
            ObjectMapper mapper = new ObjectMapper();
            form = mapper.readValue(dBProjectForm.getFormData(),ProjectSpecificForms.class);
            if (projectIdToFormMap.get(dBProjectForm.getProjectId()) == null) {
                Map<String, ProjectSpecificForms> actionToFormMap = new HashMap<String, ProjectSpecificForms>();
                actionToFormMap.put(dBProjectForm.getformType(), form);
                projectIdToFormMap.put(dBProjectForm.getProjectId(), actionToFormMap);
            } else {
                Map<String, ProjectSpecificForms> actionToFormMap = projectIdToFormMap.get(dBProjectForm.getProjectId());
                if (actionToFormMap.get(dBProjectForm.getformType()) == null) {
                    actionToFormMap.put(dBProjectForm.getformType(), form);
                    projectIdToFormMap.put(dBProjectForm.getProjectId(), actionToFormMap);
                } else {
                    ProjectSpecificForms availableForm = actionToFormMap.get(dBProjectForm.getformType());
                    if (availableForm.mFormVerion <= form.mFormVerion) {
                        actionToFormMap.put(dBProjectForm.getformType(), form);
                        projectIdToFormMap.put(dBProjectForm.getProjectId(), actionToFormMap);
                    }
                }
            }}
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        projectTypeConfiguration.mContent = projectIdToFormMap;
        if (projectIdToFormMap.isEmpty()) {
            return null;
        }
        return projectTypeConfiguration;
    }

    /**
     * Returns config files
     *
     * @param userId
     * @param configName
     * @return
     */
    public ConfigFile getConfigFile(String userId, String configName) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_FILE_CONTENT,
                UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_VERSION,
                UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_LAST_SYNC_TS
        };

        String selection = UnifiedAppDbContract.ConfigFilesEntry
                .COLUMN_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ConfigFilesEntry
                .COLUMN_CONFIG_NAME + " = ?";

        String[] selectionArgs = {userId, configName};

        Cursor cursor = db.query(
                UnifiedAppDbContract.ConfigFilesEntry.TABLE_CONFIG,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        ConfigFile configFile = null;
        while (cursor.moveToNext()) {

            if (configFile != null) {
                // Error - found more than one row/entry
                Utils.logError(LogTags.CONFIG_ERROR, "Found multiple config entries for : "
                        + userId + ":" + configName + " -- continuing with the last/latest config");
            }

            int version = cursor.getInt(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ConfigFilesEntry.COLUMN_CONFIG_VERSION));
            String configFileContent = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ConfigFilesEntry.COLUMN_CONFIG_FILE_CONTENT));
            long ts = cursor.getLong(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ConfigFilesEntry.COLUMN_CONFIG_LAST_SYNC_TS));
            configFile = new ConfigFile(userId, configName,
                    configFileContent, version, ts);
        }
        cursor.close();

        return configFile;
    }

    public Set<String> getDimensionValues(String filter, int index, String userId, String appId, String groupingFilter) {
        Set<String> valuesSet = new HashSet<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_PROJECT_USER_ID + " LIKE ?" + " AND "
                + UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_PROJECT_APP_ID + " LIKE ?" + " AND "
                + UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_FILTERING_DIMENSION_VALUES + " LIKE ?";

        List<String> selectionArgsList = new ArrayList<>(Arrays.asList(userId, appId, filter + "%"));

        if (groupingFilter != null) {
            selection += " AND " + UnifiedAppDbContract.ProjectTableEntry.COLUMN_GROUPING_DIMENSION_VALUES
                    + " LIKE ?";
            selectionArgsList.add("%" + groupingFilter + "%");
        }
        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgs = selectionArgsList.toArray(selectionArgs);

        Cursor cursor = db.query(UnifiedAppDbContract.ProjectTableEntry.TABLE_PROJECT,
                new String[]{UnifiedAppDbContract.ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES, UnifiedAppDbContract.ProjectTableEntry.COLUMN_GROUPING_DIMENSION_VALUES},
                selection, selectionArgs, null, null, null,
                null);
        while (cursor.moveToNext()) {
            String groupingValue = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.ProjectTableEntry.COLUMN_GROUPING_DIMENSION_VALUES));
            // For the case when query returns both Atmakur and Bandi Atmakur(due to use of %)- Get the values exactly matching the grouping filter
            if (groupingFilter == null) {
                String dimensionValue = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES));
                String[] dimArr = dimensionValue.split("#");
                if (index >= 0 && index < dimArr.length) {
                    String value = dimArr[index];
                    valuesSet.add(Constants.capitalize(value));
                }
            }
            String[] groupingAttribute = groupingValue.split("#");
            for (String attr : groupingAttribute) {
                if (attr.equalsIgnoreCase(groupingFilter)) {
                    String dimensionValue = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES));
                    String[] dimArr = dimensionValue.split("#");
                    if (index >= 0 && index < dimArr.length) {
                        String value = dimArr[index];
                        valuesSet.add(Constants.capitalize(value));
                    }
                }
            }
        }
        return valuesSet;
    }

    public List<String> getProjectIdsForFilterQuery(String userId, String appId, String dimensionValues) {

        List<String> projectIds = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Set<String> projectIdSet = new HashSet<>();
        String rawQuery = "SELECT " + UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ID + ", " +
                UnifiedAppDbContract.ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES +
                " FROM " + UnifiedAppDbContract.ProjectTableEntry.TABLE_PROJECT + " WHERE " +
                UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_USER_ID + " = '" + userId.trim() + "' AND " + UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_APP_ID
                + " = '" + appId.trim() + "' AND " + UnifiedAppDbContract.ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES + " LIKE '%" + dimensionValues + "%'";
        Cursor cursor = db.rawQuery(rawQuery, null);

        while (cursor.moveToNext()) {
            String projectId = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectTableEntry.COLUMN_PROJECT_ID));
            String dbDimensions = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES));
            List<String> dbValues = Arrays.asList(dbDimensions.split("#"));
            List<String> inputValues = Arrays.asList(dimensionValues.split("#"));
            if(dbValues.containsAll(inputValues)) {
                projectIdSet.add(projectId);
            }
        }
        projectIds.addAll(projectIdSet);

        return projectIds;
    }


    /**
     * Get user meta data
     * @param username
     * @return
     */
    public UserMetaData getUserMeta(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                UnifiedAppDbContract.UserMetaEntry.COLUMN_USER_ID,
                UnifiedAppDbContract.UserMetaEntry.COLUMN_PASSWORD,
                UnifiedAppDbContract.UserMetaEntry.COLUMN_TOKEN,
                UnifiedAppDbContract.UserMetaEntry.COLUMN_LAST_NETWORK_SYNC_TIME,
                UnifiedAppDbContract.UserMetaEntry.COLUMN_IS_LOGGEN_IN,
                UnifiedAppDbContract.UserMetaEntry.COLUMN_LAST_LOGIN_TS,
                UnifiedAppDbContract.UserMetaEntry.COLUMN_USER_DETAILS
        };

        String selection = UnifiedAppDbContract.UserMetaEntry.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(
                UnifiedAppDbContract.UserMetaEntry.TABLE_USER,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        ArrayList<UserMetaData> users = new ArrayList<>();
        while (cursor.moveToNext()) {
            String dbPassword = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                    .UserMetaEntry.COLUMN_PASSWORD));
            String dbUserId = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                    .UserMetaEntry.COLUMN_USER_ID));
            String dbToken = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                    .UserMetaEntry.COLUMN_TOKEN));
            long dbServerTime = cursor.getLong(cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                    .UserMetaEntry.COLUMN_LAST_NETWORK_SYNC_TIME));
            Long lastLoginTs = cursor.getLong(cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                    .UserMetaEntry.COLUMN_LAST_LOGIN_TS));
            boolean isLoggedIn = (cursor.getInt(cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                    .UserMetaEntry.COLUMN_LAST_NETWORK_SYNC_TIME))) > 0;
            String userInfo = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                    .UserMetaEntry.COLUMN_USER_DETAILS));

            UserMetaData dbUserMetaData = new UserMetaData(dbPassword, dbUserId
                    , dbToken, dbServerTime, isLoggedIn, lastLoginTs, userInfo);
            users.add(dbUserMetaData);
        }
        cursor.close();
        if (users.size() > 0)
            return users.get(0);
        else
            return null;
    }

    public int updateUsermeta(ContentValues values, String username) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Which row to update, based on the title
        String selection = UnifiedAppDbContract.UserMetaEntry.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {username};

        Utils.getInstance().showLog("DB :", "User Updated");

        return db.update(
                UnifiedAppDbContract.UserMetaEntry.TABLE_USER,
                values,
                selection,
                selectionArgs);
    }

    /**
     * Incoming images get images
     * @param imageType
     * @return
     */
    public IncomingImage getImage(String imageType) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                UnifiedAppDbContract.IncomingImagesEntry.COLUMN_IMAGE_TYPE,
                UnifiedAppDbContract.IncomingImagesEntry.COLUMN_IMAGE_URL,
                UnifiedAppDbContract.IncomingImagesEntry.COLUMN_IMAGE_LOCAL_PATH
        };

        String selection = UnifiedAppDbContract.IncomingImagesEntry.COLUMN_IMAGE_TYPE + " = ?";
        String[] selectionArgs = {imageType};

        Cursor cursor = db.query(
                UnifiedAppDbContract.IncomingImagesEntry.TABLE_IMAGES,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        IncomingImage incomingImage = null;
        while (cursor.moveToNext()) {
            incomingImage = getIncomingImageObject(cursor);
        }
        cursor.close();
        return incomingImage;
    }

    /**
     * Get image with URL
     * @param url
     * @return
     */
    public IncomingImage getIncomingImageWithUrl(String url) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                UnifiedAppDbContract.IncomingImagesEntry.COLUMN_IMAGE_TYPE,
                UnifiedAppDbContract.IncomingImagesEntry.COLUMN_IMAGE_URL,
                UnifiedAppDbContract.IncomingImagesEntry.COLUMN_IMAGE_LOCAL_PATH
        };

        String selection = UnifiedAppDbContract.IncomingImagesEntry.COLUMN_IMAGE_URL + " = ?";
        String[] selectionArgs = {url};

        Cursor cursor = db.query(
                UnifiedAppDbContract.IncomingImagesEntry.TABLE_IMAGES,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        IncomingImage incomingImage = null;
        while (cursor.moveToNext()) {
            incomingImage = getIncomingImageObject(cursor);
        }
        cursor.close();
        return incomingImage;
    }

    /**
     * Utility to get @{@link IncomingImage} object from cursor
     * @param cursor
     * @return
     */
    private IncomingImage getIncomingImageObject(Cursor cursor) {
        String type = cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .IncomingImagesEntry.COLUMN_IMAGE_TYPE));
        String url = cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .IncomingImagesEntry.COLUMN_IMAGE_URL));
        String path = cursor.getString(
                cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                        .IncomingImagesEntry.COLUMN_IMAGE_LOCAL_PATH));
        IncomingImage incomingImage = new IncomingImage(type, path, url);
        return incomingImage;
    }

    public int updateImage(ContentValues values, String imageUrl) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Which row to update, based on the title
        String selection = UnifiedAppDbContract.IncomingImagesEntry.COLUMN_IMAGE_URL + " LIKE ?";
        String[] selectionArgs = {imageUrl};

        Utils.getInstance().showLog("DB :", "Image Updated");

        return db.update(
                UnifiedAppDbContract.IncomingImagesEntry.TABLE_IMAGES,
                values,
                selection,
                selectionArgs);
    }

    public List<ProjectSubmission> getProjectsToSubmit(String username, String appId, List<ProjectSubmissionUploadStatus> uploadStatusList) {

        List<ProjectSubmission> projectSubmissions = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        List<String> statusList = new ArrayList<>();
        for (ProjectSubmissionUploadStatus status : uploadStatusList) {
            statusList.add(String.valueOf(status.getValue()));
        }
        String status = StringUtils.getconcatenatedStringFromStringList(",", statusList);
        status = "(" + status + ")";

        String[] projection = {
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FORM_ID,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_TYPE,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_API,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_MD_INSTANCE_ID,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RESPONSE,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_SERVER_SYNC_TS,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RETRY_COUNT,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_ADDITIONAL_PROPERTIES

        };

        String selection = UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS + " IN " + status;
        String[] selectionArgs = {appId, username};

        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        ProjectSubmission projectSubmission;
        while (cursor.moveToNext()) {

            String projectId = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID));
            String userId = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID));
            String formId = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FORM_ID));
            String userType = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_TYPE));
            Long time = cursor.getLong(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP));
            String object = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS));
            String api = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_API));

            String mdInstanceId = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_MD_INSTANCE_ID));
            String response = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RESPONSE));
            Long serverSyncTs = cursor.getLong(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_SERVER_SYNC_TS));
            int retryCount = cursor.getInt(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RETRY_COUNT));
            int uploadStatus = cursor.getInt(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS));

            String additionalProps = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_ADDITIONAL_PROPERTIES));

            Map<String, String> additionalPropertiesMap = new HashMap<>();
            if (additionalProps != null && !additionalProps.isEmpty()) {
//                Type type = new TypeToken<Map<String, String>>() {
//                }.getType();
//                myMap = gson.fromJson(additionalProps, type);
                try {
                    additionalPropertiesMap =  objectMapper.readValue(additionalProps, new TypeReference<Map<String, String>>() {});
                } catch (IOException e) {
                    Utils.logError(LogTags.UNIFIED_DB_HELPER,"failed to parse json string for AdditionalProperties :: " + additionalProps);
                    e.printStackTrace();
                }
            }

            projectSubmission = new ProjectSubmission(userId, appId, formId, time
                    , userType, projectId, object, api, mdInstanceId, uploadStatus, response, serverSyncTs, retryCount, additionalPropertiesMap);
            projectSubmissions.add(projectSubmission);
        }
        cursor.close();
        return projectSubmissions;
    }

    public List<ProjectSubmission> getAllProjectsToSubmit(List<ProjectSubmissionUploadStatus> uploadStatusList) {

        List<ProjectSubmission> projectSubmissions = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        List<String> statusList = new ArrayList<>();
        for (ProjectSubmissionUploadStatus status : uploadStatusList) {
            statusList.add(String.valueOf(status.getValue()));
        }
        String status = StringUtils.getconcatenatedStringFromStringList(",", statusList);
        status = "(" + status + ")";

        String[] projection = {
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FORM_ID,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_TYPE,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_API,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_MD_INSTANCE_ID,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RESPONSE,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_SERVER_SYNC_TS,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RETRY_COUNT,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_ADDITIONAL_PROPERTIES

        };

        String selection = UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS + " IN " + status;

        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        ProjectSubmission projectSubmission;
        while (cursor.moveToNext()) {

            String projectId = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID));
            String userId = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID));
            String formId = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FORM_ID));
            String userType = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_TYPE));
            Long time = cursor.getLong(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP));
            String object = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS));
            String api = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_API));

            String mdInstanceId = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_MD_INSTANCE_ID));
            String response = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RESPONSE));
            Long serverSyncTs = cursor.getLong(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_SERVER_SYNC_TS));
            int retryCount = cursor.getInt(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RETRY_COUNT));
            int uploadStatus = cursor.getInt(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS));

            String additionalProps = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_ADDITIONAL_PROPERTIES));

            String appId = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID));

            Map<String, String> additionalPropertiesMap = new HashMap<>();
            if (additionalProps != null && !additionalProps.isEmpty()) {
//                Type type = new TypeToken<Map<String, String>>() {
//                }.getType();
//                myMap = gson.fromJson(additionalProps, type);
                try {
                    additionalPropertiesMap =  objectMapper.readValue(additionalProps, new TypeReference<Map<String, String>>() {});
                } catch (IOException e) {
                    Utils.logError(LogTags.UNIFIED_DB_HELPER,"failed to parse json string for AdditionalProperties :: " + additionalProps);
                    e.printStackTrace();
                }
            }

            projectSubmission = new ProjectSubmission(userId, appId, formId, time
                    , userType, projectId, object, api, mdInstanceId, uploadStatus, response, serverSyncTs, retryCount, additionalPropertiesMap);
            projectSubmissions.add(projectSubmission);
        }
        cursor.close();
        return projectSubmissions;
    }

    public int getProjectSubmissionStatus(String appId, String userId, String projectId, Long timestamp) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS,
        };

        String selection = UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_PROJECT_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_TIMESTAMP + " = " + timestamp;
        String[] selectionArgs = {appId, userId, projectId};

        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        int status = Constants.DEFAULT_UPLOAD_STATUS;
        Utils.logInfo("MEDIA_THREAD", "cursor count" + cursor.getCount());
        while (cursor.moveToNext()) {
            status = cursor.getInt(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS));
            Utils.logInfo("MEDIA_THREAD", "Status" + status);
        }
        cursor.close();
        return status;
    }

//    public void deleteProjectSubmission(String appId, String userId, Long timestamp, String projectId) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        Utils.getInstance().showLog("SUBMISSIONPARAMETERS", "appId "+ appId +" userId "
//                +userId+" timestamp "+String.valueOf(timestamp)+" projectId "+projectId);
////        db.execSQL("DELETE FROM " + UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION
////                + " WHERE "+ UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID
////                + " ='" + appId + "' AND " + UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP
////                + " ='" + timestamp + "' AND " + UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID
////                + " ='" + userId + "'");
//        int delete = db.delete(UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,
//                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID + " = ? AND " +
//                        UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP + " = "+ timestamp +" AND " +
//                        UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID + " = ? AND " +
//                        UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID + " = ?",
//                new String[] {appId, projectId, userId});
//        Utils.getInstance().showLog("DATABASE DELETION ", String.valueOf(delete));
//    }


    public int getProjectSubmissionCount(String username, String appId) {

        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_USER_ID + " = ?";
        String[] selectionArgs = {appId, username};

        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        int count = cursor.getCount();
        cursor.close();
        return count;
    }


    public void deleteFormMedia(String uuid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + UnifiedAppDbContract.FormMediaEntry.TABLE_FORM_MEDIA
                + " WHERE " + UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_UUID
                + " ='" + uuid + "'");
    }

    public long addOrUpdateConfigFile(ContentValues values) {
        // Gets the data repository in write mode
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        String userId = String.valueOf(values.get(UnifiedAppDbContract.ConfigFilesEntry.COLUMN_USER_ID));
        String configName = String.valueOf(values.get(UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_NAME));

        int id = -1;

        String whereClause = UnifiedAppDbContract.ConfigFilesEntry.COLUMN_USER_ID + " = ? AND "
                + UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_NAME + " = ?";
        id = db.update(UnifiedAppDbContract.ConfigFilesEntry.TABLE_CONFIG, values, whereClause,
                new String[]{userId, configName});

        if (id == 0) {
            id = (int) db.insert(UnifiedAppDbContract.ConfigFilesEntry.TABLE_CONFIG, null, values);
        }

        return id;
    }

    public long addOrUpdateProjectFormEntry(ContentValues values) {
        // Gets the data repository in write mode
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        String userId = String.valueOf(values.get(UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_USER_ID));
        String appId = String.valueOf(values.get(UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_APP_ID));
        String projectId = String.valueOf(values.get(UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_PROJECT_ID));
        String formType = String.valueOf(values.get(UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_FORM_TYPE));
        String formVersion = String.valueOf(values.get(UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_VERSION));

        int id = -1;

        String whereClause = UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_USER_ID + " = ? AND "
                + UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_APP_ID + " = ? AND "
                + UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_PROJECT_ID + " = ? AND "
                + UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_FORM_TYPE + " = ? AND "
                + UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_VERSION + " = ?";
        id = db.update(UnifiedAppDbContract.ProjectFormTableEntry.TABLE_PROJECT_FORM, values, whereClause,
                new String[]{userId, appId, projectId, formType, formVersion});

        if (id == 0) {
            id = (int) db.insert(UnifiedAppDbContract.ProjectFormTableEntry.TABLE_PROJECT_FORM, null, values);
        }

        return id;
    }

    /**
     * Deletes a project
     * @param appId
     * @param userId
     * @param projectId
     */
    public void deleteProject(String appId, String userId, String projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int delete = db.delete(UnifiedAppDbContract.ProjectTableEntry.TABLE_PROJECT,
                UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_APP_ID + " = ? AND " +
                        UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_USER_ID + " = ? AND " +
                        UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ID + " = ?",
                new String[]{appId, userId, projectId});

        // TODO: Delete all the entries of this particular project from the ProjectSubmissionTable
        Utils.logInfo(LogTags.APP_BACKGROUND_SYNC + "Deleted project with project id " + projectId
                + "with no of rows deleted : " + delete);
    }

    public int updateProjectSubmission(String userId, String appId, String projectId, Long ts, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Which row to update
        String selection = UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID + " = ? AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID + " = ? AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP + " = " + ts + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID + " = ?";
        String[] selectionArgs = {userId, appId, projectId};

        int id = -1;
        id = db.update(
                UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,
                values,
                selection,
                selectionArgs);

        Utils.getInstance().showLog("DB :", "ProjectSubmission Updated");
        return id;

    }

    public int addOrUpdateProjectSubmission(ContentValues values) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        String userId = String.valueOf(values.get(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID));
        String appId = String.valueOf(values.get(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID));
        Long ts = values.getAsLong(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP);
        String projId = String.valueOf(values.get(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID));

        int id = -1;

        String whereClause = UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID + " = ? AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID + " = ? AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP + " = " + ts + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID + " = ?";
        id = db.update(UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION, values, whereClause,
                new String[]{userId, appId, projId});

        if (id == 0) {
            id = (int) db.insert(UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION, null, values);
        }

        Utils.logInfo("XXXXXXXXXXXXX" + getTableCount(UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION));

        return id;
    }

    /**
     * GET COUNT OF PROJECT SUBMISSIONS FOR given AppId, UserId, ProjectId,UploadStatus
     *
     * @param appId
     * @param userId
     * @param projectId
     * @param uploadStatusList
     * @return
     */
    public int getProjectSubmissionCountForGivenStatusList(String appId, String userId, String projectId
            , List<ProjectSubmissionUploadStatus> uploadStatusList) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS
        };

        List<String> statusList = new ArrayList<>();
        for (ProjectSubmissionUploadStatus status : uploadStatusList) {
            statusList.add(String.valueOf(status.getValue()));
        }
        String status = StringUtils.getconcatenatedStringFromStringList(",", statusList);
        status = "(" + status + ")";
        String selection = UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_PROJECT_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS + " IN " + status;

        String[] selectionArgs = {appId, userId, projectId};

        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * Deleted all Project Submission entries for given AppId, UserId, ProjectId
     * @param appId
     * @param userId
     * @param projectId
     * @return
     */
    public int deleteAllProjectSubmission(String appId, String userId, String projectId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int delete = db.delete(UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID + " = ? AND " +
                        UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID + " = ? AND " +
                        UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID + " = ?",
                new String[]{appId, userId, projectId});
        Utils.logInfo(LogTags.PROJECT_SUBMIT, "Deleted all Project Submission enteries for appId: "
                + appId + " , userId:" + userId + " ,projectId: " + projectId);
        return delete;
    }

    /**
     * Returns project from the Project table (meta data) for the corresponding parameters
     * @param userId
     * @param appId
     * @param projectId
     * @return
     */
    public Project getLatestProjectEntry(String userId, String appId
            , String projectId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_FIELDS,
                UnifiedAppDbContract.ProjectTableEntry.COLUMN_SERVER_SYNC_TS,
                UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ASSINGED_STATUS,

        };

        String selection = UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_PROJECT_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_PROJECT_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_PROJECT_ID + " = ?";

        String[] selectionArgs = {userId, appId, projectId};

        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectTableEntry.TABLE_PROJECT,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        Project project = null;
        while (cursor.moveToNext()) {

            if (project != null) {
                // Error - found more than one row/entry for a project in the Project table (meta data)
                Utils.logError(LogTags.PROJECT_LIST, "Found multiple entries for project -- "
                        + projectId + " -- app -- " + appId + " -- user -- " + userId);
            }

            project = new Project();
            project.setProjectId(projectId);
            project.setAssigned(cursor.getInt(cursor.getColumnIndex(UnifiedAppDbContract
                    .ProjectTableEntry.COLUMN_PROJECT_ASSINGED_STATUS)) == 1);
            project.setLastSyncTimestamp(cursor.getLong(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectTableEntry.COLUMN_SERVER_SYNC_TS)));
            String projectFieldsString = cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectTableEntry.COLUMN_PROJECT_FIELDS));
            List<ProjectListFieldModel> fieldList;
            if (projectFieldsString != null) {
//                Type type = new TypeToken<List<ProjectListFieldModel>>() {
//                }.getType();
//                fieldList = gson.fromJson(projectFieldsString, type);
                try {
                    fieldList =  objectMapper.readValue(projectFieldsString, new TypeReference<List<ProjectListFieldModel>>() {});
//
                } catch (IOException e) {
                    fieldList = new ArrayList<>();
                    Utils.logError(LogTags.UNIFIED_DB_HELPER,"failed to parse json string for ProjectFields :: " + projectFieldsString);
                    e.printStackTrace();
                }
            } else {
                fieldList = new ArrayList<>();
            }
            project.setFields((ArrayList<ProjectListFieldModel>) fieldList);
        }
        cursor.close();
        return project;
    }

    /**
     * Returns project from the ProjectSubmission table for the corresponding parameters
     * @param userId
     * @param appId
     * @param projectId
     * @return
     */
    public ProjectSubmission getLatestProjectSubmissionEntry(String userId, String appId, String projectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<ProjectSubmissionUploadStatus> uploadStatusList = new ArrayList<>();
        uploadStatusList.add(ProjectSubmissionUploadStatus.SERVER_ERROR);
        uploadStatusList.add(ProjectSubmissionUploadStatus.UNSYNCED);
        uploadStatusList.add(ProjectSubmissionUploadStatus.SYNCED);
        uploadStatusList.add(ProjectSubmissionUploadStatus.SYNCED_WITH_MEDIA);
        List<String> statusList = new ArrayList<>();
        for (ProjectSubmissionUploadStatus status : uploadStatusList) {
            statusList.add(String.valueOf(status.getValue()));
        }
        String status = StringUtils.getconcatenatedStringFromStringList(",", statusList);
        status = "(" + status + ")";
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP
        };

        String selection = UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_PROJECT_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_TIMESTAMP + " = (SELECT MAX(" + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_TIMESTAMP + ") FROM " + UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION
                + " WHERE (" + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_USER_ID + " = ? AND  " + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_APP_ID + " = ? AND " + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_PROJECT_ID + " = ?" +
                "  AND " + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS + " IN " + status +
                "))";

        String[] selectionArgs = {userId, appId, projectId, userId, appId, projectId};

        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        ProjectSubmission projectSubmission = null;
        while (cursor.moveToNext()) {

            if (projectSubmission != null) {
                // Error - found more than one row/entry for a project in the Project table (meta data)
                Utils.logError(LogTags.PROJECT_LIST, "Found multiple entries for project with " +
                        "greatest submission timestamp -- projectId -- " + projectId + " -- app -- "
                        + appId + " -- user -- " + userId);
            }

            projectSubmission = new ProjectSubmission();
            projectSubmission.setProjectId(projectId);
            projectSubmission.setTimestamp(cursor.getLong(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP)));
            projectSubmission.setFields(cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS)));
        }
        cursor.close();
        return projectSubmission;
    }

    /**
     * Returns latest N submissions for a user
     * @param appId
     * @param userId
     * @param n
     * @return
     */
    public List<TransactionLogObject> getLastNSubmissionsForUser(String appId, String userId, int n) {
        List<TransactionLogObject> transactionLogList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RESPONSE
        };
        String selection = UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID + " = ? ";

        String[] selectionArgs = {userId, appId};

        Cursor cursor = db.query(UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,
                projection, selection, selectionArgs, null, null,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP + " DESC", String.valueOf(n));

        while(cursor.moveToNext()) {
            try {
                TransactionLogObject transactionLogObject = new TransactionLogObject();
                String projectId = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                        ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID));
                Project project = getProjectMetaData(appId, userId, projectId);
                transactionLogObject.setProjectName(project.mProjectName);
                transactionLogObject.setResponse(cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                        ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RESPONSE)));
                transactionLogObject.setSubmissionTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                        ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP)));

                String fields = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                        ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS));
                Map<String, String> keyToValueMap = transactionLogObject.getFields();
                Map<Integer, List<String>> mediaTypeToUUIDs  = transactionLogObject.getMediaUUIDs();
                JSONObject jsonObject = new JSONObject(fields);
                JSONArray jsonArray = jsonObject.getJSONArray(Constants.SUBMISSION_FIELD_ARRAY);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);
                    if(object.getString("ui").contains("image") || object.getString("ui").contains("video")) {
                       String uiType = object.getString("ui");
                       Integer key;
                       if(uiType.contains("image")) {
                           key = MediaType.IMAGE.getValue();
                       } else {
                           key = MediaType.VIDEO.getValue();
                       }
                       if(!mediaTypeToUUIDs.containsKey(key)) {
                           mediaTypeToUUIDs.put(key, new ArrayList<>());
                       }
                       mediaTypeToUUIDs.get(key).add(object.getString("val").split("##")[0]);
                    } else {
                        keyToValueMap.put(object.getString("key"), object.getString("val"));
                    }
                }

                transactionLogList.add(transactionLogObject);
            } catch (JSONException e) {
                Utils.logError("JSON_EXCEPTION", "JSON Exception while parsing submission data");
                continue;
            }
        }
        return transactionLogList;
    }

    private Project getProjectMetaData(String appId, String userId, String projectId) {

        Project project = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_PROJECT_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectTableEntry
                .COLUMN_PROJECT_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectTableEntry.
                COLUMN_PROJECT_ID + " = ?";
        String[] selectionArgs = {userId, appId, projectId};

        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectTableEntry.TABLE_PROJECT,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            if (project != null) {
                // Error - found more than one row/entry
                Utils.logError(LogTags.CONFIG_ERROR, "Found multiple project entries for : "
                        + userId + ":" + projectId + " -- continuing with the last/latest entry");
            }
            project = getProjectObjectFromDB(cursor, appId);
        }
        cursor.close();
        return project;
    }

    public List<FormMedia> getFormMediaEntries(ArrayList<Integer> statusList, int batchSize) {

        List<FormMedia> formMediaEntries = new ArrayList<>();

        if (statusList == null || statusList.isEmpty())
            return formMediaEntries;

        SQLiteDatabase db = this.getReadableDatabase();

        String statusValues = "( ";
        statusValues += android.text.TextUtils.join(",", statusList);
        statusValues += ")";

        String selection = UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_REQUEST_STATUS + " IN " + statusValues;

        Cursor cursor = db.query(UnifiedAppDbContract.FormMediaEntry.TABLE_FORM_MEDIA,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null);

        while (cursor.moveToNext()) {

            if (formMediaEntries.size() > batchSize)
                break;
            FormMedia formMedia = getFormMediaDBObject(cursor);
            formMediaEntries.add(formMedia);

        }
        cursor.close();
        return formMediaEntries;
    }

    public int updateFormMedia(ContentValues values, String uuid) {

        SQLiteDatabase db = this.getWritableDatabase();

        String selection = UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_UUID + " = ?";
        String[] selectionArgs = {uuid};

        Utils.getInstance().showLog("DB :", "User Updated");

        return db.update(
                UnifiedAppDbContract.FormMediaEntry.TABLE_FORM_MEDIA,
                values,
                selection,
                selectionArgs);
    }

    public int updateFormMediaForProjectAtGivenTimestamp(ContentValues values, String projectId, Long ts) {

        SQLiteDatabase db = this.getWritableDatabase();

        String selection = UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_PROJECT_ID + " = ? " +
                "AND " + UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_SUBMISSION_TIMESTAMP + " = " + ts;
        String[] selectionArgs = {projectId};

        Utils.getInstance().showLog("DB :", "User Updated");

        return db.update(
                UnifiedAppDbContract.FormMediaEntry.TABLE_FORM_MEDIA,
                values,
                selection,
                selectionArgs);
    }
    public FormMedia getFormMedia(String uuid, String appId, String userId) {
        FormMedia formMedia = null;

        SQLiteDatabase db = this.getReadableDatabase();

        String selection = UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_UUID + " = ?";

        String[] selectionArgs = {userId, appId, uuid};

        Cursor cursor = db.query(
                UnifiedAppDbContract.FormMediaEntry.TABLE_FORM_MEDIA,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        while (cursor.moveToNext()) {
            formMedia = getFormMediaDBObject(cursor);
        }
        cursor.close();
        return formMedia;
    }

    public int getFormMediaCountForGivenStatusList(String appid, String userid, String projectid, List<Integer> statusList) {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        String statusValues = "( ";
        statusValues += android.text.TextUtils.join(",", statusList);
        statusValues += ")";

        String[] projection = {
                UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_UUID,
                UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_SUBMISSION_TIMESTAMP};

        String selection = UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_PROJECT_ID + " = ?" + " AND "
                + UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_REQUEST_STATUS + " IN " + statusValues;

        String[] selectionArgs = {appid, userid, projectid};

        Cursor cursor = db.query(
                UnifiedAppDbContract.FormMediaEntry.TABLE_FORM_MEDIA,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            long form_sub_ts = cursor.getLong(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                    FormMediaEntry.COLUMN_FORM_SUBMISSION_TIMESTAMP));
            if (form_sub_ts > 0)
                count++;
        }
        cursor.close();
        return count;

    }

    public int deleteAllTheMediaForProject(String appId, String userId, String projectId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int delete = db.delete(UnifiedAppDbContract.FormMediaEntry.TABLE_FORM_MEDIA,
                UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_APP_ID + " = ? AND " +
                        UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_USER_ID + " = ? AND " +
                        UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_PROJECT_ID + " = ?",
                new String[]{appId, userId, projectId});

        return delete;

    }

    public int getFormMediaCountForUser(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_UUID
        };
        String selection = UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_USER_ID + " = ?";

        String[] selectionArgs = {userId};
        Cursor cursor = db.query(
                UnifiedAppDbContract.FormMediaEntry.TABLE_FORM_MEDIA,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public List<ProjectSubmission> getProjectSubmissionEntryListAfterGivenTimestamp(String userId, String appId
            , String projectId, Long timestamp) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP
        };

        String selection = UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_PROJECT_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_TIMESTAMP + " >= " + timestamp;

        String[] selectionArgs = {userId, appId, projectId, userId, appId, projectId};
        String orderBy = UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_TIMESTAMP + " ASC";
        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                orderBy
        );
        List<ProjectSubmission> projectSubmissionList = new ArrayList<>();
        ProjectSubmission projectSubmission = null;
        while (cursor.moveToNext()) {
            projectSubmission = new ProjectSubmission();
            projectSubmission.setProjectId(projectId);
            projectSubmission.setTimestamp(cursor.getLong(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP)));
            projectSubmission.setFields(cursor.getString(
                    cursor.getColumnIndexOrThrow(UnifiedAppDbContract
                            .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS)));
            projectSubmissionList.add(projectSubmission);
        }
        cursor.close();
        return projectSubmissionList;
    }

    public int updateAssignedStatusOfProjectFromUser(String userId, String appId, String projectId, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Which row to update
        String selection = UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_USER_ID + " = ? AND "
                + UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_APP_ID + " = ? AND "
                + UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ID + " = ?";
        String[] selectionArgs = {userId, appId, projectId};

        int id = -1;
        id = db.update(
                UnifiedAppDbContract.ProjectTableEntry.TABLE_PROJECT,
                values,
                selection,
                selectionArgs);

        Utils.getInstance().showLog("DB :", "Project AssignedStatus Updated");
        return id;

    }

    public int getFormMediaCountForApp(String appid, String userid, List<Integer> statusList) {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        String statusValues = "( ";
        statusValues += android.text.TextUtils.join(",", statusList);
        statusValues += ")";

        String[] projection = {
                UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_UUID,
                UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_PROJECT_ID,
                UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_SUBMISSION_TIMESTAMP};

        String selection = UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_REQUEST_STATUS + " IN " + statusValues;

        String[] selectionArgs = {appid, userid};

        Cursor cursor = db.query(
                UnifiedAppDbContract.FormMediaEntry.TABLE_FORM_MEDIA,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        while (cursor.moveToNext()) {
            long form_sub_ts = cursor.getLong(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                    FormMediaEntry.COLUMN_FORM_SUBMISSION_TIMESTAMP));
            String projectId = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                    FormMediaEntry.COLUMN_FORM_MEDIA_PROJECT_ID));

            Project mProject = getLatestProjectEntry(userid, appid, projectId);

            if (form_sub_ts > 0 && mProject != null && mProject.mAssigned)
                count++;
        }

        cursor.close();
        return count;
    }

    public int getProjectSubmissionCountForApp(String appId, String userId
            , List<ProjectSubmissionUploadStatus> uploadStatusList) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID,
                UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS
        };

        List<String> statusList = new ArrayList<>();
        for (ProjectSubmissionUploadStatus status : uploadStatusList) {
            statusList.add(String.valueOf(status.getValue()));
        }
        String status = StringUtils.getconcatenatedStringFromStringList(",", statusList);
        status = "(" + status + ")";
        String selection = UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS + " IN " + status;

        String[] selectionArgs = {appId, userId};

        int count = 0;
        Cursor cursor = db.query(
                UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        while (cursor.moveToNext()) {
            String projectId
                    = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                    ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID));

            Project mProject = getLatestProjectEntry(userId, appId, projectId);
            if (mProject != null && mProject.mAssigned) {
                Utils.logInfo("COUNT OF PROJECT SUBMISSION", String.valueOf(count));
                count++;
            }
        }
        cursor.close();
        return count;
    }

    public List<Entity> getEntityList(String super_app_id, String app_id, String project_id, String user_id,
                                           String parent_name, String entity_name)  {

        SQLiteDatabase db = this.getReadableDatabase();
        List<Entity> entityList = new ArrayList<>();

        String[] projection = {
                UnifiedAppDbContract.EntityMetaEntry.COLUMN_ELEMENTS
        };
        if (app_id == null)
            app_id = ServerConstants.DEFAULT_UUID;
        if (project_id == null)
            project_id = ServerConstants.DEFAULT_UUID;
        if (user_id == null)
            user_id = ServerConstants.ENTITY_METADATA_CONFIG_DEFAULT_USERID;
        String selection = UnifiedAppDbContract.EntityMetaEntry
                .COLUMN_SUPER_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.EntityMetaEntry
                .COLUMN_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.EntityMetaEntry
                .COLUMN_PROJECT_ID + " = ?" + " AND "
                + UnifiedAppDbContract.EntityMetaEntry
                .COLUMN_USER_ID + " = ?" + " AND "
                + UnifiedAppDbContract.EntityMetaEntry
                .COLUMN_PARENT_ENTITY + " = ?" + " AND "
                + UnifiedAppDbContract.EntityMetaEntry
                .COLUMN_ENTITY_NAME + " = ?";


        String[] selectionArgs = {super_app_id, app_id, project_id, user_id, parent_name, entity_name};

        Cursor cursor = db.query(
                UnifiedAppDbContract.EntityMetaEntry.TABLE_ENTITY_METADATA,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        while (cursor.moveToNext()) {

            String entity_list = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.EntityMetaEntry.COLUMN_ELEMENTS));

            if (entity_list != null && !entity_list.isEmpty()) {
                try {
                    JSONArray jsonArray = new JSONArray(entity_list);
                    for (int i = 0; i < jsonArray.length(); i++) {

                        String jsonStr = jsonArray.getString(i);
                        try {
                            Entity entity = objectMapper.readValue(jsonStr, Entity.class);
                            entityList.add(entity);
                        } catch (IOException e) {
                            Utils.logError(LogTags.UNIFIED_DB_HELPER, "failed to parse json string for Entity from Json String  :: " + jsonStr);
                            e.printStackTrace();
                        }
                    }
                } catch(JSONException e) {
			e.printStackTrace();
                }
            }
        }
        cursor.close();
        return entityList;
    }

    public long addOrUpdateEntityMetaData(ContentValues values) {

        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        String superAppId = String.valueOf(values.get(UnifiedAppDbContract.EntityMetaEntry.COLUMN_SUPER_APP_ID));
        String appId = String.valueOf(values.get(UnifiedAppDbContract.EntityMetaEntry.COLUMN_APP_ID));
        String userId = String.valueOf(values.get(UnifiedAppDbContract.EntityMetaEntry.COLUMN_USER_ID));
        String projectId = String.valueOf(values.get(UnifiedAppDbContract.EntityMetaEntry.COLUMN_PROJECT_ID));
        String parentName = String.valueOf(values.get(UnifiedAppDbContract.EntityMetaEntry.COLUMN_PARENT_ENTITY));
        String entityName = String.valueOf(values.get(UnifiedAppDbContract.EntityMetaEntry.COLUMN_ENTITY_NAME));

        int id = -1;
        String whereClause = UnifiedAppDbContract.EntityMetaEntry.COLUMN_SUPER_APP_ID + " = ?" + " AND " +
                UnifiedAppDbContract.EntityMetaEntry.COLUMN_APP_ID + " = ?" + " AND " +
                UnifiedAppDbContract.EntityMetaEntry.COLUMN_USER_ID + " = ?" + " AND " +
                UnifiedAppDbContract.EntityMetaEntry.COLUMN_PROJECT_ID + " = ?" + " AND " +
                UnifiedAppDbContract.EntityMetaEntry.COLUMN_PARENT_ENTITY + " = ?" + " AND " +
                UnifiedAppDbContract.EntityMetaEntry.COLUMN_ENTITY_NAME + " = ?";

        id = db.update(UnifiedAppDbContract.EntityMetaEntry.TABLE_ENTITY_METADATA, values, whereClause,
                new String[]{superAppId, appId, userId, projectId, parentName, entityName});

        if (id == 0) {
            id = (int) db.insert(UnifiedAppDbContract.EntityMetaEntry.TABLE_ENTITY_METADATA, null, values);
        }
        return id;
    }

    public long getLatestTimeStampForEntityMetaData(String superAppId, String appId) {

        long latestTimeStamp = 0;

        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                UnifiedAppDbContract.EntityMetaEntry.COLUMN_INSERT_TIMESTAMP
        };

        String selection = UnifiedAppDbContract.EntityMetaEntry
                .COLUMN_SUPER_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.EntityMetaEntry
                .COLUMN_APP_ID + " = ?";

        String[] selectionArgs = {superAppId, appId};

        Cursor cursor = db.query(
                UnifiedAppDbContract.EntityMetaEntry.TABLE_ENTITY_METADATA,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        while (cursor.moveToNext()) {

            long insert_ts = cursor.getLong(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                    EntityMetaEntry.COLUMN_INSERT_TIMESTAMP));

            if (insert_ts > latestTimeStamp) {
                latestTimeStamp = insert_ts;
            }
        }
        cursor.close();
        return latestTimeStamp;

    }

    public List<String> getMediaPathsForProject(String appId, String userId, String projectId) {

        List<String> mediaPaths = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_LOCAL_PATH
        };

        String selection = UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_PROJECT_ID + " = ?" + " AND "
                + UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_USER_ID + " = ?";

        String[] selectionArgs = {appId, projectId, userId};

        Cursor cursor = db.query(
                UnifiedAppDbContract.FormMediaEntry.TABLE_FORM_MEDIA,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        while (cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                    FormMediaEntry.COLUMN_FORM_MEDIA_LOCAL_PATH));
            if (path != null && !path.isEmpty()) {
                mediaPaths.add(path);
            }
        }
        return mediaPaths;
    }

    public List<FormMedia> getMediaForProjectWithGivenStatus(String appId, String projectId, String userId, int requestStatus, int days) {

        List<FormMedia> formMediaList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Date currentDate = new Date(System.currentTimeMillis() * 1000);
        Date beforeDate = new Date(currentDate.getTime() - days * 24 * 3600 * 1000L);

        String selection = UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_APP_ID + " = ?" + " AND "
                + UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_PROJECT_ID + " = ?" + " AND "
                + UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_REQUEST_STATUS + " = ?" + " AND "
                + UnifiedAppDbContract.FormMediaEntry
                .COLUMN_FORM_MEDIA_USER_ID + " = ?";

        String[] selectionArgs = {appId, projectId, String.valueOf(requestStatus), userId};

        Cursor cursor = db.query(
                UnifiedAppDbContract.FormMediaEntry.TABLE_FORM_MEDIA,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,
                null
        );

        while (cursor.moveToNext()) {

            long uploadTimeStamp = cursor.getLong(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_UPLOAD_TIMESTAMP));

            Date date = new Date(uploadTimeStamp * 1000);

            if (date.before(beforeDate)) {
                FormMedia formMedia = getFormMediaDBObject(cursor);
                formMediaList.add(formMedia);
            }
        }
        return formMediaList;
    }

    /**
     * Utility to create Media DB object from cursor
     * @param cursor
     * @return
     */
    private FormMedia getFormMediaDBObject(Cursor cursor) {

        String uuid = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_UUID));
        String appid = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_APP_ID));
        String userid = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_USER_ID));
        String projectid = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_PROJECT_ID));
        long form_submission_ts = cursor.getLong(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_SUBMISSION_TIMESTAMP));
        String media_local_path = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_LOCAL_PATH));
        double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_LONGITUDE));
        double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_LATITUDE));
        double gps_accuracy = cursor.getDouble(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_GPS_ACCURACY));
        boolean hasGeotag = cursor.getInt(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_HAS_GEOTAG)) > 0;
        int mediaType = cursor.getInt(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_TYPE));
        long media_click_timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_CLICK_TS));
        int subType = cursor.getInt(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_SUBTYPE));
        long uploadTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_UPLOAD_TIMESTAMP));
        int status = cursor.getInt(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_REQUEST_STATUS));
        int retries = cursor.getInt(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_UPLOAD_RETRIES));
        String mediaFileExtension = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_EXTENSION));
        byte[] bitmap = cursor.getBlob(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_BITMAP));
        int mediaActionType = cursor.getInt(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_FORM_MEDIA_ACTION_TYPE));

        String additionalProps = cursor.getString(cursor.getColumnIndexOrThrow(UnifiedAppDbContract.
                FormMediaEntry.COLUMN_ADDITIONAL_PROPERTIES));

        Map<String, String> additionalPropertiesMap = new HashMap<>();

        if (additionalProps != null && !additionalProps.isEmpty()) {
//            Type type = new TypeToken<Map<String, String>>() {}.getType();
//            additionalPropertiesMap = gson.fromJson(additionalProps, type);
            try {
                additionalPropertiesMap =  objectMapper.readValue(additionalProps, new TypeReference<Map<String, String>>() {});
            } catch (IOException e) {
                Utils.logError(LogTags.UNIFIED_DB_HELPER,"failed to parse json string for AdditionalProperties :: " + additionalProps);
                e.printStackTrace();
            }
        }
        FormMedia formMedia = new FormMedia(uuid, bitmap, media_local_path, appid, userid, projectid, hasGeotag,
                latitude, longitude, gps_accuracy, media_click_timestamp, mediaFileExtension, mediaActionType,
                mediaType, subType, status, retries, uploadTimestamp, form_submission_ts, additionalPropertiesMap);
        return formMedia;
    }
}