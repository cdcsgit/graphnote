package com.blogspot.kotlinstudy.graphnote

class Strings private constructor() {
    companion object {
        val KO = 0
        val EN = 1
        var lang = EN
            set(value) {
                if (value == KO) {
                    currStrings = StringsKo.STRINGS
                } else {
                    currStrings = StringsEn.STRINGS
                }
                field = value
            }

        var currStrings = StringsEn.STRINGS
//        var currStrings = StringsKo.STRINGS

        private var idx = 0
        private val IDX_FILE = idx++
        private val IDX_OPEN = idx++
        private val IDX_OPEN_RECENTS = idx++
        private val IDX_CLOSE = idx++
        private val IDX_EXIT = idx++
        private val IDX_VIEW = idx++
        private val IDX_HELP = idx++
        private val IDX_ABOUT = idx++
//        private val IDX_ = idx++

        val FILE: String
            get() { return currStrings[IDX_FILE] }
        val OPEN: String
            get() { return currStrings[IDX_OPEN] }
        val OPEN_RECENTS: String
            get() { return currStrings[IDX_OPEN_RECENTS] }
        val CLOSE: String
            get() { return currStrings[IDX_CLOSE] }
        val EXIT: String
            get() { return currStrings[IDX_EXIT] }
        val VIEW: String
            get() { return currStrings[IDX_VIEW] }
        val HELP: String
            get() { return currStrings[IDX_HELP] }
        val ABOUT: String
            get() { return currStrings[IDX_ABOUT] }
    }
}