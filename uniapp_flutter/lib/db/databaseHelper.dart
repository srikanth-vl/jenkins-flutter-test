import '../models/sub_app.dart';
import '../ua_app_context.dart';
import 'dart:collection';
import 'package:sqflite/sqflite.dart';
import 'dart:async';
import 'dart:convert';
import '../db/models/project_submission.dart';
import '../models/project_specific_form.dart';
import '../models/project_type_configuartion.dart';
import '../utils/common_constants.dart';
import '../utils/media_upload_status.dart';
import '../utils/string_utils.dart';
import 'models/config_table.dart';
import 'dbSchemaConstants.dart';
import 'models/form_media_table.dart';
import 'models/project_form_table.dart';
import 'models/project_master_data_table.dart';
import '../utils/project_submission_upload_status.dart';
import '../models/entity_meta_data_configuration.dart';
import 'models/user_meta_data_table.dart';

import 'package:path/path.dart' as p;

class DatabaseHelper {
  static DatabaseHelper _databaseHelper; // Singleton DatabaseHelper
  static Database _database; // Singleton Database

  String noteTable = 'note_table';
  String colId = 'id';
  String colTitle = 'title';
  String colDescription = 'description';
  String colPriority = 'priority';
  String colDate = 'date';

  DatabaseHelper._createInstance(); // Named constructor to create instance of DatabaseHelper

  factory DatabaseHelper() {
    if (_databaseHelper == null) {
      _databaseHelper = DatabaseHelper
          ._createInstance(); // This is executed only once, singleton object
    }
    return _databaseHelper;
  }

  Future<Database> get database async {
    if (_database == null) {
      _database = await _initializeDatabase();
    }
    return _database;
  }

  Future<Database> _initializeDatabase() async {
    var databasesPath = await getDatabasesPath();
    String path = p.join(databasesPath, CommonConstants.DB_NAME);

    //open/create database at a given path
    var uniappDatabase = await openDatabase(path,
        version: CommonConstants.DB_VERSION,
        onCreate: _createDb,
        onUpgrade: _upgradeDb,
        onDowngrade: _onDowngrade);

    return uniappDatabase;
  }

  //for creating db
  void _createDb(Database db, int newVersion) async {
    Batch batch = db.batch();
    batch.execute(ConfigFilesEntry.CREATE_TABLE_QUERY);
    batch.execute(UserMetaEntry.CREATE_USER_META_TABLE);
    batch.execute(FormMediaEntry.SQL_CREATE_FORM_MEDIA_TABLE);
    batch.execute(ProjectSubmissionEntry.SQL_CREATE_PROJECT_SUBMISSION_TABLE);
    batch.execute(ProjectFormTableEntry.SQL_CREATE_PROJECT_FORM_TABLE);
    batch.execute(ProjectTableEntry.SQL_CREATE_PROJECT_TABLE);
    batch.execute(EntityMetaEntry.SQL_CREATE_ENTITY_META_TABLE);
    await batch.commit();
  }

//for upgrading db
  void _upgradeDb(Database db, int newVersion, int oldVersion) async {
    //todo: create defination for upgrade db
    // This database is only a cache for online data, so its upgrade policy is
    // to simply to discard the data and start over
    Batch batch = db.batch();
    batch.execute(ConfigFilesEntry.DELETE_CONFIG_FILE_TABLE);
    batch.execute(UserMetaEntry.DELETE_USER_META_TABLE);
    batch.execute(FormMediaEntry.SQL_DELETE_MEDIA_IMAGES_TABLE);
    batch.execute(ProjectSubmissionEntry.SQL_DELETE_PROJECT_SUBMISSION_TABLE);
    batch.execute(ProjectFormTableEntry.SQL_DELETE_PROJECT_FORM_TABLE);
    batch.execute(ProjectTableEntry.SQL_DELETE_PROJECT_TABLE);
    batch.execute(EntityMetaEntry.SQL_DELETE_ENTITY_META_TABLE);
    await batch.commit();

    // Create new tables
    _createDb(db, newVersion);
    //await db.execute(migrationScript));
  }

//for downgrade db
  void _onDowngrade(Database db, int oldVersion, int newVersion) {
    _upgradeDb(db, oldVersion, newVersion);
  }

