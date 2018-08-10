echo no | ./avdmanager create avd -n travis -k "system-images;android-27;default;x86_64" --device "Nexus 5" --force
$ANDROID_HOME/tools/bin/emulator -avd travis -no-audio -no-window &
$ANDROID_HOME/platform-tools/adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'