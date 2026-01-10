// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:geoservice_mobile/main.dart';

void main() {
  testWidgets('Login screen displays correctly', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const MyApp());

    // Verify that the login screen is displayed with key elements
    expect(find.text('GeoService Mobile'), findsOneWidget);
    expect(find.text('Connexion'), findsOneWidget);
    expect(find.byIcon(Icons.location_on), findsOneWidget);

    // Verify form fields exist
    expect(find.byType(TextFormField), findsWidgets);

    // Verify the login button exists
    expect(find.text('Se connecter'), findsOneWidget);
  });
}
