package ru.bepis.utils

import ru.bepis.model.MoodType
import ru.bepis.model.Post
import ru.bepis.model.PostOnMap
import ru.bepis.model.SubjectType
import ru.bepis.utils.Config.DEFAULT_GENERATED_POSTS_RADIUS
import ru.bepis.utils.Config.DEFAULT_MAP_CENTER

object Generator {

    fun generatePost() = PostOnMap(
        coordinate = DEFAULT_MAP_CENTER.let {
            it.first + (Math.random() - 0.5) * DEFAULT_GENERATED_POSTS_RADIUS to
                    it.second + (Math.random() - 0.5) * DEFAULT_GENERATED_POSTS_RADIUS
        },

        post = Post(
            mood = MoodType.values().let { it[(it.size * Math.random()).toInt()] },
            subjectType = SubjectType.values().let { it[(it.size * Math.random()).toInt()] }
        )
    )
}