package com.example.tiptracker

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.*


/**
 * Creates the ViewLog view which will show any items logged for the selected date.
 */
class ViewLog : AppCompatActivity() {



    /**
     * Loads the data from the local file each time the view is opened
     */
    override fun onResume() {
        super.onResume()
        loadFile()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_log)
        loadFile();

        val values = findViewById<TextView>(R.id.values)


    }

    private fun loadFile() {
        val text = readFromFile(applicationContext)
        parseText(text)
    }

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

    /**
     * Parses the data from the local files for the given date
     * @param fileText - File from local storage containing all item data
     */
    private fun parseText(fileText: String) {

        val dateValue = intent.getStringExtra("date")
        val regex = Regex("DATE:"+dateValue+":(.*?):END:")
        val matches = regex.findAll(fileText)
        val itemsInputted = matches.map { it.groupValues[0] }.joinToString()
        setValues(itemsInputted)
    }

    /**
     * Displays each item for the given meal for the selected date
     * @param fileText - Text pared from local storage containing
     * only information for the given data.
     */
    private fun setValues(fileText: String) {
        val values = findViewById<TextView>(R.id.values)
        val cashValue = findViewById<TextView>(R.id.cashTipsAmount)
        val creditValue = findViewById<TextView>(R.id.cardTipsAmount)
        val totalMade = findViewById<TextView>(R.id.totalMadeAmount)
        val numDeliveries = findViewById<TextView>(R.id.numberDeliveries)
        val oweStore = findViewById<TextView>(R.id.oweStoreAmount)
        val totalCashOrders = findViewById<TextView>(R.id.cashOrderTotal)

        val cashTip = Regex(":TIPM:(.*?):CHARGE:")
        val cashTipMatches = cashTip.findAll(fileText)
        var cashTipsInput = cashTipMatches.map { it.groupValues[1] }.joinToString().replace(", ", "+")
        var cashTipsDouble = findSumFloat(cashTipsInput)
        //cashTipsInput = findSumFloat(cashTipsInput).toString()
        cashValue.setText("$" + cashTipsDouble.toString())

        val creditTip = Regex(":TIPC:(.*?):TIPM:")
        val creditTipMatches = creditTip.findAll(fileText)
        var creditTipsInput = creditTipMatches.map { it.groupValues[1] }.joinToString().replace(", ", "+")
        var creditTipsDouble = findSumFloat(creditTipsInput)
        //creditTipsInput = findSumFloat(creditTipsInput).toString()
        creditValue.setText("$" + creditTipsDouble.toString())


        val numDels = Regex(":CHARGE:(.*?):STOP:")
        val numDelsMatches = numDels.findAll(fileText)
        numDeliveries.setText((numDelsMatches.count()).toString())

        val numDelsCharges = Regex(":CHARGE:(.*?):STOP:")
        val numDelsChargesMatches = numDelsCharges.findAll(fileText)
        var numDelsInput = numDelsChargesMatches.map { it.groupValues[1] }.joinToString().replace(", ", "+")
        val numDelChargesDouble = findSumFloat(numDelsInput)

        val totalCashOrder = Regex(":OPRICEM:(.*?):TIPC:")
        val totalCashOrderMatches = totalCashOrder.findAll(fileText)
        var totalCashOrderInput = totalCashOrderMatches.map { it.groupValues[1] }.joinToString().replace(", ", "+")
        val totalCashOrderDouble = findSumFloat(totalCashOrderInput)
        totalCashOrders.setText("$" + totalCashOrderDouble.toString())

        val totalCreditTips = Regex(":TIPC:(.*?):TIPM:")
        val totalCreditTipsMatches = totalCreditTips.findAll(fileText)
        var totalCreditTipsInput = totalCreditTipsMatches.map { it.groupValues[1] }.joinToString().replace(", ", "+")
        val totalCreditTipsDouble = findSumFloat(totalCreditTipsInput)

        var totalMadeInput = (cashTipsDouble + creditTipsDouble + numDelChargesDouble).toString()
        totalMade.setText("$" + totalMadeInput)

        oweStore.setText("$" + (totalCashOrderDouble - totalCreditTipsDouble - numDelChargesDouble).toString())


        val valuesRegex = Regex("ADDRESS:(.*?):END:")
        val valuesMatches = valuesRegex.findAll(fileText)
        var valuesInput = valuesMatches.map { it.groupValues[1] }.joinToString().replace(",", "").replace(":OTYPEM:", " - ").replace(":OTYPEC:", " - ").replace(":OPRICEM:", " - Order:$").replace(":OPRICEC:", " - Order:$").replace(":TIPM:", " - Cash Tip:$").replace(":TIPC:", " - Credit Tip:$").replace(":TIPM:", " - Cash Tip:$").replace(":CHARGE:", " - Charge:$").replace(":STOP:","\n");
        values.setText(" " + valuesInput)

    }


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

    fun findSumFloat(str: String): Double {
        var temp = "0"
        var sum = 0.0

        // read each character in input string
        for (i in 0 until str.length) {
            val ch = str[i]

            if (Character.isDigit(ch) || ch == '.') temp += ch else {
                sum += temp.toDouble()
                temp = "0"
            }
        }
        return sum + temp.toDouble()
    }




}