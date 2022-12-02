package com.ecommerce.user.ui.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecommerce.user.R
import com.ecommerce.user.api.ApiClient
import com.ecommerce.user.base.BaseActivity
import com.ecommerce.user.base.BaseAdaptor
import com.ecommerce.user.databinding.ActOffersBinding
import com.ecommerce.user.databinding.RowOffersBinding
import com.ecommerce.user.model.*
import com.ecommerce.user.ui.authentication.ActLogin
import com.ecommerce.user.utils.Common
import com.ecommerce.user.utils.Common.getDate
import com.ecommerce.user.utils.SharePreference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActOffers : BaseActivity() {
    private lateinit var offersBinding: ActOffersBinding
    private var couponList = ArrayList<CouponDataItem>()
    private var couponAllDataAdapter: BaseAdaptor<CouponDataItem, RowOffersBinding>? =
        null
    private var manager: LinearLayoutManager? = null
    private var currentPage = 1
    var total_pages: Int = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var pastVisibleItems = 0
    var pos: Int = 0
    var colorArray = arrayOf(
        "#FDF7FF",
        "#FDF3F0",
        "#EDF7FD",
        "#FFFAEA",
        "#F1FFF6",
        "#FFF5EC"
    )

    override fun setLayout(): View = offersBinding.root

    override fun initView() {
        offersBinding = ActOffersBinding.inflate(layoutInflater)
        manager = LinearLayoutManager(this@ActOffers, LinearLayoutManager.VERTICAL, false)
        offersBinding.ivBack.setOnClickListener { finish() }
        offersBinding.rvOffers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    visibleItemCount = manager!!.childCount
                    totalItemCount = manager!!.itemCount
                    pastVisibleItems = manager!!.findFirstVisibleItemPosition()
                    if (currentPage < total_pages) {
                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            currentPage += 1
                            callApiOffers()
                        }
                    }
                }
            }
        })
    }

    //TODO API OFFERS CALL
    private fun callApiOffers() {
        Common.showLoadingProgress(this@ActOffers)
        val call = ApiClient.getClient.getCoupon(currentPage.toString())
        call.enqueue(object : Callback<GetCouponResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<GetCouponResponse>,
                response: Response<GetCouponResponse>
            ) {
                if (response.code() == 200) {
                    val restResponce = response.body()!!
                    if (restResponce.status == 1) {
                        Common.dismissLoadingProgress()
                        if ((restResponce.data?.data?.size ?: 0) > 0) {
                            offersBinding.rvOffers.visibility = View.VISIBLE
                            offersBinding.tvNoDataFound.visibility = View.GONE
                            this@ActOffers.currentPage =
                                restResponce.data?.currentPage!!.toInt()
                            total_pages = restResponce.data.lastPage?.toInt() ?: 0
                            restResponce.data.data?.let {
                                couponList.addAll(it)
                            }
                        } else {
                            offersBinding.rvOffers.visibility = View.GONE
                            offersBinding.tvNoDataFound.visibility = View.VISIBLE
                        }
                        couponAllDataAdapter?.notifyDataSetChanged()
                    } else if (restResponce.status == 0) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@ActOffers,
                            restResponce.message.toString()
                        )
                    }
                } else {
                    Common.dismissLoadingProgress()
                    Common.alertErrorOrValidationDialog(
                        this@ActOffers,
                        resources.getString(R.string.error_msg)
                    )
                }
            }

            override fun onFailure(call: Call<GetCouponResponse>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@ActOffers,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    //TODO SET COUPON DATA
    private fun loadCouponDetails(couponList: ArrayList<CouponDataItem>) {
        lateinit var binding: RowOffersBinding
        couponAllDataAdapter =
            object : BaseAdaptor<CouponDataItem, RowOffersBinding>(
                this@ActOffers,
                couponList
            ) {
                @SuppressLint(
                    "NewApi", "ResourceType", "SetTextI18n",
                    "UseCompatLoadingForDrawables"
                )
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: CouponDataItem,
                    position: Int
                ) {
                    binding.cloffers.setBackgroundColor(Color.parseColor(colorArray[pos % 6]))
                    binding.tvCouponDate.text =
                        couponList[position].endDate?.let { getDate(it) }
                    binding.btnCouponName.text = couponList[position].couponName

                    binding.tvCouponTitle.text = couponList[position].description
                    binding.tvMinimumAmount.text = "Minimum amount for this offer ${
                        SharePreference.getStringPref(
                            this@ActOffers,
                            SharePreference.Currency
                        ).plus(couponList[position].min_amount)
                    }"
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_offers
                }

                override fun getBinding(parent: ViewGroup): RowOffersBinding {
                    binding = RowOffersBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return binding
                }
            }
        offersBinding.rvOffers.apply {
            layoutManager = manager
            itemAnimator = DefaultItemAnimator()
            adapter = couponAllDataAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        currentPage = 1
        couponList.clear()
        loadCouponDetails(couponList)
        if (Common.isCheckNetwork(this@ActOffers)) {
            if (SharePreference.getBooleanPref(this@ActOffers, SharePreference.isLogin)) {
                callApiOffers()
            } else {
                openActivity(ActLogin::class.java)
                this.finish()
            }
        } else {
            Common.alertErrorOrValidationDialog(
                this@ActOffers,
                resources.getString(R.string.no_internet)
            )
        }
    }
}