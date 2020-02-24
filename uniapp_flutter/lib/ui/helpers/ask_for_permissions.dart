import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

/* PermissionGroup.location,
   PermissionGroup.camera,
   PermissionGroup.locationAlways,
   PermissionGroup.phone,
   PermissionGroup.sensors,
   PermissionGroup.storage,
   PermissionGroup.microphone,*/

class AskForPermission {
  PermissionStatus _status;
  BuildContext context;
  PermissionHandler permissionHandler = new PermissionHandler();
  Map<PermissionGroup, PermissionStatus> permissions;

  void _updateStatus(PermissionStatus status) {
    if (status != _status) {
      _status = status;
    }
  }

  void checkAndGetPermissions(List<PermissionGroup> permissionGroups) async{
    List<PermissionGroup> permissionToRemove = new List();
    for (PermissionGroup permissionGroup in permissionGroups) {
      PermissionStatus status = await PermissionHandler().checkPermissionStatus(permissionGroup);
      if(status == PermissionStatus.granted){
        permissionToRemove.add(permissionGroup);
      }
    }
    for(PermissionGroup permission in permissionToRemove){
      permissionGroups.remove(permission);
    }
    await getPermissions(permissionGroups);
    return;
  }

  void getPermissions(List<PermissionGroup> permissionGroups) async {
    permissions = await permissionHandler.requestPermissions(permissionGroups);
    return;
  }

  Future<List<PermissionGroup>> checkAndValidatePermission(
      List<PermissionGroup> permissionGroups) async {
    List<PermissionGroup> deniedPermissions = [];
    for (PermissionGroup permissionGroup in permissionGroups) {
      permissionHandler.requestPermissions([
        permissionGroup,
      ]).then((Map<PermissionGroup, PermissionStatus> permissionMap) {
        permissions = permissionMap;
        bool permission = _onPermissionRequested(permissionGroup);
        if (!permission) {
          deniedPermissions.add(permissionGroup);
        }
      });
    }
    return deniedPermissions;
  }

  bool _onPermissionRequested(PermissionGroup permissionGroup) {
    if (permissions[permissionGroup] == null) {
      return false;
    }
    final status = permissions[permissionGroup];
    if (status != PermissionStatus.granted) {
      return false;
    }
    return true;
  }

}
