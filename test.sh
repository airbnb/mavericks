Reboot()
{
  adb reboot
  adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'
}

echo "-----Building"
./gradlew :sample:assembleDebug :sample:assembleAndroidTest
echo "-----Installing"
adb push sample/build/outputs/apk/debug/sample-debug.apk /data/local/tmp/com.airbnb.mvrx.sample
adb shell pm install -t -r "/data/local/tmp/com.airbnb.mvrx.sample"
adb push sample/build/outputs/apk/androidTest/debug/sample-debug-androidTest.apk /data/local/tmp/com.airbnb.mvrx.sample.test
adb shell pm install -t -r "/data/local/tmp/com.airbnb.mvrx.sample.test"

echo "-----Granting WRITE_SECURE_SETTINGS"
adb shell pm grant com.airbnb.mvrx.sample android.permission.WRITE_SECURE_SETTINGS

echo "-----Setting Do Not Keep Activities to TRUE"
adb shell settings put global always_finish_activities 0
# This is necessary because the setting doesn't take effect without rebooting.
# There may be other ways of doing this but just changing the setting doesn't help.
echo "-----Rebooting"
Reboot
echo "-----Running test"
adb shell am instrument -w -r -e debug false -e package 'com.airbnb.mvrx.sample.keepactivities' com.airbnb.mvrx.sample.test/android.support.test.runner.AndroidJUnitRunner

echo "-----Setting Do Not Keep Activities to FALSE"
adb shell settings put global always_finish_activities 1
echo "-----Rebooting"
Reboot
echo "-----Running test with do not keep activities on"
adb shell am instrument -w -r -e debug false -e package 'com.airbnb.mvrx.sample.donotkeepactivities' com.airbnb.mvrx.sample.test/android.support.test.runner.AndroidJUnitRunner