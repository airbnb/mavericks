#! /bin/bash

if [ -e "$ANDROID_SDK" ]; then
    echo "Android SDK exists. Exiting"
    exit 0
fi

mkdir -p $HOME/.cache
curl https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip --output $HOME/.cache/sdk.zip
unzip $HOME/.cache/sdk.zip -d $ANDROID_HOME

sdkmanager "platform-tools" "platforms;android-27" "system-images;android-27;default;x86_64"