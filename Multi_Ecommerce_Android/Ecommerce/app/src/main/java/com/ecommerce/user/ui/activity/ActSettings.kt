package com.ecommerce.user.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import com.ecommerce.user.R
import com.ecommerce.user.api.ApiClient
import com.ecommerce.user.api.SingleResponse
import com.ecommerce.user.base.BaseActivity
import com.ecommerce.user.databinding.ActSettingsBinding
import com.ecommerce.user.ui.authentication.ActChangePassword
import com.ecommerce.user.ui.payment.ActWallet
import com.ecommerce.user.utils.Common
import com.ecommerce.user.utils.Common.getCurrentLanguage
import com.ecommerce.user.utils.SharePreference
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class ActSettings : BaseActivity() {
    private lateinit var settingsBinding: ActSettingsBinding
    override fun setLayout(): View = settingsBinding.root
    var is_notification = ""
    var referralCode = ""
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initView() {
        settingsBinding = ActSettingsBinding.inflate(layoutInflater)
        referralCode=intent.getStringExtra("referralCode").toString()
        settingsBinding.ivBack.setOnClickListener {
            finish()
            setResult(RESULT_OK)
        }
        settingsBinding.clChangepassword.setOnClickListener {
            openActivity(
                ActChangePassword::class.java
            )
        }
        settingsBinding.clMyaddress.setOnClickListener {
            openActivity(
                ActAddress::class.java
            )
        }
        settingsBinding.clWallet.setOnClickListener {
            openActivity(ActWallet::class.java)
        }
        settingsBinding.clOffers.setOnClickListener {
            openActivity(ActOffers::class.java)
        }
        settingsBinding.clReferandEarn.setOnClickListener {
            val intent = Intent(this@ActSettings, ActReferAndEarn::class.java)
            intent.putExtra("referralCode", referralCode)
            Log.d("referralCode",referralCode)
            startActivity(intent)
        }

        settingsBinding.clChnagelayout.setOnClickListener {
            val dialog = BottomSheetDialog(this@ActSettings)
            if (Common.isCheckNetwork(this@ActSettings)) {
                val view =
                    layoutInflater.inflate(R.layout.row_bottomsheetlayout, null)
                dialog.window?.setBackgroundDrawable(getDrawable(R.color.tr))
                val btnLTREng = view.findViewById<TextView>(R.id.tvLTR)
                val btnRTLHindi = view.findViewById<TextView>(R.id.tvRTL)
                val btncancel = view.findViewById<TextView>(R.id.tvCancel)
                btncancel.setOnClickListener {
                    dialog.dismiss()
                }
                btnLTREng.setOnClickListener {
                    SharePreference.setStringPref(
                        this@ActSettings,
                        SharePreference.SELECTED_LANGUAGE,
                        this@ActSettings.resources.getString(R.string.language_english)
                    )
                    getCurrentLanguage(this@ActSettings, true)
                }
                btnRTLHindi.setOnClickListener {
                    SharePreference.setStringPref(
                        this@ActSettings,
                        SharePreference.SELECTED_LANGUAGE,
                        this@ActSettings.resources.getString(R.string.language_hindi)
                    )
                    getCurrentLanguage(this@ActSettings, true)
                }
                dialog.setCancelable(false)
                dialog.setContentView(view)
                dialog.show()
            } else {
                Common.alertErrorOrValidationDialog(
                    this@ActSettings,
                    resources.getString(R.string.no_internet)
                )
                dialog.dismiss()
            }
        }
        is_notification = intent.getStringExtra("notificationStatus").toString()
        settingsBinding.swichNotification.isChecked = is_notification == "1"
        settingsBinding.swichNotification.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked) {
                is_notification = "1"
                apicallNotification()
            } else {
                is_notification = "2"
                apicallNotification()
            }
        }
    }

    //TODO cancel order api calling
    private fun apicallNotification() {
        Common.showLoadingProgress(this@ActSettings)
        val hasmap = HashMap<String, String>()
        hasmap["user_id"] = SharePreference.getStringPref(
            this@ActSettings,
            SharePreference.userId
        )!!
        hasmap["notification_status"] = is_notification
        val call = ApiClient.getClient.changeNotificationStatus(hasmap)
        call.enqueue(object : Callback<SingleResponse> {
            override fun onResponse(
                call: Call<SingleResponse>,
                response: Response<SingleResponse>
            ) {
                if (response.code() == 200) {
                    Common.dismissLoadingProgress()
                    Common.isAddOrUpdated = true
                    if (response.body()?.status == 1) {
                        Common.dismissLoadingProgress()
                    } else {
                        Common.showErrorFullMsg(
                            this@ActSettings,
                            response.body()?.message.toString()
                        )
                    }
                } else {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActSettings,
                        resources.getString(R.string.error_msg)
                    )
                }
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.showErrorFullMsg(
                    this@ActSettings,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()
        getCurrentLanguage(this@ActSettings, false)

    }
}
