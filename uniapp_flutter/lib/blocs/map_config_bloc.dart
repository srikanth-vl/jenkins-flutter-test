import 'package:rxdart/rxdart.dart';
import '../models/map_config.dart';
import '../ua_app_context.dart';

import '../resources/map_config_provider.dart';

class MapConfigBloc {
  final _repository = MapConfigProvider();
  final _mapConfigController = PublishSubject<MapConfig>();

  Stream<MapConfig> get mapConfigStream => _mapConfigController.stream;

  fetchMapConfig() async {
    MapConfig mapConfig = await _repository.initMapConfig();
    UAAppContext.getInstance().mapConfig = mapConfig;
    _mapConfigController.sink.add(mapConfig);
  }

  dispose() {
    _mapConfigController.close();
  }
}

//final mapConfigBloc = MapConfigBloc();
