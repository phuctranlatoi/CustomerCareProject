import 'package:flutter/services.dart';

/// Bridge để giao tiếp 2 chiều giữa Flutter và Android (Java/Kotlin)
class AppChannel {
  static const MethodChannel _channel = MethodChannel('com.customercareproject/bridge');

  /// Gửi event từ Flutter về Android
  static Future<void> navigateToAndroid(String route, [Map<String, dynamic>? args]) async {
    try {
      await _channel.invokeMethod('navigate', {
        'route': route,
        ...?args,
      });
    } on PlatformException catch (e) {
      // ignore: avoid_print
      print('AppChannel error: ${e.message}');
    }
  }

  /// Yêu cầu Android đóng Flutter Activity/Fragment
  static Future<void> closeFlutter() async {
    try {
      await _channel.invokeMethod('close');
    } on PlatformException catch (_) {}
  }

  /// Nhận handler từ Android (Android gửi data sang Flutter)
  static void setMethodCallHandler(Future<dynamic> Function(MethodCall) handler) {
    _channel.setMethodCallHandler(handler);
  }

  /// Gọi API từ Android (chia sẻ Firebase auth token)
  static Future<String?> getAuthToken() async {
    try {
      final token = await _channel.invokeMethod<String>('getAuthToken');
      return token;
    } on PlatformException catch (_) {
      return null;
    }
  }

  /// Gửi thông báo về Android để xử lý ticket
  static Future<void> openTicketDetail(String ticketId) async {
    await navigateToAndroid('ticketDetail', {'ticketId': ticketId});
  }

  /// Mở màn hình chat với khách hàng cụ thể
  static Future<void> openChat(String chatId, String customerName) async {
    await navigateToAndroid('chat', {
      'chatId': chatId,
      'customerName': customerName,
    });
  }
}
