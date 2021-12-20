package br.com.instagram.profile.data

import br.com.instagram.common.base.RequestCallback
import br.com.instagram.common.model.Post
import br.com.instagram.common.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query

class FireProfileDataSource : ProfileDataSource {

    override fun fetchUserProfile(userUUID: String, callback: RequestCallback<Pair<User, Boolean?>>) {
        FirebaseFirestore.getInstance()
            .collection("/users")
            .document(userUUID)
            .get()
            .addOnSuccessListener { res ->
                val user = res.toObject(User::class.java)

                when (user) {
                    null -> {
                        callback.onFailure("Falha ao converter usuário")
                    }
                    else -> {
                        if (user.uuid == FirebaseAuth.getInstance().uid) {
                            callback.onSuccess(Pair(user, null))
                        } else {
                            FirebaseFirestore.getInstance()
                                .collection("/followers")
                                .document(userUUID)
                                .get()
                                .addOnSuccessListener { response ->
                                    if (!response.exists()) {
                                        callback.onSuccess(Pair(user, false))
                                    } else {
                                        val list = response.get("followers") as List<String>
                                        callback.onSuccess(Pair(user, list.contains(FirebaseAuth.getInstance().uid)))
                                    }

                                }
                                .addOnFailureListener { exception ->
                                    callback.onFailure(exception.message ?: "Falha ao buscar seguidores")
                                }
                                .addOnCompleteListener {
                                    callback.onComplete()
                                }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                callback.onFailure(exception.message ?: "Falha ao buscar usuário")
            }
    }

    override fun fetchUserPosts(userUUID: String, callback: RequestCallback<List<Post>>) {
        FirebaseFirestore.getInstance()
            .collection("posts")
            .document(userUUID)
            .collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { res ->
                val documents = res.documents
                val posts = mutableListOf<Post>()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    post?.let {
                        posts.add(it)
                    }
                }
                callback.onSuccess(posts)
            }
            .addOnFailureListener { exception ->
                callback.onFailure(exception.message ?: "Falha ao buscar posts")
            }
            .addOnCompleteListener {
                callback.onComplete()
            }
    }

    override fun followUser(userUUID: String, isFollow: Boolean, callback: RequestCallback<Boolean>) {
        val uid = FirebaseAuth.getInstance().uid ?: throw RuntimeException("Usuário não logado!!!")
        FirebaseFirestore.getInstance()
            .collection("/followers")
            .document(userUUID)
            .update(
                "followers", if (isFollow) FieldValue.arrayUnion(uid)
                else FieldValue.arrayRemove(uid)
            )
            .addOnSuccessListener { res ->
                followingCounter(uid, isFollow)
                followersCounter(userUUID)
                callback.onSuccess(true)
            }
            .addOnFailureListener { exception ->
                val err = exception as? FirebaseFirestoreException

                if (err?.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                    FirebaseFirestore.getInstance()
                        .collection("/followers")
                        .document(userUUID)
                        .set(
                            hashMapOf(
                                "followers" to listOf(uid)
                            )
                        )
                        .addOnSuccessListener { res ->
                            followingCounter(uid, isFollow)
                            followersCounter(userUUID)
                            callback.onSuccess(true)
                        }
                        .addOnFailureListener { exception ->
                            callback.onFailure(exception.message ?: "Falha ao criar seguidor")
                        }
                }

                callback.onFailure(exception.message ?: "Falha ao atualizar seguidor")
            }
            .addOnCompleteListener {
                callback.onComplete()
            }
    }

    private fun followingCounter(uid: String, isFollow: Boolean) {
        val meRef = FirebaseFirestore.getInstance()
            .collection("/users")
            .document(uid)

        if (isFollow) meRef.update("following", FieldValue.increment(1))
        else meRef.update("following", FieldValue.increment(-1))
    }

    private fun followersCounter(uid: String) {
        val meRef = FirebaseFirestore.getInstance()
            .collection("/users")
            .document(uid)

        FirebaseFirestore.getInstance()
            .collection("/followers")
            .document(uid)
            .get()
            .addOnSuccessListener { response ->
                if (response.exists()) {
                    val list = response.get("followers") as List<String>
                    meRef.update("followers", list.size)
                }
            }

    }

}