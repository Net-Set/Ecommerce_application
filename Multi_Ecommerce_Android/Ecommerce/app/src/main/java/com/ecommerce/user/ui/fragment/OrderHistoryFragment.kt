package com.ecommerce.user.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecommerce.user.R
import com.ecommerce.user.ui.authentication.ActLogin
import com.ecommerce.user.ui.activity.ActOrderDetails
import com.ecommerce.user.api.ApiClient
import com.ecommerce.user.base.BaseAdaptor
import com.ecommerce.user.base.BaseFragment
import com.ecommerce.user.databinding.FragOrderHistoryBinding
import com.ecommerce.user.databinding.RowOrderBinding
import com.ecommerce.user.model.OrderHistoryDataItem
import com.ecommerce.user.model.OrderHistoryResponse
import com.ecommerce.user.utils.Common
import com.ecommerce.user.utils.Common.getCurrentLanguage
import com.ecommerce.user.utils.SharePreference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class OrderHistoryFragment : BaseFragment<FragOrderHistoryBinding>() {
    private lateinit var fragOrderHistoryBinding: FragOrderHistoryBinding
    private var orderHistoryDataList = ArrayList<OrderHistoryDataItem>()
    var currency: String = ""
    var currencyPosition: String = ""
    private var wishListDataAdapter: BaseAdaptor<OrderHistoryDataItem, RowOrderBinding>? =
        null
    private var linearlayoutManager: LinearLayoutManager? = null
    private var currentPage = 1
    var total_pages: Int = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var pastVisibleItems = 0
    override fun initView(view: View) {
        fragOrderHistoryBinding = FragOrderHistoryBinding.bind(view)
        linearlayoutManager =
            LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.VERTICAL,
                false
            )
        currency = SharePreference.getStringPref(requireActivity(), SharePreference.Currency)!!
        currencyPosition =
            SharePreference.getStringPref(requireActivity(), SharePreference.CurrencyPosition)!!
        fragOrderHistoryBinding.rvOrderlist.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    visibleItemCount = linearlayoutManager!!.childCount
                    totalItemCount = linearlayoutManager!!.itemCount
                    pastVisibleItems = linearlayoutManager!!.findFirstVisibleItemPosition()
                    if (currentPage < total_pages) {
                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            currentPage += 1
                            if (Common.isCheckNetwork(requireActivity())) {
                                callApiOrderHistory()
                            } else {
                                Common.alertErrorOrValidationDialog(
                                    requireActivity(),
                                    resources.getString(R.string.no_internet)
                                )
                            }
                        }
                    }
                }
            }
        })
    }

    override fun getBinding(): FragOrderHistoryBinding {
        fragOrderHistoryBinding = FragOrderHistoryBinding.inflate(layoutInflater)
        return fragOrderHistoryBinding
    }

    //TODO API ORDER HISTORY CALL
    private fun callApiOrderHistory() {
        Common.showLoadingProgress(requireActivity())
        val hasmap = HashMap<String, String>()
        hasmap["user_id"] =
            SharePreference.getStringPref(requireActivity(), SharePreference.userId)!!

        val call = ApiClient.getClient.getOrderHistory(currentPage.toString(), hasmap)
        call.enqueue(object : Callback<OrderHistoryResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<OrderHistoryResponse>,
                response: Response<OrderHistoryResponse>
            ) {
                if (response.code() == 200) {
                    val restResponce = response.body()!!
                    if (restResponce.status == 1) {
                        Common.dismissLoadingProgress()
                        if ((restResponce.data?.data?.size ?: 0) > 0) {
                            fragOrderHistoryBinding.rvOrderlist.visibility = View.VISIBLE
                            fragOrderHistoryBinding.tvNoDataFound.visibility = View.GONE
                            this@OrderHistoryFragment.currentPage =
                                restResponce.data?.currentPage!!.toInt()
                            total_pages = restResponce.data.lastPage?.toInt() ?: 0
                            restResponce.data.data?.let {
                                orderHistoryDataList.addAll(it)
                            }
                        } else {
                            fragOrderHistoryBinding.rvOrderlist.visibility = View.GONE
                            fragOrderHistoryBinding.tvNoDataFound.visibility = View.VISIBLE
                        }
                        wishListDataAdapter?.notifyDataSetChanged()
                    } else if (restResponce.status == 0) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            requireActivity(),
                            restResponce.message.toString()
                        )
                    }
                } else {
                    Common.dismissLoadingProgress()
                    Common.alertErrorOrValidationDialog(
                        requireActivity(),
                        resources.getString(R.string.error_msg)
                    )
                }
            }

            override fun onFailure(call: Call<OrderHistoryResponse>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    requireActivity(),
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    //TODO SET ORDER HISTORY DATA
    private fun loadOrderHistory(orderHistoryDataList: ArrayList<OrderHistoryDataItem>) {
        lateinit var binding: RowOrderBinding
        wishListDataAdapter =
            object : BaseAdaptor<OrderHistoryDataItem, RowOrderBinding>(
                requireActivity(),
                orderHistoryDataList
            ) {
                @SuppressLint("NewApi", "ResourceType", "SetTextI18n")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: OrderHistoryDataItem,
                    position: Int
                ) {
                    binding.tvorderid.text = orderHistoryDataList[position].orderNumber
                    when (orderHistoryDataList[position].paymentType) {
                        1 -> {
                            binding.tvpaymenttype.text = getString(R.string.cash)
                        }
                        2 -> {
                            binding.tvpaymenttype.text = getString(R.string.wallet)
                        }
                        3 -> {
                            binding.tvpaymenttype.text = getString(R.string.razorpay)
                        }
                        4 -> {
                            binding.tvpaymenttype.text = getString(R.string.stripe)
                        }
                        5 -> {
                            binding.tvpaymenttype.text = getString(R.string.flutterwave)
                        }
                        6 -> {
                            binding.tvpaymenttype.text = getString(R.string.paystack)
                        }
                    }
                    binding.tvorderdate.text = orderHistoryDataList[position].date?.let {
                        Common.getDate(
                            it
                        )
                    }
                    binding.tvordercost.text =
                        Common.getPrice(
                            currencyPosition, currency,
                            orderHistoryDataList[position].grandTotal.toString()
                        )
                    holder?.itemView?.setOnClickListener {
                        Log.e(
                            "order_number--->",
                            orderHistoryDataList[position].orderNumber.toString()
                        )
                        val intent = Intent(requireActivity(), ActOrderDetails::class.java)
                        intent.putExtra(
                            "order_number",
                            orderHistoryDataList[position].orderNumber.toString()
                        )
                        startActivity(intent)
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_order
                }

                override fun getBinding(parent: ViewGroup): RowOrderBinding {
                    binding = RowOrderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return binding
                }
            }
        if (isAdded) {
            fragOrderHistoryBinding.rvOrderlist.apply {
             layoutManager =
                        linearlayoutManager
                    itemAnimator = DefaultItemAnimator()
                    adapter = wishListDataAdapter
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getCurrentLanguage(requireActivity(), false)
        currentPage=1
        orderHistoryDataList.clear()
        loadOrderHistory(orderHistoryDataList)
        if (Common.isCheckNetwork(requireActivity())) {
            if (SharePreference.getBooleanPref(requireActivity(), SharePreference.isLogin)) {
                callApiOrderHistory()
            } else {
                openActivity(ActLogin::class.java)
                requireActivity().finish()
            }
        } else {
            Common.alertErrorOrValidationDialog(
                requireActivity(),
                resources.getString(R.string.no_internet)
            )
        }
    }
}