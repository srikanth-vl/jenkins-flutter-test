import '../../utils/common_utils.dart';
import 'package:flutter/material.dart';

import '../helpers/recase_text.dart';
import '../../utils/screen_navigate_utils.dart';
import '../../utils/string_utils.dart';

import '../themes/uniapp_css.dart';
import '../themes/text_theme.dart';
import '../themes/color_theme.dart';

class ProjectGroupListViewWidget extends StatefulWidget {
  final String appId;
  final List<String> attributes;
  final Map<String, List<String>> attributeKeyToValuesMap;
  final String sortType;

  ProjectGroupListViewWidget({
    this.appId,
    this.attributes,
    this.attributeKeyToValuesMap,
    this.sortType,
  });

  @override
  _ProjectGroupListViewWidgetState createState() =>
      _ProjectGroupListViewWidgetState();
}

class _ProjectGroupListViewWidgetState
    extends State<ProjectGroupListViewWidget> {
  @override
  void initState() {
    super.initState();
    if (CommonUtils.checkForSingleSubApp()) {
      CommonUtils.showMapDownloadDialog(context);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: UniappCSS.smallHorizontalAndVerticalPadding,
      child: _renderListOfExpandableGroups(),
    );
  }

  Widget _eachGroupList(String key, List<String> groupList) {
    groupList = (groupList == null) ? List() : groupList;

    if (groupList == null || groupList.isEmpty) {
      return Center(
        child: Text(
          "No Data found",
          style: UniappTextTheme.defaultErrorTextStyle,
        ),
      );
    }

    return ListView.builder(
      itemCount: groupList.length,
      shrinkWrap: true,
      physics: ClampingScrollPhysics(),
      itemBuilder: (BuildContext context, int index) {
        return ListTile(
          leading: Icon(
            Icons.apps,
            size: UniappCSS.defaultIconSize,
          ),
          title: Text(
            RecaseText()
                    .reCase("titleCase", StringUtils.getTranslatedString(key)) +
                ": " +
                RecaseText().reCase("headerCase", groupList[index]),
            style: UniappTextTheme.smallHeader,
          ),
          trailing: Icon(
            Icons.keyboard_arrow_right,
            size: UniappCSS.defaultIconSize,
          ),
          onTap: () {
            _navigateToProjectList(key, groupList[index]);
          },
        );
      },
    );
  }

  Widget _renderEachExpandableGroup(String eachAttribute) {
    return Container(
      child: Card(
        child: ExpansionTile(
          initiallyExpanded: false,
          leading: Container(
            width: 42.0,
            height: 42.0,
            child: Icon(
              Icons.filter_list,
              size: UniappCSS.largeIconSize,
            ),
          ),
          title: RichText(
            text: TextSpan(
              text: 'Group by: ',
              style: UniappTextTheme.smallHeader,
              children: <TextSpan>[
                TextSpan(
                  text: RecaseText().reCase(
                    "titleCase",
                    StringUtils.getTranslatedString(eachAttribute),
                  ),
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    color: UniappColorTheme.alternateColor,
                  ),
                ),
              ],
            ),
          ),
          children: <Widget>[
            Divider(
              color: UniappColorTheme.alternateColor,
              height: 4.0,
              indent: 8.0,
              endIndent: 8.0,
            ),
            _eachGroupList(
                eachAttribute, widget.attributeKeyToValuesMap[eachAttribute]),
          ],
        ),
      ),
    );
  }

  Widget _renderListOfExpandableGroups() {
    return ListView.builder(
      shrinkWrap: true,
      itemCount: widget.attributes.length,
      itemBuilder: (BuildContext context, int index) {
        return _renderEachExpandableGroup(
          widget.attributes.elementAt(index),
        );
      },
    );
  }

  _navigateToProjectList(String groupingKey, String groupingValue) {
    ScreenNavigateUtils().navigateToProjectListScreen(context, widget.appId,
        widget.sortType, groupingKey, groupingValue, null, false);
  }
}