  // Fetch Operation: Get all note objects from database
  Future<List<Map<String, dynamic>>> getConfigFileList(
      String userId, String name) async {
    Database db = await this.database;
    String whereString = '${ConfigFilesEntry.COLUMN_USER_ID} = ? AND '
        '${ConfigFilesEntry.COLUMN_CONFIG_NAME} = ?';
    List<dynamic> whereArguments = [userId, name];
    var result = await db.query(ConfigFilesEntry.TABLE_CONFIG,
        where: whereString, whereArgs: whereArguments);
    return result;
  }

//insert operation for User login details
  Future<int> insertUserMeta(UserMetaDataTable userMetaDataTable) async {
    Database db = await this.database;
    List<dynamic> whereArguments = [userMetaDataTable.userId];
    int result = await db.update(
        UserMetaEntry.TABLE_USER, userMetaDataTable.toMap(),
        where: UserMetaEntry.whereClause, whereArgs: whereArguments);
    print("REsult after update " + result.toString());
    if (result == 0) {
      result =
          await db.insert(UserMetaEntry.TABLE_USER, userMetaDataTable.toMap());
      print("Result after insert " + result.toString());
    }
    print("Result before return " + result.toString());
    return result;
  }

  /**
   * Get user meta data
   * @param username
   * @return
   */
  Future<UserMetaDataTable> getUserMeta(String username) async {
    // you will actually use after this query.
    List<dynamic> whereArguments = [username];
    Database db = await this.database;
    String whereString = '${UserMetaEntry.COLUMN_USER_ID} = ?';

    List<Map> users = await db.query(UserMetaEntry.TABLE_USER,
        where: UserMetaEntry.whereClause, whereArgs: whereArguments);

    if (users.length > 0)
      return UserMetaDataTable.fromMapObject(users[0]);
    else
      return null;
  }

  Future<int> insertConfig(ConfigFile configFile) async {
    Database db = await this.database;
    List<dynamic> whereArguments = [configFile.userId, configFile.configName];
    int result = await db.update(
        ConfigFilesEntry.TABLE_CONFIG, configFile.toMap(),
        where: ConfigFilesEntry.PRIMARY_KEY_WHERE_STRING,
        whereArgs: whereArguments);
    if (result == 0) {
      result =
          await db.insert(ConfigFilesEntry.TABLE_CONFIG, configFile.toMap());
    }
    return result;
//    var result =
//    await db.insert(ConfigFilesEntry.TABLE_CONFIG, configFile.toMap());
//    return result;
  }

  // Get the 'Map List' [ List<Map> ] and convert it to 'Note List' [ List<Note> ]
  Future<ConfigFile> getConfig(String userId, String name) async {
    var configList =
        await getConfigFileList(userId, name); // Get 'Map List' from database
    int count =
        configList.length; // Count the number of map entries in db table
    if (count > 0) {
      ConfigFile configFile = ConfigFile.fromMapObject(configList[0]);
      return configFile;
    }
    return null;
  }

  Future<int> insertProjectForm(ProjectFormTable projectForm) async {
    Database db = await this.database;
    var result = await db.insert(
        ProjectFormTableEntry.TABLE_PROJECT_FORM, projectForm.toMap());
    return result;
  }

  Future<int> insertProjectMasterData(
      ProjectMasterDataTable projectMasterDataTable) async {
    Database db = await this.database;
    List<dynamic> whereArguments = [
      projectMasterDataTable.projectAppId,
      projectMasterDataTable.projectUserId,
      projectMasterDataTable.projectId
    ];
    int result = await db.update(
        ProjectTableEntry.TABLE_PROJECT, projectMasterDataTable.toMap(),
        where: ProjectTableEntry.PRIMARY_KEY_WHERE_STRING,
        whereArgs: whereArguments);
    if (result == 0) {
      result = await db.insert(
          ProjectTableEntry.TABLE_PROJECT, projectMasterDataTable.toMap());
    }
    return result;
  }

  Future<int> getProjectTypeCountForUser(String userId) async {
    List<Map> projectFormList = List();
    Database db = await this.database;
    String whereString = '${ProjectFormTableEntry.COLUMN_USER_ID} = ?';
    List<dynamic> whereArguments = [userId];
    var result = await db.query(ProjectFormTableEntry.TABLE_PROJECT_FORM,
        where: whereString, whereArgs: whereArguments);
    projectFormList = result;
    int count = projectFormList.length;
    return count;
  }

