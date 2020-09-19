package ru.bepis

import ru.bepis.utils.Config.DEFAULT_GENERATED_POSTS
import ru.bepis.utils.Generator

object Store {
    var mood = ""

    val posts = DEFAULT_GENERATED_POSTS.let {
        var ctr = it
        generateSequence {
            val post = Generator.generatePost().takeIf { ctr > 0 }
            ctr -= 1
            post
        }
    }
}