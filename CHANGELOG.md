# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [3.0.0]
- Upped dependencies
- Adds serviceName parameter to be used in push notifications
- Updates log statement to include serviceName

## [2.0.3]
- Log sending push notification
- Name change: useRegistrationId
- Use java21
- Upgraded spring to 5.3.32
- Upped dependencies
- Remove unfinished registrations when starting new registration.
- Enrollments can not be started for existing registrations https://www.pivotaltracker.com/story/show/184549110
- Always update the registration with the last updated timestamp
- Do not log stacktrace for PushNotificationException
- Set updated when suspending authentication
- fixed tiqr-java-connector uses insecure ECB cipher mode
- Use NativePRNGNonBlocking secureRandom
- 31edb91 If lookups fail or authentications or registrations have an invalid status, then throw TiqrException
- Show QR-code if Apple Push Notification is not accepted

## [0.2.9]

If lookups fail or authentications or registrations have an invalid status,
then throw TiqrException

## [0.2.8]

If Apple Push Notification is rejected, throw error (to force the display of
QR code in the client)

## [0.2.7]

Log rejection for Apple Push Notifications

## [0.2.6]

First release
