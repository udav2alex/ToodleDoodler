package ru.gressor.toodledoodler.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.reactivex.rxjava3.core.Single
import ru.gressor.toodledoodler.DEFAULT_CONVERT_QUALITY
import java.io.File
import java.io.FileOutputStream

class MainModel(
    var file: File = File(""),
    var quality: Int = DEFAULT_CONVERT_QUALITY
) {
    fun convertPicture(file: File, convertQuality: Int): Single<File> {
        val path = file.absolutePath

        val newPath =
            if (path.endsWith(".jpg", true)) {
                path.substring(0..path.length - 4).plus("png")
            } else {
                path.plus(".png")
            }

        return Single.fromCallable {
            try {
                Thread.sleep(3000)
            } catch (e: InterruptedException) {
            }

            val bitmap = BitmapFactory.decodeFile(path)
            FileOutputStream(newPath)
                .use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, convertQuality, out)
                }

            return@fromCallable File(newPath)
        }
    }
}