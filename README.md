## ActivityResultEventBus V2

Tiny simple EventBus simulating activity result-like behaviour!

```
allprojects {
    repositories {
        maven { url 'https://andob.io/repository/open_source' }
    }
}
```

```
dependencies {
    implementation 'ro.andob.activityresult:eventbus:2.0.7'
}
```

### Example

You have two activities, ``MainActivity`` and ``CatListActivity`` containing a list of cats. ``MainActivity`` must open ``CatListActivity`` and receive the cat selected by the user from the list:

- Define the event class:

```kotlin
class OnCatChoosedEvent
(
    val cat : Cat
)
```

- Send the event in the ``CatListActivity`` context:

```kotlin
chooseCatButton.setOnClickListener {
    ActivityResultEventBus.post(OnCatChoosedEvent(cat))
    finish()
}
```

- Receive events back in the ``MainActivity`` context:

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

- You can also receive events anywhere else you want, fragments, views or other objects:

```kotlin
import ro.andreidobrescu.activityresulteventbus.onActivityResult

context.startActivity(Intent(context, CatListActivity::class.java))
onActivityResult<OnCatChoosedEvent>(context) { event ->
    catLabel.text = event.cat.name
}
```

### Java-friendly API

All APIs available in this library are Java-friendly. Example usage in Java:

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
onActivityResult(OnCatChoosedEvent.class, event -> catLabel.setText(event.cat.name));
```

- Receiving events in a fragment / view / other object:

```java
import static ro.andreidobrescu.activityresulteventbus.onActivityResult;

getContext().startActivity(new Intent(getContext(), CatListActivity.class));
onActivityResult(getContext(), OnCatChoosedEvent.class, event -> catLabel.setText(event.cat.name));
```

### Permission asker

You can also use this library to ask runtime permissions:

```kotlin
if (!PermissionAsker.arePermissionsAccepted(context = this, arrayOf(Manifest.permission.CAMERA)))
{
    PermissionAsker.ask(context = this, arrayOf(Manifest.permission.CAMERA))
        .onGranted { doSomething() }
        .onDenied { doSomething() }
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

