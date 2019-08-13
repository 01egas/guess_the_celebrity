package com.example.guess_the_celebrity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.guess_the_celebrity.data.MySQLiteHelper
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {


    lateinit var mySQLiteHelper : MySQLiteHelper
    var arrayNames = ArrayList<String>()
    var arrayBitmap = ArrayList<Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("No data available")
        builder.setMessage("Need download data. This may take some time.")
        builder.setPositiveButton(android.R.string.ok){ dialog, which ->
            val myAsyncTask = MyAsyncTask()
            myAsyncTask.execute("http://www.posh24.se/kandisar")
            mySQLiteHelper = MySQLiteHelper(this)
        }
        builder.show()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.itemReload -> {
                Toast.makeText(this, arrayNames.size.toString(), Toast.LENGTH_LONG).show()
                ivPhoto.setImageBitmap(arrayBitmap[16])
                true
            }
            R.id.itemDownload -> {
                    val myAsyncTask = MyAsyncTask()
                    myAsyncTask.execute("http://www.posh24.se/kandisar")

                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    inner class MyAsyncTask : AsyncTask<String, String, Void>() {


        override fun doInBackground(vararg params: String?): Void? {


            var resultHttpText = ""
            val url: URL
            var urlConnection: HttpURLConnection
            try {
                url = URL(params[0])
                urlConnection = url.openConnection() as HttpURLConnection
                var inputStream = urlConnection.inputStream
                var reader = InputStreamReader(inputStream)
                var currentChar: Char
                var data = reader.read()


                while (data != -1) {
                    currentChar = data.toChar()
                    resultHttpText += currentChar
                    data = reader.read()
                }
//                  val pattern = Pattern.compile("<a href=\"/(.*?)\"><div class=\"image\">")


//                val pattern = Pattern.compile("<a href=\"/(.*?)\">")
                /*
                * "http://cdn.posh24.se/images/:profile/04e3e4db7b764c66b5437de543f1c652c"
                * "http://cdn.posh24.se/images/:big_profile/04e3e4db7b764c66b5437de543f1c652c/kylie_jenner.jpg"
                *.replace("profile", "big_profile")
                *
                *
                * */
                val pattern = Pattern.compile("<img src=\"(.*?)\"/>")
                val matcher = pattern.matcher(resultHttpText)
                var itemString = ""
                var valueImageString: String
                var valueName: String
                var httpConnection : HttpURLConnection
                var inputStreamImage : InputStream
                lateinit var valueBitmap : Bitmap
                var counter = 0
                while (matcher.find()) {
                    itemString = matcher.group(1)
                    valueImageString = itemString.substring(0, itemString.indexOf("\"")).replace("profile", "big_profile")
                    valueName = itemString.substring(itemString.lastIndexOf("\"")+1)
                    var url = URL(valueImageString)
                    httpConnection = url.openConnection() as HttpURLConnection

                    httpConnection.connect()
                    try {
                        inputStreamImage = httpConnection.inputStream
                    } catch (e: FileNotFoundException) {
                        continue
                    }


                    valueBitmap = BitmapFactory.decodeStream(inputStreamImage)


                    arrayNames.add(valueName)
                    arrayBitmap.add(valueBitmap)
                    counter++

                    var byteArrayOutputStream = ByteArrayOutputStream()
                    valueBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)


                }
                publishProgress("the end")


            } catch (e: Exception) {
                publishProgress(e.printStackTrace().toString())
            }
            return null
        }

        override fun onProgressUpdate(vararg values: String?) {
            super.onProgressUpdate(*values)
            Log.i("myTag", values[0])
            ivPhoto.setImageBitmap(arrayBitmap[15])

        }

    }

    private fun getBitmapOnHtml(string: String) : Bitmap {
        val url = URL(string)
        val httpConnection : HttpURLConnection = url.openConnection() as HttpURLConnection

        httpConnection.connect()
        val inputStream = httpConnection.inputStream

        val result = BitmapFactory.decodeStream(inputStream)

        return result
    }

    fun onClick(view: View) {
        ivPhoto.setImageBitmap(getBitmapOnHtml("https://upload.wikimedia.org/wikipedia/commons/thumb/3/36/Skater_Tony_Hawk.jpg/1024px-Skater_Tony_Hawk.jpg"))
    }

    /*
    *            var result = ""
            var url : URL
            var urlConnection : HttpURLConnection

            try {
                url = URL(params[0])

                urlConnection = url.openConnection() as HttpURLConnection

                var inputStream = urlConnection.inputStream
                var reader = InputStreamReader(inputStream)
                var current: Char
                var data = reader.read()
                while (data != -1) {
                    current = data.toChar()
                    result += current
                    data = reader.read()
                }

                val p1 = Pattern.compile("<img src=\"(.*?)\"alt=\"(.*?)\"")
                val p2 = Pattern.compile("alt=\"(.*?)\"")

                val m1 = p1.matcher(result)
                val m2 = p2.matcher(result)

                var count = 0
                while (m1.find() && m2.find()) {
                    arrayPhoto.set(count, m1.group(1))
                    arrayNames.set(count, m2.group(1))
                    Log.i("myTag", count.toString())
                    count++
                }
                return result
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("myTag","failed task")
                return e.printStackTrace().toString()
            }
 */


}
