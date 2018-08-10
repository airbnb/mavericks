# Exit if any command fails
set -e
set -o pipefail

$ANDROID_HOME/tools/binsdkmanager "system-images;android-24;default;armeabi-v7a"
echo no | avdmanager create avd -n travis -k "system-images;android-24;default;armeabi-v7a" --device "Nexus 5" --force --abi "armeabi-v7a"
$ANDROID_HOME/emulator/emulator -avd travis -no-audio -no-window &
adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'