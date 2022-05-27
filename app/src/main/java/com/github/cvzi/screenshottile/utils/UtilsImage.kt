package com.github.cvzi.screenshottile.utils

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.icu.text.SimpleDateFormat
import android.media.Image
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import androidx.documentfile.provider.DocumentFile
import com.github.cvzi.screenshottile.App
import com.github.cvzi.screenshottile.BuildConfig
import com.github.cvzi.screenshottile.R
import com.github.cvzi.screenshottile.activities.TakeScreenshotActivity
import java.io.File
import java.io.InputStream
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


/**
 * Created by cuzi (cuzi@openmail.cc) on 2019/08/23.
 */

const val UTILSIMAGEKT = "UtilsImage.kt"

/**
 * Copy rectangle of image content to new bitmap or complete image if rect is null.
 */
fun imageToBitmap(image: Image, rect: Rect? = null): Bitmap {
    val offset =
        (image.planes[0].rowStride - image.planes[0].pixelStride * image.width) / image.planes[0].pixelStride
    val w = image.width + offset
    val h = image.height
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    bitmap.copyPixelsFromBuffer(image.planes[0].buffer)
    return if (rect == null) {
        Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
    } else {
        Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
    }
}


/**
 * Create notification preview icon with appropriate size according to the device screen.
 */
fun resizeToNotificationIcon(bitmap: Bitmap, screenDensity: Int): Bitmap {
    val maxSize = (min(
        max(screenDensity / 2, TakeScreenshotActivity.NOTIFICATION_PREVIEW_MIN_SIZE),
        TakeScreenshotActivity.NOTIFICATION_PREVIEW_MAX_SIZE
    )).toDouble()

    val ratioX = maxSize / bitmap.width
    val ratioY = maxSize / bitmap.height
    val ratio = min(ratioX, ratioY)
    val newWidth = (bitmap.width * ratio).toInt()
    val newHeight = (bitmap.height * ratio).toInt()

    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false)
}

/**
 * Create notification big picture icon, bitmap cropped (centered)
 */
fun resizeToBigPicture(bitmap: Bitmap): Bitmap {
    return if (bitmap.height > TakeScreenshotActivity.NOTIFICATION_BIG_PICTURE_MAX_HEIGHT) {
        val offsetY =
            (bitmap.height - TakeScreenshotActivity.NOTIFICATION_BIG_PICTURE_MAX_HEIGHT) / 2
        Bitmap.createBitmap(
            bitmap, 0, offsetY, bitmap.width,
            TakeScreenshotActivity.NOTIFICATION_BIG_PICTURE_MAX_HEIGHT
        )
    } else {
        bitmap
    }
}


/**
 * Add image file and information to media store. (used only for Android P and lower)
 */
fun addImageToGallery(
    context: Context,
    filepath: String,
    title: String,
    description: String,
    mimeType: String = "image/jpeg",
    date: Date,
    dim: Point
): Uri? {
    val dateSeconds = date.time / 1000
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.TITLE, title)
        put(MediaStore.Images.Media.DESCRIPTION, description)
        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        put(MediaStore.Images.ImageColumns.DATE_ADDED, dateSeconds)
        put(MediaStore.Images.ImageColumns.DATE_MODIFIED, dateSeconds)
        if (dim.x > 0 && dim.y > 0) {
            put(MediaStore.Images.ImageColumns.WIDTH, dim.x)
            put(MediaStore.Images.ImageColumns.HEIGHT, dim.y)
        }
        @Suppress("DEPRECATION")
        put(MediaStore.MediaColumns.DATA, filepath)
    }
    return context.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
}


/**
 * Delete image. Return true on success, false on failure.
 * content:// Uris are deleted from MediaStore
 * file:// Uris are deleted from filesystem and MediaStore
 */
