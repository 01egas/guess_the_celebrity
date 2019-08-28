package com.example.guess_the_celebrity.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MySQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_CELEBRITIE_TABLE = "CREATE TABLE ${SqlContract.CelebrityEntry.TABLE_NAME} (${SqlContract.CelebrityEntry._ID} INTEGER PRIMARY KEY AUTOINCREMENT, ${SqlContract.CelebrityEntry.COLUMN_NAME} BLOB NOT NULL, ${SqlContract.CelebrityEntry.COLUMN_IMAGE} TEXT NOT NULL);"
        db.execSQL(SQL_CREATE_CELEBRITIE_TABLE)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL("DROP TABLE IF EXISTS ${SqlContract.CelebrityEntry.TABLE_NAME}")
        onCreate(db);
    }
    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "celebritiesDB"
    }
}