  Future<ProjectTypeConfiguration> getProjectFormForApp(
      String userId, String appId) async {
    Map<String, Map<String, ProjectSpecificForm>> projectIdToFormMap =
        new Map<String, Map<String, ProjectSpecificForm>>();
    ProjectTypeConfiguration projectTypeConfiguration =
        new ProjectTypeConfiguration();
    List<Map> projectFormList = List();
    Database db = await this.database;
    String whereString = '${ProjectFormTableEntry.COLUMN_USER_ID} = ?'
        ' AND ${ProjectFormTableEntry.COLUMN_APP_ID} = ?';
    List<dynamic> whereArguments = [userId, appId];
    var result = await db.query(ProjectFormTableEntry.TABLE_PROJECT_FORM,
        where: whereString, whereArgs: whereArguments);
    projectFormList = result;
    int count = projectFormList.length;
    if (count > 0) {
      for (Map projectFormMap in projectFormList) {
        ProjectFormTable projectFormTable =
            ProjectFormTable.fromMapObject(projectFormMap);
        ProjectSpecificForm form =
            ProjectSpecificForm.fromJson(jsonDecode(projectFormTable.formData));
        ;
        if (projectIdToFormMap[projectFormTable.projectId] == null) {
          Map<String, ProjectSpecificForm> actionToFormMap =
              new Map<String, ProjectSpecificForm>();
          actionToFormMap[projectFormTable.formType] = form;
          projectIdToFormMap[projectFormTable.projectId] = actionToFormMap;
        } else {
          Map<String, ProjectSpecificForm> actionToFormMap =
              projectIdToFormMap[projectFormTable.projectId];
          if (actionToFormMap == null) {
            actionToFormMap = Map<String, ProjectSpecificForm>();
          }
          if (actionToFormMap[projectFormTable.formType] == null) {
            actionToFormMap[projectFormTable.formType] = form;
            projectIdToFormMap[projectFormTable.projectId] = actionToFormMap;
          } else {
            ProjectSpecificForm availableForm =
                actionToFormMap[projectFormTable.formType];
            if (availableForm.formversion <= form.formversion) {
              actionToFormMap[projectFormTable.formType] = form;
              projectIdToFormMap[projectFormTable.projectId] = actionToFormMap;
            }
          }
        }
      }
      projectTypeConfiguration.content = projectIdToFormMap;
      projectTypeConfiguration.userId = userId;
      projectTypeConfiguration.projecttype = appId;

      if (projectIdToFormMap.isNotEmpty) {
        return projectTypeConfiguration;
      } else {
        return null;
      }
    }
    return null;
  }

  Future<int> getProjectCountForUser(String userId) async {
    List<Map> projects = List();
    Database db = await this.database;
    String whereString = '${ProjectTableEntry.COLUMN_PROJECT_USER_ID} = ?';
    List<dynamic> whereArguments = [userId];
    var result = await db.query(ProjectTableEntry.TABLE_PROJECT,
        where: whereString, whereArgs: whereArguments);
    projects = result;
    int count = projects.length;
    return count;
  }

  Future<List<ProjectMasterDataTable>> getProjectsForUser(
      String userId, String appId,String groupKey, String groupValue) async {
    List<ProjectMasterDataTable> projects = List();
    Database db = await this.database;
    String whereString = '${ProjectTableEntry.COLUMN_PROJECT_APP_ID} = ? AND '
        '${ProjectTableEntry.COLUMN_PROJECT_USER_ID} = ? AND '
        '${ProjectTableEntry.COLUMN_PROJECT_ASSINGED_STATUS} = 1';
    List<dynamic> whereArguments = [appId, userId];
    if(groupKey != null && groupValue != null && groupValue.isNotEmpty && groupKey.isNotEmpty) {
      SubApp subApp = UAAppContext.getInstance().rootConfig.config.firstWhere((a) => a.appId == appId);
      if(subApp != null && subApp.groupingAttributes != null && subApp.groupingAttributes.isNotEmpty) {

        String groupFilterExpression = "";
        int i=0;
        for(String key in subApp.groupingAttributes) {
          if(key==groupKey) {
            if(i==subApp.groupingAttributes.length-1) {
              groupFilterExpression +=groupValue ;
            }
            else {groupFilterExpression +=groupValue+"#" ;
            }
          }

           else {
            if(i==subApp.groupingAttributes.length-1){
            groupFilterExpression +="%";
            } else {
              groupFilterExpression = groupFilterExpression+"%#";
            }
          }
          i++;
        }
        print('groupFilterExpression ${groupFilterExpression}');
        whereString +=' AND ${ProjectTableEntry.COLUMN_GROUPING_DIMENSION_VALUES} LIKE ?';
        whereArguments.add(groupFilterExpression);
      }
    }

    List<Map> result = await db.query(ProjectTableEntry.TABLE_PROJECT,
        where: whereString, whereArgs: whereArguments);

    int count = result.length;
    if (count > 0) {
      for (Map row in result) {
        projects.add(ProjectMasterDataTable.fromMapObject(row));
      }
    }
    return projects;
  }

