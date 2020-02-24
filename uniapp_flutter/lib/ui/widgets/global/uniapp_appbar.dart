import 'package:flutter/material.dart';

import './image_avatar.dart';
import './search_delegate.dart';
import './uniapp_preferences.dart';
import '../sync_button_widget.dart';
import '../../helpers/appbar_helper.dart';
import '../../themes/color_theme.dart';
import '../../themes/text_theme.dart';
import '../../../models/map_config.dart';
import '../../../resources/logout_service.dart';
import '../../../ua_app_context.dart';
import '../../../utils/screen_navigate_utils.dart';
import '../../../utils/network_utils.dart';
import '../../../utils/common_constants.dart';
import '../../../localization/app_translations.dart';

/// Instructions:
///   To add an action in the Three-dot view (Vertical ellipsis):
///     1. Create an instance of the Choice class and provide id, title and icon for the action.
///     2. Add the "named choice" to the List<Choice> choices in the initState of the UniAppBar class.
///     3. Register the "onTap" event in the onChoiceTap method of UniappActionWidgets class.
///
///   To add an action drirectly in the Appbar (i.e. outside of the Three-dot view)
///     1. UniAppBarState()._getActionWidgets >> Append the action to the List<Widget> appBarActions.

class UniAppBar extends StatefulWidget with PreferredSizeWidget {
  final String appId;
  final String appBarTitle;
  final ValueChanged<bool> onChanged;
  final bool active;
  final bool showProjectListView;
  final bool showMultilineAppbar;
  final AppbarParameters appbarParams;

  UniAppBar({
    this.appBarTitle,
    this.showProjectListView,
    this.active,
    this.onChanged,

    //
    this.appId,
    this.showMultilineAppbar,
    this.appbarParams,
  }) : assert(showMultilineAppbar != null);

  @override
  _UniAppBarState createState() => _UniAppBarState();

  @override
  Size get preferredSize => Size.fromHeight((this.showMultilineAppbar == true)
      ? kToolbarHeight * 1.75
      : kToolbarHeight * 1);

  void _handleTap() {
    onChanged(!active);
  }
}

class _UniAppBarState extends State<UniAppBar>
    with SingleTickerProviderStateMixin {
  Choice _selectedChoice;

  @override
  void dispose() {
    super.dispose();
  }

  // Causes the app to rebuild with the new _selectedChoice.
  void _select(Choice choice) {
    setState(() {
      _selectedChoice = choice;
      print("Selected Choice $_selectedChoice");
    });
  }

  @override
  Widget build(BuildContext context) {
    List<Choice> choices = <Choice>[];
    _addChoicesToEllipsis(choices, context);
    if (widget.showMultilineAppbar == true) {
      return AppBar(
        elevation: 8.0,
        title: Row(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: <Widget>[
            Expanded(
              flex: 1,
              child: ImageAvatar(
                imagePath: CommonConstants.APP_INVERTED_LOGO,
                radius: 32.0,
              ),
            ),
            Expanded(
              flex: 4,
              child: Text(
                widget.appBarTitle ??
                    AppTranslations.of(context)
                        .text(CommonConstants.DEFAULT_APPBAR_TITLE),
                style: UniappTextTheme.invertedWidgetStyle,
              ),
            ),
          ],
        ),
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(48.0),
          child: Theme(
            data: Theme.of(context).copyWith(accentColor: Colors.white),
            child: Container(
              height: 48.0,
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: _getActionWidgets(choices, context),
              ),
            ),
          ),
        ),
        actions: _getEllipsisActions(choices, context),
      );
    }
    return AppBar(
      elevation: 8.0,
      title: Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: <Widget>[
          Expanded(
            flex: 1,
            child: ImageAvatar(
              imagePath: CommonConstants.APP_INVERTED_LOGO,
              radius: 32.0,
            ),
          ),
          Expanded(
            flex: 4,
            child: Text(
              widget.appBarTitle ??
                  AppTranslations.of(context)
                      .text(CommonConstants.DEFAULT_APPBAR_TITLE),
              style: UniappTextTheme.invertedWidgetStyle,
            ),
          ),
        ],
      ),
      actions: _getEllipsisActions(choices, context),
    );
  }

  void _addChoicesToEllipsis(List<Choice> choices, BuildContext context) {
    MapConfig mapConfig = UAAppContext.getInstance().mapConfig;
    if (widget.appbarParams.showDownloads == true &&
        mapConfig != null &&
        mapConfig.offlineMapFiles != null &&
        mapConfig.offlineMapFiles.isNotEmpty &&
        !choices
            .contains(UniappActionWidgets().getChoiceForDownloadMap(context))) {
      choices.add(UniappActionWidgets().getChoiceForDownloadMap(context));
    }

    if (choices != null && choices.length > 0)
      _selectedChoice = choices[0]; // The app's "state".

    if (widget.appbarParams.showPreferences == true &&
        !choices.contains(UniappActionWidgets().getChoicePreference(context))) {
      choices.add(UniappActionWidgets().getChoicePreference(context));
    }

    if (widget.appbarParams.showLogout == true &&
        !choices.contains(UniappActionWidgets().getChoiceLogOut(context))) {
      choices.add(UniappActionWidgets().getChoiceLogOut(context));
    }
  }

  // The Three-dot-Menu actions. (Vertical ellipsis).
  List<Widget> _getEllipsisActions(List<Choice> choices, BuildContext context) {
    List<Widget> ellipsisActions = List();

    ellipsisActions.add(PopupMenuButton<Choice>(
      onSelected: _select,
      itemBuilder: (BuildContext context) {
        return choices.map((Choice choice) {
          return PopupMenuItem<Choice>(
            value: choice,
            child: InkWell(
              onTap: () {
                UniappActionWidgets()
                    .onChoiceTap(context, choice, widget.appId);
                if (choice.id == 'preferenceAction') {
                  setState(() {});
                }
              },
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: <Widget>[
                  Text(choice.title),
                  Icon(
                    choice.icon,
                    color: UniappColorTheme.widgetColor,
                  ),
                ],
              ),
            ),
          );
        }).toList();
      },
    ));

    if (widget.showMultilineAppbar == false) {
      ellipsisActions
          .addAll(_getActionWidgets(choices, context).reversed.toList());
      ellipsisActions = ellipsisActions.reversed.toList();
    }

    return ellipsisActions;
  }

  // The actions outside of the Three-dot-Menu (Vertical ellipsis), i.e. directly on the appbar.
  List<Widget> _getActionWidgets(List<Choice> choices, BuildContext context) {
    List<Widget> appBarActions = List();
    if (widget.appbarParams.showSync) {
      appBarActions.add(UniappActionWidgets().getSyncWidget(widget.showMultilineAppbar));
    }

    if (widget.showProjectListView) {
      appBarActions.add(_handleMapAndGridIcon());
    }

    if (widget.appbarParams.showSearchView) {
      appBarActions.add(
          UniappActionWidgets().getAppbarSearchWidget(context, widget.appId));
    }

    if (widget.appbarParams.showFilter == true &&
        widget.appId != null &&
        widget.appId.isNotEmpty) {
      appBarActions.add(
          UniappActionWidgets().getAppBarFilterWidget(context, widget.appId));
    }

    return appBarActions;
  }

  Widget _handleMapAndGridIcon() {
    if (widget.showProjectListView && widget.active != null && !widget.active) {
      return IconButton(
        // icon: Icon(Icons.grid_on),
        icon: Image.asset(
          'assets/images/map_action_bar_icon.png',
          color: Colors.white,
        ),
        tooltip: 'Map',
        onPressed: () {
          widget._handleTap();
        },
      );
    } else {
      return IconButton(
        icon: Icon(
          Icons.view_list,
          color: Colors.white,
        ),
        tooltip: 'List View',
        onPressed: () {
          widget._handleTap();
        },
      );
    }
  }
}

