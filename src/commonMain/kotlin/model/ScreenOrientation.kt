package model

sealed interface ScreenOrientation {

    val name: String

    companion object {
        val Default = Auto
        val ScreenOrientations = listOf(Auto, Portrait, Landscape)
    }

    object Auto : ScreenOrientation {
        override val name = "自动"
    }

    object Portrait : ScreenOrientation {
        override val name = "竖屏"
    }


    object Landscape : ScreenOrientation {
        override val name = "横屏"
    }


}