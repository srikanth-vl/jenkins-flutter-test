import 'package:rxdart/rxdart.dart';
import '../models/root_config.dart';
import '../models/sub_app.dart';
import '../resources/root_config_provider.dart';

class RootConfigBloc {
  final _repository = RootConfigProvider();
  final _rootConfigController = PublishSubject<RootConfig>();
  final _projectTypeListController = PublishSubject<List<SubApp>>();

  Stream<RootConfig> get rootConfigStream => _rootConfigController.stream;

  Stream<List<SubApp>> get projectTypeListStream =>
      _projectTypeListController.stream;
  fetchRootConfig() async {
    RootConfig rootConfig = await _repository.initRootConfig();
    if(_rootConfigController.isClosed)
      return;
    _rootConfigController.sink.add(rootConfig);
  }

  fetchChildProjectTypes(String parentId) async {
    List<SubApp> apps;
    if (parentId == null) {
      // TODO : Return root project types
      rootConfigStream.listen((RootConfig rootconfig) {
        // print(rootconfig.toJson());
        List<SubApp> apps = List();
        if(_projectTypeListController.isClosed) return;
        _projectTypeListController.sink.add(rootconfig.config);
//        if (rootconfig != null &&
//            rootconfig.config != null &&
//            rootconfig.config.isNotEmpty) {
//          rootconfig.config.map((subApp) {
////            if (subApp.parentAppId == CommonConstants.SUPER_APP_ID) {SUPER_APP_ID
//              apps.add(subApp);
//              print('SubApp');
//              print(subApp.toJson());
////            }
//          });
//        }
//        print('fetched data');
//        _projectTypeListController.sink.add(apps);
      });
    } else {
      // TODO : Return child project types
      _projectTypeListController.sink.add(apps);
    }
  }

  dispose() {
    _rootConfigController.close();
    _projectTypeListController.close();
  }
}

//final rootConfigBloc = RootConfigBloc();
