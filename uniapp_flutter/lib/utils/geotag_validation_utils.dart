import '../models/gps_validation.dart';
import '../ui/helpers/form_values.dart';
import '../utils/string_utils.dart';
import 'package:geolocator/geolocator.dart';

import 'common_constants.dart';

class GeotagValidationUtils {


  Future<bool> validateGeotagImage(Position currentPosition,  GpsValidation gpsValidation, String projLat, String projLon) async {
    bool isValid = true;
    double mLatitude = currentPosition.latitude;
    double mLongitude = currentPosition.longitude;
    double mAccuracy = currentPosition.accuracy;

    if (mLatitude != CommonConstants.DEFAULT_LATITUDE &&
        mLongitude != CommonConstants.DEFAULT_LONGITUDE &&
        mAccuracy != CommonConstants.DEFAULT_ACCURACY) {
      if (gpsValidation != null &&
          gpsValidation.type != null &&
          gpsValidation.type.isNotEmpty &&
          gpsValidation.radius > 0) {
        switch (gpsValidation.type) {
          case "circular":
            if (gpsValidation.source == "project") {
              if (projLat != null &&
                  projLat.isNotEmpty &&
                  projLon != null &&
                  projLon.isNotEmpty &&
                  projLat !=
                      CommonConstants.DEFAULT_LATITUDE.toString() &&
                  projLon !=
                      CommonConstants.DEFAULT_LONGITUDE.toString()) {
                bool valid = await _validateGps(projLat, projLon, currentPosition.latitude.toString(), currentPosition.longitude.toString(), gpsValidation);
                if (valid) {
                } else {
//                  CommonUtils.showToast(message, context);
//                  Toast.makeText(getActivity(), getResources().getString(R.string.SELECT_VALID_LOCATION), Toast.LENGTH_SHORT).show();
                  isValid = false;
                }
              } else {
                //CommonUtils.showToast(message, context);
//                Toast.makeText(getActivity(), getResources().getString(R.string.VALIDATION_VALUES_NOT_FOUND), Toast.LENGTH_SHORT).show();
              }
            } else if (gpsValidation.source == "key") {
              String valueFromSource = getSubmittedFieldFromKey(gpsValidation.key);
              List<String> geotag = _getGeotagFromValue(gpsValidation.keyType, valueFromSource);

              if (geotag[0] != null && geotag[0].isNotEmpty && geotag[1] != null && geotag[1].isNotEmpty) {
                bool valid = await _validateGps(geotag[0], geotag[1], mLatitude.toString(), mLongitude.toString(), gpsValidation);
                if (valid) {
                } else {
                  //                  CommonUtils.showToast(message, context);
//                  Toast.makeText(getActivity(), getResources().getString(R.string.SELECT_VALID_LOCATION), Toast.LENGTH_SHORT).show();
                  isValid = false;
                }
              }else {
                //                  CommonUtils.showToast(message, context);
//                Toast.makeText(getActivity(), getResources().getString(R.string.VALIDATION_VALUES_NOT_FOUND), Toast.LENGTH_SHORT).show();
              }
            }
            break;

          case "bbox":
            break;
          default:
        }
      }
//      else if (mProject.mBBoxValidation != null && !mProject.mBBoxValidation.isEmpty()) {
//        // BBox validation
//        isValid = Utils.getInstance().geotagValidateImage(mLatitude, mLongitude, mProject.mBBoxValidation);
//
//      } else if (mProject.mCentroidValidation != null && !mProject.mCentroidValidation.isEmpty()) {
//        // Centroid validation
//        isValid = Utils.getInstance().geotagValidateImage(mLatitude, mLongitude, mProject.mCentroidValidation);
//      }
      else {
//                        Toast.makeText(getApplicationContext(), "No validation for location to be performed", Toast.LENGTH_LONG).show();
        // No validation to be performed
      }
    }
    return isValid;
  }

  Future<bool> _validateGps(String centerLatitude, String centerLongitude, String droppedLatitude, String droppedLongtiude, GpsValidation gpsValidation) async {
    double centerLat = double.parse(centerLatitude);
    double centerLon = double.parse(centerLongitude);

    double droppedLat = double.parse(droppedLatitude);
    double droppedLon = double.parse(droppedLongtiude);

    double radius = gpsValidation.radius.toDouble();
    double result = await Geolocator().distanceBetween(centerLat, centerLon, droppedLat, droppedLon);
    if (result <= radius){
      return true;
    } else{
      return false;
    }
  }

  List<String> _getGeotagFromValue(String keyType, String value) {
    List<String> geotag = List();
    if (keyType != null && value != null) {
      if (keyType.toLowerCase() == "image") {
        List<String> geotagWithUUID = StringUtils.getStringListFromDelimiter(
            CommonConstants.IMAGE_UUID_LONG_LAT_SEPARATOR, value);
        geotag.add(geotagWithUUID[1]);
        geotag.add(geotagWithUUID[2]);
      } else if (keyType.toLowerCase() == "geotag") {
        geotag = StringUtils.getStringListFromDelimiter(",", value);
      }
    }
    return geotag;
  }

  String getSubmittedFieldFromKey(String sskey){
    for(String key in formMap.formValues.keys) {
      List<String> keys = key.split("#");
      String finalKey = getKeyName(keys[keys.length - 1]);
      if (finalKey.toLowerCase() == sskey){
        return formMap.formValues[key];
      }
    }
    return null;
  }

  String getKeyName(String keyName) {
    List<String> keys = keyName.trim().split("\$\$");
    String key = null;
    if (keys != null && keys.isNotEmpty) {
      key = keys[keys.length - 1];
    }
    return key;
  }
}

final geotagValidationUtils = GeotagValidationUtils();