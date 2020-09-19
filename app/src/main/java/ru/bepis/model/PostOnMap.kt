package ru.bepis.model

data class PostOnMap(
    // lat/lon
    val coordinate: Pair<Double, Double>,
    val post: Post
)

data class Post(
    val title: String = "sdf;ld",
    val mood: MoodType,
    val subjectType: SubjectType
) {
    fun emoji(): String {
        /*return when(mood) {
            MoodType.HIGH_ENERGY -> "\uD83D\uDE1C"
            MoodType.LOW_ENERGY -> "\uD83D\uDE34"
            MoodType.NEGATIVE -> "\uD83D\uDE41"
            MoodType.POSITIVE -> "\uD83D\uDE03"
        }*/
        return when(subjectType) {
            SubjectType.IT -> "\uD83D\uDCBB"
            SubjectType.PHOTO -> "\uD83D\uDCF7"
            SubjectType.ART -> "\uD83D\uDDBC️"
            SubjectType.AUTUMN -> "\uD83C\uDF42"
        }
    }

    companion object {
        /*fun Post.toJson() = JsonObject().also {
            it.addProperty("mood", this.mood.name)
            it.addProperty("subjectType", this.subjectType.name)

            val icon = when(this.mood) {
                MoodType.HIGH_ENERGY -> MapActivity.EMOJI_EXITED
                MoodType.LOW_ENERGY -> MapActivity.EMOJI_SLEEPY
                MoodType.NEGATIVE -> MapActivity.EMOJI_SAD
                MoodType.POSITIVE -> MapActivity.EMOJI_HAPPY
            }
            it.addProperty("icon", icon)
        }

        fun JsonObject.toPost(json: JsonObject): Post {
            val mood = MoodType.valueOf(json["mood"].asString)
            val subjectType = SubjectType.valueOf(json["subjectType"].asString)

            return Post(mood, subjectType
        }*/
    }

}