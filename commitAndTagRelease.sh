# Pass in a release number, eg 3.0.1
c "Prepare for release $1" && git tag -a "v$1" -m "Release $1"