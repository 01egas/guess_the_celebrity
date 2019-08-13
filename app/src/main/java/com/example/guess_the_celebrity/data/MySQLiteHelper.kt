package com.example.guess_the_celebrity.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.guess_the_celebrity.MainActivity

class MySQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_CELEBRITIE_TABLE = "CREATE TABLE ${SqlContract.CelebritieEntry.TABLE_NAME} (${SqlContract.CelebritieEntry._ID} INTEGER PRIMARY KEY AUTOINCREMENT, ${SqlContract.CelebritieEntry.COLUMN_NAME} TEXT NOT NULL, ${SqlContract.CelebritieEntry.COLUMN_IMAGE} TEXT NOT NULL);"
        db.execSQL(SQL_CREATE_CELEBRITIE_TABLE)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        onCreate(db)
    }
    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "celebrities.db"
    }
}




