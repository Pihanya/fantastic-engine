package ru.bepis

import ru.bepis.model.MoodType
import ru.bepis.model.Post
import ru.bepis.model.PostOnMap
import ru.bepis.model.SubjectType

object Store {
    var mood: MoodType? = null
    var subjectType: SubjectType? = null

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
            coordinate = 59.9549548 to 30.3067678, 3,
            post = Post("IT", MoodType.HIGH_ENERGY, SubjectType.IT)
        ),
        PostOnMap(
            coordinate = 59.9552436 to 30.3056198, 2,
            post = Post("Натюрморты", MoodType.POSITIVE, SubjectType.ART)
        ),
        PostOnMap(
            coordinate = 59.954220 to 30.306442, 1,
            post = Post("Музыка", MoodType.LOW_ENERGY, SubjectType.MUSIC)
        ),
        PostOnMap(
            coordinate = 59.954375 to 30.305620, 0,
            post = Post("Съемки", MoodType.LOW_ENERGY, SubjectType.PHOTO)
        ),


    )
}