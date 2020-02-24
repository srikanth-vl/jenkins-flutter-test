import 'dart:io';

import 'package:synchronized/synchronized.dart';
import '../resources/root_config_provider.dart';
import '../resources/project_list_provider.dart';
import '../resources/project_type_provider.dart';
import '../resources/app_meta_config_provider.dart';
import '../resources/map_config_provider.dart';
import '../models/app_meta_data_config.dart';
import '../resources/entity_config_provider.dart';
import '../resources/localization_config_provider.dart';
import '../ua_app_context.dart';
import '../utils/network_utils.dart';
import '../error/all_custom_exception.dart';
import '../log/uniapp_logger.dart';


class AppBackGroundSyncService {
  static var lock = new Lock();
  static Object SYNC_LOCK = new Object();
  static bool isSyncInProgress = false;
  Logger logger = getLogger("AppBackGroundSyncService");
  static Object getSyncLock() {
    return SYNC_LOCK;
  }

  execute() async {
    logger.d("Start of App Background Sync Thread");
// todo check if internet available


    // Add a check if Background thread sync is in progress
    // Then do not have to run
    await lock.synchronized(() async {
      if (isSyncInProgress) {
        logger.d(
            "Another Background Sync thread is in progress - aborting this call");
        return true;
      }
      isSyncInProgress = true;


      bool result = true;
      // Check if server available
      bool isOnline  =  await networkUtils.hasActiveInternet();
      if (!isOnline) {
        // App offline - do not run background sync
        isSyncInProgress = false;
        logger.d( "Cannot initiate the sync without an active internet connection");
        return false;
      }

      // Start APP MD Config Sync
//        if (Thread.currentThread().isInterrupted())
//  return false;
      try{
        await startAppMDConfigSync();

        // Start Localization Config Sync
// //  if (Thread.currentThread().isInterrupted())
//  return false;
        await startLocalizationConfigSync();

        // Start Root Config Sync
        //  if (Thread.currentThread().isInterrupted())
//  return false;
        await startRootConfigSync();

        //Start Entity Config Sync
        //  if (Thread.currentThread().isInterrupted())
//  return false;
        await startEntityConfigSync();

        // Start Map Config Sync
        //  if (Thread.currentThread().isInterrupted())
//  return false;
        await startMapConfigSync();

        // Start Project Type Sync
        //  if (Thread.currentThread().isInterrupted())
//  return false;
        await startProjectTypeSync();

        // Start Project List Sync
        //  if (Thread.currentThread().isInterrupted())
//  return false;
        await startProjectListSync();


        isSyncInProgress = false;
      } on SocketException catch (e) {
        result = false;
        logger.e('SocketException :: Could not connect to server');
      }  on TokenExpiredException catch (e) {
        result = false;
        logger.e( "Token expired -- stopping sync", e);
      } on AppCriticalException catch (e) {
        result = false;
        logger.e( "App critical exception -- stopping sync", e);
      } on AppOfflineException catch (e) {
        result = false;
        logger.e("App is offline, cannot continues sync -- stopping sync");
      } on UAException catch (e) {
        result = false;
        logger.e("UAException while background sync was" +
            " in progress -- stopping sync");
      } on Exception catch (e) {
        result = false;
        logger.e("Exception while background sync was" +
            " in progress -- stopping sync");
      } on Error catch (e) {
        result = false;
        logger.e("Error occured while background sync was" +
            " in progress -- stopping sync");
      } finally {
        isSyncInProgress = false;
      }
      isSyncInProgress = false;
      logger.d("App Background completed. Status -- ${result}");
      return result;
    });

  }

  void startAppMDConfigSync() async {
    if(!UAAppContext.getInstance().isLoggedIn)
      return;
// Read AppMDConfig from Server and update local DB if there is a change
    AppMetaConfigProvider appMetaConfigProvider = AppMetaConfigProvider.instance;
    try {
      await appMetaConfigProvider.fetchFromServerForBackgroundSync();
    } on UAException catch (e) {
      logger.e('Could not sync app meta config');
    }
  }

  void startRootConfigSync() async {
    if(!UAAppContext.getInstance().isLoggedIn)
      return;
    RootConfigProvider rootConfigProvider =  RootConfigProvider.getInstance();
    try {
      await rootConfigProvider.fetchFromServerForBackgroundSync();
    } on UAException catch (e) {
      logger.e('Could not sync root config');
    }
  }

  void startLocalizationConfigSync() async {
    if(!UAAppContext.getInstance().isLoggedIn)
      return;
    LocalizationConfigProvider localizationConfigProvider =  LocalizationConfigProvider.getInstance();
    try {
      await localizationConfigProvider.fetchFromServerForBackgroundSync();
    } on UAException catch (e) {
      logger.e('Could not sync localization config');
    }
  }

  void startMapConfigSync() async {
    if(!UAAppContext.getInstance().isLoggedIn)
      return;
    try {
      MapConfigProvider mapConfigProvider = MapConfigProvider.getInstance();
      await mapConfigProvider.fetchFromServerForBackgroundSync();
    } on UAException catch (e) {
      logger.e('Could not sync map config');
    }
  }
  void startProjectTypeSync() async {
    if(!UAAppContext.getInstance().isLoggedIn)
      return;
    try {
      ProjectTypeProvider projectTypeProvider = ProjectTypeProvider
          .getInstance();
      await projectTypeProvider.fetchFromServerForBackgroundSync();
    } on UAException catch (e) {
      logger.e('Could not sync project type config');
    }
  }

  void startProjectListSync() async {
    if(!UAAppContext.getInstance().isLoggedIn)
      return;
    try {
      ProjectListProvider projectListProvider =  ProjectListProvider.getInstance();
      await projectListProvider.fetchFromServerForBackgroundSync();
    } on UAException catch (e) {
      logger.e('Could not sync project list config');
    }
  }

  void startEntityConfigSync() async {
    AppMetaDataConfig appMDConfig = UAAppContext.getInstance().appMDConfig;
    if(appMDConfig != null && appMDConfig.fetchEntityMetaData != null && appMDConfig.fetchEntityMetaData) {
      EntityMetaDataConfigurationProvider entityMetaDataConfigurationProvider =  EntityMetaDataConfigurationProvider.getInstance();
      await entityMetaDataConfigurationProvider.fetchFromServerForBackgroundSync();
    }  else {
      logger.i('fetchEntityMetaData Flag for appMDConfig is false');
    }
  }
}