fun deleteImage(context: Context, uri: Uri?): Boolean {
    if (uri == null) {
        Log.e(UTILSIMAGEKT, "Could not delete file: uri is null")
        return false
    }

    when (uri.normalizeScheme().scheme) {
        "content" -> { // Android Q+
            return deleteContentResolver(context, uri)
        }

        "file" -> { // until Android P
            val path = uri.path
            if (path == null) {
                Log.e(UTILSIMAGEKT, "deleteImage() File path is null. uri=$uri")
                return false
            }

            val file = File(path)

            return deleteFileSystem(context, file)
        }
        else -> {
            Log.e(UTILSIMAGEKT, "deleteImage() Could not delete file. Unknown error. uri=$uri")
            return false
        }

    }
}

/**
 * Delete file via DocumentFile
 */
fun deleteDocumentFile(context: Context, uri: Uri): Boolean {
    val docDir = DocumentFile.fromSingleUri(context, uri)
    if (docDir != null) {
        if (!docDir.isFile) {
            return false
        }
        return try {
            docDir.delete()
        } catch (e: SecurityException) {
            Log.e(
                UTILSIMAGEKT,
                "SecurityException in deleteDocumentFile($context, $uri)"
            )
            false
        }
    } else {
        return false
    }
}

/**
 * Delete file via contentResolver
 */
fun deleteContentResolver(context: Context, uri: Uri): Boolean {
    val deletedRows = try {
        context.contentResolver.delete(uri, null, null)
    } catch (e: UnsupportedOperationException) {
        // Try to delete DocumentFile in custom directory
        if (App.getInstance().prefManager.screenshotDirectory != null) {
            return deleteDocumentFile(context, uri)
        }
        0
    } catch (e: SecurityException) {
        // Try to delete DocumentFile in custom directory
        if (App.getInstance().prefManager.screenshotDirectory != null) {
            return deleteDocumentFile(context, uri)
        }
        0
    }
    Log.v(
        UTILSIMAGEKT,
        "deleteImage() File deleted from MediaStore ($deletedRows rows deleted)"
    )
    return deletedRows > 0
}

/**
 * Delete file from file system and MediaStore
 */
fun deleteFileSystem(context: Context, file: File): Boolean {
    if (!file.exists()) {
        Log.w(UTILSIMAGEKT, "deleteImage() File does not exist: ${file.absoluteFile}")
        return false
    }

    if (!file.canWrite()) {
        Log.w(UTILSIMAGEKT, "deleteImage() File is not writable: ${file.absoluteFile}")
        return false
    }

    if (file.delete()) {
        if (BuildConfig.DEBUG) Log.v(
            UTILSIMAGEKT,
            "deleteImage() File deleted from storage: ${file.absoluteFile}"
        )
        val externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)

        @Suppress("DEPRECATION")
        val selection = MediaStore.Images.Media.DATA + " = ?"
        val queryArgs = arrayOf(file.absolutePath)
        context.contentResolver.query(
            externalContentUri,
            projection,
            selection,
            queryArgs,
            null
        )?.apply {
            if (moveToFirst()) {
                val id = getLong(getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                context.contentResolver.delete(contentUri, null, null)
                if (BuildConfig.DEBUG) Log.v(
                    UTILSIMAGEKT,
                    "deleteImage() File deleted from MediaStore: $contentUri"
                )
            }
            close()
        }
        return true
    } else {
        Log.w(UTILSIMAGEKT, "deleteImage() Could not delete file: ${file.absoluteFile}")
        return false
    }
}

/**
 * Rename image. Return true on success, false on failure.
 * content:// Uris are moved via MediaStore if possible otherwise copied and original file deleted
 * file:// Uris are renamed on filesystem and removed and added to MediaStore
 */
fun renameImage(context: Context, uri: Uri?, newName: String): Pair<Boolean, Uri?> {
    if (uri == null) {
        Log.e(UTILSIMAGEKT, "Could not move file: uri is null")
        return Pair(false, null)
    }

    val (newFileName, newFileTitle) = fileNameFileTitle(newName, compressionPreference(context))

    when (uri.normalizeScheme().scheme) {
        "content" -> { // Android Q+
            return renameContentResolver(context, uri, newFileTitle, newFileName)
        }

        "file" -> { // until Android P
            val path = uri.path
            if (path == null) {
                Log.e(UTILSIMAGEKT, "renameImage() File path is null. uri=$uri")
                return Pair(false, null)
            }

            val file = File(path)
            val dest = File(file.parent, newFileName)

            return renameFileSystem(context, file, dest)
        }
        else -> {
            Log.e(UTILSIMAGEKT, "renameImage() Could not move file. Unknown error. uri=$uri")
            return Pair(false, null)
        }

    }
}

