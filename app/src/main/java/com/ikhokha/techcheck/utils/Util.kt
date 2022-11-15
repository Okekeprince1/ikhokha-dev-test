package com.ikhokha.techcheck.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.TransitionDrawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.ikhokha.techcheck.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine


fun String.onlyLetters() = all { it.isLetter() }

infix fun <T> Boolean.then(param: T): T? = if (this) param else null

val <T> T.exhaustive: T
    get() = this

@ExperimentalCoroutinesApi
fun DatabaseReference.observeValue(): Flow<DataSnapshot?> =
    callbackFlow {
        val listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot).isSuccess
            }
        }
        addValueEventListener(listener)
        awaitClose { removeEventListener(listener) }
    }

sealed class ValueEventResult {
    class Success(val dataSnapshot: DataSnapshot): ValueEventResult()
    class Error(val error: DatabaseError): ValueEventResult()
}

@ExperimentalCoroutinesApi
suspend fun DatabaseReference.awaitSingleValue(onCancellation: ((cause: Throwable) -> Unit)? = null) =
    suspendCancellableCoroutine<ValueEventResult> { continuation ->

    val valueEventListener = object: ValueEventListener{
        override fun onCancelled(error: DatabaseError) {
            continuation.resume(ValueEventResult.Error(error = error), onCancellation)
        }
        override fun onDataChange(snapshot: DataSnapshot) {
            continuation.resume(ValueEventResult.Success(snapshot), onCancellation)
        }
    }
    addListenerForSingleValueEvent(valueEventListener)
    continuation.invokeOnCancellation { removeEventListener(valueEventListener) }
}

fun hasCameraPermission(context: Context) =
    (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED)
fun hasReadPermission(context: Context) =
    (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED)
fun hasWritePermission(context: Context) =
    (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED)

fun getTransition(ctx: Context) = TransitionDrawable( arrayOf(
            AppCompatResources.getDrawable(ctx, R.drawable.camera),
            AppCompatResources.getDrawable(ctx, R.drawable.scanner)
        ))