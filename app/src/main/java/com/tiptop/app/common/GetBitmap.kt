package com.tiptop.app.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.ParcelFileDescriptor
import android.util.Base64
import androidx.core.net.toUri
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

//fun getBitmap(book: Book): Bitmap? {
//    if (book.type == TYPE_PDF) {
//        getBitmapFromPref(book.id)?.let { return it }
//        val file: File? = APP.getFileStreamPath(book.id)
//        val inputStream: InputStream =
//            APP.contentResolver.openInputStream(file!!.toUri())!!
//        val outputFile = APP.getFileStreamPath("decrypted" + book.id)
//        Encryptor.decryptToFile(
//            book.keyStr,
//            book.specStr,
//            inputStream,
//            FileOutputStream(outputFile)
//        )
//        val pageNum = 0
//        val pdfiumCore = PdfiumCore(APP)
//        try {
//            val pdfDocument: PdfDocument = pdfiumCore.newDocument(openFile(outputFile))
//            pdfiumCore.openPage(pdfDocument, pageNum)
//            val width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNum)
//            val height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNum)
//            // ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
//            // RGB_565 - little worse quality, twice less memory usage
//            val bitmap = Bitmap.createBitmap(
//                width, height,
//                Bitmap.Config.RGB_565
//            )
//            pdfiumCore.renderPageBitmap(
//                pdfDocument, bitmap, pageNum, 0, 0,
//                width, height
//            )
//            //if you need to render annotations and form fields, you can use
//            //the same method above adding 'true' as last param
//            pdfiumCore.closeDocument(pdfDocument) // important!
//// save to share
//            saveBitmapToPref(bitmap, book.id)
//            return bitmap
//        } catch (ex: IOException) {
//            ex.printStackTrace()
//        }
//    }
//    return null
//}
//
//private fun openFile(file: File?): ParcelFileDescriptor? {
//    val descriptor: ParcelFileDescriptor = try {
//        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
//    } catch (e: FileNotFoundException) {
//        e.printStackTrace()
//        return null
//    }
//    return descriptor
//}
//
//private fun saveBitmapToPref(bitmap: Bitmap, bookId: String) {
//    val baos = ByteArrayOutputStream()
//    bitmap.compress(Bitmap.CompressFormat.PNG, 25, baos)
//    val compressImage = baos.toByteArray()
//    val sEncodedImage = Base64.encodeToString(compressImage, Base64.DEFAULT)
//    PREF.setString(KEY_LAST_BOOK_BITMAP + bookId, sEncodedImage)
//}
//
//private fun getBitmapFromPref(bookId: String): Bitmap? {
//    val encodedImage = PREF.getString(KEY_LAST_BOOK_BITMAP + bookId) ?: ""
//    val b = Base64.decode(encodedImage, Base64.DEFAULT)
//    val bitmapImage = BitmapFactory.decodeByteArray(b, 0, b.size)
//    return bitmapImage
//}