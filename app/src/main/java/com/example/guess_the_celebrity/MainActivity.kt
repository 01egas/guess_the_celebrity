package com.example.guess_the_celebrity

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.guess_the_celebrity.data.MySQLiteHelper
import com.example.guess_the_celebrity.data.SqlContract
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern
import android.database.DatabaseUtils
import kotlin.random.Random


class MainActivity : AppCompatActivity() {


    lateinit var mySQLiteHelper : MySQLiteHelper
    var arrayNames = ArrayList<String>()
    var arrayBitmap = ArrayList<Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i("myTag", "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")

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

    fun generateQuestion() {
        var arrayList = ArrayList<String>()

        var db = mySQLiteHelper.readableDatabase
        val numberOfRows = DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " + SqlContract.CelebrityEntry.TABLE_NAME, null).toInt()

        var rightAsk = Random.nextInt(0, numberOfRows - 1)
        arrayList.add(0, rightAsk.toString())
        for (i in 1..3) {
            var n = Random.nextInt(0, numberOfRows - 1)
            arrayList.add(n.toString())
        }
        arrayList.shuffle()

        var cursor = db.rawQuery("select * from celebrities where _ID IN (${arrayList[0]}, ${arrayList[1]}, ${arrayList[2]}, ${arrayList[3]})", null)

        while (cursor.moveToNext()) {
            Log.i("myTag", cursor.getInt(0).toString()+ "  " + cursor.getString(1))


        }



        if (cursor.moveToFirst()) {
            var byteArrayImage =  cursor.getBlob(2)
            var nameImage = cursor.getString(1)

            var bitmap = BitmapFactory.decodeByteArray(byteArrayImage, 0, byteArrayImage.size)
            ivPhoto.setImageBitmap(bitmap)
        }

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
            R.id.itemPutImage -> {
                var db = mySQLiteHelper.readableDatabase
                var cursor = db.query(SqlContract.CelebrityEntry.TABLE_NAME, null, "_id = 15", null, null, null, null)

                if (cursor.moveToFirst()) {
                    var byteArray =  cursor.getBlob(2)
                    var bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    ivPhoto.setImageBitmap(bitmap)
                }

                true
            }
            R.id.itemGenerateQuestion -> {
                generateQuestion()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    inner class MyAsyncTask : AsyncTask<String, String, Void>() {


        override fun doInBackground(vararg params: String?): Void? {
            Log.i("myTag", "start asynctask")

            var db = mySQLiteHelper.writableDatabase
            db.execSQL("DROP TABLE IF EXISTS ${SqlContract.CelebrityEntry.TABLE_NAME}")
            mySQLiteHelper.onUpgrade(db, 0, 1)
            var cursor = db.query(SqlContract.CelebrityEntry.TABLE_NAME, null, null, null, null, null, null)
            Log.i("myTag", "number of records in start ${cursor.count}")


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

                var byteArrayOutputStream : ByteArrayOutputStream
                var contentValues : ContentValues


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
                    Log.i("myTag", valueName)
                    arrayBitmap.add(valueBitmap)




                    counter++

                    byteArrayOutputStream = ByteArrayOutputStream()
                    valueBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    contentValues = ContentValues()
                    contentValues.put(SqlContract.CelebrityEntry.COLUMN_NAME, valueName)
                    contentValues.put(SqlContract.CelebrityEntry.COLUMN_IMAGE, byteArrayOutputStream.toByteArray())
                    db.insert(SqlContract.CelebrityEntry.TABLE_NAME, null, contentValues)
//                    var cursor = db.query(SqlContract.CelebrityEntry.TABLE_NAME, null, null, null, null, null, null)
                    val numRows = DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " + SqlContract.CelebrityEntry.TABLE_NAME, null).toInt()
                    Log.i("myTag", "number of records in the process $numRows")




                }
                val numRows = DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " + SqlContract.CelebrityEntry.TABLE_NAME, null).toInt()

                publishProgress(numRows.toString())


            } catch (e: Exception) {
                publishProgress(e.printStackTrace().toString())
            }
            return null
        }

        override fun onProgressUpdate(vararg values: String?) {
//            Toast.makeText(this, "data download", Toast.LENGTH_LONG).show()
//            super.onProgressUpdate(*values)
            Log.i("myTag", "data download, number of records" + values[0])

//            ivPhoto.setImageBitmap(arrayBitmap[15])

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




}
