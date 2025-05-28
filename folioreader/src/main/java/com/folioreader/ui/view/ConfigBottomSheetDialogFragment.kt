package com.folioreader.ui.view

//import kotlinx.android.synthetic.main.view_config.*
import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.folioreader.Config
import com.folioreader.R
import com.folioreader.databinding.ViewConfigBinding
import com.folioreader.model.event.ReloadDataEvent
import com.folioreader.ui.activity.FolioActivity
import com.folioreader.ui.activity.FolioActivityCallback
import com.folioreader.ui.adapter.FontAdapter
import com.folioreader.ui.fragment.MediaControllerFragment
import com.folioreader.util.AppUtil
import com.folioreader.util.UiUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.EventBus

class ConfigBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val FADE_DAY_NIGHT_MODE = 500

        @JvmField
        val LOG_TAG: String = ConfigBottomSheetDialogFragment::class.java.simpleName
    }

    private lateinit var config: Config
    private var isNightMode = false
    private lateinit var activityCallback: FolioActivityCallback
    private lateinit var viewConfigBinding: ViewConfigBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewConfigBinding = ViewConfigBinding.inflate(inflater)
        return viewConfigBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (activity is FolioActivity)
            activityCallback = activity as FolioActivity

        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = 0
        }

        config = AppUtil.getSavedConfig(activity)!!
        initViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        view?.viewTreeObserver?.addOnGlobalLayoutListener(null)
    }

    private fun initViews() {
        inflateView()
        configFonts()

        viewConfigBinding.viewConfigFontSizeSeekBar.progress = config.fontSize
        configSeekBar()
        selectFont(config.font, false)
        isNightMode = config.isNightMode
        if (isNightMode) {
            viewConfigBinding.container.setBackgroundColor(ContextCompat.getColor(context!!, R.color.night))
        } else {
            viewConfigBinding.container.setBackgroundColor(ContextCompat.getColor(context!!, R.color.white))
        }

        if (isNightMode) {


            viewConfigBinding.viewConfigIbDayMode.isSelected = false
            viewConfigBinding.viewConfigIbNightMode.isSelected = true
            UiUtil.setColorIntToDrawable(
                config.currentThemeColor,
                viewConfigBinding.viewConfigIbNightMode.drawable
            )
            UiUtil.setColorResToDrawable(R.color.app_gray, viewConfigBinding.viewConfigIbDayMode.drawable)
        } else {
            viewConfigBinding.viewConfigIbDayMode.isSelected = true
            viewConfigBinding.viewConfigIbNightMode.isSelected = false
            UiUtil.setColorIntToDrawable(
                config.currentThemeColor,
                viewConfigBinding.viewConfigIbDayMode.drawable
            )
            UiUtil.setColorResToDrawable(R.color.app_gray, viewConfigBinding.viewConfigIbNightMode.drawable)
        }
    }

    private fun inflateView() {

        if (config.allowedDirection != Config.AllowedDirection.VERTICAL_AND_HORIZONTAL) {
            viewConfigBinding.view5.visibility = View.GONE
            viewConfigBinding.buttonVertical.visibility = View.GONE
            viewConfigBinding.buttonHorizontal.visibility = View.GONE
        }

        viewConfigBinding.viewConfigIbDayMode.setOnClickListener {
            isNightMode = true
            toggleBlackTheme()
            viewConfigBinding.viewConfigIbDayMode.isSelected = true
            viewConfigBinding.viewConfigIbNightMode.isSelected = false
            setToolBarColor()
            setAudioPlayerBackground()
            UiUtil.setColorResToDrawable(R.color.app_gray, viewConfigBinding.viewConfigIbNightMode.drawable)
            UiUtil.setColorIntToDrawable(config.currentThemeColor, viewConfigBinding.viewConfigIbDayMode.drawable)
            dialog?.hide()
        }

        viewConfigBinding.viewConfigIbNightMode.setOnClickListener {
            isNightMode = false
            toggleBlackTheme()
            viewConfigBinding.viewConfigIbDayMode.isSelected = false
            viewConfigBinding.viewConfigIbNightMode.isSelected = true
            UiUtil.setColorResToDrawable(R.color.app_gray, viewConfigBinding.viewConfigIbDayMode.drawable)
            UiUtil.setColorIntToDrawable(
                config.currentThemeColor,
                viewConfigBinding.viewConfigIbNightMode.drawable
            )
            setToolBarColor()
            setAudioPlayerBackground()
            dialog?.hide()
        }

        if (activityCallback.direction == Config.Direction.HORIZONTAL) {
            viewConfigBinding.buttonHorizontal.isSelected = true
        } else if (activityCallback.direction == Config.Direction.VERTICAL) {
            viewConfigBinding.buttonVertical.isSelected = true
        }

        viewConfigBinding.buttonVertical.setOnClickListener {
            config = AppUtil.getSavedConfig(context)!!
            config.direction = Config.Direction.VERTICAL
            AppUtil.saveConfig(context, config)
            activityCallback.onDirectionChange(Config.Direction.VERTICAL)
            viewConfigBinding.buttonHorizontal.isSelected = false
            viewConfigBinding.buttonVertical.isSelected = true
        }

        viewConfigBinding.buttonHorizontal.setOnClickListener {
            config = AppUtil.getSavedConfig(context)!!
            config.direction = Config.Direction.HORIZONTAL
            AppUtil.saveConfig(context, config)
            activityCallback.onDirectionChange(Config.Direction.HORIZONTAL)
            viewConfigBinding.buttonHorizontal.isSelected = true
            viewConfigBinding.buttonVertical.isSelected = false
        }
    }

    private fun configFonts() {

        val colorStateList = UiUtil.getColorList(
            config.currentThemeColor,
            ContextCompat.getColor(context!!, R.color.grey_color)
        )

        viewConfigBinding.buttonVertical.setTextColor(colorStateList)
        viewConfigBinding.buttonHorizontal.setTextColor(colorStateList)

        val adapter = FontAdapter(config, context!!)

        viewConfigBinding.viewConfigFontSpinner.adapter = adapter
        val color:Int
        color = if (config.isNightMode) {
            R.color.night_default_font_color
        } else {
            R.color.day_default_font_color
        }
        //,

        viewConfigBinding.viewConfigFontSpinner.background.setColorFilter(
            PorterDuffColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_ATOP)
        )

        val fontIndex = adapter.fontKeyList.indexOf(config.font)
        viewConfigBinding.viewConfigFontSpinner.setSelection(if (fontIndex < 0) 0 else fontIndex)

        viewConfigBinding.viewConfigFontSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectFont(adapter.fontKeyList[position], true)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
    }

    private fun selectFont(selectedFont: String, isReloadNeeded: Boolean) {

        config.font = selectedFont

        if (isAdded && isReloadNeeded) {
            AppUtil.saveConfig(activity, config)
            EventBus.getDefault().post(ReloadDataEvent())
        }
    }

    private fun toggleBlackTheme() {

        val day = ContextCompat.getColor(context!!, R.color.white)
        val night = ContextCompat.getColor(context!!, R.color.night)

        val colorAnimation = ValueAnimator.ofObject(
            ArgbEvaluator(),
            if (isNightMode) night else day, if (isNightMode) day else night
        )
        colorAnimation.duration = FADE_DAY_NIGHT_MODE.toLong()

        colorAnimation.addUpdateListener { animator ->
            val value = animator.animatedValue as Int
            viewConfigBinding.container.setBackgroundColor(value)
        }

        colorAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}

            override fun onAnimationEnd(animator: Animator) {
                isNightMode = !isNightMode
                config.isNightMode = isNightMode
                AppUtil.saveConfig(activity, config)
                EventBus.getDefault().post(ReloadDataEvent())
            }

            override fun onAnimationCancel(animator: Animator) {}

            override fun onAnimationRepeat(animator: Animator) {}
        })

        colorAnimation.duration = FADE_DAY_NIGHT_MODE.toLong()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            val attrs = intArrayOf(android.R.attr.navigationBarColor)
            val typedArray = activity?.theme?.obtainStyledAttributes(attrs)
            val defaultNavigationBarColor = typedArray?.getColor(
                0,
                ContextCompat.getColor(context!!, R.color.white)
            )
            val black = ContextCompat.getColor(context!!, R.color.black)

            val navigationColorAnim = ValueAnimator.ofObject(
                ArgbEvaluator(),
                if (isNightMode) black else defaultNavigationBarColor,
                if (isNightMode) defaultNavigationBarColor else black
            )

            navigationColorAnim.addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Int
                activity?.window?.navigationBarColor = value
            }

            navigationColorAnim.duration = FADE_DAY_NIGHT_MODE.toLong()
            navigationColorAnim.start()
        }

        colorAnimation.start()
    }

    private fun configSeekBar() {
        val thumbDrawable = ContextCompat.getDrawable(activity!!, R.drawable.seekbar_thumb)
        UiUtil.setColorIntToDrawable(config.currentThemeColor, thumbDrawable)
        UiUtil.setColorResToDrawable(
            R.color.grey_color,
            viewConfigBinding.viewConfigFontSizeSeekBar.progressDrawable
        )
        viewConfigBinding.viewConfigFontSizeSeekBar.thumb = thumbDrawable

        viewConfigBinding.viewConfigFontSizeSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                config.fontSize = progress
                AppUtil.saveConfig(activity, config)
                EventBus.getDefault().post(ReloadDataEvent())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun setToolBarColor() {
        if (isNightMode) {
            activityCallback.setDayMode()
        } else {
            activityCallback.setNightMode()
        }
    }

    private fun setAudioPlayerBackground() {

        var mediaControllerFragment: Fragment? =
            fragmentManager?.findFragmentByTag(MediaControllerFragment.LOG_TAG)
                ?: return
        mediaControllerFragment = mediaControllerFragment as MediaControllerFragment
        if (isNightMode) {
            mediaControllerFragment.setDayMode()
        } else {
            mediaControllerFragment.setNightMode()
        }
    }
}
