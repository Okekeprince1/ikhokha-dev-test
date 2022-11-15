package com.ikhokha.techcheck.viewmodels

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ikhokha.techcheck.repositories.LocalRepository
import com.ikhokha.techcheck.utils.PdfCreator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val app: Application,
    private val localRepo: LocalRepository
): AndroidViewModel(app) {

    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page
    private var currentPageNumber: Int = 0
    private val _bmpPdf: MutableLiveData<Bitmap> = MutableLiveData()
    val bmpPdf: LiveData<Bitmap> = _bmpPdf
    private val orderChannel = Channel<OrderEvents>()
    val orderEvent = orderChannel.receiveAsFlow()
    private lateinit var file: File

    init { getSummary() }

    fun deleteAll() = viewModelScope.launch(IO) { localRepo.deleteAll() }

    private fun getSummary() = viewModelScope.launch(IO) {
        localRepo.basketItems.collect { items ->
            val orderLocation = PdfCreator(app, items, 1).create()
            orderLocation?.let { openRenderer(it) }
        }
    }

    @Throws(IOException::class)
    fun openRenderer(orderFile: File) {
        file = orderFile
        val orderUri = Uri.fromFile(orderFile)
        Log.d("myT", "openRenderer: $orderUri")
        val fileDescriptor = app.contentResolver.openFileDescriptor(orderUri, "r")
        pdfRenderer = PdfRenderer(fileDescriptor!!)
        currentPage = pdfRenderer.openPage(currentPageNumber)

        val bitmap = createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        _bmpPdf.postValue(bitmap)
    }

    @Throws(IOException::class)
    fun closeRenderer() {
        currentPage.close()
        pdfRenderer.close()
    }

    fun share() = viewModelScope.launch(IO) {
        if (this@OrderViewModel::file.isInitialized) {
            val uri = FileProvider.getUriForFile(
                app, "com.ikhokha.techcheck.fileprovider", file)
            val share = Intent()
            share.action = Intent.ACTION_SEND
            share.type = "application/pdf"
            share.putExtra(Intent.EXTRA_STREAM, uri)
            orderChannel.send(OrderEvents.Share(Intent.createChooser(share, "Share file")))
        } else orderChannel.send(OrderEvents.ShareError)
    }

    sealed class OrderEvents {
        class Share(val intent: Intent): OrderEvents()
        object ShareError: OrderEvents()
    }
}