  Future<int> updateProjectSubmissionStatus(
      String userId,
      String appId,
      String projectId,
      int ts,
      ProjectSubmissionUploadStatus projectSubmissionUploadStatus) async {
    String updateExpression =
        'UPDATE ${ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION} SET '
        ' ${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS} = ? '
        '${ProjectSubmissionEntry.PRIMARY_KEY_WHERE_STRING}';

    List<dynamic> whereArguments = [
      ProjectSubmissionUploadStatusHelper.getValue(
          projectSubmissionUploadStatus),
      appId,
      userId,
      projectId,
      ts
    ];
    Database db = await this.database;
    int result = await db.rawUpdate(updateExpression, whereArguments);
    return result;
  }

  Future<int> updateProjectSubmission(
      String userId,
      String appId,
      String projectId,
      int sync_ts,
      int submission_ts,
      ProjectSubmissionUploadStatus projectSubmissionUploadStatus,
      String responseMessage,
      int pendingRetries) async {
    String updateExpression =
        'UPDATE ${ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION} SET '
        ' ${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS} = ? ,'
        ' ${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_SERVER_SYNC_TS} = ? ,'
        ' ${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RESPONSE} = ? ,'
        ' ${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RETRY_COUNT} = ? '
        ' WHERE ${ProjectSubmissionEntry.PRIMARY_KEY_WHERE_STRING}';

    List<dynamic> whereArguments = [
      ProjectSubmissionUploadStatusHelper.getValue(
          projectSubmissionUploadStatus),
      sync_ts,
      responseMessage,
      pendingRetries,
      appId,
      userId,
      projectId,
      submission_ts
    ];
    Database db = await this.database;
    int result = await db.rawUpdate(updateExpression, whereArguments);
    return result;
  }

  Future<int> updateAssignedStatusOfProjectFromUser(
      String userId, String appId, String projectId, int assignedStatus) async {
    String updateExpression = 'UPDATE ${ProjectTableEntry.TABLE_PROJECT} SET '
        ' ${ProjectTableEntry.COLUMN_PROJECT_ASSINGED_STATUS} = ${assignedStatus} WHERE '
        '${ProjectTableEntry.PRIMARY_KEY_WHERE_STRING}';

    List<dynamic> whereArguments = [appId, userId, projectId];
    Database db = await this.database;
    int result = await db.rawUpdate(updateExpression, whereArguments);
    return result;
  }

  Future<ProjectMasterDataTable> getLatestProjectEntry(
      String userId, String appId, String projectId) async {
    List<Map> projects = List();
    Database db = await this.database;
    List<dynamic> whereArguments = [appId, userId, projectId];
    var result = await db.query(ProjectTableEntry.TABLE_PROJECT,
        where: ProjectTableEntry.PRIMARY_KEY_WHERE_STRING,
        whereArgs: whereArguments);
    projects = result;
    int count = projects.length;
    if (count > 0) {
      return ProjectMasterDataTable.fromMapObject(result[0]);
    }
    return null;
  }

  Future<int> getProjectSubmissionStatus(
      String appId, String userId, String projectId, int timestamp) async {
    List<String> columns = List();
    columns.add(ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS);

    List<ProjectMasterDataTable> projects = List();
    Database db = await this.database;
    print("INSERTING TO DB PROJECT SUBMISSION INFO : " +
        appId +
        "  " +
        userId +
        "  " +
        projectId);
    String whereString =
        '${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID} = ?'
        ' AND  ${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID} = ?'
        ' AND  ${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID} = ?'
        ' AND  ${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP} = ?';
    List<dynamic> whereArguments = [appId, userId, projectId];
    List<Map> result = await db.query(
        ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,
        where:
            '${ProjectSubmissionEntry.PRIMARY_KEY_WHERE_STRING_WITHOUTTIMESTAMP} ${timestamp}',
        whereArgs: whereArguments);

    int status = CommonConstants.DEFAULT_UPLOAD_STATUS;

    if (result.length > 0) {
      ProjectSubmission project = ProjectSubmission.fromMapObject(result[0]);
      status = project.submissionStatus;
    }
    return status;
  }

