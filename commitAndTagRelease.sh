# Pass in a release number, eg 3.0.1
git commit -am "Prepare for release $1" && git tag -a "v$1" -m "Release $1"
git push -u origin HEAD
# The github release action only runs when a tag is pushed.
# It requires tags to start with "v" in order to recognize them as releases.
git push -u origin "v$1"
branchName=$(git rev-parse --abbrev-ref HEAD)
/usr/bin/open -a "/Applications/Google Chrome.app" "https://github.com/airbnb/mavericks/compare/$branchName?expand=1"