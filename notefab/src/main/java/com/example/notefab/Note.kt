package com.example.notefab


class Note {
    var id: Int? = null
    var title: String? = null
    var content: String? = null
    constructor(id: Int, title: String, content: String) {
        this.id = id
        this.title = title
        this.content = content
    }
}