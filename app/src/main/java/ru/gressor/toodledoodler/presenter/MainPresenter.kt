package ru.gressor.toodledoodler.presenter

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import moxy.MvpPresenter
import ru.gressor.toodledoodler.model.MainModel
import ru.gressor.toodledoodler.view.MainView
import java.io.File

class MainPresenter : MvpPresenter<MainView>() {
    private val model = MainModel()
    private val compositeDisposable = CompositeDisposable()

    fun setFilePath(filePath: String) {
        model.file = File(filePath)
    }

    fun setConvertQuality(quality: Int) {
        model.quality = quality
    }

    fun buttonLoadPressed() {
        viewState.showPicture(model.file)
    }

    fun buttonConvertPressed() {
        model.convertPicture(model.file, model.quality)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    viewState.showSuccessMessage()
                    viewState.showPicture(it)
                },
                { throwable ->
                    throwable.message?.let { viewState.showError(it) }
                }
            )?.let {
                compositeDisposable.add(it)
            }
    }

    fun cancelConvert() {
        compositeDisposable.clear()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
    }
}