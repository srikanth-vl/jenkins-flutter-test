import 'package:logger/logger.dart';
import 'simple_log_printer.dart';
export 'log_tags.dart';
export 'package:logger/logger.dart';
Logger getLogger(String className) {
  return Logger(printer: SimpleLogPrinter(className));
}