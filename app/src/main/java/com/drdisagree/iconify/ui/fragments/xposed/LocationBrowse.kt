package com.drdisagree.iconify.ui.fragments.xposed

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.drawables.TintedDrawableSpan
import com.drdisagree.iconify.utils.NetworkUtils
import com.drdisagree.iconify.utils.weather.WeatherConfig
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

open class LocationBrowse : BaseFragment() {

    private val mLocationBrowseList: MutableList<LocationBrowseItem> = ArrayList()
    private var mAdapter: LocationListAdapter? = null
    private val mExecutorService: ExecutorService = Executors.newSingleThreadExecutor()
    private val mHandler = Handler(Looper.getMainLooper())
    private var mQueryString: String? = null
    private var mProgressBar: FrameLayout? = null

    private val mQueryRunnable = Runnable {
        mExecutorService.submit {
            getLocations(mQueryString)
        }
    }

    private open class LocationBrowseItem(
        val cityExt: String,
        private val mCountryId: String,
        val city: String,
        val lat: Double,
        val lon: Double
    ) {
        protected val id: String
            get() = "$city,$mCountryId"

        override fun equals(other: Any?): Boolean {
            return (other is LocationBrowseItem) && this.id == other.id
        }

        override fun hashCode(): Int {
            var result = cityExt.hashCode()
            result = 31 * result + mCountryId.hashCode()
            result = 31 * result + city.hashCode()
            result = 31 * result + lat.hashCode()
            result = 31 * result + lon.hashCode()
            return result
        }
    }

    inner class LocationListAdapter : RecyclerView.Adapter<LocationListAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return ViewHolder(
                inflater.inflate(
                    R.layout.view_list_item_location_browse,
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val city = mLocationBrowseList[position]

            holder.itemView.findViewById<TextView>(R.id.location_city).text = city.city
            holder.itemView.findViewById<TextView>(R.id.location_city_ext).text = city.cityExt

            holder.itemView.setOnClickListener {
                WeatherConfig.apply {
                    setLocationId(requireContext(), city.lat.toString(), city.lon.toString())
                    setLocationName(requireContext(), city.city)
                }
                val resultBundle = Bundle().apply {
                    putString(DATA_LOCATION_NAME, city.city)
                    putDouble(DATA_LOCATION_LAT, city.lat)
                    putDouble(DATA_LOCATION_LON, city.lon)
                }
                setFragmentResult(DATA_LOCATION_KEY, resultBundle)
                parentFragmentManager.popBackStack()
            }
        }

        override fun getItemCount(): Int = mLocationBrowseList.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_xposed_location_browse, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: MaterialToolbar = view.findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.custom_location_title)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mProgressBar = view.findViewById(R.id.query_progressbar)

        val queryPattern: EditText = view.findViewById(R.id.query_pattern_text)
        queryPattern.hint =
            prefixTextWithIcon(requireContext(), R.drawable.ic_search, queryPattern.hint)
        queryPattern.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mHandler.removeCallbacks(mQueryRunnable)
                mQueryString = s.toString()
                if (mQueryString.isNullOrEmpty()) {
                    hideProgress()
                    mLocationBrowseList.clear()
                    mAdapter?.notifyDataSetChanged()
                } else {
                    showProgress()
                    mHandler.postDelayed(mQueryRunnable, 750)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        mAdapter = LocationListAdapter()
        val queryList: RecyclerView = view.findViewById(R.id.query_result)
        queryList.adapter = mAdapter
        queryList.layoutManager = LinearLayoutManager(requireContext())
    }

    @Deprecated("Deprecated in Java")
    @Suppress("deprecation")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    protected fun getLocations(input: String?) {
        mLocationBrowseList.clear()

        try {
            val lang = Locale.getDefault().language.replaceFirst("_".toRegex(), "-")
            val url = String.format(URL_PLACES, Uri.encode(input?.trim { it <= ' ' }), lang)
            val response: String = NetworkUtils.downloadUrlMemoryAsString(url)
            val jsonResults = JSONObject(response).getJSONArray("geonames")
            val count = jsonResults.length()
            Log.d(TAG, "URL = $url returning a response of count = $count")

            for (i in 0 until count) {
                val result = jsonResults.getJSONObject(i)

                val population = if (result.has("population")) result.getInt("population") else 0
                if (population == 0) continue

                val city = result.getString("name")
                val country = result.getString("countryName")
                val countryId = result.getString("countryId")
                val adminName = if (result.has("adminName1")) result.getString("adminName1") else ""
                val cityExt = (if (adminName.isNullOrEmpty()) "" else "$adminName, ") + country
                val lat = result.getDouble("lat")
                val lon = result.getDouble("lng")

                val locationItem = LocationBrowseItem(cityExt, countryId, city, lat, lon)
                if (!mLocationBrowseList.contains(locationItem)) {
                    mLocationBrowseList.add(locationItem)
                    if (mLocationBrowseList.size == 5) break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Received malformed location data input=$input", e)
        } finally {
            mHandler.post {
                hideProgress()
                mAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun showProgress() {
        mProgressBar?.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        mProgressBar?.visibility = View.GONE
    }

    companion object {
        private val TAG = LocationBrowse::class.java.simpleName

        const val DATA_LOCATION_KEY = "locationRequestKey"
        const val DATA_LOCATION_NAME = "location_name"
        const val DATA_LOCATION_LAT = "location_lat"
        const val DATA_LOCATION_LON = "location_lon"

        private const val URL_PLACES =
            "https://secure.geonames.org/searchJSON?name_startsWith=%s&lang=%s&username=omnijaws&maxRows=20"

        fun prefixTextWithIcon(context: Context, iconRes: Int, msg: CharSequence): CharSequence {
            // Update the hint to contain the icon.
            // Prefix the original hint with two spaces. The first space gets replaced by the icon
            // using span. The second space is used for a singe space character between the hint
            // and the icon.
            val spanned = SpannableString("  $msg")
            spanned.setSpan(
                TintedDrawableSpan(context, iconRes),
                0, 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            return spanned
        }
    }
}