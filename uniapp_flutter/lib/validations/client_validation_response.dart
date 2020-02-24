class ClientValidationResponse {
  bool mIsValid;
  String mMessage;
  Map<String, String> mKeyToErrorMessage;

  ClientValidationResponse ok() {
    ClientValidationResponse clientValidationResponse = new ClientValidationResponse();
    clientValidationResponse.mIsValid = true;
    clientValidationResponse.mKeyToErrorMessage = new Map();
    return clientValidationResponse;
  }
}