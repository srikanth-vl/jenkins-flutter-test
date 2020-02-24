
class TokenExpiredException
    implements Exception {

 String errorCode;
 String message;
 String source;

 TokenExpiredException(this.errorCode, this.message,{this.source});

}
