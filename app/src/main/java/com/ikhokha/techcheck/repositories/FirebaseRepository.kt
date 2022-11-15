package com.ikhokha.techcheck.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.ikhokha.techcheck.data.entities.Product
import com.ikhokha.techcheck.utils.ValueEventResult
import com.ikhokha.techcheck.utils.awaitSingleValue
import com.ikhokha.techcheck.utils.observeValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

const val STORAGE_BASE_URL = "gs://the-busy-shop.appspot.com/"

@Singleton
class FirebaseRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    fbDatabase: FirebaseDatabase
) {

    private val database = fbDatabase.reference

    suspend fun signInUser(email: String, password: String): FirebaseUser =
        firebaseAuth.signInWithEmailAndPassword(email, password).await().user ?:
        throw FirebaseAuthException("", "")

    fun getProducts(): Flow<List<Product>> = database.observeValue()
            .map { data ->
                val list = mutableListOf<Product>()
                for (snap in data!!.children) {
                    val result = snap.getValue<Product>()
                    snap.key?.let {
                        result?.id = it
                        list.add(result!!)
                    }
                }
                list
            }.catch { it.stackTrace }

    suspend fun getItem(code: String): ValueEventResult =
        database.child(code).awaitSingleValue()

}