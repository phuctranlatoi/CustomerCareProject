import 'package:flutter/material.dart';
import 'src/app.dart';

// Entry point khi chạy standalone (để test)
void main() {
  runApp(const CustomerCareApp());
}

// Entry point chính cho Add-to-App — Android gọi hàm này
@pragma('vm:entry-point')
void chatScreen() {
  runApp(const CustomerCareApp(initialRoute: '/chat'));
}

@pragma('vm:entry-point')
void homeScreen() {
  runApp(const CustomerCareApp(initialRoute: '/home'));
}

@pragma('vm:entry-point')
void ticketScreen() {
  runApp(const CustomerCareApp(initialRoute: '/tickets'));
}

@pragma('vm:entry-point')
void profileScreen() {
  runApp(const CustomerCareApp(initialRoute: '/profile'));
}
