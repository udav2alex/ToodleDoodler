package ru.gressor.toodledoodler.view

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import java.io.File

@AddToEndSingle
interface MainView : MvpView {

    fun setFilePath(filePath: String)
    fun setConvertQuality(quality: Int)
    fun loadPicture(file: File)
    fun convertPicture(file: File, convertQuality: Int)
}