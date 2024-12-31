package com.rehman.docscan.ui.containerActivity.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.rehman.docscan.R
import com.rehman.docscan.databinding.FragmentSettingBinding
import com.rehman.docscan.databinding.ModesDialogBinding
import com.rehman.docscan.local_db.TinyDB
import com.rehman.utilities.Utils


class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
    private lateinit var tinyDB: TinyDB
    private var lastClickTime: Long = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingBinding.inflate(layoutInflater, container, false)
        tinyDB = TinyDB(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utils.measureExpandableViewHeight(binding.scanModeCard)

        when (tinyDB.getInt("Mode_Radio")) {
            R.id.basicModeRadio -> binding.scanModeText.text = "Basic Mode"
            R.id.basicModeFilterRadio -> binding.scanModeText.text = "Basic Mode with Filters"
            R.id.advanceModeRadio -> binding.scanModeText.text = "Advance Mode"
        }

        when (tinyDB.getInt("Limit_Radio")) {
            R.id.singleModeRadio -> binding.limitModeText.text = "Single Mode"
            R.id.burstModeRadio -> binding.limitModeText.text = "Burst Mode"
        }

        binding.importSwitch.isChecked = tinyDB.getBoolean("Import_Switch")



        binding.upperLayout1.setOnClickListener {
            Utils.toggleCardViewHeight(
                450,
                binding.moreDetails1,
                binding.scanModeCard,
                binding.arrowExpand
            )
        }

        Utils.measureExpandableViewHeight(binding.scanLimitCard)
        binding.upperLayout2.setOnClickListener {
            Utils.toggleCardViewHeight(
                450,
                binding.moreDetails2,
                binding.scanLimitCard,
                binding.arrowExpand2
            )

        }

        binding.importCardSwitch.setOnClickListener {
            binding.importSwitch.performClick()
        }

        binding.importSwitch.setOnCheckedChangeListener { _, check ->
            tinyDB.putBoolean("Import_Switch",check)
        }

        binding.limitMode.setOnClickListener {

//            val currentTime = System.currentTimeMillis()
//            if (currentTime - lastClickTime > 1000) { // 1000ms = 1 second debounce time
//                lastClickTime = currentTime
                openDialog("limit")
//            }


        }

        binding.scanMode.setOnClickListener {

            openDialog("scan")

        }


    }

    private fun openDialog(type: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = ModesDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)


        if (type == "limit") {
            dialogBinding.limitRadioGroup.visibility = View.VISIBLE
            dialogBinding.modeRadioGroup.visibility = View.GONE
            applyCustomFontAndColor(
                dialogBinding.singleModeRadio,
                "Single mode\nTake one focused picture at a time.",
                11
            )
            applyCustomFontAndColor(
                dialogBinding.burstModeRadio,
                "Burst Mode\nMultiple pictures seamlessly for rapid scanning.",
                10
            )


            when (tinyDB.getInt("Limit_Radio")) {
                R.id.singleModeRadio -> dialogBinding.singleModeRadio.isChecked = true
                R.id.burstModeRadio -> dialogBinding.burstModeRadio.isChecked = true
            }

            dialogBinding.limitRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                tinyDB.putInt("Limit_Radio", checkedId)
                when (checkedId) {
                    R.id.singleModeRadio -> binding.limitModeText.text = "Single Mode"
                    R.id.burstModeRadio -> binding.limitModeText.text = "Burst Mode"
                }

                dialog.dismiss()
            }

        } else {
            dialogBinding.limitRadioGroup.visibility = View.GONE
            dialogBinding.modeRadioGroup.visibility = View.VISIBLE

            applyCustomFontAndColor(
                dialogBinding.basicModeRadio,
                "Basic Mode\nBasic editing (crop, rotate, reorder pages).",
                10
            )
            applyCustomFontAndColor(
                dialogBinding.basicModeFilterRadio,
                "Basic Mode with Filters\nAdds image filters (grayscale, enhancement).",
                23
            )
            applyCustomFontAndColor(
                dialogBinding.advanceModeRadio,
                "Advance Mode\nML-enabled cleaning (erase stains, fingers) and future major features.",
                12
            )

            when (tinyDB.getInt("Mode_Radio")) {
                R.id.basicModeRadio -> dialogBinding.basicModeRadio.isChecked = true
                R.id.basicModeFilterRadio -> dialogBinding.basicModeFilterRadio.isChecked = true
                R.id.advanceModeRadio -> dialogBinding.advanceModeRadio.isChecked = true
            }

            dialogBinding.modeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                tinyDB.putInt("Mode_Radio", checkedId)
                when (checkedId) {
                    R.id.basicModeRadio -> binding.scanModeText.text = "Basic Mode"
                    R.id.basicModeFilterRadio -> binding.scanModeText.text = "Basic Mode with Filters"
                    R.id.advanceModeRadio -> binding.scanModeText.text = "Advance Mode"
                }
                dialog.dismiss()
            }
        }



        dialog.show()
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes.windowAnimations = R.style.DialogAnim
            setGravity(Gravity.BOTTOM)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            // Ensure visibility flags are set
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

            // Set navigation bar color
            navigationBarColor = ContextCompat.getColor(requireContext(), R.color.lightGrey)
        }
    }

    private fun applyCustomFontAndColor(
        radioButton: RadioButton,
        text: String,
        boldTextLength: Int,
    ) {

        // Define the text with different styles
        val spannableString = SpannableString(text)

        // Apply bold style to "Burst Mode" and set its font to Roboto Bold
        val robotoBold = ResourcesCompat.getFont(requireContext(), R.font.roboto_bold)
        spannableString.setSpan(
            CustomTypefaceSpan("", robotoBold!!),
            0,
            boldTextLength,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )


        val roboto = ResourcesCompat.getFont(requireContext(), R.font.roboto)
        spannableString.setSpan(
            CustomTypefaceSpan("", roboto!!),
            boldTextLength + 1,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )


        // Apply white color to "Burst Mode" and grey color to the remaining text
        val whiteColorSpan = ForegroundColorSpan(requireContext().getColor(R.color.white))
        val greyColorSpan = ForegroundColorSpan(requireContext().getColor(R.color.textGrey))
        spannableString.setSpan(whiteColorSpan, 0, boldTextLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(
            greyColorSpan,
            boldTextLength + 1,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Increase the size of "Burst Mode"
        spannableString.setSpan(
            RelativeSizeSpan(1.05f),
            0,
            boldTextLength,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            RelativeSizeSpan(0.88f),
            boldTextLength + 1,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )


        // Set the text with styles to the RadioButton
        radioButton.text = spannableString
    }


    inner class CustomTypefaceSpan(family: String?, private val typeface: Typeface) :
        MetricAffectingSpan() {

        override fun updateDrawState(tp: TextPaint) {
            applyCustomTypeFace(tp, typeface)
        }

        override fun updateMeasureState(p: TextPaint) {
            applyCustomTypeFace(p, typeface)
        }

        private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
            paint.typeface = tf
        }
    }


}