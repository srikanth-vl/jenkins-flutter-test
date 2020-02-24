import 'package:flutter/material.dart';
import 'dart:io';

import '../global/empty_container.dart';
import '../../../models/form_model.dart';
import '../../screens/image_preview_screen.dart';
import '../../screens/video_preview_screen.dart';
import '../../../utils/common_utils.dart';
import '../../../utils/string_utils.dart';
import '../../themes/uniapp_css.dart';
import '../../themes/color_theme.dart';
import '../../themes/text_theme.dart';

class FormHeaders extends StatelessWidget {
  final String id;
  final String appId;
  final String userId;
  final FormHeaderModel formHeaderModel;

  const FormHeaders({
    @required this.id,
    @required this.appId,
    @required this.userId,
    @required this.formHeaderModel,
  });

  _formHeaderValue() {
    if (formHeaderModel.headerType == "text") {
      return _textheaderValue();
    } else if (formHeaderModel.headerType == "image") {
      return _imageThumbnail();
    } else if (formHeaderModel.headerType == "video") {
      return _videoThumbnail();
    } else {
      return EmptyContainer();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      color: UniappColorTheme.headerColor,
      child: Row(
        children: <Widget>[
          Expanded(
            flex: formHeaderModel.headerLabelFlex,
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16.0, 6.0, 6.0, 6.0),
              child: Text(
                formHeaderModel.headerLabel + " : " ?? "",
                style: UniappTextTheme.smallInvertedHeader,
              ),
            ),
          ),
          _formHeaderValue(),
        ],
      ),
    );
  }

  Widget _textheaderValue() {
    var val =
        (formHeaderModel.headerValue != "") ? formHeaderModel.headerValue : "-";
    return Expanded(
      flex: formHeaderModel.headerValueFlex,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(8.0, 4.0, 8.0, 4.0),
        child: Text(
          val ?? "-",
          style: UniappTextTheme.smallInvertedHeader,
        ),
      ),
    );
  }

  Widget _imageThumbnail() {
    return Expanded(
      child: Container(
        width: double.maxFinite,
        height: 64.0,
        child: ListView.builder(
          scrollDirection: Axis.horizontal,
          itemBuilder: (BuildContext context, int index) {
            return _createListImageThumbnailItem(
                formHeaderModel.headerValue[index]);
          },
          itemCount: formHeaderModel.headerValue.length,
        ),
      ),
    );
  }

  Widget _createListImageThumbnailItem(String uuid) {
    uuid = StringUtils.getStringListFromDelimiter("##", uuid)[0];
    Widget placeholderWidget;
    if ((uuid != "-" && uuid != "")) {
      placeholderWidget = FutureBuilder<File>(
          future: CommonUtils.getFileFromDB(appId, uuid),
          builder: (BuildContext context, AsyncSnapshot<File> snapshot) {
            File imgFile;
            if (snapshot.hasData && snapshot.data != null) {
              imgFile = snapshot.data;
              return Container(
                padding: EdgeInsets.all(4.0),
                width: 64.0,
                height: 64.0,
                child: FlatButton(
                  color: Colors.black,
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => ImagePreviewScreen(
                          image: imgFile,
                        ),
                      ),
                    );
                  },
                  child: Image.file(
                    imgFile,
                  ),
                ),
              );
            }
            return EmptyContainer();
          });
    } else {
      placeholderWidget = Padding(
        padding: const EdgeInsets.fromLTRB(8.0, 4.0, 8.0, 4.0),
        child: Text(
          "-",
          style: UniappTextTheme.smallInvertedHeader,
        ),
      );
    }
    return placeholderWidget;
  }

  Widget _videoThumbnail() {
    Widget val;
    if ((formHeaderModel.headerValue != "-" &&
        formHeaderModel.headerValue != "")) {
      val = FutureBuilder<File>(
          future: CommonUtils.getFileFromDB(appId, formHeaderModel.headerValue),
          builder: (BuildContext context, AsyncSnapshot<File> snapshot) {
            if (snapshot.hasData && snapshot.data != null) {
              return Container(
                padding: EdgeInsets.all(4.0),
                width: 64.0,
                height: 64.0,
                child: FlatButton(
                  color: Colors.black,
                  onPressed: () {
                    Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (context) => VideoPreviewScreen(
                          videoPath: snapshot.data.path,
                        ),
                      ),
                    );
                  },
                  child: Icon(
                    Icons.play_circle_filled,
                    color: Colors.white,
                  ),
                ),
              );
            }
            return EmptyContainer();
          });
    } else {
      val = Padding(
        padding: const EdgeInsets.fromLTRB(8.0, 4.0, 8.0, 4.0),
        child: Text(
          "-",
          style: UniappCSS.mediumInvertedHeader,
        ),
      );
    }
    return Expanded(
      flex: formHeaderModel.headerValueFlex,
      child: SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        child: Row(
          mainAxisSize: MainAxisSize.max,
          children: <Widget>[val],
        ),
      ),
    );
  }
}
