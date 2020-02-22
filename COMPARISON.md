## Vanilla onActivityResult vs GreenRobot EventBus vs ActivityResultEventBus comparison

In this page I will try to explain why you should use ActivityResultEventBus instead of onActivityResult or GreenRobot's EventBus for passing results from one activity to another.

### The vanilla way - onActivityResult

I believe the vanilla way is broken because it uses Intents and Bundles in order to store the data. Sure, Intents are great when you want to communicate with another app, start an activity from another app and then get the result from that activity (for instance, taking an image with the camera, choosing a file, choosing a photo from gallery and so on).

BUT most of the times, your app is NOT starting an activity from the outside of the app, but rather an activity from the app. You are already using some model classes all around the code. These classes are static typed, compile-time checked. Intents, on the other hand, are weakly typed. They're more like a HashMap<String, Object>. You can write anything in an intent and read anything from an intent, and nothing is compile time checked. Ugh.

```kotlin
class CatChooserActivity : AppCompatActivity
{
    fun showCat(cat : Cat)
    {
        catButton.setOnClickListener {
            val resultIntent=Intent()
            resultIntent.putExtra("cat", cat)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
```

```kotlin
class MainActivity : BaseActivity()
{
    companion object
    {
        val REQUEST_CHOOSE_CAT = 1234
    }
    
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        catLabel.setOnClickListener {
            val intent = Intent(this, CatChooserActivity::class.java)
            startActivityForResult(intent, REQUEST_CHOOSE_CAT)
        }
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CHOOSE_CAT && resultCode == Activity.RESULT_OK&&
            data != null && data.hasExtra("cat"))
        {
            val cat = data.getSerializableExtra("cat") as Cat
            println(cat)
        }
    }
}
```

Sure, it looks okish now because the example is trivial. As the project grow, it will get harder and harder to mantain this serialization / deserialization code, request codes, result codes. Furthermore, the compiler doesn't help you at all!!

The code also looks very verbose. Verbosity and boilerplate is evil on the long run, on big projects. We should write code as simple as possible. Because the project can always get a lot of features and can get very complicated, why shall we do trivial tasks using complicated approaches?

The vanilla way is also not suitable for navigation flow logic. But this also applies to GreenRobot EventBus. We'll discuss this in the next section.

### GreenRobot EventBus

The GR EventBus is great because with it, we can get rid of Intents. Thus, our code will become compile-time checked :)

```kotlin
class OnCatChoosedEvent
(
    val cat : Cat
)
```

```kotlin
class CatChooserActivity : BaseActivity()
{
    fun showCat(cat : Cat)
    {
        catButton.setOnClickListener {
            EventBus.getDefault().post(OnCatChoosedEvent(cat))
            finish()
        }
    }
}
```

```kotlin
class MainActivity : BaseActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        catLabel.setOnClickListener {
            startActivity(Intent(this, CatChooserActivity::class.java))
        }
    }

    @Subscribe
    fun onCatChoosed(event : OnCatChoosedEvent)
    {
        println(event.cat)
    }
}
```

This looks so nice! But still, we can identify two problems with this approach.

The ``onCatChoosed(event)`` method from ``MainActivity`` is called before activity's ``onResume``. This is ok, but it can lead to some subtle bugs, which can be easily avoided. But I believe it's not a big deal, IMHO we should put code cleanness before everything else, including bugs.

The second problem is that forcing to write a class-level method to handle the event is not quite suitable for complex navigation flow logic. This problem also applies to the vanilla onActivityResult. Let's look at the following example:

```kotlin
class OnQRCodeScannedEvent
(
    val url : String = ""
)
```

```kotlin
class QRCodeScannerActivity : BaseActivity()
{
    fun onScanned(url : String)
    {
        EventBus.getDefault().post(OnQRCodeScannedEvent(url))
        finish()
    }
}
```

```kotlin
@BundleBuilder
class RestaurantDetailsActivity : BaseActivity()
{
    @Arg @JvmField
    var restaurantId : Int = 0

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        RestaurantDetailsActivityBundleBuilder.inject(intent.extras, this)
    }
}
```