  Future<int> getProjectSubmissionCountForGivenStatusList(
      String appId,
      String userId,
      String projectId,
      List<ProjectSubmissionUploadStatus> uploadStatusList) async {
    List<String> statusList = new List();
    for (ProjectSubmissionUploadStatus status in uploadStatusList) {
      statusList
          .add(ProjectSubmissionUploadStatusHelper.getValue(status).toString());
    }
    String status =
        StringUtils.getconcatenatedStringFromStringList(",", statusList);
    status = "(" + status + ")";
    List<String> columns = List();
    columns.add(ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS);

    List<ProjectMasterDataTable> projects = List();
    Database db = await this.database;
    String whereString =
        '${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID} = ?'
        ' AND ${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID} = ?'
        ' AND ${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID} = ?'
        ' AND ${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS} IN ${status}';
    List<dynamic> whereArguments = [appId, userId, projectId];
    List<Map> result = await db.query(
        ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,
        columns: columns,
        where: whereString,
        whereArgs: whereArguments);
    int count = result.length;
    return count;
  }

  Future<int> getFormMediaCountForGivenStatusList(String appId, String userId,
      String projectId, List<MediaUploadStatus> uploadStatusList) async {
    List<String> statusList = new List();
    for (MediaUploadStatus status in uploadStatusList) {
      statusList.add(MediaUploadStatusHelper.getValue(status).toString());
    }
    String status =
        StringUtils.getconcatenatedStringFromStringList(",", statusList);
    status = "(" + status + ")";
    List<String> columns = List();
    columns.add(FormMediaEntry.COLUMN_FORM_MEDIA_REQUEST_STATUS);

    List<ProjectMasterDataTable> projects = List();
    Database db = await this.database;
    String whereString = '${FormMediaEntry.COLUMN_FORM_MEDIA_APP_ID} = ?'
        ' AND ${FormMediaEntry.COLUMN_FORM_MEDIA_USER_ID} = ?'
        ' AND ${FormMediaEntry.COLUMN_FORM_MEDIA_PROJECT_ID} = ?'
        ' AND ${FormMediaEntry.COLUMN_FORM_MEDIA_REQUEST_STATUS} IN ${status}';
    List<dynamic> whereArguments = [appId, userId, projectId];
    List<Map> result = await db.query(FormMediaEntry.TABLE_FORM_MEDIA,
        columns: columns, where: whereString, whereArgs: whereArguments);
    int count = result.length;
    return count;
  }

  Future<int> deleteAllProjectSubmission(appId, userId, projectId) async {
    Database db = await this.database;
    List<dynamic> whereArguments = [appId, userId, projectId];
    String whereString =
        '${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID} = ? AND ' +
            '${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID}  = ? AND ' +
            '${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID} = ?';
    int delete = await db.delete(
        ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,
        where: whereString,
        whereArgs: whereArguments);
    return delete;
  }

  Future<int> deleteAllTheMediaForProject(
      String appId, String userId, String projectId) async {
    Database db = await this.database;
    List<dynamic> whereArguments = [appId, userId, projectId];
    String whereString = '${FormMediaEntry.COLUMN_FORM_MEDIA_APP_ID} = ? AND ' +
        '${FormMediaEntry.COLUMN_FORM_MEDIA_USER_ID}  = ? AND ' +
        '${FormMediaEntry.COLUMN_FORM_MEDIA_PROJECT_ID} = ?';
    int delete = await db.delete(FormMediaEntry.TABLE_FORM_MEDIA,
        where: whereString, whereArgs: whereArguments);
    return delete;
  }

  Future<int> deleteProject(
      String appId, String userId, String projectId) async {
    Database db = await this.database;
    List<dynamic> whereArguments = [appId, userId, projectId];
    int delete = await db.delete(ProjectTableEntry.TABLE_PROJECT,
        where: ProjectTableEntry.PRIMARY_KEY_WHERE_STRING,
        whereArgs: whereArguments);
    return delete;
  }

