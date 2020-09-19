package ru.bepis

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.Property.NONE
import com.mapbox.mapboxsdk.style.layers.Property.VISIBLE
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility
import com.mapbox.mapboxsdk.style.layers.TransitionOptions
import kotlinx.android.synthetic.main.activity_map.*
import ru.bepis.model.MoodType
import ru.bepis.model.PostOnMap
import ru.bepis.model.SubjectType
import ru.bepis.utils.Config.DEFAULT_MAP_CENTER
import ru.bepis.utils.Config.DEFAULT_ZOOM_LEVEL


class MapActivity : AppCompatActivity() {

    private lateinit var mapboxMap: MapboxMap
    private lateinit var markViewManager: MarkerViewManager

    fun onFilterButtonClick(view: View) {
        /*when (view.id) {
            R.id.buttonFloatTop -> {
                toggleLayer("cluster-" + MoodType.HIGH_ENERGY)
            }
            R.id.buttonFloatRight -> toggleLayer("cluster-" + MoodType.POSITIVE)
            R.id.buttonFloatBottom -> toggleLayer("cluster-" + MoodType.LOW_ENERGY)
            R.id.buttonFloatLeft -> toggleLayer("cluster-" + MoodType.NEGATIVE)
            else -> {
            }
        }*/
        val moodType = when (view.id) {
            R.id.buttonFloatTop -> MoodType.HIGH_ENERGY
            R.id.buttonFloatRight -> MoodType.POSITIVE
            R.id.buttonFloatBottom ->  MoodType.LOW_ENERGY
            R.id.buttonFloatLeft ->  MoodType.NEGATIVE
            else -> throw IllegalStateException()
        }
        filterOn(moodType)

        showDropDownLoayerAndHideButtons(view.id)
    }

    private val viewByMoodType = mutableMapOf<MoodType, MutableList<View>>()

    fun filterOn(moodType: MoodType? = null) {
        viewByMoodType.values.forEach { view -> view.forEach { it.visibility = View.VISIBLE } }
        if(moodType == null) {
            return
        }

        viewByMoodType.entries
            .filter { (mood, _) -> mood != moodType }
            .forEach { (_, view) -> view.forEach { it.visibility = View.GONE } }
    }

    fun onViewMoodClicked(view: View) {
        val ll = view as LinearLayout
        val themeView = ll.getChildAt(1) as TextView

        Store.subjectType = SubjectType.fromTitle(themeView.text.toString())
        Store.mood = MoodType.fromSubjectType(Store.subjectType!!)

        val intent = Intent(this, MoodFeeds::class.java)
        startActivity(intent)
    }

    private fun toggleLayer(selectedLayerId: String) {
        mapboxMap.getStyle { style ->
            val clusters = arrayListOf<String>(
                "cluster-" + MoodType.HIGH_ENERGY,
                "cluster-" + MoodType.POSITIVE,
                "cluster-" + MoodType.LOW_ENERGY,
                "cluster-" + MoodType.NEGATIVE
            )
            for (layerId in clusters) {
                val layer: Layer? = style.getLayer(layerId)
                if (layer != null) {
                    if (layerId == selectedLayerId) {
                        layer.setProperties(visibility(VISIBLE))
                    } else {
                        layer.setProperties(visibility(NONE))
                    }
                }
            }
        }
    }

