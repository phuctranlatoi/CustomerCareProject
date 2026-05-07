import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class AppTheme {
  // Brand Colors — khớp với màu primary của Android app
  static const Color primaryBlue = Color(0xFF1565C0);
  static const Color primaryBlueDark = Color(0xFF0D47A1);
  static const Color primaryBlueLight = Color(0xFF1976D2);
  static const Color accentTeal = Color(0xFF00897B);
  static const Color errorRed = Color(0xFFD32F2F);
  static const Color warningAmber = Color(0xFFF57C00);
  static const Color successGreen = Color(0xFF2E7D32);

  static ThemeData get lightTheme {
    final colorScheme = ColorScheme.fromSeed(
      seedColor: primaryBlue,
      brightness: Brightness.light,
      primary: primaryBlue,
      secondary: accentTeal,
      error: errorRed,
      surface: const Color(0xFFF8F9FA),
      onSurface: const Color(0xFF1A1C1E),
    );

    return ThemeData(
      useMaterial3: true,
      colorScheme: colorScheme,
      fontFamily: 'Roboto',
      appBarTheme: AppBarTheme(
        backgroundColor: primaryBlue,
        foregroundColor: Colors.white,
        elevation: 0,
        centerTitle: false,
        systemOverlayStyle: SystemUiOverlayStyle.light,
        titleTextStyle: const TextStyle(
          color: Colors.white,
          fontSize: 20,
          fontWeight: FontWeight.w600,
          letterSpacing: 0.15,
        ),
      ),
      cardTheme: CardTheme(
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
        ),
        color: Colors.white,
        shadowColor: Colors.black.withOpacity(0.08),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: primaryBlue,
          foregroundColor: Colors.white,
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
          textStyle: const TextStyle(
            fontSize: 15,
            fontWeight: FontWeight.w600,
            letterSpacing: 0.5,
          ),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: const Color(0xFFF3F4F6),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide.none,
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: primaryBlue, width: 2),
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      ),
      chipTheme: ChipThemeData(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(8),
        ),
      ),
      dividerTheme: const DividerThemeData(
        space: 0,
        thickness: 0.5,
        color: Color(0xFFE0E0E0),
      ),
    );
  }

  static ThemeData get darkTheme {
    final colorScheme = ColorScheme.fromSeed(
      seedColor: primaryBlueLight,
      brightness: Brightness.dark,
      primary: primaryBlueLight,
      secondary: accentTeal,
      error: const Color(0xFFEF9A9A),
      surface: const Color(0xFF1A1C1E),
      onSurface: const Color(0xFFE2E2E6),
    );

    return ThemeData(
      useMaterial3: true,
      colorScheme: colorScheme,
      fontFamily: 'Roboto',
      appBarTheme: AppBarTheme(
        backgroundColor: const Color(0xFF1A1C1E),
        foregroundColor: Colors.white,
        elevation: 0,
        centerTitle: false,
        systemOverlayStyle: SystemUiOverlayStyle.light,
        titleTextStyle: const TextStyle(
          color: Colors.white,
          fontSize: 20,
          fontWeight: FontWeight.w600,
        ),
      ),
      cardTheme: CardTheme(
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
        ),
        color: const Color(0xFF2C2E30),
      ),
    );
  }

  // Status colors
  static Color statusColor(String status) {
    switch (status.toLowerCase()) {
      case 'mở':
      case 'open':
        return const Color(0xFF1565C0);
      case 'đang xử lý':
      case 'in_progress':
        return const Color(0xFFF57C00);
      case 'đã đóng':
      case 'closed':
        return const Color(0xFF2E7D32);
      case 'chờ phản hồi':
      case 'waiting':
        return const Color(0xFF6A1B9A);
      default:
        return const Color(0xFF607D8B);
    }
  }

  // Priority colors
  static Color priorityColor(String priority) {
    switch (priority.toLowerCase()) {
      case 'cao':
      case 'high':
        return errorRed;
      case 'trung bình':
      case 'medium':
        return warningAmber;
      case 'thấp':
      case 'low':
        return successGreen;
      default:
        return const Color(0xFF607D8B);
    }
  }
}
