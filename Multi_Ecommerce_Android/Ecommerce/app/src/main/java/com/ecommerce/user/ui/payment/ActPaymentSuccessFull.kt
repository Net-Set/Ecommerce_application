package com.ecommerce.user.ui.payment

import android.view.View
import com.ecommerce.user.ui.activity.ActMain
import com.ecommerce.user.base.BaseActivity
import com.ecommerce.user.databinding.ActPaymentSuccessFullBinding

class ActPaymentSuccessFull : BaseActivity() {
    private lateinit var actPaymentSuccessFullBinding: ActPaymentSuccessFullBinding

    override fun setLayout(): View =actPaymentSuccessFullBinding.root

    override fun initView() {
        actPaymentSuccessFullBinding= ActPaymentSuccessFullBinding.inflate(layoutInflater)
        actPaymentSuccessFullBinding.btncontinueshopping.setOnClickListener { openActivity(
            ActMain::class.java
        ) }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        openActivity(
            ActMain::class.java
        )
    }
}