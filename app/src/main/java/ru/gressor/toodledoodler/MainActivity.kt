package ru.gressor.toodledoodler

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter
import ru.gressor.toodledoodler.databinding.ActivityMainBinding
import ru.gressor.toodledoodler.presenter.MainPresenter
import ru.gressor.toodledoodler.view.MainView
import java.io.File
import java.io.FileOutputStream

class MainActivity : MvpAppCompatActivity(), MainView {
    private val presenter by moxyPresenter { MainPresenter() }
    private lateinit var binding: ActivityMainBinding

    private val compositeDisposable = CompositeDisposable()
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.filePath.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.setFilePath(s.toString())
            }
        })

        binding.buttonLoad.setOnClickListener {
            if (hasFileIOPermissions()) {
                presenter.buttonLoadPressed()
            } else {
                requestFileIOPermissions()
            }
        }

        binding.convertQuality.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isDigitsOnly()) {
                    presenter.setConvertQuality(s.toString().toInt())
                }
            }
        })

        binding.buttonConvert.setOnClickListener {
            if (hasFileIOPermissions()) {
                presenter.buttonConvertPressed()
            } else {
                requestFileIOPermissions()
            }
        }

        presenter.setFilePath(binding.filePath.text.toString())
    }

    override fun convertPicture(file: File, convertQuality: Int) {
        val path = file.absolutePath
        val newPath = if (path.endsWith(".jpg", true)) {
            path.substring(0..path.length-4).plus("png")
        } else {
            path.plus(".png")
        }

        Completable.fromAction {
            val bitmap = BitmapFactory.decodeFile(path)
            FileOutputStream(newPath)
                .use { out ->
                    try {
                        Thread.sleep(3000)
                    } catch (e: InterruptedException) {
                    }
                    bitmap.compress(Bitmap.CompressFormat.PNG, convertQuality, out)
                }
        }
            ?.subscribeOn(Schedulers.computation())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.doOnSubscribe { disposable ->
                dialog = createCancelDialog { dialogInterface, _ ->
                    disposable?.let { if (!it.isDisposed) it.dispose() }
                    dialogInterface.dismiss()
                }
                dialog?.show()
            }
            ?.doFinally {
                dialog?.dismiss()
            }
            ?.subscribe(
                {
                    Toast.makeText(this, getString(R.string.well_done), Toast.LENGTH_LONG).show()
                },
                { throwable ->
                    Toast.makeText(this, throwable.message, Toast.LENGTH_LONG).show()
                }
            )?.let {
                compositeDisposable.add(it)
            }
    }

    override fun loadPicture(file: File) {
        Single.fromCallable {
            BitmapFactory.decodeFile(file.absolutePath)
        }
            ?.subscribeOn(Schedulers.computation())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.doOnSubscribe { disposable ->
                dialog = createCancelDialog { dialogInterface, _ ->
                    disposable?.let { if (!it.isDisposed) it.dispose() }
                    dialogInterface.dismiss()
                }
                dialog?.show()
            }
            ?.doFinally {
                dialog?.dismiss()
            }
            ?.subscribe(
                { bitmap ->
                    binding.imageView.setImageBitmap(bitmap)
                },
                { throwable ->
                    Toast.makeText(this, throwable.message, Toast.LENGTH_LONG).show()
                }
            )?.let {
                compositeDisposable.add(it)
            }
    }

    private fun createCancelDialog(cancelOperation: (DialogInterface, Int) -> Unit) =
        AlertDialog.Builder(this).apply {
            title = getString(R.string.work_in_progress)
            setMessage(getString(R.string.cancel_message))
            setPositiveButton("Stop operation", cancelOperation)
        }.create()

    override fun setFilePath(filePath: String) = binding.filePath.setText(filePath)

    override fun setConvertQuality(quality: Int) = binding.convertQuality.setText(quality)

    private fun hasFileIOPermissions(): Boolean {
        val read = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val write = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        return read && write
    }

    private fun requestFileIOPermissions() {
        // TODO: deal with rationale & "Never ask again"

        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (!shouldShowRationale) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // TODO: process permission denial
        if (requestCode == PERMISSIONS_REQUEST_CODE
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permissions granted!")
                .setMessage("Please push button again!")
                .setPositiveButton(android.R.string.ok, null)
                .create().show()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }
}