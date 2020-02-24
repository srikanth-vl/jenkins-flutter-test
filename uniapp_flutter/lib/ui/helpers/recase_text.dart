import 'package:recase/recase.dart';

class RecaseText {
  String reCase(String caseStyle, String textToRecase) {
    if (textToRecase != null && textToRecase.isNotEmpty) {
      ReCase rc = new ReCase(textToRecase);
      // print(rc.)
      switch (caseStyle) {
        case 'camelCase':
          return rc.camelCase;

        case 'constantCase':
          return rc.constantCase;

        case 'dotCase':
          return rc.dotCase;

        case 'headerCase':
          return rc.headerCase;

        case 'paramCase':
          return rc.paramCase;

        case 'pascalCase':
          return rc.pascalCase;

        case 'pathCase':
          return rc.pathCase;

        case 'sentenceCase':
          return rc.sentenceCase;

        case 'snakeCase':
          return rc.snakeCase;

        case 'titleCase':
          return rc.titleCase;

        default:
          return textToRecase;
      }
    } else {
      return '-';
    }
  }
}
