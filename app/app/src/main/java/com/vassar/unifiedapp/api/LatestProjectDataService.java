package com.vassar.unifiedapp.api;

import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.model.LatestFieldValue;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectListFieldModel;
import com.vassar.unifiedapp.model.ProjectListFieldValue;
import com.vassar.unifiedapp.model.ProjectSubmission;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LatestProjectDataService {

    private static final LatestProjectDataService instance = new LatestProjectDataService();

    public static LatestProjectDataService getInstance() {
        return instance;
    }

    private LatestProjectDataService() { }

    /**
     * This method returns the latest values for a project among the Project table
     * and the ProjectSubmission table
     */
    public List<LatestFieldValue> getLatestValuesForProject(String userId, String appId
            , String projectId) {

        List<LatestFieldValue> latestFieldValues = new ArrayList<>();

        // Get the latest values from the ProjectSubmission and Project table
        // Compare them
        // Return the fields

        // TODO : Get latest values according to the fields of the form, from different submissions
        // so that we can show latest values for more fields. Get clarity is this is important!
        // Also, for values from ProjectSubmission, we will only be able to show the latest values,
        // but not the max values!

        // Latest values from the Project table (Meta Data table)
        Project projectData = UAAppContext.getInstance().getDBHelper().getLatestProjectEntry(userId, appId, projectId);

        // Latest values from the ProjectSubmission table
        ProjectSubmission projectSubmission = UAAppContext.getInstance().getDBHelper()
                .getLatestProjectSubmissionEntry(userId, appId, projectId);

        if (projectData == null && projectSubmission == null) {
            Utils.logError(UAAppErrorCodes.PROJECT_LIST_FETCH, "Project -- " + projectId +
                    "seems to have been deleted form the DB for appId -- " + appId + " -- userId -- " + userId);
            return latestFieldValues;
        }

        if (projectSubmission == null) {
            // Submission is null, but Project table has latest values
            latestFieldValues.addAll(getLatestFieldValuesFromProjectTable(projectData.mFields));
            return latestFieldValues;
        }

        if (projectData == null) {
            // Project not in table, error case. Log error, and return submission values
            Utils.logError(UAAppErrorCodes.PROJECT_ERROR, "No project in table, but present " +
                    "in Submission -- projectId -- "+ projectSubmission.getProjectId() + " -- appId -- "
                    + projectSubmission.getAppId() + "  -- userId -- " + projectSubmission.getUserId()
                    + " -- returning values from the project submission table");
            latestFieldValues.addAll(getLatestFieldValuesFromProjectSubmissionTable(projectSubmission));
            return latestFieldValues;
        }

        if (projectData.getLastSyncTimestamp() > projectSubmission.getTimestamp()) {
            latestFieldValues.addAll(getLatestFieldValuesFromProjectTable(projectData.mFields));
            return latestFieldValues;
        } else {
            latestFieldValues.addAll(getLatestFieldValuesFromProjectSubmissionTable(projectSubmission));
            return latestFieldValues;
        }
    }

    // Convert ProjectListFieldModels of a project to LatestFieldValue list
    private List<LatestFieldValue> getLatestFieldValuesFromProjectTable(ArrayList<ProjectListFieldModel> fields) {
        List<LatestFieldValue> latestFieldValues = new ArrayList<>();
        for (ProjectListFieldModel fieldModel : fields) {
            latestFieldValues.add(new
                    LatestFieldValue(fieldModel.mIdentifier,
                    fieldModel.mProjectListFieldValue.mLabel,
                    fieldModel.mProjectListFieldValue.mValue));
        }
        return latestFieldValues;
    }

    // Convert ProjectSubmission fields to LatestFieldValue list
    private List<LatestFieldValue> getLatestFieldValuesFromProjectSubmissionTable(ProjectSubmission projectSubmission) {
        List<LatestFieldValue> latestFieldValues = new ArrayList<>();
        try {
            JSONObject fieldsObject = new JSONObject(projectSubmission.getFields());
            JSONArray jsonArray = fieldsObject.getJSONArray(Constants.SUBMISSION_FIELD_ARRAY);
            for (int i=0; i<jsonArray.length(); i++) {
                JSONObject field = jsonArray.getJSONObject(i);

                String key = field.getString("key");
                String val = field.getString("val");
                String label = null;
                if (val != null && !val.isEmpty()) {
                    label = "P : " + val;
                }
                latestFieldValues.add(new LatestFieldValue(key, label, val));
            }
            return latestFieldValues;
        } catch (JSONException e) {
            Utils.logError(UAAppErrorCodes.JSON_ERROR, "Error parsing JSON of the fields "
                    + "for project -- " + projectSubmission.getProjectId() + " -- userId -- "
                    + projectSubmission.getUserId() + " -- appId -- " + projectSubmission.getAppId()
                    + " -- continuing without showing latest values");
            return  latestFieldValues;
        }
    }
    public Map<String, LatestFieldValue> getLatestFieldValuesFromProjectSubmissionTable(String userId, String appId
            , String projectId, Long lastSyncTimestampOfProjectOnServer) {

        // get list of projectSubmissions done after given timestamps
        List<ProjectSubmission> projectSubmissionList = UAAppContext.getInstance().getDBHelper()
                .getProjectSubmissionEntryListAfterGivenTimestamp(userId, appId, projectId, lastSyncTimestampOfProjectOnServer);

        Map<String,LatestFieldValue> keyToFieldValueMap = new HashMap<>();
        // get consolidated list of fieldValues submitted till now
        for (ProjectSubmission projectSubmission : projectSubmissionList) {
            List<LatestFieldValue>  fieldValuesList = getLatestFieldValuesFromProjectSubmissionTable(projectSubmission);

            for (LatestFieldValue fieldValue : fieldValuesList) {
                keyToFieldValueMap.put(fieldValue.getKey(), fieldValue);
            }

        }
        return keyToFieldValueMap;
    }
    private Map<String, LatestFieldValue> getKeyToFieldValueMap(List<LatestFieldValue> latestFieldValueList) {
        Map<String,LatestFieldValue> keyToFieldValueMap = new HashMap<>();
        if(latestFieldValueList == null || latestFieldValueList.isEmpty()) {
            return keyToFieldValueMap;
        }
        for (LatestFieldValue fieldValue: latestFieldValueList) {
            keyToFieldValueMap.put(fieldValue.getKey(), fieldValue);
        }

        return keyToFieldValueMap;

    }
    /**
     * This method returns the key To latestValuesField Map for a project among the Project table
     * and the ProjectSubmission table
     */
    public Map<String, LatestFieldValue> getKeyLatestValuesMapForProject(String userId, String appId
            , String projectId) {

        List<LatestFieldValue> latestFieldValues = new ArrayList<>();

        // Get the latest values from the ProjectSubmission and Project table
        // Compare them
        // Return the fields

        // TODO : Get latest values according to the fields of the form, from different submissions
        // so that we can show latest values for more fields. Get clarity is this is important!
        // Also, for values from ProjectSubmission, we will only be able to show the latest values,
        // but not the max values!

        // Latest values from the Project table (Meta Data table)
        Project projectData = UAAppContext.getInstance().getDBHelper().getLatestProjectEntry(userId, appId, projectId);

        // Latest values from the ProjectSubmission table
        ProjectSubmission projectSubmission = UAAppContext.getInstance().getDBHelper()
                .getLatestProjectSubmissionEntry(userId, appId, projectId);

        if (projectData == null && projectSubmission == null) {
            Utils.logError(UAAppErrorCodes.PROJECT_LIST_FETCH, "Project -- " + projectId +
                    "seems to have been deleted form the DB for appId -- " + appId + " -- userId -- " + userId);
            return getKeyToFieldValueMap(latestFieldValues);
        }

        if (projectSubmission != null && projectData != null && projectData.getLastSyncTimestamp() < projectSubmission.getTimestamp()) {
            latestFieldValues.addAll(getLatestFieldValuesFromProjectSubmissionTable(projectSubmission));
            return getKeyToFieldValueMap(latestFieldValues);
        }

        return getKeyToFieldValueMap(latestFieldValues);
    }
    public String getLatestValueForKey(Map<String, LatestFieldValue> keyToLatestSubmittedValue, List<ProjectListFieldModel> projectFieldsFromServer, String key ) {
        String value = null;
        if(keyToLatestSubmittedValue != null && !keyToLatestSubmittedValue.isEmpty() && keyToLatestSubmittedValue.get(key)!= null) {
            value = keyToLatestSubmittedValue.get(key).getValue();
        } else {
            for (ProjectListFieldModel fields: projectFieldsFromServer) {
                if(fields.mIdentifier.equalsIgnoreCase(key)) {
                    ProjectListFieldValue valueObject = fields.mProjectListFieldValue;
                    if(valueObject != null && valueObject.mValue != null) {
                        value = valueObject.mValue;
                    }
                    break;
                }
            }
        }
        return value;
    }
}
