package com.ecommerce.user.ui.activity

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecommerce.user.R
import com.ecommerce.user.adapter.FilterViewAdapter
import com.ecommerce.user.adapter.ProductViewAdapter
import com.ecommerce.user.adapter.ViewAllAdapter
import com.ecommerce.user.api.ApiClient
import com.ecommerce.user.base.BaseActivity
import com.ecommerce.user.base.BaseAdaptor
import com.ecommerce.user.databinding.ActViewAllBinding
import com.ecommerce.user.databinding.RowViewallBinding
import com.ecommerce.user.model.*
import com.ecommerce.user.utils.Common
import com.ecommerce.user.utils.PaginationScrollListener
import com.ecommerce.user.utils.SharePreference
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class ActViewAll : BaseActivity() {
    private lateinit var viewAllBinding: ActViewAllBinding
    private var viewAllDataList = ArrayList<ViewAllDataItem>()
    private var filterAllDataList = ArrayList<FilterDataItem>()
    var currency: String = ""
    var currencyPosition: String = ""
    private var viewAllDataAdapter: ViewAllAdapter? =
        null
    private var filterAllDataAdapter: FilterViewAdapter? =
        null
    private var gridLayoutManager: GridLayoutManager? = null
    private var gridLayoutManagerFilter: GridLayoutManager? = null
    private var currentPage = 1
    private var currentPageFilter = 1
    internal var isLoadingFilter = false
    internal var isLastPageFilter = false
    internal var isLoading = false
    internal var isLastPage = false
    var total_pages: Int = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var pastVisibleItems = 0
    var protitle = ""
    var colorArray = arrayOf(
        "#FDF7FF",
        "#FDF3F0",
        "#EDF7FD",
        "#FFFAEA",
        "#F1FFF6",
        "#FFF5EC"
    )
    var title: String? = ""
    var type: String = ""
    override fun setLayout(): View = viewAllBinding.root

    override fun initView() {
        viewAllBinding = ActViewAllBinding.inflate(layoutInflater)
        gridLayoutManager = GridLayoutManager(this@ActViewAll, 2, GridLayoutManager.VERTICAL, false)
        gridLayoutManagerFilter =
            GridLayoutManager(this@ActViewAll, 2, GridLayoutManager.VERTICAL, false)
        currency = SharePreference.getStringPref(this@ActViewAll, SharePreference.Currency)!!
        currencyPosition =
            SharePreference.getStringPref(this@ActViewAll, SharePreference.CurrencyPosition)!!
        title = intent.getStringExtra("title")

        viewAllBinding.tvviewall.text = title.toString()

        if (Common.isCheckNetwork(this@ActViewAll)) {
            when (title) {
                "Featured Products" -> {
                    viewAllBinding.rvFilterall.visibility = View.GONE
                    //viewAllBinding.rvProduct.visibility = View.GONE
                    viewAllBinding.rvViewall.visibility = View.VISIBLE
                    title = "featured_products"
                    protitle = "featured_products"
                    Log.d("title", title.toString())
                    viewAllData(title.toString())
                }
                "New Arrivals" -> {
                    viewAllBinding.rvFilterall.visibility = View.GONE
                    //viewAllBinding.rvProduct.visibility = View.GONE
                    viewAllBinding.rvViewall.visibility = View.VISIBLE
                    title = "new_products"
                    protitle = "new_products"
                    Log.d("title", title.toString())
                    viewAllData(title.toString())
                }
                "Hot Deals" -> {
                    viewAllBinding.rvFilterall.visibility = View.GONE
                    // viewAllBinding.rvProduct.visibility = View.GONE
                    viewAllBinding.rvViewall.visibility = View.VISIBLE
                    title = "hot_products"
                    protitle = "hot_products"
                    Log.d("title", title.toString())
                    viewAllData(title.toString())
                }
            }
        } else {
            Common.alertErrorOrValidationDialog(
                this,
                resources.getString(R.string.no_internet)
            )
        }
        loadViewAll(viewAllDataList)
        paginationViewAll()

        viewAllBinding.ivBack.setOnClickListener {
            finish()
        }
        viewAllBinding.ivFilter.setOnClickListener {
            isLoadingFilter = false
            isLastPageFilter = false
            val dialog = BottomSheetDialog(this@ActViewAll)
            if (Common.isCheckNetwork(this@ActViewAll)) {
                val view =
                    layoutInflater.inflate(R.layout.row_bottomsheetsortby, null)
                val latest = view.findViewById<TextView>(R.id.tvlatest)
                val pricelowtohigh = view.findViewById<TextView>(R.id.tvpricelowtohigh)
                val pricehightolow = view.findViewById<TextView>(R.id.tvpricehightolow)
                val rattinglowtohigh = view.findViewById<TextView>(R.id.tvrattinglowtohigh)
                val rattinghightolow = view.findViewById<TextView>(R.id.tvrattinghightolow)
                val close = view.findViewById<ImageView>(R.id.iv_close)

                latest.setOnClickListener {
                    type = "new"
                    viewAllBinding.rvFilterall.visibility = View.VISIBLE
                    viewAllBinding.rvViewall.visibility = View.GONE

                    currentPageFilter = 1
                    filterAllDataList.clear()
                    loadFilterAdapter(filterAllDataList)
                    callApiFilter(type)
                    Log.d("New", type + currentPageFilter)

                    dialog.dismiss()
                }
                pricelowtohigh.setOnClickListener {
                    type = "price-low-to-high"
                    viewAllBinding.rvFilterall.visibility = View.VISIBLE
                    viewAllBinding.rvViewall.visibility = View.GONE
                    currentPageFilter = 1
                    filterAllDataList.clear()
                    loadFilterAdapter(filterAllDataList)
                    callApiFilter(type)
                    Log.d("price-low-to-high", type + currentPageFilter)

                    dialog.dismiss()
                }
                pricehightolow.setOnClickListener {
                    type = "price-high-to-low"
                    viewAllBinding.rvFilterall.visibility = View.VISIBLE
                    viewAllBinding.rvViewall.visibility = View.GONE

                    currentPageFilter = 1
                    filterAllDataList.clear()
                    loadFilterAdapter(filterAllDataList)
                    callApiFilter(type)
                    Log.d("PricehighTolow", type + currentPageFilter)
                    dialog.dismiss()
                }
                rattinglowtohigh.setOnClickListener {
                    type = "ratting-low-to-high"
                    viewAllBinding.rvFilterall.visibility = View.VISIBLE
                    viewAllBinding.rvViewall.visibility = View.GONE

                    currentPageFilter = 1
                    filterAllDataList.clear()
                    loadFilterAdapter(filterAllDataList)
                    callApiFilter(type)
                    Log.d("ratting-low-to-high", type + currentPageFilter)

                    dialog.dismiss()
                }
                rattinghightolow.setOnClickListener {
                    type = "ratting-high-to-low"
                    viewAllBinding.rvFilterall.visibility = View.VISIBLE
                    viewAllBinding.rvViewall.visibility = View.GONE
                    currentPageFilter = 1
                    filterAllDataList.clear()
                    loadFilterAdapter(filterAllDataList)
                    callApiFilter(type)
                    Log.d("ratting-high-to-low", type + currentPageFilter)
                    dialog.dismiss()
                }
                paginationFilter()
                close.setOnClickListener { dialog.dismiss() }
                dialog.setCancelable(false)
                dialog.setContentView(view)
                dialog.show()
            } else {
                Common.alertErrorOrValidationDialog(
                    this@ActViewAll,
                    resources.getString(R.string.no_internet)
                )
                dialog.dismiss()
            }
        }

    }


    private fun paginationFilter() {
        val paginationListener = object : PaginationScrollListener(gridLayoutManagerFilter) {
            override fun isLastPage(): Boolean {
                return isLastPageFilter
            }

            override fun isLoading(): Boolean {
                return isLoadingFilter
            }

            override fun loadMoreItems() {
                isLoadingFilter = true
                currentPageFilter++
                Log.d("type", type.toString())
                callApiFilter(type)
            }
        }
        viewAllBinding.rvFilterall.addOnScrollListener(paginationListener)
    }

    private fun paginationViewAll() {
        val paginationListener = object : PaginationScrollListener(gridLayoutManager) {
            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            override fun loadMoreItems() {
                isLoading = true
                currentPage++
                Log.d("title", title.toString())
                viewAllData(title.toString())
            }
        }
        viewAllBinding.rvViewall.addOnScrollListener(paginationListener)
    }

    private fun loadViewAll(viewAllDataList: ArrayList<ViewAllDataItem>) {
        viewAllDataAdapter = ViewAllAdapter(this@ActViewAll, viewAllDataList)
        viewAllBinding.rvViewall.layoutManager =
            gridLayoutManager
        viewAllBinding.rvViewall.adapter = viewAllDataAdapter
    }

    private fun loadFilterAdapter(filterAllDataList: ArrayList<FilterDataItem>) {
        filterAllDataAdapter = FilterViewAdapter(this@ActViewAll, filterAllDataList)
        viewAllBinding.rvFilterall.layoutManager =
            gridLayoutManagerFilter
        viewAllBinding.rvFilterall.adapter = filterAllDataAdapter
    }


    //TODO CALL FILTER API
    private fun callApiFilter(type: String) {
        Common.showLoadingProgress(this@ActViewAll)
        val hasmap = HashMap<String, String>()
        hasmap["user_id"] = SharePreference.getStringPref(this@ActViewAll, SharePreference.userId)!!
        hasmap["type"] = type
        hasmap["product"] = protitle
        if (intent.getSerializableExtra("innersubcategory_id").toString() == "null") {
            hasmap["innersubcat_id"] = ""
        } else {
            hasmap["innersubcat_id"] =
                intent.getSerializableExtra("innersubcategory_id").toString()
        }
        val call = ApiClient.getClient.getFilter(currentPageFilter.toString(), hasmap)
        call.enqueue(object : Callback<GetFilterResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<GetFilterResponse>,
                response: Response<GetFilterResponse>
            ) {
                if (response.code() == 200) {
                    val restResponce = response.body()!!
                    if (restResponce.status == 1) {
                        Common.dismissLoadingProgress()
                        if ((restResponce.data?.data?.size ?: 0) > 0) {
                            viewAllBinding.rvFilterall.visibility = View.VISIBLE
                            viewAllBinding.tvNoDataFound.visibility = View.GONE
                            this@ActViewAll.currentPageFilter =
                                restResponce.data?.currentPage!!.toInt()
                            total_pages = restResponce.data.lastPage?.toInt() ?: 0
                            restResponce.data.data?.let {
                                filterAllDataList.addAll(it)
                            }
                            Log.d("Current Page", currentPageFilter.toString())
                            Log.d("total_pages", total_pages.toString())
                            if (currentPageFilter >= total_pages) {
                                isLastPageFilter = true
                            }
                            isLoadingFilter = false
                        } else {
                            viewAllBinding.rvFilterall.visibility = View.GONE
                            viewAllBinding.tvNoDataFound.visibility = View.VISIBLE
                        }
                        filterAllDataAdapter?.notifyDataSetChanged()
                    } else if (restResponce.status == 0) {
                        Common.dismissLoadingProgress()
                        if (restResponce.message == "No data found") {
                            viewAllBinding.tvNoDataFound.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Common.dismissLoadingProgress()
                    Common.alertErrorOrValidationDialog(
                        this@ActViewAll,
                        resources.getString(R.string.error_msg)
                    )
                }
            }

            override fun onFailure(call: Call<GetFilterResponse>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@ActViewAll,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    //TODO CALL VIEW ALL API
    private fun viewAllData(title: String) {
        Common.showLoadingProgress(this@ActViewAll)
        val hasmap = HashMap<String, String>()
        Log.d("title", title.toString())
        hasmap["user_id"] = SharePreference.getStringPref(this@ActViewAll, SharePreference.userId)!!
        hasmap["type"] = title
        val call = ApiClient.getClient.setViewAllListing(currentPage.toString(), hasmap)
        call.enqueue(object : Callback<ViewAllListResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<ViewAllListResponse>,
                response: Response<ViewAllListResponse>
            ) {
                if (response.code() == 200) {
                    val restResponce = response.body()!!
                    if (restResponce.status == 1) {
                        Common.dismissLoadingProgress()
                        if ((restResponce.alldata?.data?.size ?: 0) > 0) {
                            viewAllBinding.rvViewall.visibility = View.VISIBLE
                            viewAllBinding.tvNoDataFound.visibility = View.GONE
                            this@ActViewAll.currentPage =
                                restResponce.alldata?.currentPage!!.toInt()
                            total_pages = restResponce.alldata.lastPage!!.toInt()
                            restResponce.alldata.data?.let {
                                viewAllDataList.addAll(it)
                            }
                            if (currentPage >= total_pages) {
                                isLastPage = true
                            }
                            isLoading = false
                        } else {
                            viewAllBinding.rvViewall.visibility = View.GONE
                            viewAllBinding.tvNoDataFound.visibility = View.VISIBLE
                        }
                        viewAllDataAdapter?.notifyDataSetChanged()
                    } else if (restResponce.status == 0) {
                        Common.dismissLoadingProgress()
                        if (restResponce.message == "No data found") {
                            viewAllBinding.tvNoDataFound.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Common.dismissLoadingProgress()
                    Common.alertErrorOrValidationDialog(
                        this@ActViewAll,
                        resources.getString(R.string.error_msg)
                    )
                }
            }

            override fun onFailure(call: Call<ViewAllListResponse>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@ActViewAll,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }
}