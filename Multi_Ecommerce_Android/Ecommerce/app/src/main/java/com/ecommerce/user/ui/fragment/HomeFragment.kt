package com.ecommerce.user.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.ecommerce.user.R
import com.ecommerce.user.ui.activity.*
import com.ecommerce.user.api.ApiClient
import com.ecommerce.user.api.SingleResponse
import com.ecommerce.user.base.BaseAdaptor
import com.ecommerce.user.base.BaseFragment
import com.ecommerce.user.databinding.*
import com.ecommerce.user.model.*
import com.ecommerce.user.ui.authentication.ActLogin
import com.ecommerce.user.utils.Common
import com.ecommerce.user.utils.Common.alertErrorOrValidationDialog
import com.ecommerce.user.utils.Common.dismissLoadingProgress
import com.ecommerce.user.utils.Common.getCurrentLanguage
import com.ecommerce.user.utils.Common.isCheckNetwork
import com.ecommerce.user.utils.Common.showLoadingProgress
import com.ecommerce.user.utils.SharePreference
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class HomeFragment : BaseFragment<FragHomeBinding>() {
    private lateinit var fragHomeBinding: FragHomeBinding
    private var bannerList: ArrayList<TopbannerItem>? = null
    private var leftbannerList: ArrayList<LeftbannerItem>? = null
    private var bottombannerList: ArrayList<BottombannerItem>? = null
    private var featuredProductsList: ArrayList<FeaturedProductsItem>? = null
    private var hotdealsList: ArrayList<HotProductsItem>? = null
    private var newProductsList: ArrayList<NewProductsItem>? = null
    private var largebannerList: ArrayList<LargebannerItem>? = null
    private var categoriesList: ArrayList<DataItem>? = null
    private var featuredProductsAdapter: BaseAdaptor<FeaturedProductsItem, RowFeaturedproductBinding>? =
        null
    private var newProductsAdaptor: BaseAdaptor<NewProductsItem, RowFeaturedproductBinding>? =
        null
    private var hotdealsAdaptor: BaseAdaptor<HotProductsItem, RowFeaturedproductBinding>? =
        null
    var isAPICalling: Boolean = false
    var colorArray = arrayOf(
        "#FDF7FF",
        "#FDF3F0",
        "#EDF7FD",
        "#FFFAEA",
        "#F1FFF6",
        "#FFF5EC"
    )

    override fun initView(view: View) {
        fragHomeBinding = FragHomeBinding.bind(view)
        init()
        val snapHelper = PagerSnapHelper()
        fragHomeBinding.rvBannerproduct.let { snapHelper.attachToRecyclerView(it) }
        fragHomeBinding.rvBanner.let { snapHelper.attachToRecyclerView(it) }
        fragHomeBinding.rvNewBanner.let { snapHelper.attachToRecyclerView(it) }
        fragHomeBinding.rvHotDealsBanner.let { snapHelper.attachToRecyclerView(it) }
    }

    private fun init() {
        fragHomeBinding.tvViewAllCate.setOnClickListener {
            openActivity(
                ActAllCategories::class.java
            )
        }
        fragHomeBinding.tvViewAllfp.setOnClickListener {
            val intent = Intent(requireActivity(), ActViewAll::class.java)
            intent.putExtra("title", fragHomeBinding.tvfeaturedproduct.text.toString())
            startActivity(intent)
        }
        fragHomeBinding.tvViewAllvendors.setOnClickListener {
            openActivity(ActVendors::class.java)
        }
        fragHomeBinding.tvViewArrivals.setOnClickListener {
            val intent = Intent(requireActivity(), ActViewAll::class.java)
            intent.putExtra("title", fragHomeBinding.tvnewArrivals.text.toString())
            startActivity(intent)
        }
        fragHomeBinding.tvViewAllhotdeals.setOnClickListener {
            val intent = Intent(requireActivity(), ActViewAll::class.java)
            intent.putExtra("title", fragHomeBinding.tvHotDeals.text.toString())
            startActivity(intent)
        }

        fragHomeBinding.tvViewAllBrand.setOnClickListener { openActivity(ActBrand::class.java) }

        fragHomeBinding.ivnotification.setOnClickListener { openActivity(ActNotification::class.java) }

        fragHomeBinding.ivSearch.setOnClickListener { openActivity(ActSearch::class.java) }
    }

    override fun getBinding(): FragHomeBinding {
        fragHomeBinding = FragHomeBinding.inflate(layoutInflater)
        return fragHomeBinding
    }

    //TODO FirstBanner
    private fun callApiBanner() {
        showLoadingProgress(requireActivity())
        val call = ApiClient.getClient.getbanner()
        call.enqueue(object : Callback<BannerResponse> {
            override fun onResponse(
                call: Call<BannerResponse>,
                response: Response<BannerResponse>
            ) {
                if (response.code() == 200) {
                    val restResponce = response.body()!!
                    dismissLoadingProgress()
                    if (restResponce.status == 1) {
                        fragHomeBinding.rvBanner.visibility = View.VISIBLE
                        fragHomeBinding.rvBannerproduct.visibility = View.VISIBLE
                        fragHomeBinding.rvBrandBanner.visibility = View.VISIBLE
                        fragHomeBinding.view.visibility = View.VISIBLE
                        fragHomeBinding.tvNoDataFound.visibility = View.GONE
                        bannerList = restResponce.topbanner
                        bottombannerList = restResponce.bottombanner
                        leftbannerList = restResponce.leftbanner
                        largebannerList = restResponce.largebanner
                        if (isAdded) {
                            restResponce.sliders?.let { loadPagerImagesSliders(it) }
                            callCategories()
                        }
                    } else if (restResponce.status == 0) {
                        fragHomeBinding.rvBanner.visibility = View.GONE
                        fragHomeBinding.tvNoDataFound.visibility = View.VISIBLE
                        fragHomeBinding.rvBannerproduct.visibility = View.GONE
                        fragHomeBinding.rvBrandBanner.visibility = View.GONE
                        fragHomeBinding.view.visibility = View.GONE
                        fragHomeBinding.view7.visibility = View.GONE
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            requireActivity(),
                            restResponce.message.toString()
                        )
                    }
                } else {
                    if (isAdded) {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            requireActivity(),
                            resources.getString(R.string.error_msg)
                        )
                    }
                }
            }

            override fun onFailure(call: Call<BannerResponse>, t: Throwable) {
                if (isAdded) {
                    dismissLoadingProgress()
                    alertErrorOrValidationDialog(
                        requireActivity(),
                        resources.getString(R.string.error_msg)
                    )
                }

            }
        })
    }

    //TODO first banner
    private fun loadPagerImagesSliders(slidersList: ArrayList<SlidersItem>) {
        lateinit var binding: RowBannerBinding
        val bannerAdapter = object :
            BaseAdaptor<SlidersItem, RowBannerBinding>(requireActivity(), slidersList) {
            @SuppressLint("NewApi", "ResourceType")
            override fun onBindData(
                holder: RecyclerView.ViewHolder?,
                `val`: SlidersItem,
                position: Int
            ) {
                Glide.with(requireActivity()).load(slidersList[position].imageUrl)
                    .into(binding.ivBanner)
                binding.ivBanner.setBackgroundColor(Color.parseColor(colorArray[position % 6]))

                holder?.itemView?.setOnClickListener {
                    val webpage: Uri = Uri.parse(slidersList[position].link)
                    val intent = Intent(Intent.ACTION_VIEW, webpage)
                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(intent)
                    }
                }
            }

            override fun setItemLayout(): Int {
                return R.layout.row_banner
            }

            override fun getBinding(parent: ViewGroup): RowBannerBinding {
                binding = RowBannerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return binding
            }
        }

        fragHomeBinding.rvBanner.apply {
            layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            itemAnimator = DefaultItemAnimator()
            adapter = bannerAdapter
        }
    }


    //TODO SecondBanner Images
    private fun loadPagerImages(topbannerList: ArrayList<TopbannerItem>) {
        lateinit var binding: RowBannerproductBinding
        val bannerAdapter = object :
            BaseAdaptor<TopbannerItem, RowBannerproductBinding>(requireActivity(), topbannerList) {
            @SuppressLint("NewApi", "ResourceType")
            override fun onBindData(
                holder: RecyclerView.ViewHolder?,
                `val`: TopbannerItem,
                position: Int
            ) {
                val data = topbannerList[position]
                binding.ivBanner.setBackgroundColor(Color.parseColor(colorArray[position % 6]))

                Glide.with(requireActivity()).load(data.imageUrl)
                    .into(binding.ivBanner)
                holder?.itemView?.setOnClickListener {
                    typeWiseNavigation(
                        data.type.toString(),
                        data.catId.toString(),
                        data.categoryName.toString(),
                        data.productId.toString()
                    )
                }
            }

            override fun setItemLayout(): Int {
                return R.layout.row_bannerproduct
            }

            override fun getBinding(parent: ViewGroup): RowBannerproductBinding {
                binding = RowBannerproductBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return binding
            }
        }

        if (isAdded) {
            fragHomeBinding.rvBannerproduct.apply {
                layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                itemAnimator = DefaultItemAnimator()
                adapter = bannerAdapter
            }
        }
    }

    fun typeWiseNavigation(type: String, catId: String, catName: String, productId: String) {
        if (type == "category") {
            val intent = Intent(requireActivity(), ActAllSubCategories::class.java)
            val extras = Bundle()
            extras.putString("cat_id", catId.toString())
            extras.putString(
                "categoryName",
                catName
            )
            intent.putExtras(extras)
            startActivity(intent)
        } else if (type == "product") {
            val intent = Intent(requireActivity(), ActProductDetails::class.java)
            intent.putExtra("product_id", productId)
            startActivity(intent)
        }
    }

    //TODO categories api call
    private fun callCategories() {
        val call = ApiClient.getClient.getcategory()
        call.enqueue(object : Callback<CategoriesResponse> {
            override fun onResponse(
                call: Call<CategoriesResponse>,
                response: Response<CategoriesResponse>
            ) {
                if (response.code() == 200) {
                    val restResponce = response.body()!!
                    if (restResponce.status == 1) {
                        dismissLoadingProgress()
                        categoriesList = restResponce.data
                        if (isAdded) {
                            if (categoriesList?.size ?: 0 > 0) {

                                fragHomeBinding.rvCategories.visibility = View.VISIBLE
                                fragHomeBinding.tvCategories.visibility = View.VISIBLE
                                fragHomeBinding.tvViewAllCate.visibility = View.VISIBLE
                                fragHomeBinding.view1.visibility = View.VISIBLE
                                loadCategoriesDeals(categoriesList!!)
                                callApiFeaturedProducts()
                            } else {
                                fragHomeBinding.rvCategories.visibility = View.GONE
                                fragHomeBinding.tvCategories.visibility = View.GONE
                                fragHomeBinding.tvViewAllCate.visibility = View.GONE
                                fragHomeBinding.view1.visibility = View.GONE
                            }

                        }

                    } else if (restResponce.status == 0) {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            requireActivity(),
                            restResponce.message.toString()
                        )
                    }
                } else {
                    if (isAdded) {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            requireActivity(),
                            resources.getString(R.string.error_msg)
                        )
                    }
                }
            }

            override fun onFailure(call: Call<CategoriesResponse>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    requireActivity(),
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    //TODO categories Adapter
    private fun loadCategoriesDeals(categoriesList: ArrayList<DataItem>) {
        lateinit var binding: RowCategoriesBinding
        val categoriesAdaptor =
            object : BaseAdaptor<DataItem, RowCategoriesBinding>(
                requireActivity(),
                categoriesList
            ) {
                @SuppressLint("NewApi", "ResourceType")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: DataItem,
                    position: Int
                ) {

                    binding.tvCategoriesName.text = categoriesList[position].categoryName

                    Glide.with(requireActivity())
                        .load(categoriesList[position].imageUrl).into(binding.ivCategories)
                    binding.clMain.setBackgroundColor(Color.parseColor(colorArray[position % 6]))
                    binding.ivCategories.setBackgroundColor(Color.parseColor(colorArray[position % 6]))
                    holder?.itemView?.setOnClickListener {
                        Log.e("cat_id--->", categoriesList[position].id.toString())
                        val intent = Intent(requireActivity(), ActAllSubCategories::class.java)
                        val extras = Bundle()
                        extras.putString("cat_id", categoriesList[position].id.toString() + "")
                        extras.putString(
                            "categoryName",
                            categoriesList[position].categoryName.toString() + ""
                        )
                        intent.putExtras(extras)
                        startActivity(intent)
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_categories
                }

                override fun getBinding(parent: ViewGroup): RowCategoriesBinding {
                    binding = RowCategoriesBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return binding
                }
            }
        if (isAdded) {
            fragHomeBinding.rvCategories.apply {
                layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                itemAnimator = DefaultItemAnimator()
                adapter = categoriesAdaptor
            }
        }
    }

    //TODO Api HomeFeed call
    private fun callApiFeaturedProducts() {
        val hasmap = HashMap<String, String>()
        hasmap["user_id"] =
            SharePreference.getStringPref(requireActivity(), SharePreference.userId)!!
        val call = ApiClient.getClient.gethomefeeds(hasmap)
        call.enqueue(object : Callback<HomefeedResponse> {
            override fun onResponse(
                call: Call<HomefeedResponse>,
                response: Response<HomefeedResponse>
            ) {
                if (response.code() == 200) {
                    val restResponce = response.body()!!
                    if (restResponce.status == 1) {
                        featuredProductsList = restResponce.featuredProducts
                        newProductsList = restResponce.newProducts
                        hotdealsList = restResponce.hotProducts
                        if (isAdded) {
                            dismissLoadingProgress()
                            activity?.let {
                                SharePreference.setStringPref(
                                    it,
                                    SharePreference.Currency,
                                    restResponce.currency.toString()
                                )
                            }
                            activity?.let {
                                SharePreference.setStringPref(
                                    it,
                                    SharePreference.CurrencyPosition,
                                    restResponce.currencyPosition.toString()
                                )
                            }
                            activity?.let { SharePreference.setStringPref(
                                it,
                                SharePreference.ReferralAmount,
                                restResponce.referralAmount.toString()
                            ) }
                            if ((bannerList?.size ?: 0) > 0) {
                                bannerList?.let { loadPagerImages(it) }
                                fragHomeBinding.rvBannerproduct.visibility = View.VISIBLE
                                fragHomeBinding.view2.visibility = View.VISIBLE
                            } else {
                                fragHomeBinding.rvBannerproduct.visibility = View.GONE
                                fragHomeBinding.view2.visibility = View.GONE
                            }

                            if ((featuredProductsList?.size ?: 0) > 0) {
                                loadFeaturedProducts(
                                    featuredProductsList!!,
                                    restResponce.currency,
                                    restResponce.currencyPosition
                                )
                                fragHomeBinding.rvfeaturedproduct.visibility = View.VISIBLE
                                fragHomeBinding.tvfeaturedproduct.visibility = View.VISIBLE
                                fragHomeBinding.tvViewAllfp.visibility = View.VISIBLE
                                fragHomeBinding.view1.visibility = View.VISIBLE
                            } else {
                                fragHomeBinding.rvfeaturedproduct.visibility = View.GONE
                                fragHomeBinding.tvfeaturedproduct.visibility = View.GONE
                                fragHomeBinding.tvViewAllfp.visibility = View.GONE
                                fragHomeBinding.view1.visibility = View.GONE
                            }

                            if ((restResponce.vendors?.size ?: 0) > 0) {
                                loadVendors(restResponce.vendors!!)
                                fragHomeBinding.rvvendors.visibility = View.VISIBLE
                                fragHomeBinding.tvvendors.visibility = View.VISIBLE
                                fragHomeBinding.tvViewAllvendors.visibility = View.VISIBLE
                                fragHomeBinding.view3.visibility = View.VISIBLE
                            } else {
                                fragHomeBinding.rvvendors.visibility = View.GONE
                                fragHomeBinding.tvvendors.visibility = View.GONE
                                fragHomeBinding.tvViewAllvendors.visibility = View.GONE
                                fragHomeBinding.view3.visibility = View.GONE
                            }
                            if ((newProductsList?.size ?: 0) > 0) {
                                loadNewProduct(
                                    restResponce.newProducts!!,
                                    restResponce.currency,
                                    restResponce.currencyPosition
                                )
                                fragHomeBinding.tvnewArrivals.visibility = View.VISIBLE
                                fragHomeBinding.tvViewArrivals.visibility = View.VISIBLE
                                fragHomeBinding.rvnewArrivals.visibility = View.VISIBLE
                                fragHomeBinding.view4.visibility = View.VISIBLE
                            } else {
                                fragHomeBinding.rvnewArrivals.visibility = View.GONE
                                fragHomeBinding.tvnewArrivals.visibility = View.GONE
                                fragHomeBinding.tvViewArrivals.visibility = View.GONE
                                fragHomeBinding.view4.visibility = View.GONE
                            }
                            if ((bottombannerList?.size ?: 0) > 0) {
                                bottombannerList?.let { loadPagerImagesBottomBanner(it) }
                                fragHomeBinding.rvNewBanner.visibility = View.VISIBLE
                                fragHomeBinding.view5.visibility = View.VISIBLE
                            } else {
                                fragHomeBinding.rvNewBanner.visibility = View.GONE
                                fragHomeBinding.view5.visibility = View.GONE
                            }
                            if ((leftbannerList?.size ?: 0) > 0) {
                                leftbannerList?.let { loadPagerImagesLeftBanner(it) }
                                fragHomeBinding.rvBrandBanner.visibility = View.VISIBLE
                            } else {
                                fragHomeBinding.rvBrandBanner.visibility = View.GONE
                            }
                            if ((restResponce.brands?.size ?: 0) > 0) {
                                loadBrands(restResponce.brands!!)
                                fragHomeBinding.rvBrand.visibility = View.VISIBLE
                                fragHomeBinding.tvBrand.visibility = View.VISIBLE
                                fragHomeBinding.tvViewAllBrand.visibility = View.VISIBLE
                                fragHomeBinding.view6.visibility = View.VISIBLE
                            } else {
                                fragHomeBinding.rvBrand.visibility = View.GONE
                                fragHomeBinding.tvBrand.visibility = View.GONE
                                fragHomeBinding.tvViewAllBrand.visibility = View.GONE
                                fragHomeBinding.view6.visibility = View.GONE
                            }

                            if ((hotdealsList?.size ?: 0) > 0) {
                                loadHotDeals(
                                    restResponce.hotProducts!!,
                                    restResponce.currency,
                                    restResponce.currencyPosition
                                )
                                fragHomeBinding.rvHotDeals.visibility = View.VISIBLE
                                fragHomeBinding.tvHotDeals.visibility = View.VISIBLE
                                fragHomeBinding.tvViewAllhotdeals.visibility = View.VISIBLE
                                fragHomeBinding.view7.visibility = View.VISIBLE
                            } else {
                                fragHomeBinding.rvHotDeals.visibility = View.GONE
                                fragHomeBinding.tvHotDeals.visibility = View.GONE
                                fragHomeBinding.tvViewAllhotdeals.visibility = View.GONE
                                fragHomeBinding.view7.visibility = View.GONE
                            }
                            if ((largebannerList?.size ?: 0) > 0) {
                                largebannerList?.let { loadPagerImagesLargeBanner(it) }
                                fragHomeBinding.rvHotDealsBanner.visibility = View.VISIBLE
                            } else {
                                fragHomeBinding.rvHotDealsBanner.visibility = View.GONE
                            }
                            if (restResponce.notifications == 1) {
                                fragHomeBinding.rlCountnotification.visibility = View.VISIBLE
                            } else {
                                fragHomeBinding.rlCountnotification.visibility = View.GONE
                            }
                        }
                    } else if (restResponce.status == 0) {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            requireActivity(),
                            restResponce.message.toString()
                        )
                    }

                } else {
                    if (isAdded) {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            requireActivity(),
                            resources.getString(R.string.error_msg)
                        )
                    }
                }
            }

            override fun onFailure(call: Call<HomefeedResponse>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    requireActivity(),
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    //TODO featured Products Adapter
    private fun loadFeaturedProducts(
        featuredProductsList: ArrayList<FeaturedProductsItem>,
        currency: String?,
        currencyPosition: String?
    ) {
        lateinit var binding: RowFeaturedproductBinding
        featuredProductsAdapter =
            object : BaseAdaptor<FeaturedProductsItem, RowFeaturedproductBinding>(
                requireActivity(),
                featuredProductsList
            ) {
                @SuppressLint("NewApi", "ResourceType", "SetTextI18n")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: FeaturedProductsItem,
                    position: Int
                ) {
                    if (featuredProductsList.get(position).isWishlist == 0) {
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
                            if (featuredProductsList[position].isWishlist == 0) {
                                val map = HashMap<String, String>()
                                map["product_id"] =
                                    featuredProductsList[position].id.toString()
                                map["user_id"] = SharePreference.getStringPref(
                                    requireActivity(),
                                    SharePreference.userId
                                )!!

                                if (isCheckNetwork(requireActivity())) {
                                    callApiFavourite(map, position, "1")
                                } else {
                                    alertErrorOrValidationDialog(
                                        requireActivity(),
                                        resources.getString(R.string.no_internet)
                                    )
                                }
                            }

                            if (featuredProductsList[position].isWishlist == 1) {
                                val map = HashMap<String, String>()
                                map["product_id"] =
                                    featuredProductsList[position].id.toString()
                                map["user_id"] = SharePreference.getStringPref(
                                    requireActivity(),
                                    SharePreference.userId
                                )!!

                                if (isCheckNetwork(requireActivity())) {
                                    callApiRemoveFavourite(map, position, "1")
                                } else {
                                    alertErrorOrValidationDialog(
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
                    if (featuredProductsList[position].rattings?.size == 0) {
                        binding.tvRatePro.text =
                            "0.0"
                    } else {
                        binding.tvRatePro.text =
                            featuredProductsList[position].rattings?.get(0)?.avgRatting?.toString()
                    }

                    binding.tvProductName.text = featuredProductsList[position].productName

                    binding.tvProductPrice.text =
                        Common.getPrice(
                            currencyPosition!!, currency!!,
                            featuredProductsList[position].productPrice!!.toString()
                        )
                    if (featuredProductsList[position].discountedPrice == "0" || featuredProductsList[position].discountedPrice == null) {
                        binding.tvProductDisprice.visibility = View.GONE
                    } else {
                        binding.tvProductDisprice.text =
                            Common.getPrice(
                                currencyPosition, currency,
                                featuredProductsList[position].discountedPrice!!.toString()
                            )
                    }
                    Glide.with(requireActivity())
                        .load(featuredProductsList[position].productimage?.imageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade(1000))
                        .into(binding.ivProduct)
                    binding.ivProduct.setBackgroundColor(Color.parseColor(colorArray[position % 6]))
                    holder?.itemView?.setOnClickListener {
                        Log.e("product_id--->", featuredProductsList[position].id.toString())
                        val intent = Intent(requireActivity(), ActProductDetails::class.java)
                        intent.putExtra("product_id", featuredProductsList[position].id.toString())
                        startActivity(intent)
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_featuredproduct
                }

                override fun getBinding(parent: ViewGroup): RowFeaturedproductBinding {
                    binding = RowFeaturedproductBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return binding
                }
            }
        if (isAdded) {
            fragHomeBinding.rvfeaturedproduct.apply {
                layoutManager =
                    GridLayoutManager(requireContext(), 1, GridLayoutManager.HORIZONTAL, false)
                fragHomeBinding.rvfeaturedproduct.isNestedScrollingEnabled = true
                itemAnimator = DefaultItemAnimator()
                adapter = featuredProductsAdapter
            }
        }
    }

    //TODO SecondBanner Images
    private fun loadPagerImagesBottomBanner(bottombannerList: ArrayList<BottombannerItem>) {
        lateinit var binding: RowBannernewBinding
        val bottombannerAdaptor =
            object : BaseAdaptor<BottombannerItem, RowBannernewBinding>(
                requireActivity(),
                bottombannerList
            ) {
                @SuppressLint("NewApi", "ResourceType")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: BottombannerItem,
                    position: Int
                ) {
                    val data = bottombannerList[position]
                    Glide.with(requireActivity()).load(bottombannerList[position].imageUrl)
                        .into(binding.ivBanner)
                    binding.ivBanner.setBackgroundColor(Color.parseColor(colorArray[position % 6]))
                    holder?.itemView?.setOnClickListener {
                        typeWiseNavigation(
                            data.type.toString(),
                            data.catId.toString(),
                            data.categoryName.toString(),
                            data.productId.toString()
                        )
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_bannernew
                }

                override fun getBinding(parent: ViewGroup): RowBannernewBinding {
                    binding = RowBannernewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return binding
                }
            }
        if (isAdded) {
            fragHomeBinding.rvNewBanner.apply {
                layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                itemAnimator = DefaultItemAnimator()
                adapter = bottombannerAdaptor
            }
        }
    }

    //TODO Vendors Adapter
    private fun loadVendors(vendorsList: ArrayList<VendorsItem>) {
        lateinit var binding: RowVendorsBinding
        val vendorsAdapter =
            object : BaseAdaptor<VendorsItem, RowVendorsBinding>(requireActivity(), vendorsList) {
                @SuppressLint("NewApi", "ResourceType")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: VendorsItem,
                    position: Int
                ) {
                    binding.tvVendorsName.text = vendorsList[position].name
                    Glide.with(requireActivity()).load(vendorsList[position].imageUrl)
                        .into(binding.ivvendors)
                    holder?.itemView?.setOnClickListener {
                        Log.e("vendor_id--->", vendorsList[position].id.toString())
                        val intent = Intent(requireActivity(), ActVendorsDetails::class.java)
                        intent.putExtra("vendor_id", vendorsList[position].id.toString())
                        intent.putExtra("vendors_name", vendorsList[position].name.toString())
                        intent.putExtra("vendors_iv", vendorsList[position].imageUrl.toString())
                        intent.putExtra("vendors_rate", "0.0")
                        startActivity(intent)
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_vendors
                }

                override fun getBinding(parent: ViewGroup): RowVendorsBinding {
                    binding = RowVendorsBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return binding
                }
            }
        if (isAdded) {
            fragHomeBinding.rvvendors.apply {
                layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                itemAnimator = DefaultItemAnimator()
                adapter = vendorsAdapter
            }
        }
    }

    //TODO New Product Adapter
    private fun loadNewProduct(
        newProductsList: ArrayList<NewProductsItem>,
        currency: String?,
        currencyPosition: String?
    ) {
        lateinit var binding: RowFeaturedproductBinding
        newProductsAdaptor =
            object : BaseAdaptor<NewProductsItem, RowFeaturedproductBinding>(
                requireActivity(),
                newProductsList
            ) {
                @SuppressLint("NewApi", "ResourceType", "SetTextI18n")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: NewProductsItem,
                    position: Int
                ) {
                    if (newProductsList[position].rattings?.size == 0) {
                        binding.tvRatePro.text =
                            "0.0"
                    } else {
                        binding.tvRatePro.text =
                            newProductsList[position].rattings?.get(0)?.avgRatting?.toString()
                    }

                    if (newProductsList[position].isWishlist == 0) {
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
                            if (newProductsList[position].isWishlist == 0) {
                                val map = HashMap<String, String>()
                                map["product_id"] =
                                    newProductsList[position].id.toString()
                                map["user_id"] = SharePreference.getStringPref(
                                    requireActivity(),
                                    SharePreference.userId
                                )!!
                                if (isCheckNetwork(requireActivity())) {
                                    callApiFavourite(map, position, "2")
                                } else {
                                    alertErrorOrValidationDialog(
                                        requireActivity(),
                                        resources.getString(R.string.no_internet)
                                    )
                                }
                            }

                            if (newProductsList[position].isWishlist == 1) {
                                val map = HashMap<String, String>()
                                map["product_id"] =
                                    newProductsList[position].id.toString()
                                map["user_id"] = SharePreference.getStringPref(
                                    requireActivity(),
                                    SharePreference.userId
                                )!!
                                if (isCheckNetwork(requireActivity())) {
                                    callApiRemoveFavourite(map, position, "2")
                                } else {
                                    alertErrorOrValidationDialog(
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

                    binding.tvProductName.text = newProductsList[position].productName

                    binding.tvProductPrice.text =
                        Common.getPrice(
                            currencyPosition!!, currency!!,
                            newProductsList[position].productPrice!!.toString()
                        )
                    if (newProductsList[position].discountedPrice == "0" || newProductsList[position].discountedPrice == null) {
                        binding.tvProductDisprice.visibility = View.GONE
                    } else {
                        binding.tvProductDisprice.text =
                            Common.getPrice(
                                currencyPosition, currency,
                                newProductsList[position].discountedPrice!!.toString()
                            )
                    }
                    Glide.with(requireActivity())
                        .load(newProductsList[position].productimage?.imageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade(1000))
                        .into(binding.ivProduct)
                    binding.ivProduct.setBackgroundColor(Color.parseColor(colorArray[position % 6]))
                    holder?.itemView?.setOnClickListener {
                        Log.e("product_id--->", newProductsList[position].id.toString())
                        val intent = Intent(requireActivity(), ActProductDetails::class.java)
                        intent.putExtra("product_id", newProductsList[position].id.toString())
                        startActivity(intent)
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_featuredproduct
                }

                override fun getBinding(parent: ViewGroup): RowFeaturedproductBinding {
                    binding = RowFeaturedproductBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return binding
                }
            }
        if (isAdded) {
            fragHomeBinding.rvnewArrivals.apply {
                layoutManager =
                    GridLayoutManager(requireContext(), 1, GridLayoutManager.HORIZONTAL, false)
                itemAnimator = DefaultItemAnimator()
                adapter = newProductsAdaptor
            }
        }
    }


    //TODO ThirdBanner Images
    private fun loadPagerImagesLeftBanner(leftbannerList: ArrayList<LeftbannerItem>) {
        lateinit var binding: RowBannerleftBinding
        val leftbannerAdaptor =
            object : BaseAdaptor<LeftbannerItem, RowBannerleftBinding>(
                requireActivity(),
                leftbannerList
            ) {
                @SuppressLint("NewApi", "ResourceType")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: LeftbannerItem,
                    position: Int
                ) {
                    val data = leftbannerList[position]
                    Glide.with(requireActivity()).load(leftbannerList[position].imageUrl)
                        .into(binding.ivBanner)
                    binding.ivBanner.setBackgroundColor(Color.parseColor(colorArray[position % 6]))
                    holder?.itemView?.setOnClickListener {
                        typeWiseNavigation(
                            data.type.toString(),
                            data.catId.toString(),
                            data.categoryName.toString(),
                            data.productId.toString()
                        )
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_bannerleft
                }

                override fun getBinding(parent: ViewGroup): RowBannerleftBinding {
                    binding = RowBannerleftBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return binding
                }
            }
        if (isAdded) {
            fragHomeBinding.rvBrandBanner.apply {
                layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                itemAnimator = DefaultItemAnimator()
                adapter = leftbannerAdaptor
            }
        }
    }

    //TODO Brand Adapter
    private fun loadBrands(brandsList: ArrayList<BrandsItem>) {
        lateinit var binding: RowBrandBinding
        val brandsAdaptor =
            object : BaseAdaptor<BrandsItem, RowBrandBinding>(
                requireActivity(),
                brandsList
            ) {
                @SuppressLint("NewApi", "ResourceType")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: BrandsItem,
                    position: Int
                ) {
                    Glide.with(requireActivity())
                        .load(brandsList[position].imageUrl).into(binding.ivBrands)
                    binding.clMain.setBackgroundColor(Color.parseColor(colorArray[position % 6]))
                    binding.ivBrands.setBackgroundColor(Color.parseColor(colorArray[position % 6]))
                    holder?.itemView?.setOnClickListener {
                        Log.e("brand_id--->", brandsList[position].id.toString())
                        val intent = Intent(requireActivity(), ActBrandDetails::class.java)
                        intent.putExtra("brand_id", brandsList[position].id.toString())
                        intent.putExtra("brand_name", brandsList[position].brandName.toString())
                        startActivity(intent)
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_brand
                }

                override fun getBinding(parent: ViewGroup): RowBrandBinding {
                    binding = RowBrandBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return binding
                }
            }

        if (isAdded) {
            fragHomeBinding.rvBrand.apply {
                layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                itemAnimator = DefaultItemAnimator()
                adapter = brandsAdaptor
            }
        }
    }

    //TODO Hot Deals Adapter
    private fun loadHotDeals(
        hotdealsList: ArrayList<HotProductsItem>,
        currency: String?,
        currencyPosition: String?
    ) {
        lateinit var binding: RowFeaturedproductBinding
        hotdealsAdaptor =
            object : BaseAdaptor<HotProductsItem, RowFeaturedproductBinding>(
                requireActivity(),
                hotdealsList
            ) {
                @SuppressLint("NewApi", "ResourceType", "SetTextI18n")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: HotProductsItem,
                    position: Int
                ) {
                    if (hotdealsList[position].rattings?.size == 0) {
                        binding.tvRatePro.text =
                            "0.0"
                    } else {
                        binding.tvRatePro.text =
                            hotdealsList[position].rattings?.get(0)?.avgRatting?.toString()
                    }
                    if (hotdealsList[position].isWishlist == 0) {
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
                            if (hotdealsList[position].isWishlist == 0) {
                                val map = HashMap<String, String>()
                                map["product_id"] =
                                    hotdealsList[position].id.toString()
                                map["user_id"] = SharePreference.getStringPref(
                                    requireActivity(),
                                    SharePreference.userId
                                )!!
                                if (isCheckNetwork(requireActivity())) {
                                    callApiFavourite(map, position, "3")
                                } else {
                                    alertErrorOrValidationDialog(
                                        requireActivity(),
                                        resources.getString(R.string.no_internet)
                                    )
                                }
                            }

                            if (hotdealsList[position].isWishlist == 1) {
                                val map = HashMap<String, String>()
                                map["product_id"] =
                                    hotdealsList[position].id.toString()
                                map["user_id"] = SharePreference.getStringPref(
                                    requireActivity(),
                                    SharePreference.userId
                                )!!
                                if (isCheckNetwork(requireActivity())) {
                                    callApiRemoveFavourite(map, position, "3")
                                } else {
                                    alertErrorOrValidationDialog(
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
                    binding.tvProductName.text = hotdealsList[position].productName

                    binding.tvProductPrice.text =
                        Common.getPrice(
                            currencyPosition!!, currency!!,
                            hotdealsList[position].productPrice!!.toString()
                        )
                    if (hotdealsList[position].discountedPrice == "0" || hotdealsList[position].discountedPrice == null) {
                        binding.tvProductDisprice.visibility = View.GONE
                    } else {
                        binding.tvProductDisprice.text =
                            Common.getPrice(
                                currencyPosition, currency,
                                hotdealsList[position].discountedPrice!!.toString()
                            )
                    }
                    Glide.with(requireActivity())
                        .load(hotdealsList[position].productimage?.imageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade(1000))
                        .into(binding.ivProduct)
                    binding.ivProduct.setBackgroundColor(Color.parseColor(colorArray[position % 6]))
                    holder?.itemView?.setOnClickListener {
                        Log.e("product_id--->", hotdealsList[position].id.toString())
                        val intent = Intent(requireActivity(), ActProductDetails::class.java)
                        intent.putExtra("product_id", hotdealsList[position].id.toString())
                        startActivity(intent)
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_featuredproduct
                }

                override fun getBinding(parent: ViewGroup): RowFeaturedproductBinding {
                    binding = RowFeaturedproductBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return binding
                }
            }
        if (isAdded) {
            fragHomeBinding.rvHotDeals.apply {
                layoutManager =
                    GridLayoutManager(requireContext(), 1, GridLayoutManager.HORIZONTAL, false)
                itemAnimator = DefaultItemAnimator()
                adapter = hotdealsAdaptor
            }
        }
    }

    //TODO LastBanner Images
    private fun loadPagerImagesLargeBanner(largebannerList: ArrayList<LargebannerItem>) {
        lateinit var binding: RowHotdealsbannerBinding
        val largebannerAdaptor =
            object : BaseAdaptor<LargebannerItem, RowHotdealsbannerBinding>(
                requireActivity(),
                largebannerList
            ) {
                @SuppressLint("NewApi", "ResourceType")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: LargebannerItem,
                    position: Int
                ) {
                    val data = largebannerList[position]
                    Glide.with(requireActivity()).load(largebannerList[position].imageUrl)
                        .into(binding.ivBanner)
                    binding.ivBanner.setBackgroundColor(Color.parseColor(colorArray[position % 6]))
                    holder?.itemView?.setOnClickListener {
                        typeWiseNavigation(
                            data.type.toString(),
                            data.catId.toString(),
                            data.categoryName.toString(),
                            data.productId.toString()
                        )
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_hotdealsbanner
                }

                override fun getBinding(parent: ViewGroup): RowHotdealsbannerBinding {
                    binding = RowHotdealsbannerBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return binding
                }
            }
        if (isAdded) {
            fragHomeBinding.rvHotDealsBanner.apply {
                layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                itemAnimator = DefaultItemAnimator()
                adapter = largebannerAdaptor
            }
        }
    }

    //TODO product remover favourite
    private fun callApiRemoveFavourite(map: HashMap<String, String>, position: Int, type: String) {
        if (isAPICalling) {
            return
        }
        isAPICalling = true
        showLoadingProgress(requireActivity())
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
                        dismissLoadingProgress()
                        when (type) {
                            "1" -> {
                                featuredProductsList!![position].isWishlist = 0
                                featuredProductsAdapter!!.notifyItemChanged(position)
                            }
                            "2" -> {
                                newProductsList!![position].isWishlist = 0
                                newProductsAdaptor!!.notifyItemChanged(position)
                            }
                            "3" -> {
                                hotdealsList!![position].isWishlist = 0
                                hotdealsAdaptor!!.notifyItemChanged(position)
                            }
                        }
                        onResume()
                    } else if (restResponse.status == 0) {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            requireActivity(),
                            restResponse.message
                        )
                    }
                }
                isAPICalling = false
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    requireActivity(),
                    resources.getString(R.string.error_msg)
                )
                isAPICalling = false
            }
        })
    }

    //TODO product favourite
    private fun callApiFavourite(map: HashMap<String, String>, position: Int, type: String) {
        if (isAPICalling) {
            return
        }
        isAPICalling = true
        showLoadingProgress(requireActivity())
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
                        dismissLoadingProgress()
                        when (type) {
                            "1" -> {
                                featuredProductsList!![position].isWishlist = 1
                                featuredProductsAdapter!!.notifyItemChanged(position)
                            }
                            "2" -> {
                                newProductsList!![position].isWishlist = 1
                                newProductsAdaptor!!.notifyItemChanged(position)
                            }
                            "3" -> {
                                hotdealsList!![position].isWishlist = 1
                                hotdealsAdaptor!!.notifyItemChanged(position)
                            }
                        }
                        onResume()
                    } else if (restResponse.status == 0) {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            requireActivity(),
                            restResponse.message
                        )
                    }
                }
                isAPICalling = false
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    requireActivity(),
                    resources.getString(R.string.error_msg)
                )
                isAPICalling = false
            }
        })
    }

    override fun onResume() {
        super.onResume()
        getCurrentLanguage(requireActivity(), false)
        if (isAdded) {
            if (isCheckNetwork(requireActivity())) {
                callApiBanner()
            } else {
                alertErrorOrValidationDialog(
                    requireActivity(),
                    resources.getString(R.string.no_internet)
                )
            }
        }
    }
}





