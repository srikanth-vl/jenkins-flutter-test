import 'package:rxdart/rxdart.dart';
import '../ua_app_context.dart';
import '../models/app_meta_data_config.dart';
import '../resources/app_meta_config_provider.dart';

class AppMetaConfigBloc {
  final _repository = AppMetaConfigProvider.instance;
  final _appMetaDataConfigFetcher = PublishSubject<AppMetaDataConfig>();
 bool _isDisposed = false;
  Observable<AppMetaDataConfig> get appMetaConfig =>
      _appMetaDataConfigFetcher.stream;

  fetchAppMetaConfig() async {
    if(_isDisposed) {
      return;
    }
    AppMetaDataConfig data = await _repository.initAppMDConfig();
    UAAppContext.getInstance().appMDConfig = data;
    if(!_appMetaDataConfigFetcher.isClosed) {
      _appMetaDataConfigFetcher.sink.add(data);
    }
  }

  dispose() {
    _appMetaDataConfigFetcher.close();
    _isDisposed = true;
  }
}

//final appMetaConfigBloc = AppMetaConfigBloc();
