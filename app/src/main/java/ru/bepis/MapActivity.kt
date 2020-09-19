package ru.bepis

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.Property.NONE
import com.mapbox.mapboxsdk.style.layers.Property.VISIBLE
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.layers.TransitionOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_map.*
import ru.bepis.model.MoodType
import ru.bepis.model.Post.Companion.toJson
import ru.bepis.utils.Config.DEFAULT_MAP_CENTER
import ru.bepis.utils.Config.DEFAULT_ZOOM_LEVEL


class MapActivity : AppCompatActivity() {
    companion object {
        const val EMOJI_HAPPY = "emoji-happy"
        const val EMOJI_SAD = "emoji-sad"
        const val EMOJI_EXITED = "emoji-exited"
        const val EMOJI_SLEEPY = "emoji-sleepy"

        val CLUSTER_ONE = "cluster-0"
        val CLUSTER_TWP = "cluster-1"
        val CLUSTER_THREE = "cluster-2"

        val SOURCE_ID = "SOURCE_ID"
        var n = 0
    }

    private val EMOJIS_IMAGES = mapOf<String, Int>(
        EMOJI_HAPPY to R.drawable.ic_news_outline,
        EMOJI_SAD to R.drawable.ic_clips_outline,
        EMOJI_EXITED to R.drawable.ic_clips_outline,
        EMOJI_SLEEPY to R.drawable.ic_clips_outline
    )

    fun onFilterButtonClick(view: View) {
        when (view.id) {
            R.id.buttonFloatTop -> {
                toggleLayer("cluster-" + MoodType.HIGH_ENERGY)
            }
            R.id.buttonFloatRight -> toggleLayer("cluster-" + MoodType.POSITIVE)
            R.id.buttonFloatBottom -> toggleLayer("cluster-" + MoodType.LOW_ENERGY)
            R.id.buttonFloatLeft -> toggleLayer("cluster-" + MoodType.NEGATIVE)
            else -> {
            }
        }
        showDropDownLoayerAndHideButtons(view.id)
    }


    fun onViewMoodClicked(view: View) {
        val intent = Intent(this, MoodFeeds::class.java)
        startActivity(intent)
    }

    private fun toggleLayer(selectedLayerId: String) {
        mapboxMap.getStyle { style ->
            val clusters = arrayListOf<String>("cluster-" + MoodType.HIGH_ENERGY,
                                                                "cluster-" + MoodType.POSITIVE,
                                                                "cluster-" + MoodType.LOW_ENERGY,
                                                                "cluster-" + MoodType.NEGATIVE)
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

        emotionSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                when (id) {
                    0L -> toggleLayer("cluster-" + MoodType.HIGH_ENERGY)
                    1L -> toggleLayer("cluster-" + MoodType.POSITIVE)
                    2L -> toggleLayer("cluster-" + MoodType.LOW_ENERGY)
                    3L -> toggleLayer("cluster-" + MoodType.NEGATIVE)
                    else -> {
                    }
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        })

    }

    private lateinit var mapboxMap: MapboxMap

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

                addClusteredGeoJsonSource(style)

                EMOJIS_IMAGES.entries.forEach { (icon, iconId) ->
                    style.addImage(
                        icon,
                        BitmapUtils.getBitmapFromDrawable(resources.getDrawable(iconId))!!,
                        true
                    )
                }
            }
        }
    }

    private fun addClusteredGeoJsonSource(loadedMapStyle: Style) {
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

        /*val symbolLayerIconFeatureList = arrayListOf<Feature>(
            Feature.fromGeometry(Point.fromLngLat(30.3609, 59.9311), itType),
            Feature.fromGeometry(Point.fromLngLat(30.3609, 59.9348), itType),
            Feature.fromGeometry(Point.fromLngLat(30.3649, 59.9311), winterType),
            Feature.fromGeometry(Point.fromLngLat(30.3649, 59.9321), winterType)
        )*/
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
        unclustered.setProperties(iconImage(get("icon"),), iconSize(4f))
//        unclustered.setFilter(has("id"));
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

            val circles = CircleLayer("cluster-$moodType", "earthquakes")
            val pointCount = toNumber(get("point_count"))

            val iconImage = when(moodType) {
                MoodType.HIGH_ENERGY -> EMOJI_EXITED
                MoodType.LOW_ENERGY -> EMOJI_SLEEPY
                MoodType.NEGATIVE -> EMOJI_SAD
                MoodType.POSITIVE -> EMOJI_HAPPY
            }

            circles.setProperties(
                circleColor(layers[i][1] as Int),
                iconImage(iconImage),
                circleColor(R.color.white),
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
    }

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
