import '../../localization/app_translations.dart';

import 'global/empty_container.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import '../../utils/common_constants.dart';
import '../../utils/network_utils.dart';

import '../../models/root_config.dart';
import '../../models/sub_app.dart';
import '../../ua_app_context.dart';
import '../../utils/common_utils.dart';

import '../../sync/sync_initiater.dart';
import '../../event/event_utils.dart';

class SyncButtonWidget extends StatefulWidget {
  final bool showMultilineAppbar;

  SyncButtonWidget({
    this.showMultilineAppbar,
  });

  @override
  SyncButtonWidgetState createState() => SyncButtonWidgetState();
}

class SyncButtonWidgetState extends State<SyncButtonWidget>
    with SingleTickerProviderStateMixin {
  AnimationController syncController;
  StreamSubscription preSyncEventSubscription;
  StreamSubscription postSyncEventSubscription;
  StreamSubscription postSubmissionSubscription;
  bool isSyncRunning = false;

  @override
  void initState() {
    preSyncEventSubscription = eventBus.on<PreSyncEvent>().listen((event) {
      syncController.forward();
      syncController.repeat();
      isSyncRunning = true;
      print('Sync Started');
    });
    postSyncEventSubscription = eventBus.on<PostSyncEvent>().listen((event) {
      syncController.reset();
      isSyncRunning = false;
      print('Sync completed');
    });
    postSubmissionSubscription =
        eventBus.on<PostSubmissionEvent>().listen((event) {
      setState(() {
        print("POST SUB EVENT CALLED");
        if (isSyncRunning) {
          syncController.forward();
          syncController.repeat();
        } else {
          syncController.reset();
        }
      });
    });

    syncController = AnimationController(
      duration: const Duration(milliseconds: 800),
      vsync: this,
    );

    super.initState();
  }

  @override
  void dispose() {
    // TODO: implement dispose
    if (syncController != null) {
      syncController.dispose();
    }
    super.dispose();

    if (preSyncEventSubscription != null) {
      preSyncEventSubscription.cancel();
    }
    if (postSyncEventSubscription != null) {
      postSyncEventSubscription.cancel();
    }
    if (postSubmissionSubscription != null) {
      postSubmissionSubscription.cancel();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.symmetric(horizontal: 0.0, vertical: 4.0),
      child: Stack(
        children: <Widget>[
          GestureDetector(
            child: RotationTransition(
              turns: Tween(begin: 0.0, end: 1.0).animate(syncController),
              child: IconButton(
                icon: Icon(
                  Icons.sync,
                  color: Colors.white,
                ),
                onPressed: () {
                  _initiateManualSync();
                },
              ),
            ),
            onLongPress: () {
              showMenu(
                position: (widget.showMultilineAppbar == false)
                    ? RelativeRect.fromLTRB(
                        MediaQuery.of(context).size.width / 2, 88.0, 0.0, 0.0)
                    : RelativeRect.fromLTRB(
                        MediaQuery.of(context).size.width/4,
                        128.0,
                        MediaQuery.of(context).size.width/4,
                        0.0),
                context: context,
                items: <PopupMenuEntry>[
                  PopupMenuItem(
                    child: FutureBuilder<String>(
                        future: _getUnsyncedCountForProjects(),
                        builder: (BuildContext context,
                            AsyncSnapshot<String> snapshot) {
                          if (snapshot.hasData) {
                            return Row(
                              children: <Widget>[
                                Expanded(
                                  flex: 4,
                                  child: Text(
                                    AppTranslations.of(context)
                                        .text("unsynced_projects"),
                                    style:
                                        TextStyle(fontWeight: FontWeight.w400),
                                  ),
                                ),
                                Expanded(
                                  flex: 1,
                                  child: Text(snapshot.data.toString() ?? "-"),
                                ),
                              ],
                            );
                          } else {
                            return EmptyContainer();
                          }
                        }),
                  ),
                  PopupMenuItem(
                    child: FutureBuilder<String>(
                        future: _getUnsyncedCountForMedia(),
                        builder: (BuildContext context,
                            AsyncSnapshot<String> snapshot) {
                          if (snapshot.hasData) {
                            return Row(
                              children: <Widget>[
                                Expanded(
                                  flex: 4,
                                  child: Text(
                                    AppTranslations.of(context)
                                        .text("unsynced_media"),
                                    style: TextStyle(
                                      fontWeight: FontWeight.w400,
                                    ),
                                  ),
                                ),
                                Expanded(
                                  flex: 1,
                                  child: Text(snapshot.data.toString() ?? "-"),
                                ),
                              ],
                            );
                          } else {
                            return EmptyContainer();
                          }
                        }),
                  ),
                ],
              );
            },
          ),
          FutureBuilder<String>(
              future: _getUnsyncedProjectAndMediaCount(),
              builder: (BuildContext context, AsyncSnapshot<String> snapshot) {
                if (snapshot.hasData) {
                  return Positioned(
                    bottom: 4.0,
                    right: 4.0,
                    child: Text(
                      snapshot.data,
                      style: TextStyle(color: Colors.white, fontSize: 10),
                    ),
                  );
                } else {
                  return Positioned(
                    bottom: 4.0,
                    right: 4.0,
                    child: Text(
                      "",
                      style: TextStyle(color: Colors.white, fontSize: 10),
                    ),
                  );
                }
              })
        ],
      ),
    );
  }

  Future<String> _getUnsyncedProjectAndMediaCount() async {
    String count = "";
    int mediaCount = 0;
    int projectCount = 0;

    RootConfig rootConfig = UAAppContext.getInstance().rootConfig;
    if (rootConfig != null) {
      for (SubApp app in rootConfig.config) {
        mediaCount += await CommonUtils.getUnsyncedMediaCount(app.appId);
        projectCount += await CommonUtils.getUnsyncedProjectCount(app.appId);
      }
      if (mediaCount > 0 || projectCount > 0) {
        count = (mediaCount + projectCount).toString();
      }
    }
    if (UAAppContext.getInstance().userID == CommonConstants.GUEST_USER_ID) {
      return "";
    }
    return count;
  }

  Future<String> _getUnsyncedCountForMedia() async {
    RootConfig rootConfig = UAAppContext.getInstance().rootConfig;
    int mediaCount = 0;
    if (rootConfig != null) {
      for (SubApp app in rootConfig.config) {
        mediaCount += await CommonUtils.getUnsyncedMediaCount(app.appId);
      }
    }
    if (UAAppContext.getInstance().userID == CommonConstants.GUEST_USER_ID) {
      return "0";
    }
    return mediaCount.toString();
  }

  Future<String> _getUnsyncedCountForProjects() async {
    int projectCount = 0;
    RootConfig rootConfig = UAAppContext.getInstance().rootConfig;
    if (rootConfig != null) {
      for (SubApp app in rootConfig.config) {
        projectCount += await CommonUtils.getUnsyncedProjectCount(app.appId);
      }
    }
    if (UAAppContext.getInstance().userID == CommonConstants.GUEST_USER_ID) {
      return "0";
    }
    return projectCount.toString();
  }

  _initiateManualSync() async {
    bool isOnline = await networkUtils.hasActiveInternet();
    if (!isOnline) {
      CommonUtils.showToast(CommonConstants.NETWORK_UNAVAILABLE, context);
      return;
    }
    SyncInitiator().initiateManualBackGroundSync();
  }
}
