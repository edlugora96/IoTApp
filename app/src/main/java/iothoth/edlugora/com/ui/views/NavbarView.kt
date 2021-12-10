package iothoth.edlugora.com.ui.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import iothoth.edlugora.com.R
import iothoth.edlugora.com.databinding.FragmentProfileViewBinding
import iothoth.edlugora.com.databinding.NavbarProfileBinding

class NavbarView : FrameLayout {
    private lateinit var binding: NavbarProfileBinding

    constructor(context: Context, attrs: AttributeSet, defStyle : Int) : super(
        context,
        attrs,
        defStyle
    ){
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        initView()
    }

    constructor(context: Context) : super(context){
        initView()
    }

    private fun initView(){
        val view = View.inflate(context, R.layout.navbar_profile, null)

        binding = NavbarProfileBinding.bind(view)
        addView(binding.root)
    }

    fun getBind() = binding

}