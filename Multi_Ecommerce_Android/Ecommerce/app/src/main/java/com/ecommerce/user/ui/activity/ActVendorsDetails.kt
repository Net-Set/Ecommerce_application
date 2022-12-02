package com.ecommerce.user.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ecommerce.user.R
import com.ecommerce.user.adapter.VendorsDetailsAdapter
import com.ecommerce.user.api.ApiClient
import com.ecommerce.user.base.BaseActivity
import com.ecommerce.user.base.BaseAdaptor
import com.ecommerce.user.databinding.ActVendorsDetailsBinding
import com.ecommerce.user.databinding.RowGravityBinding
import com.ecommerce.user.databinding.RowStoreBannerBinding
import com.ecommerce.user.model.TopbannerItem
import com.ecommerce.user.model.VendorsDetailsDataItem
import com.ecommerce.user.model.VendorsDetailsResponse
import com.ecommerce.user.utils.Common
import com.ecommerce.user.utils.SharePreference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class ActVendorsDetails : BaseActivity() {
    private lateinit var vendorsdetailsBinding: ActVendorsDetailsBinding
    private var bannerList = ArrayList<TopbannerItem>()
    var currency: String = ""
    var currencyPosition: String = ""
    private var vendorsdetailsDataList = ArrayList<VendorsDetailsDataItem>()
    private var vendorsDataAdapter:VendorsDetailsAdapter? =
        null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var currentPage = 1
    var total_pages: Int = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var pastVisibleItems = 0
    var image = ""
    var rate = ""
    var vendorsName = ""
    var colorArray = arrayOf(
        "#FDF7FF",
        "#FDF3F0",
        "#EDF7FD",
        "#FFFAEA",
        "#F1FFF6",
        "#FFF5EC"
    )
    var vendorsId = ""
    override fun setLayout(): View = vendorsdetailsBinding.root

    override fun initView() {
        vendorsdetailsBinding = ActVendorsDetailsBinding.inflate(layoutInflater)
        currency = SharePreference.getStringPref(this@ActVendorsDetails, SharePreference.Currency)!!
        currencyPosition = SharePreference.getStringPref(
            this@ActVendorsDetails,
            SharePreference.CurrencyPosition
        )!!
        vendorsdetailsBinding.ivAboutus.visibility = View.GONE
        linearLayoutManager = LinearLayoutManager(
            this@ActVendorsDetails,
            LinearLayoutManager.VERTICAL,
            false
        )
        vendorsdetailsBinding.ivBack.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
        vendorsId = intent.getStringExtra("vendor_id") ?: ""
        vendorsdetailsBinding.tvtitle.text = intent.getStringExtra("vendors_name")!!
        if (Common.isCheckNetwork(this@ActVendorsDetails)) {
            image = intent.getStringExtra("vendors_iv")!!
            rate = intent.getStringExtra("vendors_rate") ?: "0.0"
            vendorsName = intent.getStringExtra("vendors_name")!!
        }

        vendorsdetailsBinding.rvVendorsList.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    visibleItemCount = linearLayoutManager!!.childCount
                    totalItemCount = linearLayoutManager!!.itemCount
                    pastVisibleItems = linearLayoutManager!!.findFirstVisibleItemPosition()
                    if (currentPage < total_pages) {
                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            currentPage += 1
                            if (Common.isCheckNetwork(this@ActVendorsDetails)) {
                                callApiVendorsDetail(vendorsId)
                            } else {
                                Common.alertErrorOrValidationDialog(
                                    this@ActVendorsDetails,
                                    resources.getString(R.string.no_internet)
                                )
                            }
                        }
                    }
                }
            }
        })
    }

    //TODO first banner
    private fun loadPagerImagesSliders(slidersList: ArrayList<TopbannerItem>) {
        lateinit var binding: RowStoreBannerBinding
        val bannerAdapter = object :
            BaseAdaptor<TopbannerItem, RowStoreBannerBinding>(this@ActVendorsDetails, slidersList) {
            @SuppressLint("NewApi", "ResourceType")
            override fun onBindData(
                holder: RecyclerView.ViewHolder?,
                `val`: TopbannerItem,
                position: Int
            ) {
                Glide.with(this@ActVendorsDetails).load(slidersList[position].imageUrl)
                    .into(binding.ivBanner)
                binding.ivBanner.setBackgroundColor(Color.parseColor(colorArray[position % 6]))
            }

            override fun setItemLayout(): Int {
                return R.layout.row_banner
            }

            override fun getBinding(parent: ViewGroup): RowStoreBannerBinding {
                binding = RowStoreBannerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return binding
            }
        }
        if (bannerList.size > 0) {
            vendorsdetailsBinding.rvBanner.visibility = View.VISIBLE
            vendorsdetailsBinding.rvBanner.apply {
                layoutManager = LinearLayoutManager(
                    this@ActVendorsDetails,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                itemAnimator = DefaultItemAnimator()
                adapter = bannerAdapter
                isNestedScrollingEnabled = true
            }
        } else {
            vendorsdetailsBinding.rvBanner.visibility = View.GONE
        }
    }

    //TODO CALL VENDORES DETAILS API
    private fun callApiVendorsDetail(vendorsId: String) {
        Common.showLoadingProgress(this@ActVendorsDetails)
        val hasmap = HashMap<String, String>()
        hasmap["user_id"] =
            SharePreference.getStringPref(this@ActVendorsDetails, SharePreference.userId)!!
        hasmap["vendor_id"] = vendorsId
        val call = ApiClient.getClient.getVendorsDetails(currentPage.toString(), hasmap)
        call.enqueue(object : Callback<VendorsDetailsResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<VendorsDetailsResponse>,
                response: Response<VendorsDetailsResponse>
            ) {
                if (response.code() == 200) {
                    val restResponce = response.body()!!
                    Log.e("Status", restResponce.status.toString())
                    if (restResponce.status == 1) {
                        Common.dismissLoadingProgress()
                        rate = if (restResponce.vendordetails?.rattings?.size == 0) {
                            if (restResponce.vendordetails.rattings.size > 0) {
                                restResponce.vendordetails.rattings[0].avgRatting.toString()
                            } else {
                                " 0.0"
                            }
                        } else {
                            "0.0"
                        }
                        bannerList.addAll(restResponce.bannerList)
                        loadPagerImagesSliders(bannerList)

                        if (restResponce.data?.data?.size ?: 0 > 0) {
                            vendorsdetailsBinding.rvVendorsList.visibility = View.VISIBLE
                            vendorsdetailsBinding.tvNoDataFound.visibility = View.GONE
                            this@ActVendorsDetails.currentPage =
                                restResponce.data?.currentPage!!.toInt()
                            total_pages = restResponce.data.lastPage!!.toInt()
                            restResponce.data.data?.let {
                                vendorsdetailsDataList.addAll(it)

                            }
                        } else {
                            vendorsdetailsBinding.rvVendorsList.visibility = View.GONE
                            vendorsdetailsBinding.tvNoDataFound.visibility = View.VISIBLE
                        }
                        vendorsDataAdapter?.notifyDataSetChanged()
                        vendorsdetailsBinding.ivAboutus.visibility = View.VISIBLE
                        vendorsdetailsBinding.ivAboutus.setOnClickListener {
                            val intent = Intent(this@ActVendorsDetails, ActStoreInfo::class.java)
                            intent.putExtra("mobile", restResponce.vendordetails?.mobile)
                            intent.putExtra("email", restResponce.vendordetails?.email)
                            intent.putExtra("image", image)
                            intent.putExtra("rate", rate)
                            intent.putExtra("vendorsName", vendorsName)
                            intent.putExtra(
                                "storeaddress",
                                restResponce.vendordetails?.storeAddress
                            )
                            startActivity(intent)
                        }
                    } else if (restResponce.status == 0) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@ActVendorsDetails,
                            restResponce.message.toString()
                        )
                        vendorsdetailsBinding.ivAboutus.visibility = View.GONE
                    }
                } else {
                    Common.dismissLoadingProgress()
                    Common.alertErrorOrValidationDialog(
                        this@ActVendorsDetails,
                        resources.getString(R.string.error_msg)
                    )
                }
            }

            override fun onFailure(call: Call<VendorsDetailsResponse>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@ActVendorsDetails,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }


    override fun onResume() {
        super.onResume()
        vendorsdetailsDataList.clear()
        currentPage = 1
        loadVendorsDetails(vendorsdetailsDataList)
        callApiVendorsDetail(vendorsId)
    }

    private fun loadVendorsDetails(vendorsdetailsDataList: ArrayList<VendorsDetailsDataItem>) {
        vendorsDataAdapter= VendorsDetailsAdapter(
            this@ActVendorsDetails,
            vendorsdetailsDataList
        )
        vendorsdetailsBinding.rvVendorsList.layoutManager =
            linearLayoutManager
        vendorsdetailsBinding.rvVendorsList.adapter = vendorsDataAdapter
    }
}