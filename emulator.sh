#! /usr/bin/sh

cmd=$1
device="-s emulator-5554"
dev_name="newpc"
#device="-s 20080411"
name="nav"
pkg="com.android.nav"
activity="loginActivity"
path="nav"
pkg_file="./build/outputs/apk/"$path"-debug.apk"
src_path="./src"
debug_port=8700

# android list targets
platform="android-25"

if [ $cmd = "create" ]; then
	android create project \
		--target $platform \
		--name $name \
		--path $path \
		--activity $activity \
		--package $pkg \
		--gradle \
		--gradle-version 2.1.3 #plugin version

	#change version accordingly

	#Plugin version	Required Gradle version
	#1.0.0 - 1.1.3	2.2.1 - 2.3
	#1.2.0 - 1.3.1	2.2.1 - 2.9
	#1.5.0		2.2.1 - 2.13
	#2.0.0 - 2.1.2	2.10 - 2.13
	#2.1.3+		2.14.1+

	sed -i 's/runProguard false/minifyEnabled true/' \
		$path/build.gradle
	sed -i 's/gradle-1.12-all.zip/gradle-2.14.1-all.zip/' \
		$path/gradle/wrapper/gradle-wrapper.properties
	cp script.sh $path/emulator.sh

	cat > $path/src/main/res/values/styles.xml << "EOF"
	<resources>
	<style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
	</style>
	</resources>
EOF
	cat >> $path/build.gradle << "EOF"
	dependencies {
        compile 'com.android.support:appcompat-v7:25.2.0'
}
EOF

elif [ $cmd = 'start' ]; then 
	emulator -use-system-libs -avd $dev_name -no-boot-anim -scale 1.50 -show-kernel
elif [ $cmd = "build" ]; then 
	sh gradlew build
elif [ $cmd = "clean" ]; then 
	sh gradlew clean
elif [ $cmd = "install" ]; then 
	adb install $pkg_file
	#ant -Dadb.device.arg="$device" debug install 
elif [ $cmd = "reinstall" ]; then 
	adb install -r $pkg_file
elif [ $cmd = "run" ]; then 
	string="am start -n $pkg/.$activity"
	adb shell $string
elif [ $cmd = "stop" ]; then
        #adb shell am clear-debug-app                                          
        string="am force-stop $pkg"                                            
        adb shell $string                                                      
elif [ $cmd = "debug" ]; then                                                  
        #adb shell am set-debug-app -w $pkg                                    
        string="am start -D -n $pkg/.$activity"                                
        adb shell $string                                                      
        sleep 1
        #APP_PORT=$(adb jdwp | tail -1)
	APP_PORT=$(adb shell ps | grep $pkg | cut -d ' ' -f 4)
        adb forward tcp:$debug_port jdwp:$APP_PORT                             
        jdb -attach localhost:$debug_port -sourcepath $src_path                
elif [ $cmd = "uninstall" ]; then 
	adb uninstall $pkg
	#ant -Dadb.device.arg="$device" uninstall
elif [ $cmd = "logcat" ]; then 
	adb logcat 
elif [ $cmd = "tar" ]; then
        tar -C $(dirname $(pwd)) --exclude=$path/*.tar --exclude=$path/*.sh \
                --exclude=$path/build/* --exclude=$path/backend/lib/* \
		--exclude=$path/backend/env/* -cvf $path.tar $path
        zip $path.tar.zip $path.tar
elif [ $cmd = "video" ]; then
	ffmpeg -f x11grab -s 640x1060 -r 25 -i $DISPLAY -f alsa -i default \
	-c:v libx264 -b:v 200k -s 640x1060 test.mp4
elif [ $cmd = "videocut" ]; then
	ffmpeg -i test.mp4 -ss 00:00:01 -t 00:03:55 -an -async 1 $path.mp4
fi

#ant -Dadb.device.arg="-s emulator-5554" debug
#ant -Dadb.device.arg=-s emulator-5554 debug install 
#adb -s emulator-5554 shell 'am start -n com.android.testproject/.test_activity' 
#adb shell 'am start -n com.android.testproject/.test_activity'
