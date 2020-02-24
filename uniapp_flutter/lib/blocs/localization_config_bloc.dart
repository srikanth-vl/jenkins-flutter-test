import '../resources/repository.dart';
import 'package:rxdart/rxdart.dart';
import '../models/localization_config.dart';

class LocalizationBloc {
  final _repository = Repository();
  final _localizationController = PublishSubject<LocalizationConfig>();

  Stream<LocalizationConfig> get localizationConfigStream => _localizationController.stream;

  fetchLocalizationConfig() async {
    LocalizationConfig localizationConfig = await _repository.fetchLocalizationConfig();
    _localizationController.sink.add(localizationConfig);
  }

  dispose() {
    _localizationController.close();
  }
}

//final localizationBloc = LocalizationBloc();
