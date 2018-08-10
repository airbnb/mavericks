echo no | ./avdmanager create avd -n travis -k "system-images;android-28;default;x86_64" --device "Nexus 5" --force
$ANDROID_HOMEtools/bin/emulator -avd travis -no-audio -no-window &
$ANDROID_HOMEplatform-tools/adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'