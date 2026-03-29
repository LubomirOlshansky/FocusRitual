package com.focusritual.app.feature.about

data class SoundCredit(
    val name: String,
    val title: String,
    val author: String,
    val license: String,
    val source: String? = null,
)

fun soundCredits(): List<SoundCredit> = listOf(
    SoundCredit(
        name = "Rain",
        title = "The rain falls against the parasol",
        author = "straget",
        license = "CC BY 4.0",
    ),
    SoundCredit(
        name = "Thunder",
        title = "Thunder rumbling and rain",
        author = "FlatHill",
        license = "CC BY 4.0",
    ),
    SoundCredit(
        name = "Wind",
        title = "Wind blowing through trees",
        author = "felix.blume",
        license = "CC BY 4.0",
    ),
    SoundCredit(
        name = "Forest",
        title = "Birds singing in the forest",
        author = "reinsamba",
        license = "CC BY 4.0",
    ),
    SoundCredit(
        name = "Stream",
        title = "A gentle stream flowing over rocks",
        author = "YleArkisto",
        license = "CC BY 4.0",
    ),
    SoundCredit(
        name = "Cafe",
        title = "Busy cafe ambient chatter",
        author = "stephan",
        license = "CC BY 4.0",
    ),
    SoundCredit(
        name = "Fireplace",
        title = "Crackling fire in a fireplace",
        author = "levinj",
        license = "CC BY 4.0",
    ),
    SoundCredit(
        name = "Brown Noise",
        title = "Smooth brown noise",
        author = "FocusRitual",
        license = "Original",
    ),
    SoundCredit(
        name = "Waves",
        title = "Ocean waves washing ashore",
        author = "Luftrum",
        license = "CC BY 4.0",
    ),
)
