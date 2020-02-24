import 'dart:async';
import 'dart:collection';

import '../../ui/themes/color_theme.dart';
import '../../ui/themes/text_theme.dart';
import '../../ui/themes/uniapp_css.dart';
import '../../models/sub_app.dart';

import '../../event/event_utils.dart';
import '../../utils/common_utils.dart';
import '../widgets/global/empty_container.dart';
import 'package:geolocator/geolocator.dart';
import '../../resources/project_list_sorting_service.dart';

import 'projectform_screen.dart';
import '../../utils/common_constants.dart';
import '../helpers/form_values.dart';
import 'package:flutter/material.dart';
import 'package:toast/toast.dart';
import '../helpers/appbar_helper.dart';

import '../../blocs/project_list_bloc.dart';
import '../../db/models/project_master_data_table.dart';
import '../widgets/global/uniapp_appbar.dart';
import '../widgets/project_list_view_widget.dart';
import '../../map_view.dart';
import '../../ua_app_context.dart';

class ProjectListScreen extends StatefulWidget {
  final String appId;
  final String sortType;
  final String groupingKey;
  final String groupingValue;
  final List<ProjectMasterDataTable> projectMasterDataTableList;

  ProjectListScreen({
    @required this.appId,
    this.sortType,
    this.groupingKey,
    this.groupingValue,
    this.projectMasterDataTableList,
  }) : assert(appId != null && appId.isNotEmpty);

  /// TODO:
  /// @Swapnil, directly use widget.(...) instead of doing the following:
  @override
  ProjectListScreenState createState() => ProjectListScreenState(
        appId,
        groupingKey,
        groupingValue,
      );
}

class ProjectListScreenState extends State<ProjectListScreen> {
  bool _mapactive = true;
  String appId;
  String groupingKey;
  String groupingValue;
  Map<String, String> selectedFilterValue = new Map();
  List<ProjectMasterDataTable> filteredProjects = List();
  StreamSubscription<PostFilterEvent> postFilterSubscription;
  SubApp app;
  ProjectListBloc projectListBloc = ProjectListBloc();

  List<ProjectMasterDataTable> projectList = List();
  ProjectListScreenState(
    this.appId,
    this.groupingKey,
    this.groupingValue,
  ) {
    projectListBloc.fetchProjects(
      this.appId,
      this.groupingKey,
      this.groupingValue,
    );
  }

  @override
  void dispose() {
    super.dispose();
    projectListBloc.dispose();
    postFilterSubscription.cancel();
  }

  @override
  void initState() {
    super.initState();

    UAAppContext.getInstance().selectedAppId = widget.appId;
    UAAppContext.getInstance().context = context;
    UAAppContext.getInstance().projectList = List();
    UAAppContext.getInstance().filterSelectedValueMap = Map();

    formMap.appId = appId ?? "";
    app = CommonUtils.getSubAppFromConfig(widget.appId);

    if (app == null || app.mapEnabled == null || !app.mapEnabled) {
      _mapactive = false;
    } else if (app != null && app.mapEnabled != null && app.mapEnabled) {
      if (CommonUtils.checkForSingleSubApp()) {
        _mapactive = false;
      } else {
        _mapactive = true;
      }
    }
    if (CommonUtils.checkForSingleSubApp() &&
        (groupingKey == null || groupingValue == null)) {
      CommonUtils.showMapDownloadDialog(context);
    }
  }

  void _handleTapboxChanged(bool newValue) {
    setState(() {
      _mapactive = newValue;
    });
  }

  @override
  Widget build(BuildContext context) {
    eventFromFilterScreen();

    return SafeArea(
      child: Scaffold(
        appBar: UniAppBar(
          appbarParams:
              AppbarParameterMapping().getAppbarMapping("ProjectListScreen"),
          showMultilineAppbar:
              AppbarParameterMapping().getActionCount("ProjectListScreen"),
          showProjectListView:
              app != null && app.mapEnabled != null && app.mapEnabled
                  ? true
                  : false,
          onChanged: _handleTapboxChanged,
          active: _mapactive,
          appId: appId,
        ),
        body: StreamBuilder(
            stream: projectListBloc.projectListStream,
            builder: (context,
                AsyncSnapshot<List<ProjectMasterDataTable>> snapshot) {
              if (snapshot.hasError) {
                return Center(
                  child: Text(
                    'Error: ${snapshot.error}',
                    style: UniappTextTheme.defaultErrorTextStyle,
                  ),
                );
              }

              switch (snapshot.connectionState) {
                case ConnectionState.waiting:
                  return Center(
                    child: Text(
                      'Loading...',
                      style: UniappTextTheme.defaultErrorTextStyle,
                    ),
                  );
                default:
                  if (snapshot.data.isEmpty) {
                    return Center(
                      child: Text(
                        'No Projects Found.',
                        style: UniappTextTheme.defaultErrorTextStyle,
                      ),
                    );
                  }
                  UAAppContext.getInstance().projectList = snapshot.data;
                  if (selectedFilterValue != null &&
                      selectedFilterValue.isNotEmpty) {
                    UAAppContext.getInstance().filterSelectedValueMap =
                        selectedFilterValue;
                  }

                  // print(
                  // "\n\n\n\n Length: ${widget.projectMasterDataTableList.length}");
                  if ((filteredProjects == null || filteredProjects.isEmpty) &&
                      (widget.projectMasterDataTableList == null)) {
                    projectList = snapshot.data;
                    print("\n\n\n\n\n Snapshot: ${snapshot.data.length}");
                    return getView(snapshot.data);
                  } else {
                    if (widget.projectMasterDataTableList != null &&
                        widget.projectMasterDataTableList.isNotEmpty) {
                      print(
                          "\n\n\n\n\n projectMasterDataTableList: ${widget.projectMasterDataTableList.length}");
                      return getView(widget.projectMasterDataTableList);
                    } else if (filteredProjects != null &&
                        filteredProjects.isNotEmpty) {
                      print(
                          "\n\n\n\n\n filteredProjects: ${filteredProjects.length}");
                      return getView(filteredProjects);
                    }
                  }
              }
            }),
        floatingActionButton: FutureBuilder<bool>(
          future: CommonUtils.checkAccessForAssetCreation(appId),
          builder: (BuildContext context, AsyncSnapshot<bool> snapshot) {
            if (snapshot.hasData &&
                snapshot.data == true &&
                _mapactive == false) {
              return new FloatingActionButton(
                backgroundColor: UniappColorTheme.fabPrimaryButtonColor,
                child: Icon(
                  Icons.add,
                  size: UniappCSS.largeIconSize,
                  color: UniappColorTheme.fabPrimaryTextColor,
                ),
                onPressed: () => navigateToProjectFormScreen(
                    appId, null, CommonConstants.INSERT_FORM_KEY),
              );
            } else if (snapshot.connectionState == ConnectionState.waiting) {
              return CircularProgressIndicator();
            }
            return EmptyContainer();
          },
        ),
      ),
    );
  }