    private fun showDropDownLoayerAndHideButtons(selectedMood: Int) {
        emotionSpinnerLayout.visibility = View.VISIBLE

        buttonFloatBottomLayout.visibility = View.GONE
        buttonFloatTopLayout.visibility = View.GONE
        buttonFloatLeftLayout.visibility = View.GONE
        buttonFloatRightLayout.visibility = View.GONE

        when (selectedMood) {
            R.id.buttonFloatTop -> emotionSpinner.setSelection(0)
            R.id.buttonFloatRight -> emotionSpinner.setSelection(1)
            R.id.buttonFloatBottom -> emotionSpinner.setSelection(2)
            R.id.buttonFloatLeft -> emotionSpinner.setSelection(3)
            else -> {
            }
        }

        val moodType = when (emotionSpinner.selectedItemId) {
            0L -> MoodType.HIGH_ENERGY
            1L -> MoodType.POSITIVE
            2L -> MoodType.LOW_ENERGY
            3L -> MoodType.NEGATIVE
            else -> throw IllegalStateException()
        }
        filterOn(moodType)

        emotionSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                val moodType = when (id) {
                    0L -> MoodType.HIGH_ENERGY
                    1L -> MoodType.POSITIVE
                    2L -> MoodType.LOW_ENERGY
                    3L -> MoodType.NEGATIVE
                    else -> throw IllegalStateException()
                }
                filterOn(moodType)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        })

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            mapboxMap = map
            map.setStyle(Style.LIGHT) { style -> // Disable any type of fading transition when icons collide on the map. This enhances the visual
                // look of the data clustering together and breaking apart.
                style.transition = TransitionOptions(0, 0, false)


                /*CameraUpdateFactory.newLatLngZoom( LatLng(12.099, -79.045), 15.0)
                    .also { mapboxMap.animateCamera(it) }*/

                CameraUpdateFactory.newLatLngZoom(
                    LatLng(DEFAULT_MAP_CENTER.first, DEFAULT_MAP_CENTER.second),
                    DEFAULT_ZOOM_LEVEL
                ).also { mapboxMap.animateCamera(it) }

//                addClusteredGeoJsonSource(style)

                val markerViewManager = MarkerViewManager(mapView, mapboxMap)

                Store.posts.forEach { post -> markerViewManager.addMarker(createMarker(post, post.size)) }
                /*EMOJIS_IMAGES.entries.forEach { (icon, iconId) ->
                    style.addImage(
                        icon,
                        BitmapUtils.getBitmapFromDrawable(resources.getDrawable(iconId))!!,
                        true
                    )
                }*/
            }
        }
    }

    fun createMarker(post: PostOnMap, size: Int? = null): MarkerView {
        val layout =  when (size) {
            0 -> R.layout.icon_small
            1 -> R.layout.icon_small_medium
            2 -> R.layout.icon_medium
            3 -> R.layout.icon_large
            else -> throw IllegalArgumentException()
        }

        val view = LayoutInflater.from(this).inflate(layout, mapView, false)

        if(post.post.mood in viewByMoodType) viewByMoodType[post.post.mood]?.also { it.add(view) }
        else viewByMoodType[post.post.mood] = mutableListOf(view)

        val title = view.findViewById(R.id.title) as TextView?
        val emoji = view.findViewById(R.id.emoji) as TextView

        if (title != null) title.text = post.post.title
        emoji.text = post.post.emoji()

        view.setOnClickListener { openTopic(post) }
        return MarkerView(LatLng(post.coordinate.first, post.coordinate.second), view)
    }

    private fun openTopic(topic: PostOnMap) {
        val intent = Intent(this, MoodFeeds::class.java)
        Store.subjectType = topic.post.subjectType
        Store.mood = topic.post.mood
        startActivity(intent)
    }

    /*private fun addClusteredGeoJsonSource(loadedMapStyle: Style) {
        val itType = JsonObject().also { it.addProperty("id", "it") }
        val winterType = JsonObject().also { it.addProperty("id", "winter") }

        /*val itType = JsonObject().also {
            it.addProperty("winter", 0)
            it.addProperty("it", 1)
        }
        val winterType = JsonObject().also {
            it.addProperty("winter", 1)
            it.addProperty("it", 0)
        }*/

        val symbolLayerIconFeatureList = Store.posts.map {
            Feature.fromGeometry(Point.fromLngLat(it.coordinate.second, it.coordinate.first), it.post.toJson())
        }.toList()

        loadedMapStyle.addSource( // Point to GeoJSON data. This example visualizes all M1.0+ earthquakes from
            GeoJsonSource(
                "earthquakes",
                FeatureCollection.fromFeatures(symbolLayerIconFeatureList),
                GeoJsonOptions()
                    .withCluster(true)
                    .withClusterMaxZoom(25)
                    .withClusterRadius(50)
                /*.withClusterProperty(
                    "winter",
                    Expression("+"),
                    Expression(
                        "case",
                        eq(get("id"), "winter"),
                        toNumber(literal(1)),
                        toNumber(literal(0))
                    )
                )
                .withClusterProperty(
                    "it",
                    Expression("+"),
                    Expression(
                        "case",
                        eq(get("id"), "it"),
                        toNumber(literal(1)),
                        toNumber(literal(0))
                    )
                )*/
            )
        )

        //Creating a marker layer for single data points
        val unclustered = SymbolLayer("unclustered-points", "earthquakes")
        unclustered.setProperties(iconImage(get("icon")), iconSize(4f))
        unclustered.setFilter(has("id"));
        loadedMapStyle.addLayer(unclustered)

        // Use the earthquakes GeoJSON source to create three layers: One layer for each cluster category.
        // Each point range gets a different fill color.
        val layers = arrayOf(
            arrayOf(MoodType.HIGH_ENERGY, ContextCompat.getColor(this, R.color.mapbox_white)),
            arrayOf(MoodType.LOW_ENERGY, ContextCompat.getColor(this, R.color.blueTextColor)),
            arrayOf(MoodType.POSITIVE, ContextCompat.getColor(this, R.color.mapboxGreen)),
            arrayOf(MoodType.NEGATIVE, ContextCompat.getColor(this, R.color.mapboxRed))
        )
        for (i in layers.indices) {
            val moodType = layers[i][0] as MoodType
            /*val icon = when (moodType) {
                MoodType.HIGH_ENERGY -> EMOJI_EXITED
                MoodType.LOW_ENERGY -> EMOJI_SLEEPY
                MoodType.NEGATIVE -> EMOJI_SAD
                MoodType.POSITIVE -> EMOJI_HAPPY
            }*/

            val circles = CircleLayer("cluster-$moodType", "earthquakes")
            val pointCount = toNumber(get("point_count"))

            circles.setProperties(
                circleColor(layers[i][1] as Int),
                iconImage("cross-icon-id"),
                iconSize(4f),
                circleRadius(
                    interpolate(
                        exponential(1),
                        pointCount,
                        stop(10, 10f),
                        stop(20, 20f),
                        stop(40, 30f),
                        stop(80, 40f),
                        stop(160, 50f),
                        stop(320, 60f)
                    )
                )
            )

            circles.setFilter(all(has("mood"), eq(get("mood"), literal(moodType.name))))
            loadedMapStyle.addLayer(circles)
        }

        //Add the count labels
        val count = SymbolLayer("count", "earthquakes")
        count.setProperties(
            textField(get("winter")),
            textSize(12f),
            textColor(Color.BLACK),
            textIgnorePlacement(true),
            textAllowOverlap(true)
        )
        loadedMapStyle.addLayer(count)
    }*/

    public override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    public override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
