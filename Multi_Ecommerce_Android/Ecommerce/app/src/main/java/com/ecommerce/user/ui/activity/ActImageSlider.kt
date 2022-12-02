package com.ecommerce.user.ui.activity

import android.view.View
import com.denzcoskun.imageslider.models.SlideModel
import com.ecommerce.user.base.BaseActivity
import com.ecommerce.user.databinding.ActImageSliderBinding
import com.ecommerce.user.model.ProductimagesItem

class ActImageSlider : BaseActivity() {
    private lateinit var imageSliderBinding: ActImageSliderBinding
    var imgList: ArrayList<ProductimagesItem>? = null

    override fun setLayout(): View = imageSliderBinding.root

    override fun initView() {
        imageSliderBinding = ActImageSliderBinding.inflate(layoutInflater)
        imgList = intent.getStringArrayListExtra("imageList") as ArrayList<ProductimagesItem>?
        val imageList = ArrayList<SlideModel>()
        for (i in 0 until imgList?.size!!) {
            val slideModel = SlideModel(imgList!![i].imageUrl)
            imageList.add(slideModel)
        }
        imageSliderBinding.imageSlider.setImageList(imageList)
        imageSliderBinding.ivCancle.setOnClickListener {
            finish()
        }
    }
}