package ru.bepis.model

import com.google.gson.JsonObject
import ru.bepis.MapActivity

data class PostOnMap(
    // lat/lon
    val coordinate: Pair<Double, Double>,
    val post: Post
)

data class Post(
    val mood: MoodType,
    val subjectType: SubjectType
) {
    companion object {
        fun Post.toJson() = JsonObject().also {
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

            return Post(mood, subjectType)
        }
    }

}