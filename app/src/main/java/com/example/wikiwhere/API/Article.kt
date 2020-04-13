package com.example.wikiwhere.API

import java.lang.Math.random

class Article {
    private var title: String = ""
    private var snippet: String = ""
    private var distance = 0.0

    init {
        this.title = "test title"
        this.snippet = "test snippet"
        this.distance = random()
    }

    fun title(): String {
        return title
    }

    fun snippet(): String {
        return snippet
    }

    fun distance(): Double {
        return distance
    }

    fun getNearbyList(): ArrayList<Article> {
        val nearbyList = ArrayList<Article>()
        for(i in 0 until 20){
            val article: Article =
                Article()
            nearbyList.add(article)
        }

        return nearbyList
    }
}