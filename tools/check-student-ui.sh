#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
./mvnw -o -q compile dependency:build-classpath -Dmdep.outputFile=target/student-classpath.txt
student_deps="$(cat target/student-classpath.txt)"
mkdir -p target/student-probe
javac --release 21 -cp "$student_deps:target/classes" -d target/student-probe tools/StudentUiProbe.java
java -Djavafx.cachedir=/tmp/seal-javafx-cache \
  --enable-native-access=javafx.graphics \
  --module-path "$student_deps:target/classes" --add-modules com.example.seal \
  --add-exports com.example.seal/com.example.seal.session=ALL-UNNAMED \
  --add-exports com.example.seal/com.example.seal.student=ALL-UNNAMED \
  --add-exports com.example.seal/com.example.seal.student.model=ALL-UNNAMED \
  --add-exports com.example.seal/com.example.seal.student.service=ALL-UNNAMED \
  -cp target/student-probe StudentUiProbe
