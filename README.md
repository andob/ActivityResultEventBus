## ActivityResultEventBus V2

Tiny simple EventBus with activity result-like behaviour

```
allprojects {
    repositories {
        maven { url 'https://maven.andob.info/repository/open_source' }
    }
}
```

```
dependencies {
    implementation 'ro.andob.activityresult:eventbus:2.0.4'
}
```

### Example

You have two activities, ``MainActivity`` and ``CatListActivity``. ``MainActivity`` must open ``CatListActivity`` and receive the cat choosed by the user from the list:

- Define the event class:

```kotlin
class OnCatChoosedEvent
(
    val cat : Cat
)
```

- Send the event in the ``CatListActivity`` context:

```kotlin
catButton.setOnClickListener {
    ActivityResultEventBus.post(OnCatChoosedEvent(cat))
    finish()
}
```

- Receive events in the ``MainActivity`` context:

```kotlin
startActivity(Intent(context, CatListActivity::class.java))
onActivityResult<OnCatChoosedEvent> { event ->
    catLabel.text = event.cat.name
}
```

All events will be received on UI thread.

- Let all your activities extend ``AppCompatActivityWithActivityResultEventBus``:

```kotlin
abstract class BaseActivity : AppCompatActivityWithActivityResultEventBus()
```

- You can also receive events anywhere else you want, fragments, views, other objects:

```kotlin
import ro.andreidobrescu.activityresulteventbus.onActivityResult

context.startActivity(Intent(context, CatListActivity::class.java))
onActivityResult<OnCatChoosedEvent>(context) { event ->
    catLabel.text = event.cat.name
}
```

### Versioning

Version 2 of this library is based upon AndroidX ActivityResult API. It is recommended to migrate from version 1 to 2.

Version 1 had a custom mechanism, based on lifecycle callbacks. You can find the legacy documentation of V1 [here](https://github.com/andob/ActivityResultEventBus/blob/master/README_OLD.md).

### Java compatibility

All APIs available in this library are fully compatible with both Java and Kotlin. Example usage in Java:

- Posting events:

```java
catButton.setOnClickListener(v ->
{
    ActivityResultEventBus.post(new OnCatChoosedEvent(cat));
    finish();
});
```

- Receiving events in an activity:

```java
startActivity(new Intent(getContext(), CatListActivity.class));
onActivityResult(OnCatChoosedEvent.class, event -> catLabel.text = event.cat.name);
```

- Receiving events in a fragment / view / other object:

```java
import static ro.andreidobrescu.activityresulteventbus.onActivityResult;

getContext().startActivity(new Intent(getContext(), CatListActivity.class));
onActivityResult(getContext(), OnCatChoosedEvent.class, event -> catLabel.text = event.cat.name);
```

### [Vanilla onActivityResult vs GreenRobot EventBus vs AndroidX ActivityResult vs ActivityResultEventBus comparison](https://github.com/andob/ActivityResultEventBus/blob/master/COMPARISON.md)

### Yes, you can call onActivityResult<EVENT> { event -> } anywhere you want!

Unlike AndroidX's Activity Result library, you can register to receive events even after onCreate. This is highly useful in defining complex navigation flow logic, as describe in the above comparison.

With AndroidX ActivityResult, you would be forced to register the listener in onCreate. Otherwise you would get a ``IllegalStateException: LifecycleOwner is attempting to register while current state is RESUMED. LifecycleOwners must call register before they are STARTED.`` error.

### Permission asker

You can also use this library to ask runtime permissions. While ``arePermissionsAccepted`` method will return a boolean, ``ask`` method will return a pseudo-future, to which you can optionally bind ``onGranted`` and / or ``onDenied`` callbacks.

```kotlin
if (!PermissionAsker.arePermissionsAccepted(context = this, arrayOf(Manifest.permission.CAMERA)))
{
    PermissionAsker.ask(context = this, arrayOf(Manifest.permission.CAMERA))
        .onGranted { doSomething() }
        .onDenied { doSomething() }
}
```

### Vanilla requestCode / resultCode / data compatibility layer

Usually you will start activities from within your project / process. However, there are times when you must start and get result from activities from outside of your app. There is a compatibility layer for this. For instance:

- Define your events

```kotlin
class OnPictureNotChoosedFromGalleryEvent
class OnPictureChoosedFromGalleryEvent(val filePath : String)
```

- Use the compatibility layer, start the intent with it and map possible resultCodes and result data intents:

```kotlin
object ExternalActivityRouter
{
    fun startChoosePictureFromGalleryActivity(context : Context)
    {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        
        ActivityResultEventBus.createCompatibilityLayer()
            .addResultMapper(Activity.RESULT_CANCEL) { resultIntent ->
                OnPictureNotChoosedFromGalleryEvent()
            }
            .addResultMapper(Activity.RESULT_OK) { resultIntent ->
                resultIntent?.data?.toString()?.replace("file://", "")?.let { imageFilePath ->
                    OnPictureChoosedFromGalleryEvent(imageFilePath)
                }
            }
            .startActivity(context, intent)
    }
}
```

```kotlin
class PictureChooserView : CustomView
{
    private fun onChoosePictureButtonClicked()
    {
        ExternalActivityRouter.startChoosePictureFromGalleryActivity(context)
        onActivityResult<OnPictureNotChoosedFromGalleryEvent>(context) { }
        onActivityResult<OnPictureChoosedFromGalleryEvent>(context) { filePath -> addImage(filePath) }
    }
}
```

### License

```
Copyright 2019-2021 Andrei Dobrescu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.`

```

