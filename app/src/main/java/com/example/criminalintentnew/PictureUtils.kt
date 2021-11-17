package com.example.criminalintentnew

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point

fun getScalesBitmap(path: String, destWith: Int, destHeight: Int): Bitmap{
    //чтение размеров изображения на диске

    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    //выясняем, на сколько нужно уменьшить
    var inSampleSize = 1
    if(srcHeight > destHeight || srcWidth > destWith){
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWith

        val sampleScale = if(heightScale > widthScale)
            heightScale
        else
            widthScale
        inSampleSize= Math.round(sampleScale)
    }

    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize

    //чтение и создание окончательного растрового изображения
    return BitmapFactory.decodeFile(path, options)
}


fun getScaleBitmap(path: String, activity: Activity): Bitmap{
    val size = Point()
    activity.windowManager.defaultDisplay.getSize(size)
    return  getScalesBitmap(path, size.x, size.y)
}