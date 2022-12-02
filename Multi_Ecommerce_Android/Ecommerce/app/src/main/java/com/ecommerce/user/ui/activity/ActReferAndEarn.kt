package com.ecommerce.user.ui.activity

import android.content.Intent
import android.util.Log
import android.view.View
import com.ecommerce.user.R
import com.ecommerce.user.base.BaseActivity
import com.ecommerce.user.databinding.ActReferAndEarnBinding
import com.ecommerce.user.utils.Common
import com.ecommerce.user.utils.SharePreference

class ActReferAndEarn : BaseActivity() {
    private lateinit var referAndEarnBinding: ActReferAndEarnBinding
    var refercode = ""
    var currency: String = ""
    var currencyPosition: String = ""
    override fun setLayout(): View = referAndEarnBinding.root

    override fun initView() {
        referAndEarnBinding = ActReferAndEarnBinding.inflate(layoutInflater)
        init()
    }

    private fun init() {
        currency = SharePreference.getStringPref(this@ActReferAndEarn, SharePreference.Currency)!!
        currencyPosition = SharePreference.getStringPref(
            this@ActReferAndEarn,
            SharePreference.CurrencyPosition
        )!!
        refercode = intent.getStringExtra("referralCode").toString()
        referAndEarnBinding.ivBack.setOnClickListener { finish() }
        Log.d("referralCode",refercode)
        Log.d("referralAmount",SharePreference.getStringPref(this@ActReferAndEarn,SharePreference.ReferralAmount).toString())
        referAndEarnBinding.tvReferCode.text = refercode
        referAndEarnBinding.btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.usethiscode).plus(" ").plus(refercode).plus(" ").plus(getString(R.string.toregisterwithecommapandget)).plus(" ").plus(Common.getPrice(currencyPosition,currency,SharePreference.getStringPref(this@ActReferAndEarn,SharePreference.ReferralAmount).toString())).plus(" ").plus(getString(R.string.bonus_amount))
            )
            startActivity(Intent.createChooser(shareIntent, "Refer and Earn"))
        }
    }
    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this, false)
    }
}