  Future<Map<String, int>> getProjectIdToLastSyncTsMap(
      String userId, String appId, bool assignedStatus) async {
    Map<String, int> projectIdTotsMap = new Map();
    Database db = await this.database;
    Map<String, int> projectIdToTs = new Map();

    List<String> columns = [
      ProjectTableEntry.COLUMN_SERVER_SYNC_TS,
      ProjectTableEntry.COLUMN_PROJECT_ID
    ];

    String selection = ProjectTableEntry.COLUMN_PROJECT_USER_ID +
        " = ?" +
        " AND " +
        ProjectTableEntry.COLUMN_PROJECT_APP_ID +
        " = ?" +
        (assignedStatus
            ? " AND " +
                ProjectTableEntry.COLUMN_PROJECT_ASSINGED_STATUS +
                " = 1"
            : "");
    List<dynamic> selectionArgs = [userId, appId];

    List<Map> projectList = await db.query(ProjectTableEntry.TABLE_PROJECT,
        columns: columns, where: selection, whereArgs: selectionArgs);
    if (projectList.length > 0) {
      projectList.forEach((a) {
        ProjectMasterDataTable project =
            ProjectMasterDataTable.fromMapObject(a);
        projectIdTotsMap[project.projectId] = project.projectServerSyncTs;
      });
    }
    return projectIdTotsMap;
  }

  Future<List<FormMediaTable>> getFormMediaEntries(
      List<int> statusList, int batchSize) async {
    List<FormMediaTable> formMediaEntries = List();
    Database db = await this.database;

    String statusValues = "( ";
    for (int i in statusList) {
      statusValues = statusValues + i.toString() + ",";
    }
    statusValues = statusValues.substring(0, statusValues.length - 1);
    statusValues += ")";

    String whereString = '${FormMediaEntry.COLUMN_FORM_MEDIA_REQUEST_STATUS}' +
        " IN " +
        statusValues;
    List<Map> result =
        await db.query(FormMediaEntry.TABLE_FORM_MEDIA, where: whereString);

    int count = result.length;
    if (count > 0) {
      for (Map row in result) {
        formMediaEntries.add(FormMediaTable.fromMapObject(row));
      }
    }
    return formMediaEntries;
  }

  Future<int> updateFormMedia(Map<String, dynamic> values, String appId,
      String userId, String uuid) async {
    Database db = await this.database;

    String whereStr = FormMediaEntry.PRIMARY_KEY_WHERE_STRING;
    List<dynamic> whereArguments = [appId, userId, uuid];
    return db.update(FormMediaEntry.TABLE_FORM_MEDIA, values,
        where: whereStr, whereArgs: whereArguments);
  }

  Future<int> updateFormMediaForProjectAtGivenTimestamp(
      Map<String, dynamic> values, String projectId, int ts) async {
    Database db = await this.database;

    String whereStr = FormMediaEntry.COLUMN_FORM_MEDIA_PROJECT_ID +
        " = ? " +
        ", " +
        FormMediaEntry.COLUMN_FORM_SUBMISSION_TIMESTAMP +
        " = " +
        ts.toString();
    List<dynamic> whereArguments = [projectId];

    return db.update(FormMediaEntry.TABLE_FORM_MEDIA, values,
        where: whereStr, whereArgs: whereArguments);
  }

  Future<FormMediaTable> getFormMedia(
      String uuid, String appId, String userId) async {
    FormMediaTable formMedia = null;
    Database db = await this.database;

    List<dynamic> whereArguments = [appId, userId, uuid];

    List<Map> result = await db.query(FormMediaEntry.TABLE_FORM_MEDIA,
        where: FormMediaEntry.PRIMARY_KEY_WHERE_STRING,
        whereArgs: whereArguments);

    int count = result.length;
    if (count > 0) {
      formMedia = FormMediaTable.fromMapObject(result[0]);
    }
    return formMedia;
  }

  Future<int> insertFormMedia(FormMediaTable formMedia) async {
    Database db = await this.database;

    List<dynamic> whereArguments = [
      formMedia.mediaAppId,
      formMedia.mediaUserId,
      formMedia.mediaUuid
    ];

    int result = await db.update(
        FormMediaEntry.TABLE_FORM_MEDIA, formMedia.toMap(),
        where: FormMediaEntry.PRIMARY_KEY_WHERE_STRING,
        whereArgs: whereArguments);

    if (result == 0) {
      result =
          await db.insert(FormMediaEntry.TABLE_FORM_MEDIA, formMedia.toMap());
    }
    return result;
  }

