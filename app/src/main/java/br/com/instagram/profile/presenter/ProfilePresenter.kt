package br.com.instagram.profile.presenter

import br.com.instagram.common.base.RequestCallback
import br.com.instagram.common.model.Database
import br.com.instagram.common.model.Post
import br.com.instagram.common.model.UserAuth
import br.com.instagram.profile.data.ProfileRepository
import br.com.instagram.profile.Profile

class ProfilePresenter(
    private var view: Profile.View?,
    private val repository: ProfileRepository
) : Profile.Presenter {

    var posts: List<Post>? = null
    var user: UserAuth? = null

    override fun subscribe(state: Profile.State?) {
        posts = state?.fetchUserPosts()

        if (posts != null) {
            if (posts!!.isEmpty()) {
                view?.displayEmptyPosts()
            } else {
                view?.displayFullPosts(posts!!)
            }
        } else {
            val userUUID = Database.sessionAuth?.uuid ?: throw RuntimeException("user not found")
            repository.fetchUserPosts(userUUID, object : RequestCallback<List<Post>> {
                override fun onSuccess(data: List<Post>) {
                    posts = data
                    if (data.isEmpty()) {
                        view?.displayEmptyPosts()
                    } else {
                        view?.displayFullPosts(data)
                    }
                }

                override fun onFailure(message: String) {
                    view?.displayRequestFailure(message)
                }

                override fun onComplete() {
                    view?.showProgress(false)
                }
            })
        }

        user = state?.fetchUserProfile()
        if (user != null) {
            view?.displayUserProfile(user!!)
        }
        else {
            view?.showProgress(true)
            val userUUID = Database.sessionAuth?.uuid ?: throw RuntimeException("user not found")
            repository.fetchUserProfile(userUUID, object : RequestCallback<UserAuth> {
                override fun onSuccess(data: UserAuth) {
                    user = data
                    view?.displayUserProfile(data)
                }

                override fun onFailure(message: String) {
                    view?.displayRequestFailure(message)
                }

                override fun onComplete() {
                }
            })
        }
    }

    override fun getState(): Profile.State {
        return ProfileState(posts, user)
    }
//
//  override fun fetchUserProfile() {
//    view?.showProgress(true)
//    val userUUID = Database.sessionAuth?.uuid ?: throw RuntimeException("user not found")
//    repository.fetchUserProfile(userUUID, object : RequestCallback<UserAuth> {
//      override fun onSuccess(data: UserAuth) {
//        state = data
//        view?.displayUserProfile(data)
//      }
//
//      override fun onFailure(message: String) {
//        view?.displayRequestFailure(message)
//      }
//
//      override fun onComplete() {
//      }
//    })
//  }

//  override fun fetchUserPosts() {
//    val userUUID = Database.sessionAuth?.uuid ?: throw RuntimeException("user not found")
//    repository.fetchUserPosts(userUUID, object : RequestCallback<List<Post>> {
//      override fun onSuccess(data: List<Post>) {
//        if (data.isEmpty()) {
//          view?.displayEmptyPosts()
//        } else {
//          view?.displayFullPosts(data)
//        }
//      }
//
//      override fun onFailure(message: String) {
//        view?.displayRequestFailure(message)
//      }
//
//      override fun onComplete() {
//        view?.showProgress(false)
//      }
//    })
//  }

    override fun onDestroy() {
        view = null
    }

}