package ru.bepis

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.layers.Property.NONE
import com.mapbox.mapboxsdk.style.layers.Property.VISIBLE
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_map.*
import java.net.URISyntaxException


class MapActivity : AppCompatActivity() {
    companion object {
        val CLUSTER_ONE = "cluster-0"
        val CLUSTER_TWP = "cluster-1"
        val CLUSTER_THREE = "cluster-2"

        val SOURCE_ID = "SOURCE_ID"
        var n = 0
    }

    fun onFilterButtonClick(view: View) {
        when(view.id) {
            R.id.buttonFloatBottom -> toggleLayer(CLUSTER_ONE)
            else -> {

            }
        }
    }

    private fun toggleLayer(layerId: String) {
        mapboxMap!!.getStyle { style ->
            val layer: Layer? = style.getLayer(layerId)
            if (layer != null) {
                if (VISIBLE.equals(layer.getVisibility().getValue())) {
                    layer.setProperties(visibility(NONE))
                } else {
                    layer.setProperties(visibility(VISIBLE))
                }
            }
        }
    }

    private var mapboxMap: MapboxMap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_map)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(OnMapReadyCallback { map ->
            mapboxMap = map
            map.setStyle(Style.LIGHT) { style -> // Disable any type of fading transition when icons collide on the map. This enhances the visual
                // look of the data clustering together and breaking apart.
                style.transition = TransitionOptions(0, 0, false)

                mapboxMap!!.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(59.9311, 30.3609), 15.0
                    )
                )
                addClusteredGeoJsonSource(style)
                style.addImage(
                    "cross-icon-id",
                    BitmapUtils.getBitmapFromDrawable(resources.getDrawable(R.drawable.ic_close_blue))!!,
                    true
                )
            }
        })
    }

    fun returnCurrentAndIncrease(value: Expression): Expression {
        Log.d("OLOLO", "TAG VALUE: ${value}")
        return value
    }

    private fun addClusteredGeoJsonSource(loadedMapStyle: Style) {

        val itType = JsonObject()
        itType.addProperty("id", "it")

        val winterType = JsonObject()
        winterType.addProperty("id", "winter")

        val winterType2 = JsonObject()
        winterType2.addProperty("id", "winter")

        val symbolLayerIconFeatureList = arrayListOf<Feature>(
            Feature.fromGeometry(
                Point.fromLngLat(30.3609, 59.9311), itType),
            Feature.fromGeometry(
                Point.fromLngLat(30.3609, 59.9348), itType),
            Feature.fromGeometry(
                Point.fromLngLat(30.3649, 59.9311), winterType),
            Feature.fromGeometry(
                Point.fromLngLat(30.3649, 59.9321), winterType2)
        )

        val mySum = Expression("+")
//        val mySum2 = Expression("+")
        Log.d("OLOLO", "TEST: ${Expression.sum(Expression("case",
            Expression.eq(Expression.get("id"), "winter"),
            Expression.literal(1),
            Expression.literal(0)
        ))}")

        // Add a new source from the GeoJSON data and set the 'cluster' option to true.
        try {
            loadedMapStyle.addSource( // Point to GeoJSON data. This example visualizes all M1.0+ earthquakes from
                GeoJsonSource(
                    "earthquakes",
                    FeatureCollection.fromFeatures(symbolLayerIconFeatureList),
                    GeoJsonOptions()
                        .withCluster(true)
                        .withClusterMaxZoom(25)
                        .withClusterRadius(50)
                        .withClusterProperty(
                            "winter", Expression("+"), Expression("case",
                                Expression.eq(Expression.get("id"), "winter"),
                                Expression.literal(1),
                                Expression.literal(0)
                            )
                        )
                )
            )
        } catch (uriSyntaxException: URISyntaxException) {
            Log.d("ERROR", "OLOLOLOLO ${uriSyntaxException.message}")
//            Timber.e("Check the URL %s", uriSyntaxException.message)
        }

        //Creating a marker layer for single data points
        val unclustered = SymbolLayer("unclustered-points", "earthquakes")
        unclustered.setProperties(
            PropertyFactory.iconImage("cross-icon-id"),
            PropertyFactory.iconSize(4f)
        )
        unclustered.setFilter(Expression.has("id"));
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
            val pointCount =
                Expression.toNumber(
                    Expression.get("point_count")
                )

            circles.setProperties(
                PropertyFactory.circleColor(layers[i][1]),
                PropertyFactory.circleRadius(
                    Expression.interpolate(
                        Expression.exponential(1),
                        pointCount,
                        Expression.stop(10, 10f),
                        Expression.stop(20, 20f),
                        Expression.stop(40, 30f),
                        Expression.stop(80, 40f),
                        Expression.stop(160, 50f),
                        Expression.stop(320, 60f)
                    )
                )
            )
            // Add a filter to the cluster layer that hides the circles based on "point_count"
            circles.setFilter(
                if (i == 0) Expression.all(
                    Expression.has("point_count"),
                    Expression.gte(
                        Expression.toNumber(
                            Expression.get("it")
                        ),
                        Expression.toNumber(
                            Expression.get("winter")
                        )
                    )
                ) else Expression.all(
                    Expression.has("point_count"),
                    Expression.gte(
                        Expression.toNumber(
                            Expression.get("winter")
                        ),
                        2
                    )
                )
            )
            loadedMapStyle.addLayer(circles)
        }

        //Add the count labels
        val count = SymbolLayer("count", "earthquakes")
        count.setProperties(
            PropertyFactory.textField(
                Expression.toString(
                    Expression.get("winter")
                )
            ),
            PropertyFactory.textSize(12f),
            PropertyFactory.textColor(Color.BLACK),
            PropertyFactory.textIgnorePlacement(true),
            PropertyFactory.textAllowOverlap(true)
        )
        loadedMapStyle.addLayer(count)
    }

    public override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    public override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}
