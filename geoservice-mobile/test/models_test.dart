import 'package:flutter_test/flutter_test.dart';
import 'package:geoservice_mobile/models/auth_response.dart';
import 'package:geoservice_mobile/models/login_request.dart';

void main() {
  group('Models Tests', () {
    group('LoginRequest', () {
      test('LoginRequest toJson returns correct map', () {
        final request = LoginRequest(
          username: 'john.doe',
          password: 'password123',
        );

        final json = request.toJson();

        expect(json['username'], equals('john.doe'));
        expect(json['password'], equals('password123'));
      });
    });

    group('AuthResponse', () {
      test('AuthResponse.fromJson creates instance from JSON', () {
        const json = {
          'accessToken': 'token123',
          'refreshToken': 'refresh123',
          'tokenType': 'Bearer',
          'expiresIn': 3600,
          'userId': 1,
          'username': 'john.doe',
          'email': 'john@example.com',
          'fullName': 'John Doe',
          'role': 'USER',
        };

        final response = AuthResponse.fromJson(json);

        expect(response.accessToken, equals('token123'));
        expect(response.refreshToken, equals('refresh123'));
        expect(response.tokenType, equals('Bearer'));
        expect(response.expiresIn, equals(3600));
        expect(response.userId, equals(1));
        expect(response.username, equals('john.doe'));
        expect(response.email, equals('john@example.com'));
        expect(response.fullName, equals('John Doe'));
        expect(response.role, equals('USER'));
      });

      test('AuthResponse toJson returns correct map', () {
        final response = AuthResponse(
          accessToken: 'token123',
          refreshToken: 'refresh123',
          tokenType: 'Bearer',
          expiresIn: 3600,
          userId: 1,
          username: 'john.doe',
          email: 'john@example.com',
          fullName: 'John Doe',
          role: 'USER',
        );

        final json = response.toJson();

        expect(json['accessToken'], equals('token123'));
        expect(json['refreshToken'], equals('refresh123'));
        expect(json['tokenType'], equals('Bearer'));
        expect(json['expiresIn'], equals(3600));
      });

      test('AuthResponse.fromJson handles missing fields with defaults', () {
        const json = {
          'accessToken': 'token123',
          'refreshToken': 'refresh123',
        };

        final response = AuthResponse.fromJson(json);

        expect(response.tokenType, equals('Bearer'));
        expect(response.expiresIn, equals(3600));
        expect(response.userId, equals(0));
        expect(response.username, equals(''));
      });
    });
  });
}
