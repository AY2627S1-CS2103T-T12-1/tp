---
name: tp-gradle-validation
description: Build and validate the HireBase tp repository after code changes or when asked to test, check, or package it. Always run Java tests through the repository Gradle wrapper.
---

Run commands from the repository root containing `gradlew` and `build.gradle`. Use `./gradlew` for all builds, checks, tests and app launches (or `gradlew.bat` on Windows), rather than a globally installed Gradle or a direct Java/JUnit invocation. This project currently requires JDK 25; consult `build.gradle` if that requirement changes.

For completed code changes or a request to build and check, run:

```sh
./gradlew build check
```

This verifies compilation, packaging, tests and both production and test Checkstyle rules. For focused debugging, use the wrapper's test filter, for example:

```sh
./gradlew test --tests 'seedu.hirebase.logic.commands.EditCandidateTest'
```

A focused test run does not replace the full build and check before declaring a code change complete. Fix relevant failures and rerun the affected tasks. Report the commands actually run and their outcomes; distinguish tasks executed from tasks reported as up-to-date. Do not claim success if the command fails or remains running. For documentation-only edits, a build is unnecessary unless requested.

Inspect `build/reports/tests/test/index.html` for test failures and `build/reports/checkstyle/` for style failures. The distributable JAR is `build/libs/hirebase.jar`. Launch the UI with `./gradlew run` when requested; a successful launch does not replace automated checks.

If the sandbox prevents access to the Gradle cache, request the execution permission needed for the same wrapper command. Preserve the configured wrapper and build settings instead of changing them to bypass an environment restriction.
