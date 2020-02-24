import 'repository.dart';

class GetOTPService {
  final _repository = Repository();

  Future<String> requestOTPGeneration(String username) async {
    String response = await _repository.requestOTPGeneration(username);
    return response;
  }
}