Note: here I am using the [BundleArgs](https://github.com/MFlisar/BundleArgs) library. This library generates intent / bundle serialization / deserialization code, thus replacing the weakly typed intent / bundle key-value stores with nice compile-time checked Builders. I highly recommend it to pass input arguments to activities / fragments, instead of using the vanilla way.

```kotlin
class MainActivity : BaseActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanRestaurantQRCodeButton.setOnClickListener {
            Intent(this, QRCodeScannerActivity::class.java)
        }
    }

    @Subscribe
    fun onQRCodeScanned(event : OnQRCodeScannedEvent)
    {
        if (event.url.startsWith("http://example.com/restaurant/"))
        {
            event.url.split("/").last().toIntOrNull()?.let { restaurantId ->
                RestaurantDetailsActivityBundleBuilder()
                    .restaurantId(restaurantId)
                    .startActivity(this)
            }
        }
    }
}
```

This code looks completely ok for now. But months pass by, and the client wants to add a restaurant reviewing system. The user should now have two buttons on this screen:

1. a scan QR code / restaurant details button. When the user scans the QR code, the restaurant details screen is opened.
2. a scan QR code / review button. When the user clicks this button, a dialog asking for rating will pop up. The user would choose a rating between 1 star, 2 stars, ... 5 stars, then will scan the QR code, and then, the restaurant review will be submitted

```kotlin
object ReviewDialog
{
    class ReviewOption
    (
        val description : String,
        val rating : Int
    )
    {
        override fun toString() = description
    }
    
    fun show(title : String,
             reviewOptions : List<ReviewOption>,
             onReviewOptionSelected : (ReviewOption) -> (Unit))
    {
        //todo show dialog with title and a list of items.map { it.toString }
        //todo when user clicks an item from the list, call onItemSelected
        onReviewOptionSelected(reviewOptions.first())
    }
}
```

```kotlin
class MainActivity : BaseActivity()
{
    var shouldOpenRestaurantDetailsOnQRCodeScanned = false
    var shouldAssignRatingOnQRCodeScanned = false
    var ratingToAssignOnQrCodeScanned : Int? = null
    
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanRestaurantQRCodeButton.setOnClickListener {
            Intent(this, QRCodeScannerActivity::class.java)
            shouldOpenRestaurantDetailsOnQRCodeScanned = true
        }

        addReviewButton.setOnClickListener { 
            ReviewDialog.show(
                title = "Please select a rating",
                reviewOptions = (1..5).toList().map { rating ->
                    ReviewDialog.ReviewOption(description = "$rating stars", rating = rating)
                },
                onReviewOptionSelected = { reviewOption ->
                    Intent(this, QRCodeScannerActivity::class.java)
                    shouldAssignRatingOnQRCodeScanned = true
                    ratingToAssignOnQrCodeScanned = reviewOption.rating
                })
        }
    }

    @Subscribe
    fun onQRCodeScanned(event : OnQRCodeScannedEvent)
    {
        if (event.url.startsWith("http://example.com/restaurant/"))
        {
            event.url.split("/").last().toIntOrNull()?.let { restaurantId ->
                if (shouldOpenRestaurantDetailsOnQRCodeScanned)
                {
                    RestaurantDetailsActivityBundleBuilder()
                        .restaurantId(restaurantId)
                        .startActivity(this)
                }
                else if (shouldAssignRatingOnQRCodeScanned && ratingToAssignOnQrCodeScanned!=null)
                {
                    //todo presenter.assignRating(restaurantId = restaurantId, 
                    //rating = ratingToAssignOnQrCodeScanned)
                }
            }
        }
        
        shouldOpenRestaurantDetailsOnQRCodeScanned = false
        shouldAssignRatingOnQRCodeScanned = false
        ratingToAssignOnQrCodeScanned = null
    }
}
```

This is not quite ok because it violates some basic functional programming principles. We keep these mutable states, ``shouldOpenRestaurantDetailsOnQRCodeScanned`` and ``shouldAssignRatingOnQRCodeScanned``. Maybe it's ok for now, but these states can cause bugs in the future. We have to keep these states because the event is handled by another class method.

A correct and also cleaner approach would be to use lambdas:

```kotlin
class ID<T>(val value : Int)
```

```kotlin
class MainActivity : BaseActivity()
{
    var onRestaurantQrCodeScanned : ((ID<Restaurant>) -> (Unit))? = null
    
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanRestaurantQRCodeButton.setOnClickListener {
            Intent(this, QRCodeScannerActivity::class.java)
            onRestaurantQrCodeScanned = { restaurantId ->
                RestaurantDetailsActivityBundleBuilder()
                    .restaurantId(restaurantId.value)
                    .startActivity(this)
            }
        }

        addReviewButton.setOnClickListener { 
            ReviewDialog.show(
                title = "Please select a rating",
                reviewOptions = (1..5).toList().map { rating ->
                    ReviewDialog.ReviewOption(description = "$rating stars", rating = rating)
                },
                onReviewOptionSelected = { reviewOption ->
                    Intent(this, QRCodeScannerActivity::class.java)
                    onRestaurantQrCodeScanned={ restaurantId ->
                        //todo presenter.assignRating(restaurantId = restaurantId, 
                        //rating = ratingToAssignOnQrCodeScanned)
                    }
                })
        }
    }

    @Subscribe
    fun onQRCodeScanned(event : OnQRCodeScannedEvent)
    {
        if (event.url.startsWith("http://example.com/restaurant/"))
        {
            event.url.split("/").last().toIntOrNull()?.let { restaurantId ->
                onRestaurantQrCodeScanned?.invoke(ID<Restaurant>(restaurantId))
            }
        }
        
        onRestaurantQrCodeScanned = null
    }

    override fun onDestroy()
    {
        onRestaurantQrCodeScanned = null
        super.onDestroy()
    }
}
```

Now the event is no longer handled inside another class method (``onQRCodeScanned``), but rather handled inline with the actual navigation flow code. We won't ever need a mutable state, saved on the wrong scope, because lambdas are also closures.

### ActivityResultEventBus

ActivityResultEventBus solves all these problems:

1. Events are compile-time checked POJOs, not Intents or Bundles
2. Events are received after onResume
3. Events are received via lambda expressions. Not using a class method avoids side effects and spaghetti code.
4. If activity A starts activity B, events sent by B can be only received by activity A, after activity B is destoryed and after activity A resumes.

The last example using ActivityResultEventBus:

```kotlin
class QRCodeScannerActivity : BaseActivity()
{
    fun onScanned(url : String)
    {
        ActivityResultEventBus.post(OnQRCodeScannedEvent(url))
        finish()
    }
}
```

```kotlin
class ID<T>(val value : Int)
```

```kotlin
object QRCodeRestaurantUrlParser
{
    fun parse(url : String) : ID<Restaurant>?
    {
        if (url.startsWith("http://example.com/restaurant/"))
        {
            val restaurantIdValue = url.split("/").last().toIntOrNull()
            if (restaurantIdValue != null)
                return ID<Restaurant>(restaurantIdValue)
        }
        
        return null
    }
}
```

```kotlin
class MainActivity : BaseActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanRestaurantQRCodeButton.setOnClickListener { 
            onScanRestaurantQRCodeButtonClicked()
        }

        addReviewButton.setOnClickListener { 
            onAddReviewButtonClicked()
        }
    }
    
    private fun onScanRestaurantQRCodeButtonClicked()
    {
        Intent(this, QRCodeScannerActivity::class.java)
        OnActivityResult<OnQRCodeScannedEvent> { event ->
            QRCodeRestaurantUrlParser.parse(event.url)?.let { restaurantId ->
                RestaurantDetailsActivityBundleBuilder()
                    .restaurantId(restaurantId.value)
                    .startActivity(this)
            }
        }
    }
    
    private fun onAddReviewButtonClicked()
    {
        ReviewDialog.show(
            title = "Please select a rating",
            reviewOptions = (1..5).toList().map { rating ->
                ReviewDialog.ReviewOption(description = "$rating stars", rating = rating)
            },
            onReviewOptionSelected = { reviewOption ->
                Intent(this, QRCodeScannerActivity::class.java)
                OnActivityResult<OnQRCodeScannedEvent> { event ->
                    QRCodeRestaurantUrlParser.parse(event.url)?.let { restaurantId ->
                        //todo presenter.assignRating(restaurantId = restaurantId, 
                        //rating = ratingToAssignOnQrCodeScanned)
                    }
                }
            })
    }
}
```

ActivityResultEventBus disadvantages:

1. ActivityResultEventBus should be used only as an onActivityResult replacement. On any other use cases (for instance, sending a ``UpdateBackgroundColorEvent`` to all background activities), please use a general-purpose event bus, such as GreenRobot EventBus.
2. Composing code blocks and lambdas can lead to "callback hell". Still, this is completely manageable by organising methods: not having a gigantic method, but splitting it into smaller methods. For instance, in the last example, the code is splitted into three methods: ``onCreate``, ``onScanRestaurantQRCodeButtonClicked`` and ``onAddReviewButtonClicked``, instead of keeping all the code inside ``onCreate``
