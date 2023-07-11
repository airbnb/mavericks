Releasing
========

1. Bump the VERSION_NAME property in `gradle.properties` based on Major.Minor.Patch naming scheme
2. Add your sonatype login information under gradle properties mavenCentralUsername and mavenCentralPassword in your local user gradle.properties file
3. Run `./gradlew publish` to build the artifacts and publish them to maven
4. Then run `./commitAndTagRelease.sh Major.Minor.Patch` with your version number
   1. eg "./commitAndTagRelease.sh 3.0.1"
   2. This will make a commit with the given release number and add the version tag
   3. It will then push the tag to github
      1. A github action will automatically run when the tag is pushed to generate a github release for this version
      2. Automated publishing only works if commit is tagged starting with "v". commitAndTagRelease script does this for you when passing a semver version number.
      3. The release will automatically generate release notes from the commits since last release. You may want to manually edit the release notes to add more details.
5. The script will also open up the branch for a PR in github
   1. If there is additional relevant information about the release that isn't captured in the commit history you should add it to the PR description as well as to the release summary
   2. Manually merge the PR branch to main, but at this point the release is already public.


Maven Local Installation
=======================

If testing changes locally, you can install to mavenLocal via `./gradlew publishToMavenLocal`