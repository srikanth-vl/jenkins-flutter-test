import '../event/event_utils.dart';
import '../db/databaseHelper.dart';
import '../resources/uniapp_response.dart';
import '../ua_app_context.dart';
import '../utils/common_constants.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import '../error/all_custom_exception.dart';
import '../log/uniapp_logger.dart';
import '../models/localization_config.dart';
import '../localization/localization_utils.dart';
class LocalizationService {

  DatabaseHelper _databaseHelper;
  Logger logger =  getLogger("LocalizationService");
  LocalizationService() {
    _databaseHelper = UAAppContext.getInstance().unifiedAppDBHelper;
  }

  Future<String> callLocalizationService() async {

    // Logic
    // 1. Fetch from Server
    // 2. Save to json file
    // 3. Return the config String
    // 4. In case of error - return the error type
    String content = await fetchFromServer();
    String localizationConfig = "{\"hi\":{\"Structure Type\":\"संरचना प्रकार\",\"Structure Possible\":\"संरचना संभव\",\"Yes\":\"हाँ\",\"No\":\"नहीं\",\"PT\":\"पीटी\",\"LBS\":\"एलबीएस\",\"RFD\":\"आरएफडी\",\"GABION\":\"गेबियन\",\"MINI PT\":\"मिनी पीटी\",\"FARMPOND\":\"कृषि तालाब\",\"CHECKWALL\":\"चेकवाल \",\"PT STORAGE\":\"पीटी भंडारण\",\"PERCOLATION STORAGE\":\"\",\"CHECKDAM-STORAGE\":\"\",\"CHECKDAM-PERCOLATION\":\"पीटी भंडारण\",\"Drain Name\":\"नाली का नाम\",\"Maximum characters\":\"अधिकतम वर्ण\",\"Geo-tagged Picture\":\"भू-टैग चित्र\",\"Back\":\"पीछे\",\"Next\":\"अगला\",\"Submit\":\"सबमिट\",\"Preview\":\"पूर्वावलोकन\",\"Cancel\":\"हटाए\",\"Panchayat\":\"पंचायत\",\"District\":\"जिला\",\"Structre Type\":\"संरचना प्रकार\",\"Watershed\":\"वाटरशेड\",\"Survey Number\":\"सर्वे नंबर\",\"Mandal\":\"मंडल\",\"Sub Basin\":\"सब बेसिन\",\"GPS Location\":\"जीपीएस स्थान\",\"Change\":\"बदलें\",\"Get Direction\":\"दिशा प्राप्त करें\",\"ID\":\"आई डी\",\"Is the structure possible within 50m radius?\":\"क्या संरचना 50 मीटर के दायरे में संभव है?\",\"Select Reason\":\"कारण चुनें\",\"Farmer didn't agree\":\"किसान सहमत नहीं था\",\"Existing Structure\":\"मौजूदा संरचना\",\"Encroached Drain\":\"अतिक्रमित नाली\",\"Other Reason\":\"दूसरी वजह\",\"Proposed Location Picture\":\"प्रस्तावित स्थान चित्र\",\"Changed location Picture\":\"परिवर्तित स्थान चित्र\",\"Drop New Location\":\"नया स्थान चिह्नित करें\",\"Changed Location Picture\":\"परिवर्तित स्थान चित्र\",\"Location\":\"स्थान\"}}"+
        "" ;
    Map<String, dynamic> configjson;
    if (content == null || content == '{}') {
        configjson = LocalizationUtils().getData();
    }  else if (content == CommonConstants.NO_NEW_LOCALIZATION_CONFIG) {
      logger.e(LogTags.LOCALIZATION_CONFIG, "LocalizationConfig : No New Version");
        configjson = LocalizationUtils().getData();
    }else {
      // Add new RootConfig to the DB
      LocalizationConfig config = null;
      config = LocalizationConfig.fromJson(jsonDecode(content));
      localizationConfig = config.config;
      LocalizationUtils().writeToFile(config.config);
      logger.i("LocalizationConfig : saved to file");
      return localizationConfig;
    }
    if(configjson != null) {
      localizationConfig = jsonEncode(LocalizationUtils().getData());
    }
    return localizationConfig;
  }

  Future<String> fetchFromServer() async{

    var requestParam = createRequestParams();
    if (requestParam == null || requestParam == '') {
      // throw exception
      // log error
      logger.e("Error creating request parameters - Map Config");
      throw new AppCriticalException(UAAppErrorCodes.MAP_CONFIG_FETCH, "Error creating request parameters - Map Config");
    }


    final response = await http.post(
        CommonConstants.BASE_URL +"localizationconfigdata",
        headers: {"Content-Type": "application/json"},
        body: requestParam);

    if (response == null ||
        response.statusCode != 200 ||
        response.body == null) {
      // log error
      logger.e("Error getting LocalizationConfig from server 2");
      // throw exception
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting LocalizationConfig from server 2");
    }
    UniappResponse uniappResponse =
    UniappResponse.fromJson(jsonDecode(response.body));
    if (uniappResponse == null || uniappResponse.result == null) {
      // throw exception log it
      logger.e("Error getting LocalizationConfig from server , uniapp response ::${uniappResponse == null ?uniappResponse : uniappResponse.toJson()}");
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting LocalizationConfig from server , uniapp response ::${uniappResponse == null ?uniappResponse : uniappResponse.toJson()}");
    }
    int code = uniappResponse.result.status;
    String content;
    if (code == 200) {
//      logger.d("Successfully fetched LocalizationConfig from server${uniappResponse.result.toJson()}");
      content = uniappResponse.result.content;
      if (content == "{}") {
        // Same version on the server
        return CommonConstants.NO_NEW_LOCALIZATION_CONFIG;
      } else {
        return content;
      }
    } else if (code == 350) {
      //  token expired , broadcast logout event
      eventBus.fire(TokenExpiredEvent());
      // stop backround sync service
      throw new TokenExpiredException(UAAppErrorCodes.USER_TOKEN_EXPIRY_ERROR, "Tokn expired");
      //:todo update sharedpreferences
      return content;
    } else {
      // log error and exception
      logger.e("Error getting LocalizationConfig from server (invalid data) : response code : ${code} received content : " + content.toString());
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
          , 'Error getting LocalizationConfig from server (invalid data) : response code : ${code}');
    }
  }


  String createRequestParams() {
    Map requestParameters = Map();
    requestParameters[CommonConstants.MAP_CONFIG_USER_ID_KEY] = UAAppContext.getInstance().userID;
    requestParameters[CommonConstants.MAP_CONFIG_TOKEN_KEY] = UAAppContext.getInstance().token;
    requestParameters[CommonConstants.MAP_CONFIG_SUPER_APP_KEY] = CommonConstants.SUPER_APP_ID;
    requestParameters[CommonConstants.MAP_CONFIG_VERSION_KEY]=  '0';
    var body = json.encode(requestParameters);
    return body;

  }
}
