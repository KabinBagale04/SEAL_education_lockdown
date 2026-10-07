#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
./mvnw -o -q compile dependency:build-classpath -Dmdep.outputFile=target/client-classpath.txt
client_deps="$(cat target/client-classpath.txt)"
mkdir -p target/mvp-probe
javac --release 21 -cp "$client_deps:target/classes" -d target/mvp-probe tools/MvpUiProbe.java
java -Djavafx.cachedir=/tmp/seal-javafx-cache --enable-native-access=javafx.graphics \
  --module-path "$client_deps:target/classes" --add-modules com.example.seal \
  --add-exports com.example.seal/com.example.seal.service=ALL-UNNAMED \
  --add-exports com.example.seal/com.example.seal.session=ALL-UNNAMED \
  --add-exports com.example.seal/com.example.seal.model=ALL-UNNAMED \
  --add-exports com.example.seal/com.example.seal.dto=ALL-UNNAMED \
  --add-exports com.example.seal/com.example.seal.navigation=ALL-UNNAMED \
  --add-exports com.example.seal/com.example.seal.controller.teacher=ALL-UNNAMED \
  -cp target/mvp-probe MvpUiProbe
