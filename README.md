## ActivityResultEventBus

Tiny simple EventBus to handle activity result-like behaviors

```
allprojects {
    repositories {
        maven { url 'http://maven.andob.info/reporitory/open_source' }
    }
}
```
```
dependencies {
    implementation 'ro.andob.activityresult:eventbus:1.1.2'
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
startActivity(Intent(this, CatListActivity::class.java))
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
ActivityResultEventBus.INSTANCE.post(new OnCatChoosedEvent(cat));
```

```java
class BaseActivity2 extends AppCompatActivity
{
    @Override
    protected void onPostResume()
    {
        super.onPostResume();
        ActivityResultEventBus.INSTANCE.onActivityPostResumed(this);
    }

    @Override
    protected void onPause()
    {
        ActivityResultEventBus.INSTANCE.onActivityPaused(this);
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        ActivityResultEventBus.INSTANCE.onActivityDestroyed(this);
        super.onDestroy();
    }

    public <EVENT> void onActivityResult(Class<EVENT> eventType, JActivityResultEventListener<EVENT> eventListener)
    {
        ActivityResultEventBus.INSTANCE.registerActivityEventListener(this, eventType, eventListener);
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


### [Vanilla onActivityResult vs GreenRobot EventBus vs ActivityResultEventBus comparison](https://github.com/andob/ActivityResultEventBus/blob/master/COMPARISON.md)

### License

```java
Copyright 2019-2020 Andrei Dobrescu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.`
