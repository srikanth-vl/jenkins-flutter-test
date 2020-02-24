import 'package:rxdart/rxdart.dart';
import 'dart:async';

import '../db/models/user_meta_data_table.dart';
import '../resources/repository.dart';

class LoginBloc {
  final _repository = Repository();
  final _loginAuthenticator = BehaviorSubject<UserMetaDataTable>();

  Stream<UserMetaDataTable> get loginAuthenticationStream =>
      _loginAuthenticator.stream;

  authenticateLoginCredentials(String username, String password) async {
    UserMetaDataTable userMetaDataTable =
        await _repository.authenticateCredentials(username, password);
    if(!_loginAuthenticator.isClosed) {
      _loginAuthenticator.sink.add(userMetaDataTable);
    }
  }

  dispose() {
    _loginAuthenticator.close();
  }
}

//final loginBloc = LoginBloc();
