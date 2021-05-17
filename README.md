## ActivityResultEventBus

Tiny simple EventBus to handle activity result-like behaviors

```
allprojects {
    repositories {
        maven { url 'https://maven.andob.info/repository/open_source' }
    }
}
```
```
dependencies {
    implementation 'ro.andob.activityresult:eventbus:1.2.4'
}
```

### Example

You have two activities, ``MainActivity`` and ``CatListActivity``. ``MainActivity`` must open ``CatListActivity`` and receive the cat choosed by the user from the list:

- Define the event:

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

You can also post an event after a delay:

```kotlin
ActivityResultEventBus.post(OnCatChoosedEvent(cat), delay = 100) //100ms
```

- Receive events in the ``MainActivity`` context:

```kotlin
startActivity(Intent(context, CatListActivity::class.java))
OnActivityResult<OnCatChoosedEvent> { event ->
    catLabel.text=event.cat.name
}
```

``OnActivityResult`` is an extension function available for ``Activity``, ``Fragment`` and ``View`` classes.

All events will be received on UI thread.

- Register the EventBus in ``BaseActivity``:

```kotlin
abstract class BaseActivity : AppCompatActivity()
{
    override fun onPostResume()
    {
        super.onPostResume()
        ActivityResultEventBus.onActivityPostResumed(this)
    }
    
    override fun onPause()
    {
        super.onPause()
        ActivityResultEventBus.onActivityPaused(this)
    }

    override fun onDestroy()
    {
        ActivityResultEventBus.onActivityDestroyed(this)
        super.onDestroy()
    }
}
```

### Example usage in Java

```java
ActivityResultEventBus.post(new OnCatChoosedEvent(cat));
```

```java
class BaseActivity2 extends AppCompatActivity
{
    @Override
    protected void onPostResume()
    {
        super.onPostResume();
        ActivityResultEventBus.onActivityPostResumed(this);
    }

    @Override
    protected void onPause()
    {
        ActivityResultEventBus.onActivityPaused(this);
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        ActivityResultEventBus.onActivityDestroyed(this);
        super.onDestroy();
    }

    public <EVENT> void onActivityResult(Class<EVENT> eventType, JActivityResultEventListener<EVENT> eventListener)
    {
        ActivityResultEventBus.registerActivityEventListener(this, eventType, eventListener);
    }
}
```

```java
class MainActivity2 extends BaseActivity2
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        startActivity(new Intent(this, CatListActivity.class));
        onActivityResult(OnCatChoosedEvent.class, event -> System.out.println(event.getCat()));
    }
}
```


### [Vanilla onActivityResult vs GreenRobot EventBus vs AndroidX ActivityResult vs ActivityResultEventBus comparison](https://github.com/andob/ActivityResultEventBus/blob/master/COMPARISON.md)

### Yes, you can call OnActivityResult<EVENT> { event -> } anywhere you want!

Unlike AndroidX's Activity Result library, you can register to receive events even after onCreate. For instance,

```kotlin
binding.chooseSomethingButton.setOnClickListener={ v ->
    startActivity(new Intent(context, SomethingChooserActivity.class));
    OnActivityResult<OnSomethingChoosedEvent> { event -> }
}
```

With AndroidX ActivityResult, you would be forced to register the listener in onCreate, leading to more boilerplate code. Otherwise you would get a ``IllegalStateException: LifecycleOwner is attempting to register while current state is RESUMED. LifecycleOwners must call register before they are STARTED.`` error.

### Permission asker

From version 1.1.8 on, you can use this library to ask for permissions:

```kotlin
PermissionAskerActivity.ask(it.context, android.Manifest.permission.CAMERA)
OnActivityResult<OnPermissionsGrantedEvent> { event ->
    //take picture
}
```

### Vanilla Activity Result Compatibility layer

From version 1.2.1 on, you can use this library to open activities using the vanilla Activity Result mechanism. For instance, to pick an image from gallery or to open camera to take a picture. This is useful since overriding the ``onActivityResult()`` method is deprecated and the new AndroidX ActivityResult API is recommended (but I don't want to use it because of its limitations, read the comparison for more details).
 
Example usage:

```kotlin
class OnImageFileChoosedFromGalleryEvent
(
    val picturePath : String
)
```

```kotlin
object ExternalActivityRouter
{
    fun startChoosePictureFromGalleryActivity(context : Context)
    {
        VanillaActivityResultCompat.createCompatibilityLayer()
            .setIntentFactory factory@ { wrappedContext : Context ->
                val intent=Intent(/*wrappedContext, clazz*/)
                intent.type="image/*"
                intent.action=Intent.ACTION_GET_CONTENT
                return@factory intent
            }
            .addResultMapper(Activity.RESULT_OK) { resultIntent ->
                resultIntent?.data?.toString()?.let { imagePath ->
                    OnImageFileChoosedFromGalleryEvent(imagePath)
                }
            }
            .startActivity(context)
    }
}
```

```kotlin
choosePictureButton.setOnClickListener {
    PermissionAskerActivity.ask(it.context, android.Manifest.permission.CAMERA)
    OnActivityResult<OnPermissionsGrantedEvent> { grantedEvent ->
        ExternalActivityRouter.startChoosePictureFromGalleryActivity(it.context)
        OnActivityResult<OnImageFileChoosedFromGalleryEvent> { event ->
            Picasso.get().load(event.picturePath).into(imageView)
        }
    }
}
```

Compatibility layer method reference:
- ``fun setIntentFactory(factory : (Context) -> (Intent))`` - REQUIRED - you must pass a mapper that transforms a context into the intent that will be used to start the activity.
- ``fun addResultMapper(resultCode : Int, mapper : (Intent?) -> (EVENT?))`` - AT LEAST ONE REQUIRED - you must pass at least one mapper that transforms a nullable result intent into an event object or null for a specific resultCode use case.
- ``fun setOnIntentActivityStarted(listener : () -> (Unit))`` (optional) - event listener that will be called before starting the activity
- ``fun setOnIntentActivityStopped(listener : () -> (Unit))`` (optional) - event listener that will be called after the user returns from the activity

### License

```java
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