/// A model class for the Three-dot view (Vertical ellipsis)
class Choice {
  const Choice({
    this.id,
    this.title,
    this.icon,
  });

  final String id;
  final String title;
  final IconData icon;
}

/// To create the entries in the Three-dot view (Vertical ellipsis)
/// create an Instance of the Choice class in the class UniappActionWidgets
class UniappActionWidgets {
  Choice getChoiceForDownloadMap(BuildContext context) {
    Choice downloadMapsAction = Choice(
      id: "downloadMapsAction",
      title: AppTranslations.of(context).text('Download_Map_Files'),
      icon: Icons.cloud_download,
    );
    return downloadMapsAction;
  }

  Choice getChoiceLogOut(BuildContext context) {
    Choice logoutAction;
    if (UAAppContext.getInstance().userID == CommonConstants.GUEST_USER_ID) {
      logoutAction = Choice(
        id: "logoutAction",
        title: AppTranslations.of(context).text("login"),
        icon: Icons.power_settings_new,
      );
    } else {
      logoutAction = Choice(
        id: "logoutAction",
        title: AppTranslations.of(context).text("Logout"),
        icon: Icons.power_settings_new,
      );
    }

    return logoutAction;
  }

  Choice getChoicePreference(BuildContext context) {
    Choice preferenceAction = Choice(
      id: 'preferenceAction',
      title: AppTranslations.of(context).text('Preferences'),
      icon: Icons.settings,
    );
    return preferenceAction;
  }

  Widget getAppbarSearchWidget(BuildContext context, String appId) {
    return Container(
      child: IconButton(
          icon: Icon(
            Icons.search,
            color: Colors.white,
          ),
          onPressed: () =>
              UniappSearchHelper().searchOnPressed(context, appId)),
    );
  }

  Widget getSyncWidget(showMultilineAppbar) {
    return SyncButtonWidget(
        showMultilineAppbar: showMultilineAppbar,
    );
  }

  Widget getAppBarFilterWidget(BuildContext context, String appId) {
    return Container(
        child: IconButton(
      icon: Icon(
        Icons.filter_list,
        color: Colors.white,
      ),
      onPressed: () => _navigateToFilterScreen(context, appId),
    ));
  }

  /// onChoiceTap handles the onClick event of the selected choice
  /// Uses selectedChoice.id to determine the function that has to fired upon click.
  onChoiceTap(BuildContext context, Choice selectedChoice, String appId) async {
    switch (selectedChoice.id) {
      case 'downloadMapsAction':
        ScreenNavigateUtils().navigateToDownloadMapScreen(context);
        break;

      case 'logoutAction':
        bool isOnline = await networkUtils.hasActiveInternet();
        if (isOnline) {
          logoutService.logout(context);
        } else {
          logoutService.offlineLogout(context);
        }
        break;

      case 'preferenceAction':
        UniappPreferences().preferenceBottomSheet(context);
        break;

      default:
        print(
            "Unimplemented Feature: ${selectedChoice.id} ${selectedChoice.title}");
        break;
    }
  }

  _navigateToFilterScreen(context, appId) {
    ScreenNavigateUtils().navigateToFilterScreen(context, appId);
  }
}
