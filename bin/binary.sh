LEIN_SNAPSHOTS_IN_RELEASE=1 lein uberjar
cat bin/stub.sh target/octo-0.8.2-standalone.jar > target/octo && chmod +x target/octo

