
import 'package:Bluis/db/models/user_meta_data_table.dart';
import 'package:test/test.dart';

import '../lib/blocs/login_bloc.dart';
import '../lib/resources/login_api_provider.dart';


void main() {


  test('Login Credentials check', ()async  {

    final loginBloc = LoginBloc();
    String userId = "123";
    LoginApiProvider loginApiProvider = LoginApiProvider();
    UserMetaDataTable userMetaDataTable = await loginApiProvider.authenticateLoginCredentials(userId, "vassar");
    expect(userId, userMetaDataTable.userId);
  });
}
