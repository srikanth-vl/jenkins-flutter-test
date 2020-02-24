import 'package:rxdart/rxdart.dart';
import '../db/models/project_master_data_table.dart';
import '../ua_app_context.dart';
import '../resources/project_list_provider.dart';

class ProjectGroupBloc {
  final _repository = ProjectListProvider.getInstance();
  final _projectGroupsAttributeController = BehaviorSubject<Map<String,List<String>>>();

  Stream<Map<String,List<String>>> get projectGroups =>
      _projectGroupsAttributeController.stream;

  fetchProjects(String appId) async {
    String userId = UAAppContext.getInstance().userID ;
    Map<String,List<String>> projectGroupsAttribute = await _repository.fetchProjectGroupsAttribute(appId, userId);
    await _projectGroupsAttributeController.add(projectGroupsAttribute);
    print('added ProjectGroupAttribute :: ${projectGroupsAttribute.length}');
  }

  dispose() {
    _projectGroupsAttributeController.close();
  }
}

//final projectGroupBloc = ProjectGroupBloc();
