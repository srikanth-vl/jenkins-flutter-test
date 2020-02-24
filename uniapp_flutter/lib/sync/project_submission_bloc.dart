import 'package:rxdart/rxdart.dart';

import './project_submission_thread.dart';
import '../models/project_submission_result.dart';
import '../db/models/project_submission.dart';

class ProjectSubmissionBloc {

  final _projectSubmissionResultFetcher = PublishSubject<ProjectSubmissionResult>();

  Observable<ProjectSubmissionResult> get projectSubmissionResult =>
      _projectSubmissionResultFetcher.stream;


  callProjectSubmissionThread(ProjectSubmission contentValues, List<String> uuids) async {
    ProjectSubmissionThread projectSubmissionThread = new ProjectSubmissionThread(contentValues, uuids);
    ProjectSubmissionResult projectSubmissionResult = await projectSubmissionThread.callSubmissionThread();
    _projectSubmissionResultFetcher.sink.add(projectSubmissionResult);
  }

  dispose() {
    _projectSubmissionResultFetcher.close();
  }
}

final projectSubmissionBloc = ProjectSubmissionBloc();
