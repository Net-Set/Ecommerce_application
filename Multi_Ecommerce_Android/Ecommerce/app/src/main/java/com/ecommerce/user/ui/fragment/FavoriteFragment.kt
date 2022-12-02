package com.ecommerce.user.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ecommerce.user.R
import com.ecommerce.user.ui.authentication.ActLogin
import com.ecommerce.user.ui.activity.ActProductDetails
import com.ecommerce.user.api.ApiClient
import com.ecommerce.user.api.SingleResponse
import com.ecommerce.user.base.BaseAdaptor
import com.ecommerce.user.base.BaseFragment
import com.ecommerce.user.databinding.FragFavoriteBinding
import com.ecommerce.user.databinding.RemoveItemDialogBinding
import com.ecommerce.user.databinding.RowViewallBinding
import com.ecommerce.user.model.GetWishListResponse
import com.ecommerce.user.model.WishListDataItem
import com.ecommerce.user.utils.Common
import com.ecommerce.user.utils.Common.getCurrentLanguage
import com.ecommerce.user.utils.SharePreference
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class FavoriteFragment : BaseFragment<FragFavoriteBinding>() {
    private lateinit var fragFavBinding: FragFavoriteBinding
    private var wishListDataList = ArrayList<WishListDataItem>()
    var currency: String = ""
    var currencyPosition: String = ""
    var colorArray = arrayOf(
        "#FDF7FF",
        "#FDF3F0",
        "#EDF7FD",
        "#FFFAEA",
        "#F1FFF6",
        "#FFF5EC"
    )
    private var wishListDataAdapter: BaseAdaptor<WishListDataItem, RowViewallBinding>? =
        null
    private var manager: LinearLayoutManager? = null

    private var currentPage = 1
    var total_pages: Int = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var pastVisibleItems = 0

    override fun initView(view: View) {
        fragFavBinding = FragFavoriteBinding.bind(view)
        manager =
            GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
        currency = SharePreference.getStringPref(requireActivity(), SharePreference.Currency)!!
        currencyPosition =
            SharePreference.getStringPref(requireActivity(), SharePreference.CurrencyPosition)!!
        fragFavBinding.rvwhishliast.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    visibleItemCount = manager!!.childCount
                    totalItemCount = manager!!.itemCount
                    pastVisibleItems = manager!!.findFirstVisibleItemPosition()
                    if (currentPage < total_pages) {
                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            currentPage += 1
                            if (Common.isCheckNetwork(requireActivity())) {
                                callApiWishList()
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

    override fun getBinding(): FragFavoriteBinding {
        fragFavBinding = FragFavoriteBinding.inflate(layoutInflater)
        return fragFavBinding
    }

    //TODO API WISHLIST CALL
    private fun callApiWishList() {
        Common.showLoadingProgress(requireActivity())
        val hasmap = HashMap<String, String>()
        hasmap["user_id"] =
            SharePreference.getStringPref(requireActivity(), SharePreference.userId)!!

        val call = ApiClient.getClient.getWishList(currentPage.toString(), hasmap)
        call.enqueue(object : Callback<GetWishListResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<GetWishListResponse>,
                response: Response<GetWishListResponse>
            ) {
                if (response.code() == 200) {
                    val restResponce = response.body()!!
                    if (restResponce.status == 1) {
                        Common.dismissLoadingProgress()
                        if (restResponce.allData?.data?.size ?: 0 > 0) {
                            fragFavBinding.rvwhishliast.visibility = View.VISIBLE
                            fragFavBinding.tvNoDataFound.visibility = View.GONE
                            this@FavoriteFragment.currentPage =
                                restResponce.allData?.currentPage!!.toInt()
                            total_pages = restResponce.allData.lastPage!!.toInt()
                            restResponce.allData.data?.let {
                                wishListDataList.addAll(it)
                            }
                        } else {
                            fragFavBinding.rvwhishliast.visibility = View.GONE
                            fragFavBinding.tvNoDataFound.visibility = View.VISIBLE
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

            override fun onFailure(call: Call<GetWishListResponse>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    requireActivity(),
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    //TODO featured Products Adapter
    private fun loadFeaturedProducts(
        wishListDataList: ArrayList<WishListDataItem>
    ) {
        lateinit var binding: RowViewallBinding
        wishListDataAdapter =
            object : BaseAdaptor<WishListDataItem, RowViewallBinding>(
                requireActivity(),
                wishListDataList
            ) {
                @SuppressLint("NewApi", "ResourceType", "SetTextI18n")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: WishListDataItem,
                    position: Int
                ) {

                    if (wishListDataList[position].isWishlist == 0) {
                        binding.ivwishlist.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.ic_dislike,
                                null
                            )
                        )
                    } else {
                        binding.ivwishlist.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.ic_like,
                                null
                            )
                        )
                    }

                    binding.ivwishlist.setOnClickListener {
                        if (SharePreference.getBooleanPref(
                                requireActivity(),
                                SharePreference.isLogin
                            )
                        ) {
                            if (wishListDataList[position].isWishlist == 0) {
                                val map = HashMap<String, String>()
                                map["product_id"] =
                                    wishListDataList[position].id.toString()
                                map["user_id"] = SharePreference.getStringPref(
                                    requireActivity(),
                                    SharePreference.userId
                                )!!

                                if (Common.isCheckNetwork(requireActivity())) {
                                    // callApiFavourite(map, position)
                                } else {
                                    Common.alertErrorOrValidationDialog(
                                        requireActivity(),
                                        resources.getString(R.string.no_internet)
                                    )
                                }
                            }
                            if (wishListDataList[position].isWishlist == 1) {
                                if (Common.isCheckNetwork(requireActivity())) {
                                    val map = HashMap<String, String>()
                                    map["product_id"] = wishListDataList[position].id.toString()
                                    map["user_id"] = SharePreference.getStringPref(
                                        requireActivity(),
                                        SharePreference.userId
                                    ) ?: ""
                                    callApiRemoveFavourite(map, position)

                                } else {
                                    Common.alertErrorOrValidationDialog(
                                        requireActivity(),
                                        resources.getString(R.string.no_internet)
                                    )
                                }
                            }
                        } else {
                            openActivity(ActLogin::class.java)
                            activity?.finish()
                        }
                    }

                    if (wishListDataList[position].rattings?.size == 0) {
                        binding.tvRatePro.text =
                            "0.0"
                    } else {
                        binding.tvRatePro.text =
                            wishListDataList[position].rattings?.get(0)?.avgRatting?.toDouble()
                                .toString()
                    }

                    binding.tvProductName.text = wishListDataList[position].productName

                    binding.tvProductPrice.text =
                        currency.plus(
                            String.format(
                                Locale.US,
                                "%,.2f",
                                wishListDataList[position].productPrice!!.toDouble()
                            )
                        )
                    if (wishListDataList[position].discountedPrice == "0" || wishListDataList[position].discountedPrice == null) {
                        binding.tvProductDisprice.visibility = View.GONE
                    } else {
                        binding.tvProductDisprice.text =
                            currency.plus(
                                String.format(
                                    Locale.US,
                                    "%,.2f",
                                    wishListDataList[position].discountedPrice!!.toDouble()
                                )
                            )
                    }
                    Glide.with(requireActivity())
                        .load(wishListDataList[position].productimage?.imageUrl)
                        .into(binding.ivProduct)
                    binding.ivProduct.setBackgroundColor(Color.parseColor(colorArray[position % 6]))
                    holder?.itemView?.setOnClickListener {
                        Log.e(
                            "product_id--->",
                            wishListDataList[position].productimage?.productId.toString()
                        )
                        val intent = Intent(requireActivity(), ActProductDetails::class.java)
                        intent.putExtra(
                            "product_id",
                            wishListDataList[position].productimage?.productId.toString()
                        )
                        startActivity(intent)
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_viewall
                }

                override fun getBinding(parent: ViewGroup): RowViewallBinding {
                    binding = RowViewallBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return binding
                }
            }
        if (isAdded) {
            fragFavBinding.rvwhishliast.apply {

                layoutManager =
                    manager
                itemAnimator = DefaultItemAnimator()
                adapter = wishListDataAdapter

            }
        }
    }

    //TODO CALL API REMOVE FAVOURITE
    private fun callApiRemoveFavourite(map: HashMap<String, String>, position: Int) {
        Common.showLoadingProgress(requireActivity())
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
                        wishListDataList[position].isWishlist = 0
                        wishListDataAdapter?.notifyItemRemoved(position)
                        wishListDataList.removeAt(position)
                    } else if (restResponse.status == 0) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            requireActivity(),
                            restResponse.message
                        )
                    }
                } else {
                    Common.dismissLoadingProgress()
                    Common.alertErrorOrValidationDialog(
                        requireActivity(),
                        response.body()?.message
                    )
                }
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    requireActivity(),
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    override fun onResume() {
        super.onResume()
        currentPage = 1
        wishListDataList.clear()
        loadFeaturedProducts(wishListDataList)
        if (Common.isCheckNetwork(requireActivity())) {
            if (SharePreference.getBooleanPref(requireActivity(), SharePreference.isLogin)) {
                callApiWishList()
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
        getCurrentLanguage(requireActivity(), false)
    }
}