import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import './global/empty_container.dart';
import '../../ua_app_context.dart';
import '../../utils/screen_navigate_utils.dart';

import './../helpers/recase_text.dart';
import '../../db/models/project_master_data_table.dart';
import '../../localization/app_translations.dart';
import '../../models/project_type_configuartion.dart';
import '../../utils/common_constants.dart';
import '../../ui/widgets/global/image_halo_container.dart';

import '../../ui/themes/text_theme.dart';
import '../../ui/themes/uniapp_css.dart';

class ProjectListViewWidget extends StatefulWidget {
  final String title;
  final String appId;
  final List<ProjectMasterDataTable> projectList;

  ProjectListViewWidget({
    this.title,
    this.appId,
    this.projectList,
  });

  @override
  _ProjectListViewWidgetState createState() => _ProjectListViewWidgetState();
}

class _ProjectListViewWidgetState extends State<ProjectListViewWidget> {
  ProjectTypeConfiguration projectTypeConfiguration;
  bool isProgressRunning = false;

  _ProjectListViewWidgetState();

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return _renderProejctListFromStream(widget.projectList);
  }

  _renderProejctListFromStream(List<ProjectMasterDataTable> projects) {
    return Padding(
      padding: UniappCSS.smallHorizontalAndVerticalPadding,
      child: ListView.builder(
        itemCount: projects.length,
        itemBuilder: (context, index) {
          final item = projects[index].projectName;
          if (projects[index].projectName != null &&
              projects[index].projectName.isNotEmpty) {
            return Card(
              elevation: UniappCSS.smallCardElevation,
              child: ListTile(
                leading: ImageHaloContainer(
                  imagePath: CommonConstants.APP_LOGO,
                  radius: UniappCSS.haloSize,
                ),
                trailing: UAAppContext.getInstance().userID !=
                        CommonConstants.GUEST_USER_ID
                    ? Icon(Icons.keyboard_arrow_right)
                    : null,
                title: Text(
                  RecaseText().reCase("titleCase", item),

                  // StringUtils.getTranslatedString(item),
                  style: UniappTextTheme.smallHeader,
                ),
                subtitle: Text(projects[index].projectLastUpdatedTs == 0
                    ? AppTranslations.of(context).text("Last_Validated") +
                        ': - '
                    : AppTranslations.of(context).text("Last_Validated") +
                        ': ${getDate(projects[index].projectLastUpdatedTs)}'),
                onTap: () {
                  _navigateToProjectForm(
                      projects[index].projectAppId,
                      projects.elementAt(index).projectId,
                      CommonConstants.UPDATE_FORM_KEY);
                },
              ),
            );
          } else {
            return EmptyContainer();
          }
        },
      ),
    );
  }

  getDate(int ts) {
    DateFormat dateFormat = new DateFormat('dd/MM/yyyy').add_Hm();
    return dateFormat.format(DateTime.fromMillisecondsSinceEpoch(ts));
  }

  _navigateToProjectForm(appId, projectId, formActionType) {
    ScreenNavigateUtils()
        .navigateToProjectFormScreen(context, appId, projectId, formActionType);
  }
}
