import 'package:Bluis/ui/themes/color_theme.dart';
import 'package:flutter/material.dart';

import '../../screens/map_screen.dart';
import '../../../map_view.dart';
import '../../../models/map_project_info.dart';
import '../../../ui/helpers/appbar_helper.dart';
import '../../../ui/widgets/global/uniapp_appbar.dart';
import '../../../db/databaseHelper.dart';
import '../../../db/models/project_master_data_table.dart';
import '../../../ua_app_context.dart';
import '../../../utils/common_constants.dart';
import '../../../utils/common_utils.dart';
import '../../../utils/screen_navigate_utils.dart';
import '../../../localization/app_translations.dart';
import 'no_data_to_display_widget.dart';

class UniappSearch extends SearchDelegate<String> {
  final List<ProjectMasterDataTable> dataList;
  final String dataType;
  final String appId;

  UniappSearch({
    @required this.appId,
    @required this.dataList,
    @required this.dataType,
  });

  @override
  List<Widget> buildActions(BuildContext context) {
    return [
      IconButton(
        icon: Icon(Icons.clear),
        onPressed: () {
          query = "";
        },
      ),
    ];
  }

  @override
  Widget buildLeading(BuildContext context) {
    return IconButton(
      icon: AnimatedIcon(
        icon: AnimatedIcons.menu_arrow,
        progress: transitionAnimation,
      ),
      onPressed: () {
        close(context, null);
      },
    );
  }

  @override
  Widget buildResults(BuildContext context) {
    if (dataList
        .where((q) =>
            q.projectFieldsString.toLowerCase().contains(query.toLowerCase()))
        .isEmpty) {
      return NoDataToDisplayWidget();
    }

    final resultsList = dataList
        .where((q) =>
            q.projectFieldsString.toLowerCase().contains(query.toLowerCase()))
        .toList();

    ///ScreenNavigateUtils().navigateToProjectListScreen(context, appId, null, null, null, resultsList, true);

    return RaisedButton(
      onPressed: () {
        print("resultslist: ${resultsList.length}");
        ScreenNavigateUtils().navigateToProjectListScreen(
            context, appId, null, null, null, resultsList, true);
      },
    );

    // return UniappSearchTile(
    //   searchList: resultsList,
    //   searchDelegate: this,
    //   query: query,
    //   appId: appId,
    // );
  }

  @override
  Widget buildSuggestions(BuildContext context) {
    if (dataList
        .where((q) =>
            q.projectFieldsString.toLowerCase().contains(query.toLowerCase()))
        .isEmpty) {
      return NoDataToDisplayWidget();
    }

    final searchList = dataList
        .where((q) =>
            q.projectFieldsString.toLowerCase().contains(query.toLowerCase()))
        .toList();

    return UniappSearchTile(
      searchList: searchList,
      searchDelegate: this,
      query: query,
      appId: appId,
    );
  }
}

class UniappSearchTile extends StatelessWidget {
  final List<ProjectMasterDataTable> searchList;
  final SearchDelegate searchDelegate;
  final String query;
  final String appId;

  UniappSearchTile({
    this.searchDelegate,
    this.searchList,
    this.query,
    this.appId,
  });

  void _navigateToMapScreen(
      BuildContext context, List<MapProjectInfo> mapProjectInfoList) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => SafeArea(
          child: Scaffold(
            appBar: UniAppBar(
              appbarParams: AppbarParameterMapping().getAppbarMapping(""),
              showMultilineAppbar: AppbarParameterMapping().getActionCount(""),
              showProjectListView: false,
              active: false,
              appId: null,
            ),
            body: MapScreen(
              appId: appId,
              ctxt: context,
              isOnline: true,
              projectInfoList: mapProjectInfoList,
            ),
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      itemCount: searchList.length,
      itemBuilder: (BuildContext context, int index) {
        return Container(
          color: Colors.white,
          child: ListTile(
            // leading: Icon(Icons.search),
            trailing: Row(
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                IconButton(
                    icon: Icon(
                      Icons.map,
                      color: UniappColorTheme.widgetColor,
                    ),
                    onPressed: () {
                      List<MapProjectInfo> mapProjectInfoList = List();
                      mapProjectInfoList.add(
                        MapRendererState()
                            .getMapProjectInfo(searchList.elementAt(index)),
                      );

                      if (mapProjectInfoList.length > 0) {
                        _navigateToMapScreen(context, mapProjectInfoList);
                      }
                    }),
                IconButton(
                  icon: Icon(Icons.call_made),
                  onPressed: () => searchDelegate.query =
                      searchList.elementAt(index).projectName,
                ),
              ],
            ),

            title: Text(
              "${searchList[index].projectName}",
              style: TextStyle(
                fontWeight: FontWeight.bold,
              ),
            ),
            subtitle: Text(AppTranslations.of(context).text("Last_Validated") +
                " : ${CommonUtils.getDate(searchList[index].projectLastUpdatedTs)}"),
            selected: true,
            onTap: () {
              searchDelegate.close(
                  context, searchList.elementAt(index).projectId);
            },
          ),
        );
      },
    );
  }
}

class UniappSearchHelper {
  ///
  /// OnPressed function for the Search button
  /// This function calls the getProjectsForUser() method
  /// which returns a List<ProjectMasterDataTable>.
  /// This list is passed to the Search Delegate.
  ///
  void searchOnPressed(BuildContext context, String appId) {
    List<ProjectMasterDataTable> dataList = new List();
    DatabaseHelper()
        .getProjectsForUser(
            UAAppContext.getInstance().userID, appId, null, null)
        .then((onValue) {
      dataList = onValue;

      showSearch(
        context: context,
        delegate: UniappSearch(
          appId: appId,
          dataList: dataList,
          dataType: "ProjectList",
        ),
      ).then((onValue) {
        if (onValue != null && onValue.isNotEmpty) {
          ScreenNavigateUtils().navigateToProjectFormScreen(
              context, appId, onValue, CommonConstants.UPDATE_FORM_KEY);
        }
      });
    });
  }
}
