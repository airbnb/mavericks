#! /bin/bash

if [ -e "$ANDROID_SDK" ]; then
    echo "Android SDK exists. Exiting"
    exit 0
fi

mkdir -p $HOME/.cache
curl https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip --output $HOME/.cache/sdk.zip
unzip -q $HOME/.cache/sdk.zip -d $ANDROID_HOME

touch $HOME/.android/repositories.cfg
sdkmanager --update
yes | sdkmanager "platform-tools" "platforms;android-27" "system-images;android-28;default;x86_64"