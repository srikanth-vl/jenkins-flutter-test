import 'package:Bluis/ui/helpers/recase_text.dart';

import '../../utils/common_utils.dart';

import '../../ui/themes/text_theme.dart';
import '../../ui/helpers/home_screen_helper.dart';
import '../../ui/widgets/global/image_halo_container.dart';
import '../../localization/app_translations.dart';
import '../../utils/string_utils.dart';
import '../../models/app_meta_data_config.dart';
import '../../utils/common_constants.dart';
import 'package:flutter/material.dart';
import '../../models/sub_app.dart';
import '../../ua_app_context.dart';
import '../../blocs/root_config_bloc.dart';
import '../themes/uniapp_css.dart';
import '../../resources/sub_app_sorting_service.dart';

class HomeWidget extends StatefulWidget {
  final String title;

  HomeWidget({
    this.title,
  });

  @override
  _HomeWidgetState createState() => _HomeWidgetState();
}

class _HomeWidgetState extends State<HomeWidget> {
  bool isProgressRunning = false;
  bool _isDisposed = false;
  RootConfigBloc rootConfigBloc = RootConfigBloc();
  HomeScreenHelper _homeScreenHelper = HomeScreenHelper();

  @override
  void initState() {
    super.initState();
    UAAppContext.getInstance().context = context;
    rootConfigBloc.fetchRootConfig();
    rootConfigBloc.fetchChildProjectTypes(null);
    _toggleProgressBar();
    HomeScreenHelper().callProjectConfigurations();
    _toggleProgressBar();

    CommonUtils.showMapDownloadDialog(context);
  }

  @override
  void dispose() {
    // Clean up the controller when the widget is disposed.
    super.dispose();
    _isDisposed = true;
    rootConfigBloc.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return StreamBuilder(
        stream: rootConfigBloc.projectTypeListStream,
        builder: (context, AsyncSnapshot<List<SubApp>> snapshot) {
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
              if (snapshot.data == null || snapshot.data.isEmpty) {
                return Center(
                  child: Text(
                    'Could not fetch Data, Please try again!',
                    style: UniappTextTheme.defaultErrorTextStyle,
                  ),
                );
              }
              return _renderAppListFromStream(snapshot.data);
          }
        });
  }

  Widget _renderAppListFromStream(List<SubApp> subApps) {
    AppMetaDataConfig appMetaData = UAAppContext.getInstance().appMDConfig;
    if (appMetaData != null) {
      if (appMetaData.sortType != null && appMetaData.sortType.isNotEmpty) {
        // Sorting order exists
        SubAppSortingService.mList = subApps;
        SubAppSortingService.mSortType = appMetaData.sortType;
        SubAppSortingService.sort();
      }
    } else {
      // Default sorting : Alphabetical
      SubAppSortingService.mList = subApps;
      SubAppSortingService.mSortType =
          CommonConstants.PROJECT_TYPE_ALPHABETICAL_SORTING;
      SubAppSortingService.sort();
    }

    return Padding(
      padding: UniappCSS.smallHorizontalAndVerticalPadding,
      child: ListView.builder(
        shrinkWrap: true,
        itemCount: subApps.length,
        itemBuilder: (context, index) {
          return _listItemWidget(subApps[index]);
        },
      ),
    );
  }

  Widget _listItemWidget(SubApp subApps) {
    return Card(
      elevation: UniappCSS.smallCardElevation,
      child: ListTile(
        leading: ImageHaloContainer(
          imagePath: CommonConstants.APP_LOGO,
          radius: UniappCSS.haloSize,
        ),
        title: Text(
          // StringUtils.getTranslatedString(subApps.name),
          RecaseText().reCase("titleCase", subApps.name),
          style: UniappTextTheme.smallHeader,
        ),
        trailing: Icon(Icons.keyboard_arrow_right),
        subtitle: FutureBuilder<int>(
            future: CommonUtils.getUnsyncedProjectCount(subApps.appId),
            builder: (BuildContext context, AsyncSnapshot<int> snapshot) {
              if (snapshot.hasData && snapshot.data != null) {
                return Text(
                    '${AppTranslations.of(context).text("unsynced_projects")} : ' +
                        snapshot.data.toString());
              } else {
                return Text('');
              }
            }),
        onTap: () {
          _homeScreenHelper.navigateToProjectGroupScreen(context, subApps.appId,
              subApps.sortType, subApps.groupingAttributes, false);
        },
      ),
    );
  }

  _toggleProgressBar() {
    if (_isDisposed) {
      return;
    }
    setState(() {
      isProgressRunning = !isProgressRunning;
    });
  }
}
