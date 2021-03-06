package br.com.instagram.home

import br.com.instagram.common.base.BasePresenter
import br.com.instagram.common.base.BaseView
import br.com.instagram.common.model.Post

interface Home {

    interface Presenter : BasePresenter {
        fun fetchFeed()
        fun logout()
        fun clear()
    }

    interface View : BaseView<Presenter> {
        fun showProgress(enabled: Boolean)
        fun displayRequestFailure(message: String)
        fun displayEmptyPosts()
        fun displayFullPosts(posts: List<Post>)
    }

}