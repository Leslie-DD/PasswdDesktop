package model.action

sealed class LoginAction {


    class Login(
        val username: String,
        val password: String,
        val secretKey: String,
        val host: String,
        val port: Int,
        val saved: Boolean,
        val silentlyLogin: Boolean
    ) : LoginAction()

    class Signup(
        val username: String,
        val password: String,
        val host: String,
        val port: Int,
    ) : LoginAction()
}