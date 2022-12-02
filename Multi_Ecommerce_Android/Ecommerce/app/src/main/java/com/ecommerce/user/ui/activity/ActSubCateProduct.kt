package com.ecommerce.user.ui.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecommerce.user.R
import com.ecommerce.user.adapter.ProductViewAdapter
import com.ecommerce.user.api.ApiClient
import com.ecommerce.user.base.BaseActivity
import com.ecommerce.user.base.BaseAdaptor
import com.ecommerce.user.databinding.ActSubCateProductBinding
import com.ecommerce.user.databinding.RowViewallBinding
import com.ecommerce.user.model.ProductDataItem
import com.ecommerce.user.model.ProductResponse
import com.ecommerce.user.ui.authentication.ActLogin
import com.ecommerce.user.utils.Common
import com.ecommerce.user.utils.SharePreference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class ActSubCateProduct : BaseActivity() {
    private lateinit var binding: ActSubCateProductBinding
    private var currentPage = 1
    var total_pages: Int = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var pastVisibleItems = 0
    private var productAllDataList = ArrayList<ProductDataItem>()

    private var gridLayoutManagerProduct: GridLayoutManager? = null
    private var productAllDataAdapter: ProductViewAdapter? = null

    override fun setLayout(): View = binding.root

    override fun initView() {
        binding = ActSubCateProductBinding.inflate(layoutInflater)
        binding.ivBack.setOnClickListener { finish() }
        gridLayoutManagerProduct =
            GridLayoutManager(this@ActSubCateProduct, 2, GridLayoutManager.VERTICAL, false)
        var title: String? = intent.getStringExtra("title")
        binding.tvviewall.text = title.toString()
        binding.rvProduct.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    visibleItemCount = gridLayoutManagerProduct!!.childCount
                    totalItemCount = gridLayoutManagerProduct!!.itemCount
                    pastVisibleItems = gridLayoutManagerProduct!!.findFirstVisibleItemPosition()
                    if (currentPage < total_pages) {
                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            currentPage += 1
                            productData()
                        }
                    }
                }
            }
        })
    }


    private fun productData() {
        Common.showLoadingProgress(this@ActSubCateProduct)
        val hasmap = HashMap<String, String>()
        hasmap["user_id"] =
            SharePreference.getStringPref(this@ActSubCateProduct, SharePreference.userId)!!
        hasmap["innersubcategory_id"] =
            intent.getSerializableExtra("innersubcategory_id").toString()
        val call = ApiClient.getClient.getProduct(currentPage.toString(), hasmap)
        call.enqueue(object : Callback<ProductResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<ProductResponse>,
                response: Response<ProductResponse>
            ) {
                if (response.code() == 200) {
                    val restResponce = response.body()!!
                    if (restResponce.status == 1) {
                        Common.dismissLoadingProgress()

                        if ((restResponce.data?.data?.size ?: 0) > 0) {
                            binding.rvProduct.visibility = View.VISIBLE
                            binding.tvNoDataFound.visibility = View.GONE
                            this@ActSubCateProduct.currentPage =
                                restResponce.data?.currentPage!!.toInt()
                            total_pages = restResponce.data.lastPage!!.toInt()
                            restResponce.data.data?.let {
                                productAllDataList.addAll(it)
                            }
                        } else {
                            binding.rvProduct.visibility = View.GONE
                            binding.tvNoDataFound.visibility = View.VISIBLE
                        }
                        productAllDataAdapter?.notifyDataSetChanged()

                    } else if (restResponce.status == 0) {
                        Common.dismissLoadingProgress()

                        if (restResponce.message == "No data found") {
                            binding.tvNoDataFound.visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@ActSubCateProduct,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    override fun onResume() {
        super.onResume()
        currentPage = 1
        productAllDataList.clear()
        loadAdapetr(productAllDataList)
        productData()
        Common.getCurrentLanguage(this@ActSubCateProduct, false)
    }

    private fun loadAdapetr(productAllDataList: ArrayList<ProductDataItem>) {
        productAllDataAdapter =
            ProductViewAdapter(this@ActSubCateProduct, productAllDataList)
        binding.rvProduct.layoutManager =
            gridLayoutManagerProduct
        binding.rvProduct.adapter = productAllDataAdapter
    }
}