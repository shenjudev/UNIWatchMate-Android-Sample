package com.sjbt.sdk.utils

import android.content.Context
import android.util.Log
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.util.*

fun readSportTypeJsonFromAssets(context: Context): String? {
    val assetManager = context.assets
    var istream: InputStream? = null
    var jsonData = ""

    try {
        istream = assetManager.open("sports_data.json")
        val reader = Scanner(istream, "UTF-8")
        while (reader.hasNextLine()) {
            jsonData += reader.nextLine() + "\n"
        }
    } catch (e: FileNotFoundException) {
        Log.e("JSON_FILE", "File not found", e)
    } catch (e: UnsupportedEncodingException) {
        Log.e("JSON_FILE", "Unsupported Encoding", e)
    } finally {
        try {
            istream?.close()
        } catch (e: IOException) {
            Log.e("JSON_FILE", "Error in closing the stream", e)
        }
    }
    return jsonData
}