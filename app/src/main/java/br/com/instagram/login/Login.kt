package br.com.instagram.login

import androidx.annotation.StringRes
import br.com.instagram.common.base.BasePresenter
import br.com.instagram.common.base.BaseView

interface Login {

    interface Presenter : BasePresenter {
        fun login(email: String, password: String)
    }

    interface View : BaseView<Presenter> {
        fun showProgress(enabled: Boolean)
        fun displayEmailFailure(@StringRes emailError: Int?)
        fun displayPasswordFailure(@StringRes passwordError: Int?)
        fun onUserAuthenticated()
        fun onUserUnauthorized(message: String)
    }

}