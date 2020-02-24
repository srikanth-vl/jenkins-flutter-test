import 'dart:collection';
import 'package:event_bus/event_bus.dart';

class PostSyncEvent {
  PostSyncEvent();
}
class PreSyncEvent {
  PreSyncEvent();
}
class LogoutEvent {
  LogoutEvent();
}
class TokenExpiredEvent {
  TokenExpiredEvent();
}
class CheckInternetEvent{
  bool isOnline;
  CheckInternetEvent(bool isOnline){
    this.isOnline = isOnline;
  }
}
class PostSubmissionEvent{
  PostSubmissionEvent();
}
class PostFilterEvent{
  LinkedHashMap<String, String> selectedFilterMap;
  PostFilterEvent(Map<String, String> selectedFilterValueMap){
    this.selectedFilterMap = selectedFilterValueMap;
  }
}
class DownloadInprogressEvent{
  String name;
  String progress;
  DownloadInprogressEvent(name,progress){
    this.name=name;
    this.progress=progress;
  }
}
class PostDownloadEvent{
  String name;
  PostDownloadEvent(name) {
    this.name = name;
  }
}


final EventBus eventBus = EventBus();