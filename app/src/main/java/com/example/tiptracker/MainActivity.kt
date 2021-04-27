package com.example.tiptracker

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


/**
Creates the main activity which will access daily data and display calories for the day.
From here you can view the items you have previusly logged or add more items to the log
 */
class MainActivity : AppCompatActivity() {

    val myCalendar = Calendar.getInstance()

    override fun onResume() {
        super.onResume()
        loadFile()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verifyStoragePermissions(this)
        loadFile()

        val viewLogButton: Button = findViewById(R.id.viewLog)
        viewLogButton.setOnClickListener(View.OnClickListener {
            viewLogButtonClick();
        })

        val addItemButton: Button = findViewById(R.id.addItem)
        addItemButton.setOnClickListener(View.OnClickListener {
            saveFile();
        })

        val dateTest = findViewById<View>(R.id.dateText) as EditText
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        dateTest.setText(sdf.format(Date()))

        val dateListener = OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar[Calendar.YEAR] = year
            myCalendar[Calendar.MONTH] = monthOfYear
            myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
            updateLabel()
            loadFile()
        }

        dateTest.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                DatePickerDialog(this@MainActivity, dateListener, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
                    myCalendar[Calendar.DAY_OF_MONTH]).show();
            }
        })

        val cashRadio: RadioButton = findViewById<RadioButton>(R.id.radioCash)
        val radioGroup = findViewById<View>(R.id.orderRadio) as RadioGroup
        radioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                if(cashRadio.isChecked){
                    hideCashLayout();
                }
                else{
                    showCashLayout();
                }
            }
        })


    }

    /**
     * Loads the local file that we have written to from the device
     *  and parses it for information.
     */
    private fun loadFile() {
        val text = readFromFile(applicationContext)
        val date = findViewById<TextView>(R.id.dateText);
        parseText(text)
    }

    /**
     * Logic for reading from a local file
     */
    private fun readFromFile(context: Context): String {
        var ret = ""
        try {
            val inputStream: InputStream? = context.openFileInput("config.txt")
            if (inputStream != null) {
                val inputStreamReader =
                    InputStreamReader(inputStream)
                val bufferedReader =
                    BufferedReader(inputStreamReader)
                var receiveString: String? = ""
                val stringBuilder = StringBuilder()
                while (bufferedReader.readLine().also { receiveString = it } != null) {
                    stringBuilder.append("\n").append(receiveString)
                }
                inputStream.close()
                ret = stringBuilder.toString()
            }
            Log.d("Loaded file", ret)
        } catch (e: FileNotFoundException) {
            Log.e("login activity", "File not found: $e")
        } catch (e: IOException) {
            Log.e("login activity", "Can not read file: $e")
        }
        return ret
    }

    //Ensures the application has proper read/write permissions.
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    fun verifyStoragePermissions(activity: Activity?) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                112
            )
        }
    }


    /**
     * Opens the ViewLog view when the button is pressed.
     */
    private fun viewLogButtonClick(){
        val intent = Intent(this, ViewLog::class.java).apply {
        }
        val date = findViewById<TextView>(R.id.dateText);
        intent.putExtra("date", date.text.toString())
        startActivity(intent)
    }

    /**
     * Parses the text from the local file to find
     * only information for the selected date.
     * @param fileText - String data from local file
     */
    private fun parseText(fileText: String) {

        val date = findViewById<TextView>(R.id.dateText);
        val dateString = date.text.toString()

        val regex = Regex("DATE:"+dateString+":(.*?):END:")
        val matches = regex.findAll(fileText)
        val itemsInputted = matches.map { it.groupValues[0] }.joinToString()
        //setValues(itemsInputted)

//        val typeSpinner: Spinner = findViewById(R.id.typeSpinner)
//        ArrayAdapter.createFromResource(
//            this,
//            R.array.typeArray,
//            android.R.layout.simple_spinner_item
//        ).also { adapter ->
//            adapter.setDropDownViewResource(android.R.layout.simple_list_item_checked)
//            typeSpinner.adapter = adapter
//        }
    }

    /**
     * Gets sum of a given string
     */
    fun findSum(str: String): Int {
        var temp = "0"
        var sum = 0

        // read each character in input string
        for (i in 0 until str.length) {
            val ch = str[i]

            if (Character.isDigit(ch)) temp += ch else {
                sum += temp.toInt()
                temp = "0"
            }
        }
        return sum + temp.toInt()
    }



    private fun updateLabel() {
        val dateText = findViewById<View>(R.id.dateText) as EditText
        val myFormat = "MM/dd/yyyy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        dateText.setText(sdf.format(myCalendar.getTime()))
    }


    fun saveFile() {
        val date = findViewById<TextView>(R.id.dateText);
        val dateValue: String = date.text.toString()
        val address: EditText = findViewById(R.id.addressInput);
        val deliveryCharge: EditText = findViewById(R.id.ratePerDeliveryInput);
        //val typeSpinner: Spinner = findViewById(R.id.typeSpinner);
        //val orderType = typeSpinner.selectedItem.toString()
        val radioButtonCash: RadioButton = findViewById(R.id.radioCash)
        val radioButtonCred: RadioButton = findViewById(R.id.radioCred)
        val radioGroup: RadioGroup = findViewById(R.id.orderRadio);
        //var selectedID = radioGroup.checkedRadioButtonId
        var orderType = ""
        if (radioButtonCash.isChecked) {
            orderType = radioButtonCash.text.toString()
        } else if (radioButtonCred.isChecked) {
            orderType = radioButtonCred.text.toString()
        }


        val orderPrice: EditText = findViewById(R.id.priceInput);
        val tipAmount: EditText = findViewById(R.id.tipInput);
        val cashTipAmount: EditText = findViewById(R.id.cashTipInput);


        var text = ""

        if (orderType == "Cash") {
            text = "DATE:" + dateValue + ":ADDRESS:" + address.text + ":OTYPEM:" + orderType + ":OPRICEM:" + orderPrice.text +
                    ":TIPC:" + tipAmount.text + ":TIPM:" + cashTipAmount.text  + ":CHARGE:" +
                    deliveryCharge.text + ":STOP:" + ":END:"
        } else if (orderType == "Credit") {
            text = "DATE:" + dateValue + ":ADDRESS:" + address.text + ":OTYPEC:" + orderType + ":OPRICEC:" + orderPrice.text +
                    ":TIPC:" + tipAmount.text + ":TIPM:" + cashTipAmount.text + ":CHARGE:" +
                    deliveryCharge.text + ":STOP:" + ":END:";
        }

        if (dateValue == "" || address.text.toString() == "" || deliveryCharge.text.toString() == "" || orderPrice.text.toString() == "" ||
                cashTipAmount.text.toString() == "" || cashTipAmount.text.toString() == "" || (!radioButtonCash.isChecked && !radioButtonCred.isChecked)) {
                fillToastMsg("Fill out all fields");
        }
        else {
            savedToastMsg("Added Item to Log");
            address.setText("")
            orderPrice.setText("")
            tipAmount.setText("0")
            cashTipAmount.setText("0")
            radioButtonCash.isChecked = false;
            radioButtonCash.isClickable = true;
            radioButtonCred.isChecked = false;
            radioButtonCred.isClickable = true;
            writeToFile(text, applicationContext)

        }

//        if(dateValue == "" || address.text.toString() == "" || deliveryCharge.text.toString() == "" || orderPrice.text.toString() == "" ||
//                tipAmount.text.toString() == "" || (!radioButtonCash.isChecked && !radioButtonCred.isChecked)) {
//            fillToastMsg("Fill out all fields");

    }

    /**
     * Logic for writing to a file.
     */
    private fun writeToFile(
        data: String,
        context: Context
    ) {
        Log.d("writeToFile", data)
        try {
            val outputStreamWriter = OutputStreamWriter(
                context.openFileOutput(
                    "config.txt",
                    Context.MODE_APPEND
                   // Context.MODE_PRIVATE
                )
            )
            outputStreamWriter.write(data)
            outputStreamWriter.close()
            Log.d("Saved file", data)
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: $e")
        }
    }

    private fun savedToastMsg(msg: String?) {
        val savedtoast = Toast.makeText(this, msg, Toast.LENGTH_LONG)
        savedtoast.show()
    }

    private fun fillToastMsg(msg: String?) {
        val filltoast = Toast.makeText(this, msg, Toast.LENGTH_LONG)
        filltoast.show()
    }

    private fun hideCashLayout() {
        val creditTipLayout: LinearLayout = findViewById(R.id.creditTipLayout);
        creditTipLayout.visibility = View.GONE
    }

    private fun showCashLayout() {
        val creditTipLayout: LinearLayout = findViewById(R.id.creditTipLayout);
        creditTipLayout.visibility = View.VISIBLE
    }



}