/**
 * Move file via contentResolver
 */
fun renameContentResolver(
    context: Context,
    uri: Uri,
    newFileTitle: String,
    newFileName: String
): Pair<Boolean, Uri?> {
    // Try to rename file via contentResolver/MediaStore
    val updatedRows = try {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, newFileName)
        }
        context.contentResolver.update(uri, contentValues, null, null)
    } catch (e: UnsupportedOperationException) {
        Log.w(
            UTILSIMAGEKT,
            "renameContentResolver() MediaStore move failed: $e\nTrying copy and delete"
        )
        0
    }
    if (updatedRows > 0) {
        return Pair(true, uri)
    }
    // Try to copy the image to a new name
    val result = copyImageContentResolver(context, uri, newFileTitle)
    if (!result.first) {
        return Pair(false, null)
    }
    // Remove the old file
    if (!deleteImage(context, uri)) {
        Log.e(UTILSIMAGEKT, "renameContentResolver() deleteImage failed")
    }
    return result
}

/**
 * Copy file via contentResolver
 */
fun copyImageContentResolver(context: Context, uri: Uri, newName: String): Pair<Boolean, Uri?> {
    val inputStream: InputStream?
    try {
        inputStream = context.contentResolver.openInputStream(uri)
    } catch (e: Exception) {
        Log.e(UTILSIMAGEKT, "copyImageContentResolver() Could not open input stream: $e")
        return Pair(false, null)
    }
    if (inputStream == null) {
        Log.e(UTILSIMAGEKT, "copyImageContentResolver() input stream is null")
        return Pair(false, null)
    }

    val outputStreamResult = createOutputStream(
        context,
        newName,
        compressionPreference(context),
        Date(),
        Point(0, 0)
    )
    if (!outputStreamResult.success) {
        Log.e(
            UTILSIMAGEKT,
            "copyImageContentResolver() Could not open output stream: ${outputStreamResult.errorMessage}"
        )
        return Pair(false, null)
    }
    val outputStreamResultSuccess = (outputStreamResult as OutputStreamResultSuccess)
    val outputStream = outputStreamResultSuccess.fileOutputStream
    val success = try {
        val bytes = ByteArray(1024 * 32)
        var count = 0
        while (count != -1) {
            count = inputStream.read(bytes)
            if (count != -1) {
                outputStream.write(bytes, 0, count)
            }
        }
        outputStream.flush()
        inputStream.close()
        outputStream.close()
        true
    } catch (e: Exception) {
        Log.e(UTILSIMAGEKT, "copyImageContentResolver() Error while copying: $e")
        false
    } finally {
        inputStream.close()
        outputStream.close()
    }
    return if (success) {
        Pair(true, outputStreamResultSuccess.uri)
    } else {
        Pair(false, null)
    }
}


/**
 * Rename file from file system and remove and add in MediaStore
 */
