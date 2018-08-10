# Exit if any command fails
set -e
set -o pipefail

# yes | /usr/local/android-sdk/tools/bin/sdkmanager "system-images;android-24;default;armeabi-v7a"
# echo no | /usr/local/android-sdk/tools/bin/avdmanager create avd -n travis -k "system-images;android-24;default;armeabi-v7a" --device "Nexus 5" --force --abi "armeabi-v7a"
# /usr/local/android-sdk/emulator/emulator -avd travis -no-audio -no-window &
# /usr/local/android-sdk/platform-tools/adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'

android list target
echo y | android update sdk -a --no-ui --filter android-24
echo y | android update sdk -a --no-ui --filter sys-img-armeabi-v7a-android-24
echo no | android create avd --force -n travis -t android-24 --abi armeabi-v7a
echo "-----Starting emulator"
emulator -avd travis -no-audio -no-window &
echo "-----Waiting for boot"
android-wait-for-emulator
adb shell input keyevent 82 &
echo "-----Booted"