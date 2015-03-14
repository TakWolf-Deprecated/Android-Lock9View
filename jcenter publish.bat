@echo - clean -

call gradlew clean

@echo - build -

call gradlew build

@echo -begin upload to bintray -

call gradlew bintrayUpload