fun renameFileSystem(
    context: Context,
    file: File,
    dest: File,
    dimensions: Point? = null
): Pair<Boolean, Uri?> {
    if (!file.exists()) {
        Log.w(UTILSIMAGEKT, "renameFileSystem() File does not exist: ${file.absoluteFile}")
        return Pair(false, null)
    }

    if (dest.exists()) {
        Log.w(UTILSIMAGEKT, "renameFileSystem() File already exists: ${dest.absoluteFile}")
        return Pair(false, null)
    }

    if (!file.canWrite()) {
        Log.w(UTILSIMAGEKT, "renameFileSystem() File is not writable: ${file.absoluteFile}")
        return Pair(false, null)
    }

    if (file.renameTo(dest)) {
        if (BuildConfig.DEBUG) Log.v(
            UTILSIMAGEKT,
            "renameFileSystem() File ${file.absoluteFile} moved to ${dest.absoluteFile}"
        )
        val externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)

        @Suppress("DEPRECATION")
        val selection = MediaStore.Images.Media.DATA + " = ?"
        val queryArgs = arrayOf(file.absolutePath)
        context.contentResolver.query(
            externalContentUri,
            projection,
            selection,
            queryArgs,
            null
        )?.apply {
            if (moveToFirst()) {
                val id = getLong(getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                context.contentResolver.delete(contentUri, null, null)
                if (BuildConfig.DEBUG) Log.v(
                    UTILSIMAGEKT,
                    "deleteImage() File deleted from MediaStore: $contentUri"
                )
            }
            close()
        }

        val date = Date()
        addImageToGallery(
            context,
            dest.absolutePath,
            context.getString(R.string.file_title),
            context.getString(
                R.string.file_description,
                SimpleDateFormat(
                    context.getString(R.string.file_description_simple_date_format),
                    Locale.getDefault()
                ).format(
                    date
                )
            ),
            compressionPreference(context).mimeType,
            date,
            dimensions ?: Point(0, 0)
        )

        return Pair(true, Uri.fromFile(dest))
    } else {
        Log.w(UTILSIMAGEKT, "deleteImage() Could not delete file: ${file.absoluteFile}")
        return Pair(false, null)
    }
}

/**
 * Try to get the height of the status bar or return a fallback approximation
 */
@Suppress("unused")
fun statusBarHeight(context: Context): Int {
    val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        context.resources.getDimensionPixelSize(resourceId)
    } else {
        ceil(24 * context.resources.displayMetrics.density).toInt()
    }
}

/**
 * navigationBarSize, appUsableScreenSize, realScreenSize
 * From: https://stackoverflow.com/a/29609679/
 *
 */
@Suppress("unused")
fun navigationBarSize(context: Context): Point {
    val appUsableSize: Point = appUsableScreenSize(context)
    val realScreenSize: Point = realScreenSize(context)
    return when {
        // navigation bar on the side
        appUsableSize.x < realScreenSize.x -> Point(
            realScreenSize.x - appUsableSize.x,
            appUsableSize.y
        )
        // navigation bar at the bottom
        appUsableSize.y < realScreenSize.y -> Point(
            appUsableSize.x,
            realScreenSize.y - appUsableSize.y
        )
        // navigation bar is not present
        else -> Point()
    }
}

/**
 * Screen size that can be used by windows
 */
fun appUsableScreenSize(context: Context): Point {
    val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = windowManager.currentWindowMetrics.bounds
        Point(
            bounds.width(),
            bounds.height()
        )
    } else {
        Point().apply {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getSize(this)
        }
    }
}

/**
 * Full screen size including cutouts
 */
fun realScreenSize(context: Context): Point {
    val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    return Point().apply {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                context.display?.mode?.let {
                    x = it.physicalWidth
                    y = it.physicalHeight
                } ?: run {
                    windowManager.currentWindowMetrics.bounds.let {
                        x = it.width()
                        y = it.height()
                    }
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                @Suppress("DEPRECATION")
                context.display?.getRealSize(this)
            }
            else -> {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getRealSize(this)
            }
        }
    }
}

/**
 * Tint image (for debugging)
 */
fun tintImage(bitmap: Bitmap, color: Long): Bitmap? {
    val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    Canvas(newBitmap).drawBitmap(bitmap, 0f, 0f, Paint().apply {
        colorFilter = PorterDuffColorFilter(color.toInt(), PorterDuff.Mode.ADD)
    })
    return newBitmap
}

/**
 * Split into fileName and fileTitle
 */
fun fileNameFileTitle(s: String, ext: String): Pair<String, String> {
    val extWithDot = ".$ext"
    val fileTitle: String
    val fileName: String
    if (s.endsWith(extWithDot)) {
        fileTitle = s.dropLast(extWithDot.length)
        fileName = s
    } else {
        fileTitle = s
        fileName = s + extWithDot
    }
    return Pair(fileName, fileTitle)
}

/**
 * Split into fileName and fileTitle
 */
fun fileNameFileTitle(s: String, compressionOptions: CompressionOptions): Pair<String, String> {
    return fileNameFileTitle(s, compressionOptions.fileExtension)
}
