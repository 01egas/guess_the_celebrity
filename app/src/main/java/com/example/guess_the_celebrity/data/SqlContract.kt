package com.example.guess_the_celebrity.data

import android.provider.BaseColumns

class SqlContract {

    class CelebrityEntry : BaseColumns {

        companion object {
            const val TABLE_NAME = "celebrities"
            const val _ID = BaseColumns._ID
            const val COLUMN_NAME = "name"
            const val COLUMN_IMAGE = "image"
        }

    }
}