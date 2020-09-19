package ru.bepis

import android.graphics.Color
import android.os.Bundle
import android.view.View
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

    private val EMOJIS_IMAGES = listOf(
        EMOJI_HAPPY, R.drawable.ic_news_outline,
        EMOJI_SAD
        , R.drawable.ic_news_outline
    )

    fun onFilterButtonClick(view: View) {
        when(view.id) {
            R.id.buttonFloatBottom -> toggleLayer(CLUSTER_ONE)
            else -> {

            }
        }
    }

    private fun toggleLayer(layerId: String) {
        mapboxMap.getStyle { style ->
            val layer: Layer? = style.getLayer(layerId)
            if (layer != null) {
                if (VISIBLE == layer.visibility.getValue()) {
                    layer.setProperties(visibility(NONE))
                } else {
                    layer.setProperties(visibility(VISIBLE))
                }
            }
        }
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
                listOf(
                    ""
                )
                style.addImage(
                    "cross-icon-id",
                    BitmapUtils.getBitmapFromDrawable(resources.getDrawable(R.drawable.ic_close_blue))!!,
                    true
                )
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
        unclustered.setProperties(iconImage("cross-icon-id"), iconSize(4f))
//        unclustered.setFilter(has("id"));
        loadedMapStyle.addLayer(unclustered)

        // Use the earthquakes GeoJSON source to create three layers: One layer for each cluster category.
        // Each point range gets a different fill color.
        val layers = arrayOf(
            intArrayOf(20, ContextCompat.getColor(this, R.color.mapboxGreen)),
            intArrayOf(0, ContextCompat.getColor(this, R.color.mapbox_white))
        )
        for (i in layers.indices) {
            //Add clusters' circles
            val circles = CircleLayer("cluster-$i", "earthquakes")
            val pointCount = toNumber(get("point_count"))

            circles.setProperties(
                circleColor(layers[i][1]),
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
            // Add a filter to the cluster layer that hides the circles based on "point_count"
            /*circles.setFilter(
                if (i == 0) all(
                    has("point_count"),
                    gte(toNumber(get("it")), toNumber(get("winter")))
                ) else all(
                    has("point_count"),
                    gte(toNumber(get("winter")), 2)
                )
            )*/
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
