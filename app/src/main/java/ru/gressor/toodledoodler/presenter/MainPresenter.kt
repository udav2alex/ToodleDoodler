package ru.gressor.toodledoodler.presenter

import moxy.MvpPresenter
import ru.gressor.toodledoodler.model.MainModel
import ru.gressor.toodledoodler.view.MainView
import java.io.File

class MainPresenter : MvpPresenter<MainView>() {
    private val model = MainModel()

    fun setFilePath(filePath: String) {
        model.file = File(filePath)
    }

    fun setConvertQuality(quality: Int) {
        model.quality = quality
    }

    fun buttonLoadPressed() {
        viewState.loadPicture(model.file)
    }

    fun buttonConvertPressed() {
        viewState.convertPicture(model.file, model.quality)
    }

    fun convertFinished(filePath: String) {
        viewState.loadPicture(File(filePath))
    }
}