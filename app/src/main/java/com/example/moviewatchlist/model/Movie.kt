package com.example.moviewatchlist.model

import androidx.room.Entity
import androidx.room.PrimaryKey


enum class Genre(val label: String) {
    ADVENTURE("Adventure"),
    FAMILY("Family"),
    MUSICAL("Musical"),
    COMEDY("Comedy"),
    FANTASY("Fantasy")
}

enum class MovieType(val label: String) {
    ANIMATED("Animated"),
    LIVE_ACTION("Live-action"),
    CHANNEL("Channel movie")
}


enum class WatchStatus(val label: String) {
    PLANNED("Planned"),
    WATCHING("Watching"),
    WATCHED("Watched");

    val allowedNext: List<WatchStatus>
        get() = when (this) {
            PLANNED -> listOf(WATCHING, WATCHED)
            WATCHING -> listOf(WATCHED, PLANNED)
            WATCHED -> listOf(PLANNED)
        }
}

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val year: Int,
    val genre: Genre,
    val minutes: Int,
    val type: MovieType,
    val status: WatchStatus = WatchStatus.PLANNED,
    val rating: Int = 0
)

val Movie.watched: Boolean get() = status == WatchStatus.WATCHED

sealed class AddResult {
    data class Added(val movie: Movie) : AddResult()
    data class Updated(val movie: Movie) : AddResult()
    data class Duplicate(val title: String) : AddResult()
    data class YearOutOfRange(val title: String, val year: Int) : AddResult()
    data class InvalidInput(val reason: String) : AddResult()
}

fun resultMessage(result: AddResult): String = when (result) {
    is AddResult.Added -> "Added: ${result.movie.title}"
    is AddResult.Updated -> "Saved: ${result.movie.title}"
    is AddResult.Duplicate -> "'${result.title}' is already in the list"
    is AddResult.YearOutOfRange -> "${result.year} is outside 2000-2015"
    is AddResult.InvalidInput -> result.reason
}

fun sampleMovies(): List<Movie> = listOf(
    Movie(title = "Tangled", year = 2010, genre = Genre.ADVENTURE, minutes = 100, type = MovieType.ANIMATED),
    Movie(title = "Frozen", year = 2013, genre = Genre.MUSICAL, minutes = 102, type = MovieType.ANIMATED),
    Movie(title = "Lilo & Stitch", year = 2002, genre = Genre.FAMILY, minutes = 85, type = MovieType.ANIMATED),
    Movie(title = "Big Hero 6", year = 2014, genre = Genre.ADVENTURE, minutes = 102, type = MovieType.ANIMATED),
    Movie(title = "Up", year = 2009, genre = Genre.ADVENTURE, minutes = 96, type = MovieType.ANIMATED),
    Movie(title = "Finding Nemo", year = 2003, genre = Genre.FAMILY, minutes = 100, type = MovieType.ANIMATED),
    Movie(title = "Inside Out", year = 2015, genre = Genre.FAMILY, minutes = 95, type = MovieType.ANIMATED),
    Movie(title = "Enchanted", year = 2007, genre = Genre.FANTASY, minutes = 107, type = MovieType.LIVE_ACTION),
    Movie(title = "Alice in Wonderland", year = 2010, genre = Genre.FANTASY, minutes = 108, type = MovieType.LIVE_ACTION),
    Movie(title = "High School Musical", year = 2006, genre = Genre.MUSICAL, minutes = 98, type = MovieType.CHANNEL)
)
