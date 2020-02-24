
  class UAException
    implements Exception {

    String errorCode;
    String message;
    String source;

     UAException(this.errorCode, this.message,{this.source});

}