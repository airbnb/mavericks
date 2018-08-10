#! /bin/bash

if [ -e "$ANDROID_SDK" ]; then
    echo "Android SDK exists. Exiting"
    exit 0
fi

# Exit if any command fails
set -e
set -o pipefail


mkdir -p $HOME/.cache
curl https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip --output $HOME/.cache/sdk.zip
unzip -q $HOME/.cache/sdk.zip -d $ANDROID_HOME

touch $HOME/.android/repositories.cfg
sdkmanager --update
# The progress bar fills up the travis log
sdkmanager "platform-tools"
yes | sdkmanager "platforms;android-27"
sdkmanager "emulator"
sdkmanager "system-images;android-24;default;armeabi-v7a"
sdkmanager --list