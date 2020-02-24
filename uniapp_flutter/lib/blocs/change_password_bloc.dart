import '../resources/repository.dart';
import 'package:rxdart/rxdart.dart';

class ChangePasswordBloc{
  final _repository = Repository();
  final _changePasswordController = BehaviorSubject<bool>();

  Stream<bool> get changePasswordControllingStream =>
      _changePasswordController.stream;

  changeUserPassword(String otp, String newPassword) async{
    bool response = await _repository.changeUserPassword(otp, newPassword);
    if(!_changePasswordController.isClosed) {
      _changePasswordController.sink.add(response);
    }
  }

  dispose(){
    _changePasswordController.close();
  }

}

//final changePasswordBloc = ChangePasswordBloc();