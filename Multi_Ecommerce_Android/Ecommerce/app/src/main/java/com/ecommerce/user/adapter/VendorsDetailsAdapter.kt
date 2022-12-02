package com.ecommerce.user.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ecommerce.user.R
import com.ecommerce.user.ui.authentication.ActLogin
import com.ecommerce.user.ui.activity.ActProductDetails
import com.ecommerce.user.api.ApiClient
import com.ecommerce.user.api.SingleResponse
import com.ecommerce.user.model.VendorsDetailsDataItem
import com.ecommerce.user.utils.Common
import com.ecommerce.user.utils.SharePreference
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap
import kotlin.collections.ArrayList

class VendorsDetailsAdapter(
    var context: Activity,
    private val mList: ArrayList<VendorsDetailsDataItem>
) :
    RecyclerView.Adapter<VendorsDetailsAdapter.ViewHolder>() {
    var price = ""
    var currency: String = ""
    var currencyPosition: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_gravity, parent, false)
        currency = SharePreference.getStringPref(context, SharePreference.Currency)!!
        currencyPosition = SharePreference.getStringPref(
            context,
            SharePreference.CurrencyPosition
        )!!
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vendorsDetailsDataItem = mList[position]

        Glide.with(context)
            .load(vendorsDetailsDataItem.productimage?.imageUrl).into(holder.iv_cartitemm)

        holder.tvcateitemname.text = vendorsDetailsDataItem.productName


        holder.tvProductPrice.text =
            Common.getPrice(
                currencyPosition, currency,
                vendorsDetailsDataItem.productPrice!!.toString()
            )
        if (vendorsDetailsDataItem.discountedPrice == "0" || vendorsDetailsDataItem.discountedPrice==null) {
            holder.tvProductDisprice.visibility = View.GONE
        } else {
            holder.tvProductDisprice.text =
                Common.getPrice(
                    currencyPosition, currency,
                    vendorsDetailsDataItem.discountedPrice!!.toString()

                )
        }
        if (vendorsDetailsDataItem.isWishlist == 0) {
            holder.ivwishlist.setImageDrawable(
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.ic_dislike,
                    null
                )
            )
        } else {
            holder.ivwishlist.setImageDrawable(
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.ic_like,
                    null
                )
            )
        }
        holder.ivwishlist.setOnClickListener {
            if (SharePreference.getBooleanPref(context, SharePreference.isLogin)) {
                if (vendorsDetailsDataItem.isWishlist == 0) {
                    val map = HashMap<String, String>()
                    map["product_id"] =
                        vendorsDetailsDataItem.id!!.toString()
                    map["user_id"] = SharePreference.getStringPref(
                        context,
                        SharePreference.userId
                    )!!

                    if (Common.isCheckNetwork(context)) {
                        callApiFavourite(vendorsDetailsDataItem, map, position)
                    } else {
                        Common.alertErrorOrValidationDialog(
                            context,
                            context.resources.getString(R.string.no_internet)
                        )
                    }
                } else if (vendorsDetailsDataItem.isWishlist == 1) {
                    val map = HashMap<String, String>()
                    map["product_id"] =
                        vendorsDetailsDataItem.id!!.toString()
                    map["user_id"] = SharePreference.getStringPref(
                        context,
                        SharePreference.userId
                    )!!

                    if (Common.isCheckNetwork(context)) {
                        callApiRemoveFavourite(vendorsDetailsDataItem, map, position)
                    } else {
                        Common.alertErrorOrValidationDialog(
                            context,
                            context.resources.getString(R.string.no_internet)
                        )
                    }
                }
            } else {
                context.startActivity(Intent(context, ActLogin::class.java))
                context.finish()
            }
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActProductDetails::class.java)
            intent.putExtra(
                "product_id",
                vendorsDetailsDataItem.productimage?.productId.toString()
            )
            context.startActivity(intent)
        }
        if (vendorsDetailsDataItem.rattings?.size == 0) {
            holder.tvRatePro.text =
                "0.0"
        } else {
            holder.tvRatePro.text =
                vendorsDetailsDataItem.rattings?.get(0)?.avgRatting.toString()
        }
    }

    //TODO CALL API REMOVE FAVOURITE
    private fun callApiRemoveFavourite(
        vendorsDetailsDataItem: VendorsDetailsDataItem,
        map: HashMap<String, String>,
        position: Int
    ) {
        Common.showLoadingProgress(context)
        val call = ApiClient.getClient.setRemoveFromWishList(map)
        Log.e("remove-->", Gson().toJson(map))
        call.enqueue(object : Callback<SingleResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<SingleResponse>,
                response: Response<SingleResponse>
            ) {
                if (response.code() == 200) {
                    val restResponse: SingleResponse = response.body()!!
                    if (restResponse.status == 1) {

                        Common.dismissLoadingProgress()
                        vendorsDetailsDataItem.isWishlist = 0
                        notifyItemChanged(position)
                    } else if (restResponse.status == 0) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            context,
                            restResponse.message
                        )
                    }
                }
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    context,
                    context.resources.getString(R.string.error_msg)
                )
            }
        })
    }


    //TODO CALL API FAVOURITE
    private fun callApiFavourite(
        vendorsDetailsDataItem: VendorsDetailsDataItem,
        map: HashMap<String, String>,
        position: Int
    ) {
        Common.showLoadingProgress(context)
        val call = ApiClient.getClient.setAddToWishList(map)
        call.enqueue(object : Callback<SingleResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<SingleResponse>,
                response: Response<SingleResponse>
            ) {
                if (response.code() == 200) {
                    val restResponse: SingleResponse = response.body()!!
                    if (restResponse.status == 1) {
                        Common.dismissLoadingProgress()
                        vendorsDetailsDataItem.isWishlist = 1
                        notifyItemChanged(position)
                    } else if (restResponse.status == 0) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            context,
                            restResponse.message
                        )
                    }
                }
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    context,
                    context.resources.getString(R.string.error_msg)
                )
            }
        })
    }


    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tvcateitemname: TextView = itemView.findViewById(R.id.tvcateitemname)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val tvRatePro: TextView = itemView.findViewById(R.id.tvRatePro)
        val tvProductDisprice: TextView = itemView.findViewById(R.id.tvProductDisprice)
        val ivwishlist: ImageView = itemView.findViewById(R.id.ivwishlist)
        val iv_cartitemm: ImageView = itemView.findViewById(R.id.iv_cartitemm)
    }
}