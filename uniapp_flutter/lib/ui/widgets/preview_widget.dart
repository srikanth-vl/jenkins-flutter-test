import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/material.dart';

class PreviewImageScreen extends StatefulWidget {
  final String imagePath;

  PreviewImageScreen({this.imagePath});

  @override
  _PreviewImageScreenState createState() => _PreviewImageScreenState();
}

class _PreviewImageScreenState extends State<PreviewImageScreen> {
  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        // TODO: Use UniAppBar instead of your own appbar.
        appBar: AppBar(
          title: Text('Preview'),
          backgroundColor: Colors.blueGrey,
        ),
        body: Container(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: <Widget>[
              Expanded(
                  flex: 2,
                  child: Image.file(File(widget.imagePath), fit: BoxFit.cover)),
              SizedBox(height: 10.0),
              Flexible(
                flex: 1,
                child: Container(
                  padding: EdgeInsets.all(60.0),
                  child: Row(
                    mainAxisSize: MainAxisSize.max,
                    children: <Widget>[
                      Expanded(
                        child: RaisedButton(
                          color: Colors.white,
                          child: Text('Cancel'),
                          onPressed: () {
                            // TODO: Delete image from file system
                            Navigator.of(context).pop({'saved': false});
                          },
                        ),
                      ),
                      Expanded(
                        child: RaisedButton(
                          color: Colors.blue,
                          child: Text(
                            'Okay',
                            style: TextStyle(color: Colors.white),
                          ),
                          onPressed: () async {
                            Navigator.of(context).pop({'saved': true});
                          },
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<ByteData> getBytesFromFile() async {
    Uint8List bytes = File(widget.imagePath).readAsBytesSync() as Uint8List;
    return ByteData.view(bytes.buffer);
  }
}
