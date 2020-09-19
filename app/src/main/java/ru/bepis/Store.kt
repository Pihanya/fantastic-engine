package ru.bepis

import ru.bepis.model.MoodType
import ru.bepis.model.Post
import ru.bepis.model.PostOnMap
import ru.bepis.model.SubjectType

object Store {
    var mood = ""

    /*val posts = DEFAULT_GENERATED_POSTS.let {
        var ctr = it
        generateSequence {
            val post = Generator.generatePost().takeIf { ctr > 0 }
            ctr -= 1
            post
        }
    }*/

    val posts = listOf(
        PostOnMap(
            coordinate = 59.9564037 to 30.3077903,
            post = Post("IT", MoodType.HIGH_ENERGY, SubjectType.IT)
        ),
        PostOnMap(
            coordinate = 59.9564037 to 30.3077903,
            post = Post("Еда", MoodType.POSITIVE, SubjectType.ART)
        ),
        PostOnMap(
            coordinate = 59.9535502 to 30.3049822,
            post = Post("Алкоголь", MoodType.NEGATIVE, SubjectType.PHOTO)
        )
    )
}