package ru.bepis.model

enum class SubjectType(val title: String) {
    IT("IT"),
    PHOTO("Фото"),
    MUSIC("Музыка"),
    ART("Искусство"),
    AUTUMN("Осень");

    companion object {
        fun fromTitle(title: String): SubjectType {
            return values().first { it.title == title }
        }
    }
}