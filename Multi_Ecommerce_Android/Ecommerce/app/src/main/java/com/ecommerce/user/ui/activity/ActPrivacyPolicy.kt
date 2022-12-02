package com.ecommerce.user.ui.activity

import android.view.View
import com.ecommerce.user.R
import com.ecommerce.user.api.ApiClient
import com.ecommerce.user.base.BaseActivity
import com.ecommerce.user.databinding.ActPrivacyPolicyBinding
import com.ecommerce.user.model.CmsPageResponse
import com.ecommerce.user.utils.Common
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActPrivacyPolicy : BaseActivity() {
    private lateinit var actPrivacyPolicyBinding: ActPrivacyPolicyBinding


    override fun setLayout(): View = actPrivacyPolicyBinding.root

    override fun initView() {
        actPrivacyPolicyBinding = ActPrivacyPolicyBinding.inflate(layoutInflater)
        actPrivacyPolicyBinding.ivBack.setOnClickListener {
            finish()
        }
        when {
            intent.getStringExtra("Type") == "Policy" -> {
                actPrivacyPolicyBinding.tvTitle.text = intent.getStringExtra("Type")
            }
            intent.getStringExtra("Type") == "About" -> {
                actPrivacyPolicyBinding.tvTitle.text = intent.getStringExtra("Type")
            }
            intent.getStringExtra("Type") == "Terms Condition" -> {
                actPrivacyPolicyBinding.tvTitle.text = intent.getStringExtra("Type")
            }
        }
        callCmsDataApi()
    }

    private fun callCmsDataApi() {
        Common.showLoadingProgress(this@ActPrivacyPolicy)
        val call = ApiClient.getClient.getCmsData()
        call.enqueue(object : Callback<CmsPageResponse> {
            override fun onResponse(
                call: Call<CmsPageResponse>,
                response: Response<CmsPageResponse>
            ) {
                if (response.code() == 200) {
                    val restResponce = response.body()!!
                    Common.dismissLoadingProgress()
                    if (restResponce.status == 1) {
                        when {
                            intent.getStringExtra("Type") == "Policy" -> {
                                actPrivacyPolicyBinding.tvCmsData.text = restResponce.privacypolicy
                            }
                            intent.getStringExtra("Type") == "About" -> {
                                actPrivacyPolicyBinding.tvCmsData.text = restResponce.about
                            }
                            intent.getStringExtra("Type") == "Terms Condition" -> {
                                actPrivacyPolicyBinding.tvCmsData.text =
                                    restResponce.termsconditions
                            }
                        }
                    } else if (restResponce.status == 0) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@ActPrivacyPolicy,
                            restResponce.message.toString()
                        )
                    }
                } else {
                    Common.dismissLoadingProgress()
                    Common.alertErrorOrValidationDialog(
                        this@ActPrivacyPolicy,
                        resources.getString(R.string.error_msg)
                    )
                }
            }

            override fun onFailure(call: Call<CmsPageResponse>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@ActPrivacyPolicy,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }
}