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
import com.ecommerce.user.model.ViewAllDataItem
import com.ecommerce.user.utils.Common
import com.ecommerce.user.utils.SharePreference
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap
import kotlin.collections.ArrayList

class ViewAllAdapter(
    var context: Activity,
    private val mList: ArrayList<ViewAllDataItem>
) :
    RecyclerView.Adapter<ViewAllAdapter.ViewHolder>() {
    var price = ""
    var currency: String = ""
    var currencyPosition: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_featuredproduct, parent, false)
        currency = SharePreference.getStringPref(context, SharePreference.Currency)!!
        currencyPosition = SharePreference.getStringPref(
            context,
            SharePreference.CurrencyPosition
        )!!
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewAll = mList[position]

        Glide.with(context)
            .load(viewAll.productimage?.imageUrl).into(holder.ivProduct)

        holder.tvProductName.text = viewAll.productName


        holder.tvProductPrice.text =
            Common.getPrice(
                currencyPosition, currency,
                viewAll.productPrice!!.toString()
            )
        if (viewAll.discountedPrice == "0" || viewAll.discountedPrice == null) {
            holder.tvProductDisprice.visibility = View.GONE
        } else {
            holder.tvProductDisprice.text =
                Common.getPrice(
                    currencyPosition, currency,
                    viewAll.discountedPrice!!.toString()

                )
        }
        if (viewAll.isWishlist == 0) {
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
                if (viewAll.isWishlist == 0) {
                    val map = HashMap<String, String>()
                    map["product_id"] =
                        viewAll.id!!.toString()
                    map["user_id"] = SharePreference.getStringPref(
                        context,
                        SharePreference.userId
                    )!!

                    if (Common.isCheckNetwork(context)) {
                        callApiFavourite(viewAll, map, position)
                    } else {
                        Common.alertErrorOrValidationDialog(
                            context,
                            context.resources.getString(R.string.no_internet)
                        )
                    }
                } else if (viewAll.isWishlist == 1) {
                    val map = HashMap<String, String>()
                    map["product_id"] =
                        viewAll.id!!.toString()
                    map["user_id"] = SharePreference.getStringPref(
                        context,
                        SharePreference.userId
                    )!!

                    if (Common.isCheckNetwork(context)) {
                        callApiRemoveFavourite(viewAll, map, position)
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
                viewAll.productimage?.productId.toString()
            )
            context.startActivity(intent)
        }
        if (viewAll.rattings?.size == 0) {
            holder.tvRatePro.text =
                "0.0"
        } else {
            holder.tvRatePro.text =
                viewAll.rattings?.get(0)?.avgRatting.toString()
        }
    }

    //TODO CALL API REMOVE FAVOURITE
    private fun callApiRemoveFavourite(
        viewAll: ViewAllDataItem,
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
                        viewAll.isWishlist = 0
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
        viewAll: ViewAllDataItem,
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
                        viewAll.isWishlist = 1
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
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvRatePro: TextView = itemView.findViewById(R.id.tvRatePro)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val tvProductDisprice: TextView = itemView.findViewById(R.id.tvProductDisprice)
        val ivProduct: ImageView = itemView.findViewById(R.id.ivProduct)
        val ivwishlist: ImageView = itemView.findViewById(R.id.ivwishlist)
    }
}