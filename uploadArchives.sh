#! /bin/bash
# Run me to ensuer that all libraries are uploaded.
./gradlew clean -no-daemon --no-parallel --rerun-tasks assembleRelease mvrx:uploadArchives mvrx-persiststate:uploadArchives testing:uploadArchives