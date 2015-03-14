@echo - clean -

call gradlew clean

@echo - build -

call gradlew build

@echo - install -

call gradlew install

@echo -begin upload to bintray -

call gradlew bintrayUpload
