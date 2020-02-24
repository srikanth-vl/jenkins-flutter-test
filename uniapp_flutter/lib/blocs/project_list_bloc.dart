import 'package:rxdart/rxdart.dart';
import '../log/uniapp_logger.dart';
import '../db/models/project_master_data_table.dart';
import '../ua_app_context.dart';
import '../resources/project_list_provider.dart';

class ProjectListBloc {
  Logger logger = getLogger("ProjectListBloc");

  final _repository = ProjectListProvider.getInstance();
  final _projectListController =
      BehaviorSubject<List<ProjectMasterDataTable>>();

  Stream<List<ProjectMasterDataTable>> get projectListStream =>
      _projectListController.stream;

  fetchProjects(String appId, String groupKey, String groupValue) async {
    String userId = UAAppContext.getInstance().userID;
    List<ProjectMasterDataTable> projects =
        await _repository.fetchProjects(appId, userId, groupKey,groupValue);
    await _projectListController.add(projects);

    logger.i('AppId: $appId');
    logger.i('Projects available: ${projects.length}');
  }

  dispose() {
    _projectListController.close();
  }
}

//final projectListBloc = ProjectListBloc();
