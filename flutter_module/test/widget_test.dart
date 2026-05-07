// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_module/src/app.dart';

void main() {
  testWidgets('CustomerCareApp renders without crashing', (WidgetTester tester) async {
    await tester.pumpWidget(const CustomerCareApp());
    expect(find.byType(CustomerCareApp), findsOneWidget);
  });
}
