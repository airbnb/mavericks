Releasing
========

1. Bump the VERSION_NAME property in `gradle.properties` based on Major.Minor.Patch naming scheme
2. Update `CHANGELOG.md`, adding the new release version and release notes
3. `./commitAndTagRelease.sh Major.Minor.Patch`
   1. This will make a commit with the given release number and add the version tag
   2. eg "./commitAndTagRelease.sh 3.0.1"
4. Add your sonatype login information under gradle properties mavenCentralUsername and mavenCentralPassword in your local user gradle.properties file
5. `./gradlew publish` to build the artifacts and publish them to maven
6. Open PR on Github and merge. Github action automated_github_release.yml will automatically publish as release on github
   1. Automated publishing only works if commit is tagged starting with "v". commitAndTagRelease script does this for you.
   2. Automated release generates release notes from the commits since last release. You may want to manually edit the release notes to add more details.