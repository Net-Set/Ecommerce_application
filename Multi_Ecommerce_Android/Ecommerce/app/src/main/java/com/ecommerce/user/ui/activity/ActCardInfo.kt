package com.ecommerce.user.ui.activity

import android.content.Intent
import android.view.View
import com.ecommerce.user.R
import com.ecommerce.user.base.BaseActivity
import com.ecommerce.user.databinding.ActCardInfoBinding
import com.ecommerce.user.utils.Common

class ActCardInfo : BaseActivity() {
    private lateinit var actCardInfoBinding: ActCardInfoBinding
    override fun setLayout(): View = actCardInfoBinding.root

    override fun initView() {
        actCardInfoBinding = ActCardInfoBinding.inflate(layoutInflater)
        startCheckout()
        actCardInfoBinding.ivClose.setOnClickListener {
            finish()
        }
    }

    private fun startCheckout() {
        actCardInfoBinding.btnSubmit.setOnClickListener {
            if (actCardInfoBinding.edHolderName.text.isNullOrEmpty()) {
                Common.showSuccessFullMsg(
                    this@ActCardInfo,
                    resources.getString(R.string.validation_all)
                )
            } else if (actCardInfoBinding.edCardNumber.text.isNullOrEmpty()) {
                Common.showSuccessFullMsg(
                    this@ActCardInfo,
                    resources.getString(R.string.validation_all)
                )
            } else if (actCardInfoBinding.edCardNumber.text?.length ?: 0 < 16) {
                Common.showSuccessFullMsg(this@ActCardInfo, "Please enter valid card detail")
            } else if (actCardInfoBinding.etMonth.text.isNullOrEmpty()) {
                Common.showSuccessFullMsg(
                    this@ActCardInfo,
                    resources.getString(R.string.validation_all)
                )
            } else if (actCardInfoBinding.etYear.text.isNullOrEmpty()) {
                Common.showSuccessFullMsg(
                    this@ActCardInfo,
                    resources.getString(R.string.validation_all)
                )
            } else if (actCardInfoBinding.etCvv.text.isNullOrEmpty()) {
                Common.showSuccessFullMsg(
                    this@ActCardInfo,
                    resources.getString(R.string.validation_all)
                )
            } else if (actCardInfoBinding.etCvv.text?.length ?: 0 < 3) {
                Common.showSuccessFullMsg(this@ActCardInfo, "please enter valida CVV")
            } else {
                val intent = Intent()
                intent.putExtra("card_number", actCardInfoBinding.edCardNumber.text.toString())
                intent.putExtra("exp_month", actCardInfoBinding.etMonth.text.toString())
                intent.putExtra("exp_year", actCardInfoBinding.etYear.text.toString())
                intent.putExtra("cvv", actCardInfoBinding.etCvv.text.toString())
                setResult(401, intent)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@ActCardInfo, false)
    }

}