  Future<List<ProjectSubmission>> getAllProjectsToSubmit(
      List<int> statusList) async {
    List<ProjectSubmission> projects = List();
    Database db = await this.database;

    String statusValues = "( ";
    for (int i in statusList) {
      statusValues = statusValues + i.toString() + ",";
    }
    statusValues = statusValues.substring(0, statusValues.length - 1);
    statusValues += ")";

    String whereString =
        '${ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS}' +
            " IN " +
            statusValues;
    List<Map> result = await db.query(
        ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,
        where: whereString);

    int count = result.length;
    if (count > 0) {
      for (Map row in result) {
        projects.add(ProjectSubmission.fromMapObject(row));
      }
    }
    return projects;
  }

  Future<int> addOrUpdateProjectSubmission(
      ProjectSubmission projectSubmission) async {
    Database db = await this.database;
    int result = await db.insert(
        ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,
        projectSubmission.toMap());
    return result;
  }

  Future<int> deleteFormMedia(String uuid, String appId, String userId) async {
    Database db = await this.database;
    List<dynamic> whereArguments = [uuid, appId, userId];
    String whereString = '${FormMediaEntry.COLUMN_FORM_MEDIA_UUID} = ? AND ' +
        '${FormMediaEntry.COLUMN_FORM_MEDIA_APP_ID}  = ? AND ' +
        '${FormMediaEntry.COLUMN_FORM_MEDIA_USER_ID} = ?';
    int delete = await db.delete(FormMediaEntry.TABLE_FORM_MEDIA,
        where: whereString, whereArgs: whereArguments);
    return delete;
  }

  Future<int> getFormMediaCountForApp(
      String appid, String userid, List<int> statusList) async {
    int count = 0;
    Database db = await this.database;

    String statusValues = "( ";
    for (int i in statusList) {
      statusValues = statusValues + i.toString() + ",";
    }
    statusValues = statusValues.substring(0, statusValues.length - 1);
    statusValues += ")";

    List<String> projection = [
      FormMediaEntry.COLUMN_FORM_MEDIA_UUID,
      FormMediaEntry.COLUMN_FORM_MEDIA_PROJECT_ID,
      FormMediaEntry.COLUMN_FORM_SUBMISSION_TIMESTAMP
    ];

    String whereStr = FormMediaEntry.COLUMN_FORM_MEDIA_APP_ID +
        " = ?" +
        " AND " +
        FormMediaEntry.COLUMN_FORM_MEDIA_USER_ID +
        " = ?" +
        " AND " +
        FormMediaEntry.COLUMN_FORM_MEDIA_REQUEST_STATUS +
        " IN " +
        statusValues;

    List<dynamic> whereArgs = [appid, userid];
    List<Map> formMediaProj = await db.query(FormMediaEntry.TABLE_FORM_MEDIA,
        whereArgs: whereArgs, where: whereStr, columns: projection);

    if (formMediaProj != null && formMediaProj.isNotEmpty) {
      for (Map formMediaMap in formMediaProj) {
        int form_sub_ts =
            formMediaMap[FormMediaEntry.COLUMN_FORM_SUBMISSION_TIMESTAMP];
        String projectId =
            formMediaMap[FormMediaEntry.COLUMN_FORM_MEDIA_PROJECT_ID];
        ProjectMasterDataTable mProject =
            await getLatestProjectEntry(userid, appid, projectId);

        if (form_sub_ts > 0 &&
            mProject != null &&
            mProject.projectAssignedStatus == 1) count++;
      }
    }
    return count;
  }

  Future<int> getProjectSubmissionCountForApp(
      String appId, String userId, List<int> uploadStatusList) async {
    Database db = await this.database;

    String statusValues = "( ";
    for (int status in uploadStatusList) {
      statusValues = statusValues + status.toString() + ",";
    }
    statusValues = statusValues.substring(0, statusValues.length - 1);
    statusValues += ")";

    // Define a projection that specifies which columns from the database
    // you will actually use after this query.
    List<String> projection = [
      ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID,
      ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS
    ];

    String selection = ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID +
        " = ?" +
        " AND " +
        ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID +
        " = ?" +
        " AND " +
        ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS +
        " IN " +
        statusValues;

    List<dynamic> selectionArgs = [appId, userId];

    List<Map> projectSubList = await db.query(
        ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION,
        where: selection,
        whereArgs: selectionArgs,
        columns: projection);

    int count = 0;

    if (projectSubList != null && projectSubList.isNotEmpty) {
      for (Map projSubMap in projectSubList) {
        String projectId = projSubMap[
            ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID];
        ProjectMasterDataTable mProject =
            await getLatestProjectEntry(userId, appId, projectId);
        if (mProject != null && mProject.projectAssignedStatus == 1) {
          count++;
        }
      }
    }
    return count;
  }

