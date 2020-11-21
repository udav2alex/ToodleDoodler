package ru.gressor.toodledoodler.model

import ru.gressor.toodledoodler.DEFAULT_CONVERT_QUALITY
import java.io.File

data class MainModel(
    var file: File = File(""),
    var quality: Int = DEFAULT_CONVERT_QUALITY
) {

}