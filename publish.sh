set -o allexport
echo "Publishing..."
./gradlew :activityresulteventbus:publishToMavenLocal
