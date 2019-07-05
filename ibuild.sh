#!/bin/sh

gradle_compile_release()
{
    echo "build release start!"
    ./gradlew :app:clean
    ./gradlew :app:assembleRelease
    echo "build release end!"
    rm -rf out/
    mkdir -p out/apk
    cp app/build/outputs/apk/release/app-release.apk out/apk/TestPermDemo-release.apk
    adb install -r out/apk/TestPermDemo-release.apk
    adb shell am start -n com.ljz.testperm.demo/com.ljz.testperm.demo.MainActivity
}

gradle_compile_debug()
{
    echo "build debug start!"
    ./gradlew :app:clean
    ./gradlew :app:assembleDebug
    echo "build debug end!"
    rm -rf out/
    mkdir -p out/apk
    cp app/build/outputs/apk/debug/app-debug.apk out/apk/TestPermDemo-debug.apk
    adb install -r out/apk/TestPermDemo-debug.apk
    adb shell am start -n com.ljz.testperm.demo/com.ljz.testperm.demo.MainActivity
}

Main()
{
    if [ "$1" == "0" ]; then
       gradle_compile_debug
    elif [ "$1" == "1" ]; then
       gradle_compile_release
    fi

    return $?
}

Main "$@"
