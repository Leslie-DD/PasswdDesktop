package passwds.model

sealed interface UiEffect {

    object LoginScreen : UiEffect

    object PasswdScreen : UiEffect


}