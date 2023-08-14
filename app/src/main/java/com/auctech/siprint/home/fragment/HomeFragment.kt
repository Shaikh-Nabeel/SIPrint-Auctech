package com.auctech.siprint.home.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.auctech.siprint.R
import com.auctech.siprint.databinding.FragmentHomeBinding
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var startDate = "";
    private var endDate = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater,container, false)

        binding.filter.setOnClickListener {
            showFilterDialog();
        }


        for(i in 1..10)
            addRows("12/12/2023 4:30 AM", "shaikh nabeel", "invoice.pdf")

//        removeRows()

//        binding.scrolView.viewTreeObserver.addOnScrollChangedListener {
//            val scrollY = binding.scrolView.scrollY
//            val contentHeight = binding.tableLayout.height
//            val scrollViewHeight = binding.scrolView.height
//
//            // Check if we are at the bottom of the scroll view
//            if (scrollY + scrollViewHeight >= contentHeight) {
//                Toast.makeText(activity, "Reached at bottom", Toast.LENGTH_SHORT).show()
//            }
//        }


        return binding.root
    }

    private fun showFilterDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        val view: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.date_select_dialog, null, false)
        dialog.setContentView(view)
        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window!!.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val startDateTv:TextView = dialog.findViewById(R.id.startDate)
        val endDateTv:TextView = dialog.findViewById(R.id.endDate)
        val submitTv:TextView = dialog.findViewById(R.id.submit_date)

        startDateTv.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year,month,dayOfMonth,0,0,10)
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, dayOfMonth ->
                    val selectedDate = "$selectedYear-${selectedMonth + 1}-$dayOfMonth"
                    startDateTv.text = selectedDate
                    startDate = selectedDate
                },
                year,
                month,
                dayOfMonth
            )

            datePickerDialog.show()
        }

        endDateTv.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year,month,dayOfMonth,23,59,59)
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, dayOfMonth ->
                    val selectedDate = "$selectedYear-${selectedMonth + 1}-$dayOfMonth"
                    endDateTv.text = selectedDate
                    endDate = selectedDate;
                },
                year,
                month,
                dayOfMonth
            )

            datePickerDialog.show()
        }

        submitTv.setOnClickListener {
            // call api
            dialog.dismiss()
        }

        dialog.show()
    }

    //Remove all the row except top row (header)
    private fun removeRows(){
        for (i in binding.tableLayout.childCount - 1 downTo 1) {
            val row = binding.tableLayout.getChildAt(i)
            binding.tableLayout.removeView(row)
        }
    }

    private var rowSemaphore = true

    private fun addRows(dateTime: String, uploadedBy: String, documentName: String) {

        val newRow = TableRow(activity)
        newRow.showDividers = TableRow.SHOW_DIVIDER_MIDDLE
        newRow.dividerDrawable = ColorDrawable(Color.BLACK)
        newRow.tag = "id"
        if(!rowSemaphore)
            newRow.background = ColorDrawable(Color.parseColor("#FFEEEEEE"))
        rowSemaphore = !rowSemaphore

        val dateTimeTextView = TextView(activity)
        dateTimeTextView.layoutParams = TableRow.LayoutParams(110.pxToDp(), TableRow.LayoutParams.WRAP_CONTENT, 1f)
        dateTimeTextView.text = dateTime
        dateTimeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        dateTimeTextView.typeface = Typeface.SANS_SERIF
        dateTimeTextView.gravity = Gravity.CENTER
        dateTimeTextView.setPadding(8, 8, 8, 8)

        val uploadedByTextView = TextView(activity)
        uploadedByTextView.layoutParams = TableRow.LayoutParams(120.pxToDp(), TableRow.LayoutParams.WRAP_CONTENT, 1f)
        uploadedByTextView.text = uploadedBy
        uploadedByTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        uploadedByTextView.typeface = Typeface.SANS_SERIF
        uploadedByTextView.gravity = Gravity.CENTER
        uploadedByTextView.setPadding(8, 8, 8, 8)
        uploadedByTextView.maxLines = 2 // Limit to 2 lines
        uploadedByTextView.ellipsize = TextUtils.TruncateAt.END

        val docsTextView = TextView(activity)
        docsTextView.layoutParams = TableRow.LayoutParams(150.pxToDp(), TableRow.LayoutParams.WRAP_CONTENT, 1f)
        docsTextView.text = documentName
        docsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        docsTextView.typeface = Typeface.create("poppins_regular", Typeface.NORMAL)
        docsTextView.gravity = Gravity.CENTER
        docsTextView.setPadding(8, 8, 8, 8)
        docsTextView.maxLines = 2 // Limit to 2 lines
        docsTextView.ellipsize = TextUtils.TruncateAt.END

        val shareTextView = ImageView(activity)
        shareTextView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        shareTextView.setImageResource(R.mipmap.share_icon)
        shareTextView.setPadding(8, 20, 8, 8)
        shareTextView.setOnClickListener {
            Toast.makeText(
                requireContext(),
                newRow.tag.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }

        newRow.addView(dateTimeTextView)
        newRow.addView(uploadedByTextView)
        newRow.addView(docsTextView)
        newRow.addView(shareTextView)

        binding.tableLayout.addView(newRow)
        binding.tableLayout.showDividers = TableLayout.SHOW_DIVIDER_MIDDLE
        binding.tableLayout.dividerDrawable = ColorDrawable(Color.BLACK)

    }

    fun Int.pxToDp(): Int {
        val resources = requireContext().resources
        val displayMetrics = resources.displayMetrics
        return (this / (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

}