  Future<Map<String, List<String>>> getProjectGroupsAttributeValues(String userId, String appId, List<String> groupAttribute) async {
    Map<String, List<String>> attributeToValuesMap =  new Map();
    Database db = await this.database;

    List<String> columns = [
      ProjectTableEntry.COLUMN_GROUPING_DIMENSION_VALUES
    ];

    String selection = ProjectTableEntry
        .COLUMN_PROJECT_USER_ID + " = ?" + " AND "
        + ProjectTableEntry
            .COLUMN_PROJECT_APP_ID + " = ?";
    List<dynamic> selectionArgs = [userId, appId];

    List<Map> projectList = await db.query(
        ProjectTableEntry.TABLE_PROJECT,
        columns: columns,
        where: selection,
        whereArgs: selectionArgs
    );
    List<String> projectAttributeValuesList = List<String>();
    if(projectList != null && projectList.length >  0) {
      for(Map a in projectList) {
        String dimensionValueString =  a[ProjectTableEntry.COLUMN_GROUPING_DIMENSION_VALUES];
        if(dimensionValueString!= null && dimensionValueString.isNotEmpty) {
          projectAttributeValuesList.add(dimensionValueString);
        }
      }
      for(int i=0; i<groupAttribute.length; i++) {
        String key = groupAttribute[i];
        List<String> values = List<String>();
        for(String dimensionValueString in projectAttributeValuesList ) {
          List<String> dimensionValues = StringUtils.getStringListFromDelimiter("#", dimensionValueString);
          if(dimensionValues.length >0 && dimensionValues.length-1 >=i) {
            String value  = dimensionValues[i];
            if(value != null && value.isNotEmpty && value != 'null' && !values.contains(value)) {
              values.add(value);
            }
          }
          attributeToValuesMap[key] = values;
        }
      }
    }
    return attributeToValuesMap;
  }

   Future<List<String>> getProjectIdsForFilterQuery(String userId, String appId, String dimensionValues) async {
     List<String> projectIds = new List();

     Database db = await this.database;
     Set<String> projectIdSet = new HashSet();

     List<dynamic> selectionArgs = [userId, appId, dimensionValues];

     String rawQuery = "SELECT " + ProjectTableEntry.COLUMN_PROJECT_ID + ", " +
         ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES +
         " FROM " + ProjectTableEntry.TABLE_PROJECT + " WHERE " +
         ProjectTableEntry.COLUMN_PROJECT_USER_ID + " = ? " + " AND " + ProjectTableEntry.COLUMN_PROJECT_APP_ID
         + " = ? " + " AND " + ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES + " LIKE ? ";

     List<Map> projectValues = await db.rawQuery(rawQuery, selectionArgs);

     if (projectValues.length > 0) {
       for (Map value in projectValues) {
         String projectId = value[ProjectTableEntry.COLUMN_PROJECT_ID];
         projectIdSet.add(projectId);
       }
     }
     projectIds.addAll(projectIdSet);
     return projectIds;
   }
  Future<bool> insertEntityMetaDataConfiguration(EntityMetaDataConfiguration entityMetaDataConfiguration) async {
    Database db = await this.database;
    for(Entities entities in entityMetaDataConfiguration.entities) {
      List<dynamic> whereArguments = [entities.superAppId, entities.appId, entities.projectId, entities.userId, entities.parentEntity, entities.entityName];
      int result = await db.update(
          EntityMetaEntry.TABLE_ENTITY_METADATA, entities.toJson(),
          where: EntityMetaEntry.PRIMARY_KEY_WHERE_CLAUSE,
          whereArgs: whereArguments);
      if (result == 0) {
        result =
        await db.insert(EntityMetaEntry.TABLE_ENTITY_METADATA, entities.toJson());
      }
    }
    return true;
  }
  Future<int> getEntityMetaDataConfiguration(String superAppId) async {
    List<Map> entities = List();
    Database db = await this.database;
    String whereString = '${EntityMetaEntry.COLUMN_SUPER_APP_ID} = ?';
    List<dynamic> whereArguments = [superAppId];
    var result = await db.query(EntityMetaEntry.TABLE_ENTITY_METADATA,
        where: whereString, whereArgs: whereArguments);
    entities = result;
    int count = entities ==  null? 0 : entities.length;
    return count;
  }

}
