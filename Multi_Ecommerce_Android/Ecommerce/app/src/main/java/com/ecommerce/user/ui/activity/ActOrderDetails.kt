package com.ecommerce.user.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ecommerce.user.R
import com.ecommerce.user.api.ApiClient
import com.ecommerce.user.api.SingleResponse
import com.ecommerce.user.base.BaseActivity
import com.ecommerce.user.base.BaseAdaptor
import com.ecommerce.user.databinding.ActOrderDetailsBinding
import com.ecommerce.user.databinding.RemoveItemDialogBinding
import com.ecommerce.user.databinding.RowOrderdetailsproductBinding
import com.ecommerce.user.model.*
import com.ecommerce.user.ui.authentication.ActLogin
import com.ecommerce.user.utils.Common
import com.ecommerce.user.utils.SharePreference
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ActOrderDetails : BaseActivity() {
    private lateinit var orderDetailsBinding: ActOrderDetailsBinding
    private var orderData: ArrayList<OrderDataItem>? = null

    var currency: String = ""
    var currencyPosition: String = ""
    var orderDetailsList: OrderInfo? = null
    var orderstatus: String = ""
    var colorArray = arrayOf(
        "#FDF7FF",
        "#FDF3F0",
        "#EDF7FD",
        "#FFFAEA",
        "#F1FFF6",
        "#FFF5EC"
    )

    override fun setLayout(): View = orderDetailsBinding.root

    override fun initView() {
        orderDetailsBinding = ActOrderDetailsBinding.inflate(layoutInflater)
        if (Common.isCheckNetwork(this@ActOrderDetails)) {
            if (SharePreference.getBooleanPref(this@ActOrderDetails, SharePreference.isLogin)) {
                callApiOrderDetail()
            } else {
                openActivity(ActLogin::class.java)
                this.finish()
            }
        } else {
            Common.alertErrorOrValidationDialog(
                this@ActOrderDetails,
                resources.getString(R.string.no_internet)
            )
        }
        currency = SharePreference.getStringPref(this@ActOrderDetails, SharePreference.Currency)!!
        currencyPosition =
            SharePreference.getStringPref(
                this@ActOrderDetails,
                SharePreference.CurrencyPosition
            )!!
        orderDetailsBinding.ivBack.setOnClickListener {
            finish()
            setResult(RESULT_OK)
        }
    }

    //TODO API ORDER DETAILS CALL
    private fun callApiOrderDetail() {
        Common.showLoadingProgress(this@ActOrderDetails)
        val hasmap = HashMap<String, String>()
        hasmap["order_number"] = intent.getStringExtra("order_number")!!
        val call = ApiClient.getClient.getOrderDetails(hasmap)
        call.enqueue(object : Callback<OrderDetailsResponse> {
            override fun onResponse(
                call: Call<OrderDetailsResponse>,
                response: Response<OrderDetailsResponse>
            ) {
                if (response.code() == 200) {
                    val restResponce = response.body()!!
                    if (restResponce.status == 1) {
                        Common.dismissLoadingProgress()
                        orderDetailsList = restResponce.orderInfo
                        loadOrderDetails(orderDetailsList!!)
                        if (restResponce.orderData?.size ?: 0 > 0) {
                            orderDetailsBinding.rvorderproduct.visibility = View.VISIBLE
                            orderDetailsBinding.tvNoDataFound.visibility = View.GONE
                            orderData = restResponce.orderData
                            orderData?.let {
                                loadOrderProductDetails(it)
                            }
                        } else {
                            orderDetailsBinding.rvorderproduct.visibility = View.VISIBLE
                            orderDetailsBinding.tvNoDataFound.visibility = View.GONE
                        }
                    } else if (restResponce.status == 0) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@ActOrderDetails,
                            restResponce.message.toString()
                        )
                    }
                } else {
                    Common.dismissLoadingProgress()
                    Common.alertErrorOrValidationDialog(
                        this@ActOrderDetails,
                        resources.getString(R.string.error_msg)
                    )
                }
            }

            override fun onFailure(call: Call<OrderDetailsResponse>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@ActOrderDetails,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    //TODO SET ORDER PRODUCT DETAILS DATA
    private fun loadOrderProductDetails(orderData: ArrayList<OrderDataItem>) {
        lateinit var binding: RowOrderdetailsproductBinding
        val orderDetailsAdpater =
            object : BaseAdaptor<OrderDataItem, RowOrderdetailsproductBinding>(
                this@ActOrderDetails,
                orderData
            ) {
                @SuppressLint(
                    "NewApi", "ResourceType", "SetTextI18n", "InflateParams",
                    "UseCompatLoadingForDrawables"
                )
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: OrderDataItem,
                    position: Int
                ) {
                    Glide.with(this@ActOrderDetails)
                        .load(orderData[position].imageUrl).into(binding.ivCartitemm)
                    binding.ivCartitemm.setBackgroundColor(Color.parseColor(colorArray[position % 6]))
                    binding.tvcateitemname.text = orderData[position].productName
                    binding.tvcartitemqty.text =
                        "Qty: " + orderData[position].qty.toString() + "*" + Common.getPrice(
                            currencyPosition,
                            currency,
                            orderData[position].price.toString()
                        )
                    val qty = orderData[position].qty
                    val price = orderData[position].price?.toDouble()
                    val totalpriceqty = price!! * qty!!
                    if (orderData[position].variation == null) {
                        binding.tvcartitemsize.text = "-"
                    } else {
                        binding.tvcartitemsize.text =
                            orderData[position].attribute + ": " + orderData[position].variation
                    }
                    orderstatus = orderData[position].status.toString()
                    if (orderstatus == "5" || orderstatus == "7") {
                        binding.swipe.isSwipeEnabled = false
                    }
                    when (orderstatus) {
                        "5" -> {
                            binding.swipe.isSwipeEnabled
                            binding.tvorderstatus.visibility = View.VISIBLE
                            binding.tvorderstatus.text = getString(R.string.order_cancelled)
                        }
                        "7" -> {
                            binding.swipe.isSwipeEnabled
                            binding.tvorderstatus.visibility = View.VISIBLE
                            binding.tvorderstatus.text = getString(R.string.return_request)
                        }
                        else -> {
                            binding.tvorderstatus.visibility = View.GONE
                        }
                    }

                    binding.tvcartitemprice.text =
                        Common.getPrice(
                            currencyPosition, currency,
                            totalpriceqty.toString()
                        )

                    binding.tvshippingcost.text = Common.getPrice(
                        currencyPosition, currency,
                        orderData[position].shippingCost!!.toString()
                    )

                    binding.tvtax.text = Common.getPrice(
                        currencyPosition, currency,
                        orderData[position].tax!!.toString()
                    )

                    binding.tvTotalCart.text = Common.getPrice(
                        currencyPosition,
                        currency,
                        orderData[position].total.toString()
                    )

                    /*if (orderData[position].discountAmount == null) {
                        binding.tvdiscout.text = Common.getPrice(
                            currencyPosition, currency,
                            0.toString()
                        )

                    } else {
                        binding.tvdiscout.text = Common.getPrice(
                            currencyPosition, currency,
                            orderData[position].discountAmount!!.toString()
                        )

                    }*/


                    binding.tvmore.setOnClickListener {
                        val dialog = BottomSheetDialog(this@ActOrderDetails)
                        if (Common.isCheckNetwork(this@ActOrderDetails)) {
                            val view =
                                layoutInflater.inflate(R.layout.row_bottomsheetorderdetails, null)
                            dialog.window?.setBackgroundDrawable(getDrawable(R.color.tr))
                            val btnCancelorder = view.findViewById<TextView>(R.id.tvcancelorder)
                            val btnTrackorder = view.findViewById<TextView>(R.id.tvtrackorder)
                            val btnCancel = view.findViewById<TextView>(R.id.tvCancel)
                            val btnRetuenRequest = view.findViewById<TextView>(R.id.tvReturnReq)
                            val viewreturnrequest = view.findViewById<View>(R.id.view2)
                            if (orderstatus == "4") {
                                btnRetuenRequest.visibility = View.VISIBLE
                                viewreturnrequest.visibility = View.VISIBLE
                            } else {
                                btnRetuenRequest.visibility = View.GONE
                                viewreturnrequest.visibility = View.GONE
                            }
                            if (orderstatus == "7") {
                                btnRetuenRequest.visibility = View.GONE
                                viewreturnrequest.visibility = View.GONE
                            }
                            btnCancel.setOnClickListener {
                                dialog.dismiss()
                                onResume()
                            }
                            btnCancelorder.setOnClickListener {
                                cancelOrderDialog(orderData[position].id ?: 0)
                                dialog.dismiss()
                            }
                            btnRetuenRequest.setOnClickListener {
                                Log.e(
                                    "order_id--->",
                                    orderData[position].id.toString()
                                )
                                val intent =
                                    Intent(this@ActOrderDetails, ActRetuenRequest::class.java)
                                intent.putExtra(
                                    "order_id",
                                    orderData[position].id.toString()
                                )
                                intent.putExtra(
                                    "att",
                                    orderData[position].attribute.toString()
                                )
                                startActivity(intent)
                                dialog.dismiss()
                            }
                            btnTrackorder.setOnClickListener {
                                Log.e(
                                    "order_id--->",
                                    orderData[position].id.toString()
                                )
                                val intent = Intent(this@ActOrderDetails, ActTrackOrder::class.java)
                                intent.putExtra(
                                    "order_id",
                                    orderData[position].id.toString()
                                )
                                intent.putExtra(
                                    "att",
                                    orderData[position].attribute.toString()
                                )
                                intent.putExtra(
                                    "Size",
                                    binding.tvcartitemsize.text
                                )
                                startActivity(intent)
                                dialog.dismiss()
                            }
                            dialog.setCancelable(false)
                            dialog.setContentView(view)
                            dialog.show()
                        } else {
                            Common.alertErrorOrValidationDialog(
                                this@ActOrderDetails,
                                resources.getString(R.string.no_internet)
                            )
                            dialog.dismiss()
                        }
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_orderdetailsproduct
                }

                override fun getBinding(parent: ViewGroup): RowOrderdetailsproductBinding {
                    binding = RowOrderdetailsproductBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return binding
                }
            }
        orderDetailsBinding.rvorderproduct.apply {
            layoutManager =
                LinearLayoutManager(this@ActOrderDetails, LinearLayoutManager.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            adapter = orderDetailsAdpater

        }
    }


    fun cancelOrderDialog(orderId: Int) {
        val removeDialogBinding = RemoveItemDialogBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this@ActOrderDetails)
        dialog.setContentView(removeDialogBinding.root)
        removeDialogBinding.tvRemoveTitle.text = resources.getString(R.string.cancel_product)
        removeDialogBinding.tvAlertMessage.text = resources.getString(R.string.cancel_product_desc)
        removeDialogBinding.btnProceed.setOnClickListener {
            if (Common.isCheckNetwork(this@ActOrderDetails)) {
                dialog.dismiss()
                cancelOrder(orderId)

            } else {
                Common.alertErrorOrValidationDialog(
                    this@ActOrderDetails,
                    resources.getString(R.string.no_internet)
                )
            }
        }
        removeDialogBinding.ivClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    //TODO API ORDER CANCEL CALL
    private fun cancelOrder(id: Int?) {
        Common.showLoadingProgress(this@ActOrderDetails)
        val hasmap = HashMap<String, String>()
        hasmap["user_id"] =
            SharePreference.getStringPref(this@ActOrderDetails, SharePreference.userId)!!
        hasmap["order_id"] = id.toString()
        hasmap["status"] = "5"
        val call = ApiClient.getClient.getCancelOrder(hasmap)
        call.enqueue(object : Callback<SingleResponse> {
            override fun onResponse(
                call: Call<SingleResponse>,
                response: Response<SingleResponse>
            ) {
                Common.dismissLoadingProgress()
                if (response.code() == 200) {
                    if (response.body()?.status == 1) {
                        Common.isAddOrUpdated = true
                        callApiOrderDetail()
                        orderData?.let { loadOrderProductDetails(it) }
                    } else {
                        Common.showErrorFullMsg(
                            this@ActOrderDetails,
                            response.body()?.message.toString()
                        )
                    }
                } else {
                    Common.alertErrorOrValidationDialog(
                        this@ActOrderDetails,
                        resources.getString(R.string.error_msg)
                    )
                }
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@ActOrderDetails,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    //TODO SET ORDER DETAILS DATA
    @SuppressLint("SetTextI18n")
    private fun loadOrderDetails(orderDetailsList: OrderInfo) {
        if (!orderDetailsList.equals(0)) {
            orderDetailsBinding.tvorderid.visibility = View.VISIBLE
            orderDetailsBinding.tvpaymenttype.visibility = View.VISIBLE
            orderDetailsBinding.tvorderdate.visibility = View.VISIBLE
            orderDetailsBinding.tvusermailid.visibility = View.VISIBLE
            orderDetailsBinding.tvphone.visibility = View.VISIBLE
            orderDetailsBinding.tvareaaddress.visibility = View.VISIBLE
            orderDetailsBinding.tvUserName.visibility = View.VISIBLE
            orderDetailsBinding.tvsubtotal.visibility = View.VISIBLE
            orderDetailsBinding.tvtaxtotal.visibility = View.VISIBLE
            orderDetailsBinding.tvshippingtotal.visibility = View.VISIBLE
            orderDetailsBinding.tvtotal.visibility = View.VISIBLE
            orderDetailsBinding.tvdiscounttotal.visibility = View.VISIBLE
            orderDetailsBinding.tvorderid.text = orderDetailsList.orderNumber
            when (orderDetailsList.paymentType) {
                1 -> {
                    orderDetailsBinding.tvpaymenttype.text = getString(R.string.cash)
                }
                2 -> {
                    orderDetailsBinding.tvpaymenttype.text = getString(R.string.wallet)
                }
                3 -> {
                    orderDetailsBinding.tvpaymenttype.text = getString(R.string.razorpay)
                }
                4 -> {
                    orderDetailsBinding.tvpaymenttype.text = getString(R.string.stripe)
                }
                5 -> {
                    orderDetailsBinding.tvpaymenttype.text = getString(R.string.flutterwave)
                }
                6 -> {
                    orderDetailsBinding.tvpaymenttype.text = getString(R.string.paystack)
                }
            }
            orderDetailsBinding.tvorderdate.text = orderDetailsList.date?.let { Common.getDate(it) }
            orderDetailsBinding.tvusermailid.text = orderDetailsList.email
            orderDetailsBinding.tvphone.text = orderDetailsList.mobile
            if (orderDetailsList.couponName != null) {

                orderDetailsBinding.tvdiscounttitle.text =
                    getString(R.string.discount).plus("(").plus(orderDetailsList.couponName)
                        .plus(")")
            } else {
                orderDetailsBinding.tvdiscounttitle.text = getString(R.string.discount)
            }
            when {
                orderDetailsList.landmark == null -> {
                    orderDetailsBinding.tvareaaddress.text =
                        "" + orderDetailsList.streetAddress + "-" + orderDetailsList.pincode
                }
                orderDetailsList.streetAddress == null -> {
                    orderDetailsList.landmark + "-" + orderDetailsList.pincode
                }
                orderDetailsList.pincode == null -> {
                    orderDetailsList.landmark + " " + orderDetailsList.streetAddress
                }
                else -> {
                    orderDetailsBinding.tvareaaddress.text =
                        orderDetailsList.landmark + " " + orderDetailsList.streetAddress + "-" + orderDetailsList.pincode
                }
            }
            if (orderDetailsList.orderNotes == null) {
                orderDetailsBinding.clNote.visibility = View.GONE
            } else {
                orderDetailsBinding.edNote.setText(orderDetailsList.orderNotes)
            }
            orderstatus = orderDetailsList.status.toString()
            orderDetailsBinding.tvUserName.text = orderDetailsList.fullName
            orderDetailsBinding.tvsubtotal.text =
                Common.getPrice(currencyPosition, currency, orderDetailsList.subtotal.toString())
            orderDetailsBinding.tvtaxtotal.text =
                Common.getPrice(currencyPosition, currency, orderDetailsList.tax.toString())
            orderDetailsBinding.tvshippingtotal.text = Common.getPrice(
                currencyPosition,
                currency,
                orderDetailsList.shippingCost.toString()
            )
            if (orderDetailsList.discountAmount != null) {
                orderDetailsBinding.tvdiscounttotal.text = "-" + Common.getPrice(
                    currencyPosition,
                    currency,
                    orderDetailsList.discountAmount.toString()
                )
            } else {
                orderDetailsBinding.tvdiscounttotal.text =
                    Common.getPrice(currencyPosition, currency, "0.00")
            }
            orderDetailsBinding.tvtotal.text =
                Common.getPrice(currencyPosition, currency, orderDetailsList.grandTotal.toString())

        } else {
            orderDetailsBinding.tvorderid.visibility = View.GONE
            orderDetailsBinding.tvpaymenttype.visibility = View.GONE
            orderDetailsBinding.tvorderdate.visibility = View.GONE
            orderDetailsBinding.tvusermailid.visibility = View.GONE
            orderDetailsBinding.tvphone.visibility = View.GONE
            orderDetailsBinding.tvareaaddress.visibility = View.GONE
            orderDetailsBinding.tvUserName.visibility = View.GONE
            orderDetailsBinding.tvsubtotal.visibility = View.GONE
            orderDetailsBinding.tvtaxtotal.visibility = View.GONE
            orderDetailsBinding.tvshippingtotal.visibility = View.GONE
            orderDetailsBinding.tvtotal.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        callApiOrderDetail()
    }
}