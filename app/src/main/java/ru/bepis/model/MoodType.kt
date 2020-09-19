package ru.bepis.model

enum class MoodType {
    HIGH_ENERGY, LOW_ENERGY, NEGATIVE, POSITIVE;

    companion object {
        fun fromSubjectType(type: SubjectType): MoodType {
            return when(type) {
                SubjectType.IT -> HIGH_ENERGY
                SubjectType.PHOTO -> LOW_ENERGY
                SubjectType.MUSIC -> POSITIVE
                SubjectType.ART -> LOW_ENERGY
                SubjectType.AUTUMN -> NEGATIVE
            }
        }
    }
}