package model.action

sealed class UiAction {

    class WindowVisible(val visible: Boolean) : UiAction()

    class FocusOnSearch(val focus: Boolean) : UiAction()
}