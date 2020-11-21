package ru.gressor.toodledoodler.view

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import java.io.File

@AddToEndSingle
interface MainView : MvpView {

    fun setFilePath(filePath: String)
    fun setConvertQuality(quality: Int)

    fun showPicture(file: File)
    fun showSuccessMessage()
    fun showError(string: String)
}