package com.auctech.siprint.home.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.print.PrintAttributes.Margins
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.databinding.FragmentHomeBinding
import com.auctech.siprint.home.activity.EditPdfActivity
import com.auctech.siprint.home.activity.SelectUserActivity
import com.auctech.siprint.home.response.InvoicesItem
import com.auctech.siprint.home.response.ResponseDashboard
import com.auctech.siprint.home.response.ResponseDateFilter
import com.auctech.siprint.home.response.ResponseSearch
import com.auctech.siprint.initials.activity.LoginActivity
import com.auctech.siprint.initials.response.ResponseSignup
import com.auctech.siprint.services.ApiClient
import com.auctech.siprint.services.RetrofitClient
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.Calendar


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var userId: String
    private var docList = ArrayList<InvoicesItem?>()
    private var startDate = 0L
    private var endDate = 0L
    private var isDateFilterActive = false
    private var isLoadingSearch = false
    private var isSearchActive = false
    private var offset = 0
    private var searchText = ""

    override fun onResume() {
        super.onResume()
        offset = 0
        if (isDateFilterActive) {
            callDateFilterApi()
        } else if (isSearchActive) {
            searchApi()
        } else {
            callDashboardApi()
        }
    }

    private var isRequestSent = false;

    //pagination
    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val scrollY = binding.scrolView.scrollY
        val contentHeight = binding.tableLayout.height
        val scrollViewHeight = binding.scrolView.height

        // Check if we are at the bottom of the scroll view
        if (scrollY + scrollViewHeight >= contentHeight) {
//            Toast.makeText(activity, "Reached at bottom", Toast.LENGTH_SHORT).show()
            if(isRequestSent){
                return@OnScrollChangedListener
            }
            isRequestSent = true
            Handler().postDelayed({
                isRequestSent= false
            }, 3000)
            offset++
            if (isDateFilterActive) {
                callDateFilterApi()
            } else if (isSearchActive) {
                searchApi()
            } else {
                callDashboardApi()
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        userId = PreferenceManager.getStringValue(Constants.USER_ID).toString()
        binding.filter.setOnClickListener {
            showFilterDialog()
        }
        binding.upload.setOnClickListener {
            uploadDocument()
        }
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        binding.searchHideShow.setOnClickListener {
            binding.searchLl.visibility = View.VISIBLE
            binding.searchHideShow.visibility = View.GONE
            binding.logo.visibility = View.GONE
            binding.searchET.requestFocus()
            imm.showSoftInput(binding.searchET, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.searchCancelIV.setOnClickListener {
            if (!isLoadingSearch) {
                isSearchActive = false
                binding.searchET.setText("")
                binding.searchLl.visibility = View.GONE
                binding.searchHideShow.visibility = View.VISIBLE
                binding.logo.visibility = View.VISIBLE
                binding.recentUploads.text = "Recent uploads"
                binding.removeFilterIV.visibility = View.GONE
                imm.hideSoftInputFromWindow(binding.searchET.windowToken, 0)
                offset = 0
                callDashboardApi()
            }
        }

        binding.removeFilterIV.setOnClickListener {
            isDateFilterActive = false
            isSearchActive = false
            binding.recentUploads.text = "Recent uploads"
            offset = 0
            binding.searchET.setText("")
            binding.searchLl.visibility = View.GONE
            binding.searchHideShow.visibility = View.VISIBLE
            binding.logo.visibility = View.VISIBLE
            callDashboardApi()
            binding.removeFilterIV.visibility = View.GONE
            imm.hideSoftInputFromWindow(binding.searchET.windowToken, 0)
        }

        setUpSearchApi()

//        callDashboardApi()

        binding.scrolView.viewTreeObserver.addOnScrollChangedListener(scrollListener)

        return binding.root
    }

    private fun setUpSearchApi() {

        val handler = Handler()
        val delay: Long = 500 // 1 second delay

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this implementation
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this implementation
            }

            override fun afterTextChanged(s: Editable?) {
                handler.removeCallbacksAndMessages(null) // Remove previous callbacks
                isLoadingSearch = false
                Glide.with(requireContext())
                    .load(R.drawable.baseline_clear_24)
                    .into(binding.searchCancelIV)
                handler.postDelayed({
                    searchText = s.toString()
                    if (searchText.isNotEmpty()) {
                        //showing cancel button
                        activity?.runOnUiThread {
                            Glide.with(requireContext())
                                .asGif()
                                .load(R.drawable.loading)
                                .into(binding.searchCancelIV)
                            binding.recentUploads.text = "Search results"
                            binding.removeFilterIV.visibility = View.VISIBLE
                        }
                        offset = 0
                        isLoadingSearch = true
                        searchApi()
                    }
                }, delay)
            }
        }

        binding.searchET.addTextChangedListener(textWatcher)

    }

    private fun searchApi() {
        try {
            isSearchActive = true
            binding.loading.visibility = View.VISIBLE
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)

            apiService.getSearch(userId, searchText, offset)
                .enqueue(object : Callback<ResponseSearch> {
                    override fun onResponse(
                        call: Call<ResponseSearch>,
                        response: Response<ResponseSearch>
                    ) {
                        isLoadingSearch = false
                        if (response.code() == 200 && response.body() != null) {
                            val searchResponse = response.body()
                            if (searchResponse?.status == 200) {
                                setUpDocs(searchResponse.data)
                            } else {
                                Toast.makeText(activity, searchResponse?.message, Toast.LENGTH_LONG)
                                    .show()
                            }
                        } else {
                            Toast.makeText(activity, Constants.ERR_MESSAGE, Toast.LENGTH_LONG)
                                .show()
                        }
                        binding.loading.visibility = View.GONE
                        Glide.with(requireContext())
                            .load(R.drawable.baseline_clear_24)
                            .into(binding.searchCancelIV)
                    }

                    override fun onFailure(call: Call<ResponseSearch>, t: Throwable) {
                        Toast.makeText(activity, Constants.ERR_MESSAGE, Toast.LENGTH_LONG).show()
                        binding.loading.visibility = View.GONE
                        isLoadingSearch = false
                        Glide.with(requireContext())
                            .load(R.drawable.baseline_clear_24)
                            .into(binding.searchCancelIV)
                        t.printStackTrace()
                    }

                })
        } catch (e: Exception) {
            binding.loading.visibility = View.GONE
            isLoadingSearch = false
            Glide.with(requireContext())
                .load(R.drawable.baseline_clear_24)
                .into(binding.searchCancelIV)
            e.printStackTrace()
        }

    }

    private fun uploadDocument() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        pdfLauncher.launch(intent)
    }

    private var mUri: Uri? = null
    var fileName: String? = null
    var fileSize: Long? = null
    private var pdfLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            try {
                if (result.resultCode != AppCompatActivity.RESULT_OK) {
                    mUri = null
                    return@registerForActivityResult
                }
                mUri = result.data!!.data

                val cursor = activity?.contentResolver?.query(mUri!!, null, null, null, null)

                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    fileName = cursor.getString(index)
                    fileSize = cursor.getLong(sizeIndex)


                    if (fileName != null && fileSize != null && fileName!!.endsWith(".pdf")) {
                        if (isFileLessThan1MB(fileSize!!)) {
                            showUploadConfirmationDialog()
                        } else {
                            Toast.makeText(
                                activity,
                                "File should be less than 1MB",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(activity, "PDF file is supported only.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "PDF is not valid",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                cursor?.close()


            } catch (e: IOException) {
                Log.d("ImagePick", "IOException : " + e.message)
                e.printStackTrace()
            }
        }

    private fun isFileLessThan1MB(fileSizeInBytes: Long): Boolean {
        val fileSizeInKB = fileSizeInBytes / 1024 // Convert to KB
        return fileSizeInKB < 1024 // 1MB = 1024 KB
    }


    private fun showUploadConfirmationDialog() {
        val uploadDialog = Dialog(requireActivity())
        uploadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        uploadDialog.setContentView(
            LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_upload_confimation, null, false)
        )
        uploadDialog.window!!.setGravity(Gravity.CENTER)
        uploadDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        uploadDialog.setCancelable(false)
        uploadDialog.setCanceledOnTouchOutside(false)
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        uploadDialog.window!!.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        val sureTv = uploadDialog.findViewById<TextView>(R.id.dialog_sure)
        val cancelTv = uploadDialog.findViewById<TextView>(R.id.dialog_cancel)
        val descriptionTv = uploadDialog.findViewById<TextView>(R.id.dialog_description)
        val titleTv = uploadDialog.findViewById<TextView>(R.id.dialog_title)
        val imageIv = uploadDialog.findViewById<TextView>(R.id.dialog_img)
        imageIv.text = fileName
        titleTv.text = "Upload?"
        descriptionTv.text = "Are you sure, You want to upload?"
        sureTv.setOnClickListener {
            try {
                uploadPdfApi()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            uploadDialog.dismiss()
        }
        cancelTv.setOnClickListener { uploadDialog.dismiss() }
        uploadDialog.show()
    }

    private fun uploadPdfApi() {
        try {
            binding.loading.visibility = View.VISIBLE
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val owner = PreferenceManager.getStringValue(Constants.USER_NUMBER)
            val userIdRequestBody = userId.toRequestBody(MultipartBody.FORM)
            val ownerRequestBody = owner!!.toRequestBody(MultipartBody.FORM)
            val descriptionRequestBody = "SELF".toRequestBody(MultipartBody.FORM)

            val imageRequestBody = RequestBody.create(
                "*/*".toMediaTypeOrNull(),
                context?.contentResolver?.openInputStream(mUri!!)!!.readBytes()
            )
            val imagePart =
                MultipartBody.Part.createFormData("pdf", fileName, imageRequestBody)

            apiService.uploadPdf(
                userIdRequestBody,
                ownerRequestBody,
                descriptionRequestBody,
                imagePart
            ).enqueue(object : Callback<ResponseSignup> {
                override fun onResponse(
                    call: Call<ResponseSignup>,
                    response: Response<ResponseSignup>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        fileName = null
                        fileSize = null
                        mUri = null
//                        if (response.body()!!.status == 200) {
                        Toast.makeText(context, response.body()!!.message, Toast.LENGTH_SHORT)
                            .show()
                        if(!isSearchActive && !isDateFilterActive){
                            offset = 0
                            callDashboardApi()
                        }
//                        }
                    }
                    binding.loading.visibility = View.GONE
                }

                override fun onFailure(call: Call<ResponseSignup>, t: Throwable) {
                    binding.loading.visibility = View.GONE
                    t.printStackTrace()
                }

            })
        } catch (e: Exception) {
            binding.loading.visibility = View.GONE
            e.printStackTrace()
        }

    }

    private fun callDashboardApi() {
        try {
            binding.loading.visibility = View.VISIBLE
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)

            apiService.getDashboardData(userId, offset)
                .enqueue(object : Callback<ResponseDashboard> {
                    override fun onResponse(
                        call: Call<ResponseDashboard>,
                        response: Response<ResponseDashboard>
                    ) {
                        if (response.code() == 200 && response.body() != null) {
                            val dashboard = response.body()
                            if (dashboard?.status == 200) {
                                val invoice = dashboard.data?.invoices
                                setUpDocs(invoice)

                                val userData = dashboard.data?.userData!!
                                if (userData.status == Constants.BLOCKED) {
                                    PreferenceManager.deletePref()
                                    activity?.startActivity(
                                        Intent(
                                            activity,
                                            LoginActivity::class.java
                                        )
                                    )
                                }
                                PreferenceManager.setStringValue(
                                    Constants.USER_NAME,
                                    userData.name!!
                                )
                                PreferenceManager.setStringValue(
                                    Constants.USER_EMAIL,
                                    userData.email!!
                                )
                                PreferenceManager.setStringValue(
                                    Constants.USER_PHOTO,
                                    userData.photo!!
                                )
                                PreferenceManager.setIntValue(
                                    Constants.USER_LIMIT,
                                    userData.credits!!
                                )
                                PreferenceManager.setStringValue(
                                    Constants.USER_TYPE,
                                    userData.userType!!
                                )
                                PreferenceManager.setStringValue(
                                    Constants.USER_GENDER,
                                    userData.gender!!
                                )
                                PreferenceManager.setIntValue(
                                    Constants.SELF_UPLOAD,
                                    userData.selfUpload!!.toInt()
                                )
                                PreferenceManager.setIntValue(
                                    Constants.PARTY_UPLOAD,
                                    userData.partyUpload!!.toInt()
                                )
                                if(userData.userType.equals("HOST", true)){
                                    PreferenceManager.setBoolValue(
                                        Constants.IS_HOST_DRIVER,
                                        true
                                    )
                                }

                            } else {
                                Toast.makeText(
                                    context,
                                    dashboard?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        binding.loading.visibility = View.GONE
                    }

                    override fun onFailure(call: Call<ResponseDashboard>, t: Throwable) {
                        binding.loading.visibility = View.GONE
                        t.printStackTrace()
                    }

                })

        } catch (e: Exception) {
            binding.loading.visibility = View.GONE
            e.printStackTrace()
        }
    }

    private fun setUpDocs(invoice: List<InvoicesItem?>?) {
        if (offset == 0) {
            removeRows()
            docList.clear()
        }
        if (invoice?.size!! < 20) {
            binding.scrolView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
        }
        docList.addAll(invoice)
        for ((index, element) in invoice.withIndex()) {
            addRows(element!!, index)
            // Now you can use 'index' to access the index of the element
        }
    }

    private fun fileNameExtract(s: String): String {
        val arr = s.split("/")
        return arr[arr.size - 1]
    }

    private var startDateFormatted = ""
    private var endDateFormatted = ""
    private fun showFilterDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        val view: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.date_select_dialog, null, false)
        dialog.setContentView(view)
        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.setCancelable(false)
//        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window!!.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val startDateTv: TextView = dialog.findViewById(R.id.startDate)
        val endDateTv: TextView = dialog.findViewById(R.id.endDate)
        val submitTv: TextView = dialog.findViewById(R.id.submit_date)

        startDateTv.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, dayOfMonth, 0, 0, 10)
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, dayOfMonth ->
                    val selectedDate = "$dayOfMonth-${selectedMonth + 1}-$selectedYear"
                    startDateTv.text = selectedDate
                    startDateFormatted = selectedDate
                    calendar.set(selectedYear, selectedMonth, dayOfMonth)
                    calendar.set(Calendar.HOUR_OF_DAY, 1)
                    calendar.set(Calendar.MINUTE, 10)
                    calendar.set(Calendar.SECOND, 10)
                    startDate = calendar.timeInMillis
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
            calendar.set(year, month, dayOfMonth, 23, 59, 59)
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, dayOfMonth ->
                    val selectedDate = "$dayOfMonth-${selectedMonth + 1}-$selectedYear"
                    endDateTv.text = selectedDate
                    endDateFormatted = selectedDate
                    calendar.set(selectedYear, selectedMonth, dayOfMonth)
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 10)
                    endDate = calendar.timeInMillis
                },
                year,
                month,
                dayOfMonth
            )

            datePickerDialog.show()
        }

        submitTv.setOnClickListener {
            if (startDate == 0L || endDate == 0L)
                return@setOnClickListener
            binding.scrolView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
            offset = 0
            isDateFilterActive = true
            callDateFilterApi()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun callDateFilterApi() {
        try {
            if (startDate == 0L || endDate == 0L)
                return
            binding.loading.visibility = View.VISIBLE
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)

            apiService.getPdfFilter(userId, startDate.toString(), endDate.toString(), offset)
                .enqueue(object : Callback<ResponseDateFilter> {
                    override fun onResponse(
                        call: Call<ResponseDateFilter>,
                        response: Response<ResponseDateFilter>
                    ) {
                        if (response.code() == 200 && response.body() != null) {
                            val dashboard = response.body()
                            if (dashboard?.status == 200) {
                                binding.recentUploads.text =
                                    "$startDateFormatted to $endDateFormatted"
                                binding.removeFilterIV.visibility = View.VISIBLE
                                val invoice = dashboard.data
                                docList.clear()
                                removeRows()
                                docList.addAll(invoice)
                                setUpDocs(invoice)
                            } else {
                                Toast.makeText(
                                    context,
                                    dashboard?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        binding.loading.visibility = View.GONE
                    }

                    override fun onFailure(call: Call<ResponseDateFilter>, t: Throwable) {
                        binding.loading.visibility = View.GONE
                        t.printStackTrace()
                    }

                })

        } catch (e: Exception) {
            binding.loading.visibility = View.GONE
            e.printStackTrace()
        }
    }

    //Remove all the row except top row (header)
    private fun removeRows() {
        for (i in binding.tableLayout.childCount - 1 downTo 1) {
            val row = binding.tableLayout.getChildAt(i)
            binding.tableLayout.removeView(row)
        }
    }

    private var rowSemaphore = true

    private fun addRows(element: InvoicesItem, index: Int) {

        val dateTime = element.date!!
        val label = element.label!!
        val documentName = fileNameExtract(element.file!!)

        val newRow = TableRow(activity)
        newRow.gravity = Gravity.CENTER_VERTICAL
        newRow.elevation = 3f
        newRow.background = ContextCompat.getDrawable(requireActivity(),R.drawable.table_row_bg)

//        newRow.showDividers = TableRow.SHOW_DIVIDER_MIDDLE
//        newRow.dividerDrawable = ColorDrawable(Color.BLACK)
        newRow.tag = index
//        if(!rowSemaphore)
//            newRow.background = ColorDrawable(Color.parseColor("#FFEEEEEE"))
//        rowSemaphore = !rowSemaphore

        val dateTimeTextView = TextView(activity)
        dateTimeTextView.layoutParams =
            TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.1f)
        dateTimeTextView.text = dateTime.substring(0, 11)
        dateTimeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        dateTimeTextView.typeface = Typeface.SANS_SERIF
        dateTimeTextView.gravity = Gravity.CENTER
        if (element.sourceID == element.owner) {
            dateTimeTextView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.down, 0, 0, 0)
        } else {
            dateTimeTextView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.incoming, 0, 0, 0)
        }
        dateTimeTextView.compoundDrawablePadding = 10
        dateTimeTextView.setPadding(8, 8, 8, 8)

        val labelTextView = TextView(activity)
        labelTextView.layoutParams =
            TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.1f)
        labelTextView.text = label
        labelTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        labelTextView.typeface = Typeface.SANS_SERIF
        labelTextView.gravity = Gravity.CENTER
        labelTextView.setPadding(8, 8, 8, 8)
        labelTextView.maxLines = 2 // Limit to 2 lines
        labelTextView.ellipsize = TextUtils.TruncateAt.END

        val docsTextView = TextView(activity)
        docsTextView.layoutParams =
            TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.1f)
        docsTextView.text = documentName
        docsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        docsTextView.typeface = Typeface.create("poppins_light", Typeface.NORMAL)
        docsTextView.gravity = Gravity.CENTER
        docsTextView.setPadding(8, 8, 8, 8)
        docsTextView.maxLines = 2 // Limit to 2 lines
        docsTextView.ellipsize = TextUtils.TruncateAt.END

        val shareTextView = ImageView(activity)