  void _onMapRendererCreated(MapRendererController controller) {
    controller.loadMap("");
  }

  Widget getView(List<ProjectMasterDataTable> projects) {
    Widget projectView;
    if (_mapactive && app.mapEnabled != null && app.mapEnabled) {
      if (projects != null && projects.isNotEmpty) {
        projectView = MapRenderer(
          onMapRendererCreated: _onMapRendererCreated,
          projectList: projects,
          appId: appId,
        );
      } else {
        // TODO : Log error
        Toast.show(
          "No projects found.",
          context,
          duration: Toast.LENGTH_SHORT,
          gravity: Toast.BOTTOM,
        );
      }
    } else {
      ProjectListSortingService.mList = projects;
      if (widget.sortType != null && widget.sortType.isNotEmpty) {
        ProjectListSortingService.mSortType = widget.sortType;
      } else {
        ProjectListSortingService.mSortType =
            CommonConstants.PROJECT_LIST_ALPHABETICAL_SORTING;
      }
      Position position = UAAppContext.getInstance().currentLoc;
      if (position != null &&
          position.longitude != null &&
          position.latitude != null) {
        ProjectListSortingService.mUserLat = position.latitude;
        ProjectListSortingService.mUserLong = position.longitude;
      }
      ProjectListSortingService.sort();
      projectView = ProjectListViewWidget(
        appId: appId,
        projectList: projects,
      );
    }
    return projectView;
  }

  //TODO: @Rohan use navigation method declared in NavUtils class
  navigateToProjectFormScreen(appId, projectId, formActionType) async {
    String pid =
        await formMap.initializeFormMap(appId, projectId, formActionType);
    if (groupingKey != null &&
        groupingValue != null &&
        groupingKey.isNotEmpty &&
        groupingValue.isNotEmpty) {
      formMap.formValues[groupingKey] = groupingValue;
    }
    final result = await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => ProjectFormScreen(
          appId: appId,
          projectId: pid,
          formActiontype: formActionType,
        ),
      ),
    );

    if (result.toString().compareTo(CommonConstants.INSERT_FORM_KEY) == 0) {
      projectListBloc.fetchProjects(
          appId, widget.groupingKey, widget.groupingValue);
    }
  }

  eventFromFilterScreen() async {
    postFilterSubscription =
        eventBus.on<PostFilterEvent>().listen((event) async {
      if (event.selectedFilterMap != null &&
          event.selectedFilterMap.isNotEmpty) {
        LinkedHashMap data = event.selectedFilterMap;
        List<String> projectIds = new List();
        List<String> valuesList = new List();

        List<String> mFilteringAttributes =
            CommonUtils.getSubAppFromConfig(appId).filteringAttributes;
        String dimensionValues = "";
        if (mFilteringAttributes != null && mFilteringAttributes.isNotEmpty) {
          for (String attribute in mFilteringAttributes) {
            if (data != null &&
                data[attribute] != null &&
                data[attribute].toString().isNotEmpty) {
              String val = data[attribute];
              dimensionValues += '%"${attribute}":"${val}"';
              valuesList.add(val);
            }
          }
          dimensionValues += "%";
          if (data != null && data.isNotEmpty) {
            for (String key in data.keys) {
              selectedFilterValue[key] = data[key];
            }
          }
          List<ProjectMasterDataTable> mFilteredProjects = new List();

          if (dimensionValues != null && dimensionValues.isNotEmpty) {
            projectIds = await UAAppContext.getInstance()
                .unifiedAppDBHelper
                .getProjectIdsForFilterQuery(UAAppContext.getInstance().userID,
                    widget.appId, dimensionValues);
          }
          if (projectIds == null || projectIds.isEmpty) {
            CommonUtils.showToast(CommonConstants.NO_PROJECTS_FOUND, context);
          } else {
            mFilteredProjects.clear();
            mFilteredProjects.addAll(projectList);
            for (ProjectMasterDataTable project in projectList) {
              if (!projectIds.contains(project.projectId)) {
                mFilteredProjects.remove(project);
              }
            }
            filteredProjects = mFilteredProjects;
          }
        }
        setState(() {});
      }
    });
  }
}
