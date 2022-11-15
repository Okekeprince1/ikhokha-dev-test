package com.ikhokha.techcheck.utils

import android.app.Application
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.ContextCompat
import com.ikhokha.techcheck.R
import com.ikhokha.techcheck.data.entities.Product
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class PdfCreator(
    private val app: Application,
    private val items: List<Product>,
    private val orderNumber: Int
) {

    private val pageHeight = 1120
    private val pageWidth = 792
    private lateinit var bmp: Bitmap
    private lateinit var scaledBmp: Bitmap

    fun create(): File? {
        val pdfDocument = PdfDocument()
        val paint = Paint()

        bmp = BitmapFactory.decodeResource(app.resources, R.drawable.basket)
        scaledBmp = Bitmap.createScaledBitmap(bmp, 140, 140, false);

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        val myPage = pdfDocument.startPage(pageInfo)

        var canvas: Canvas = myPage.canvas

        canvas.drawBitmap(scaledBmp, 50f, 50f, paint)

        canvas = createContent(canvas)

        pdfDocument.finishPage(myPage)

        val name = app.getString(R.string.order_name, orderNumber)
        val path = app.externalMediaDirs[0].absolutePath

        val dir = File(path, "/pdf")
        if (!dir.exists()) {
            val success = dir.mkdirs()
            if (!success) {
                return null
            }
        }
        val file = File(dir , name)
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            return file
        } catch (e: IOException) { e.printStackTrace() }

        pdfDocument.close()
        return null
    }

    private fun createContent(canvas: Canvas): Canvas {
        val text = Paint()
        text.apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 35f
            color = ContextCompat.getColor(app, R.color.black)
        }

        canvas.drawText("Order Receipt", 210f, 80f, text)

        val date: Date = Calendar.getInstance().time

        val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        canvas.drawText(df.format(date), 550f, 80f, text)

        canvas.drawText("Description", 50f, 250f, text)
        canvas.drawText("Quantity", 300f, 250f, text)
        canvas.drawText("Item total", 600f, 250f, text)

        var yPos = 310f
        var total = 0.0
        items.forEach {
            val itemTotal = it.price?.times(it.quantity)
            canvas.drawText(it.description!!, 50f, yPos, text)
            canvas.drawText("x ${it.quantity}" , 300f, yPos, text)
            canvas.drawText(app.getString(R.string.product_price, itemTotal), 600f, yPos, text)
            yPos += 60f
            total += itemTotal ?: 0.0
        }

        canvas.drawText("Total:", 50f, 1050f, text)
        canvas.drawText(app.getString(R.string.product_price, total), 600f, 1050f, text)
        return addBorder(canvas)
    }

    private fun addBorder(canvas: Canvas): Canvas {
        val border = Paint()
        val rect = Rect(20, 20, 772, 1100)
        border.style = Paint.Style.STROKE;
        border.strokeWidth = 3f
        border.color = Color.BLACK;
        canvas.drawRect(rect, border);
        return canvas
    }

}