//        shareTextView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        shareTextView.layoutParams = TableRow.LayoutParams(0, 90, 0.7f)
        shareTextView.setImageResource(R.mipmap.options_big)
        shareTextView.setPadding(8, 8, 8, 8)
        shareTextView.setOnClickListener {
            openDetailDialog(newRow.tag.toString())
        }

        newRow.addView(dateTimeTextView)
        newRow.addView(labelTextView)
        newRow.addView(docsTextView)
        newRow.addView(shareTextView)

        val separator = View(requireContext())
        val separatorParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            15
        )
        separator.layoutParams = separatorParams
        binding.tableLayout.addView(separator)
        binding.tableLayout.addView(newRow)
//        binding.tableLayout.showDividers = TableLayout.SHOW_DIVIDER_MIDDLE
//        binding.tableLayout.dividerDrawable = ColorDrawable(Color.BLACK)

    }


    var progressBarDialogDetail : ProgressBar? = null
    var fileToBeShare : String? = null
    private fun openDetailDialog(indexStr: String) {

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        val view: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_doc_details, null, false)
        dialog.setContentView(view)
        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        dialog.window!!.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val title: TextView = dialog.findViewById(R.id.titleTextView)
        val dateTV: TextView = dialog.findViewById(R.id.actualDateTextView)
        val labelTv: TextView = dialog.findViewById(R.id.actualLabelTextView)
        val descriptionTv: TextView = dialog.findViewById(R.id.actualDescriptionTextView)
        val uploadedByTv: TextView = dialog.findViewById(R.id.actualUploadedByTextView)
        val editButton: Button = dialog.findViewById(R.id.editButton)
        val shareButton: Button = dialog.findViewById(R.id.shareButton)
        val downloadButton: Button = dialog.findViewById(R.id.downloadButton)
        progressBarDialogDetail = dialog.findViewById(R.id.progressBarHz)

        val index = indexStr.toInt()
        if (docList.size <= index) {
            return
        }
        val inv = docList[index]!!
        val filename = fileNameExtract(inv.file!!)
        title.text = filename
        dateTV.text = inv.date
        labelTv.text = inv.label
        descriptionTv.text = inv.description
        if (inv.sourceID == inv.owner) {
            uploadedByTv.text = "SELF"
        } else {
            uploadedByTv.text = inv.sourceName
        }
        shareButton.setOnClickListener {
//            fileToBeShare = inv.file
//            showShareChoiceDialog(inv.docID!!)
            shareDoc(inv.file)
        }

        downloadButton.setOnClickListener {
            downloadPdf(inv.file, fileNameExtract(inv.file))
        }

        editButton.setOnClickListener {
            val intent = Intent(requireActivity(), EditPdfActivity::class.java)
            intent.putExtra(Constants.DOC_ID, inv.docID.toString())
            intent.putExtra(Constants.DOC_LABEL, inv.label)
            intent.putExtra(Constants.DOC_DESCRIPTION, inv.description)
            intent.putExtra(Constants.DOC_NAME, filename)
            context?.startActivity(intent)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showShareChoiceDialog(docID : Int) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        val view: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_share, null, false)
        dialog.setContentView(view)
        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window!!.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        val siFolder: LinearLayout = dialog.findViewById(R.id.si_folder_iv)
        val shareToApp: LinearLayout = dialog.findViewById(R.id.share_to_app)

        siFolder.setOnClickListener{
            val intent = Intent(requireContext(), SelectUserActivity::class.java)
            intent.putExtra(Constants.DOC_ID, docID)
            intent.putExtra(Constants.DOC_NAME, fileNameExtract(fileToBeShare!!))
            context?.startActivity(intent)
        }

        shareToApp.setOnClickListener {
            shareDoc(fileToBeShare!!)
        }

        dialog.show()
    }

    var downloadManager: DownloadManager? = null
    var downloadId = -1L
    private fun downloadPdf(pdfUrl: String, pdfFileName: String) {
        try {
            progressBarDialogDetail?.visibility = View.VISIBLE
            val request = DownloadManager.Request(Uri.parse(pdfUrl))
            request.setTitle(pdfFileName)
            request.setDescription("Downloading $pdfFileName")
            request.setMimeType("application/pdf")
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                pdfFileName
            )
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadId = downloadManager!!.enqueue(request)

            val downloadReceiver = MyBroadcastReceiver()
            context?.registerReceiver(
                downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
            Toast.makeText(requireContext(), "Download started", Toast.LENGTH_SHORT).show()
        } catch (e: java.lang.Exception) {
            progressBarDialogDetail?.visibility = View.GONE
            e.printStackTrace()
        }
    }


    private fun shareDoc(file: String) {
        val textToShare = "Checkout this doc : "
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "$textToShare ${file.replace(" ", "%20")}")
        startActivity(Intent.createChooser(shareIntent, "Share using:"))
    }

    private fun Int.pxToDp(): Int {
        val resources = requireContext().resources
        val displayMetrics = resources.displayMetrics
        return (this / (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    //download complete listener
    inner class MyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {

                val downloadedFileUri =
                    downloadManager?.getUriForDownloadedFile(downloadId)
                val openPdfIntent = Intent(Intent.ACTION_VIEW)
                openPdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                openPdfIntent.setDataAndType(downloadedFileUri, "application/pdf")
                try {
                    context.startActivity(openPdfIntent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        "No PDF viewer application installed",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
                progressBarDialogDetail?.visibility = View.GONE
                context.unregisterReceiver(this)

            }
        }
    }

}
