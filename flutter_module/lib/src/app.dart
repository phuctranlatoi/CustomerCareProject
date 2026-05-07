import 'package:flutter/material.dart';
import 'theme/app_theme.dart';
import 'screens/chat/chat_screen.dart';
import 'screens/home/home_screen.dart';
import 'screens/tickets/ticket_list_screen.dart';
import 'screens/profile/profile_screen.dart';

class CustomerCareApp extends StatelessWidget {
  final String initialRoute;

  const CustomerCareApp({super.key, this.initialRoute = '/home'});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Chăm Sóc Khách Hàng',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      themeMode: ThemeMode.system,
      initialRoute: initialRoute,
      routes: {
        '/home': (_) => const HomeScreen(),
        '/chat': (_) => const ChatScreen(),
        '/tickets': (_) => const TicketListScreen(),
        '/profile': (_) => const ProfileScreen(),
      },
    );
  }
}
