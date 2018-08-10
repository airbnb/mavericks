#! /bin/bash

# Exit if any command fails
set -e
set -o pipefail

Reboot()
{
  adb reboot
  adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'
}

echo "-----Building and Installing"
./gradlew :sample:assembleDebug :sample:assembleAndroidTest
adb push sample/build/outputs/apk/debug/sample-debug.apk /data/local/tmp/com.airbnb.mvrx.sample
adb shell pm install -t -r "/data/local/tmp/com.airbnb.mvrx.sample"
adb push sample/build/outputs/apk/androidTest/debug/sample-debug-androidTest.apk /data/local/tmp/com.airbnb.mvrx.sample.test
adb shell pm install -t -r "/data/local/tmp/com.airbnb.mvrx.sample.test"

echo "-----Granting WRITE_SECURE_SETTINGS"
adb root
adb shell pm grant com.airbnb.mvrx.sample android.permission.WRITE_SECURE_SETTINGS

adb shell settings put global always_finish_activities 0
# This is necessary because the setting doesn't take effect without rebooting.
# There may be other ways of doing this but just changing the setting doesn't help.
Reboot
echo "-----Running test"
adb shell am instrument -w -r -e debug false -e package 'com.airbnb.mvrx.sample.keep' com.airbnb.mvrx.sample.test/android.support.test.runner.AndroidJUnitRunner

echo "-----Setting Do Not Keep Activities to TRUE"
adb shell settings put global always_finish_activities 1
Reboot
adb shell am instrument -w -r -e debug false -e package 'com.airbnb.mvrx.sample.donotkeep' com.airbnb.mvrx.sample.test/android.support.test.runner.AndroidJUnitRunner

# TODO: test disabling the background process limit
# It is not clear how to test this with espresso because the process has to die.
# This command disables background processes
# adb shell service call activity 51 i32 0
