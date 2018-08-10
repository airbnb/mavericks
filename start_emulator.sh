# Exit if any command fails
set -e
set -o pipefail

yes | /usr/local/android-sdk/tools/bin/sdkmanager "system-images;android-24;default;armeabi-v7a"
echo no | avdmanager create avd -n travis -k "system-images;android-24;default;armeabi-v7a" --device "Nexus 5" --force --abi "armeabi-v7a"
/usr/local/android-sdk/emulator/emulator -avd travis -no-audio -no-window &
adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'