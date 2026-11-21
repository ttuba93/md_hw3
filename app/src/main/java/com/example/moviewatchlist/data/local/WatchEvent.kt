package com.example.moviewatchlist.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.moviewatchlist.model.Movie
import com.example.moviewatchlist.model.WatchStatus

@Entity(
    tableName = "watch_events",
    foreignKeys = [
        ForeignKey(
            entity = Movie::class,
            parentColumns = ["id"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("movieId")]
)
data class WatchEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val movieId: Long,
    val fromStatus: WatchStatus,
    val toStatus: WatchStatus,
    val fromRating: Int,
    val toRating: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class WatchEventWithTitle(
    @Embedded val event: WatchEvent,
    val movieTitle: String
)

fun WatchEvent.describe(): String {
    fun stars(r: Int) = if (r == 0) "none" else "$r★"
    return if (fromStatus != toStatus) {
        val base = "${fromStatus.label} → ${toStatus.label}"
        if (fromRating != toRating) "$base (rating reset)" else base
    } else {
        "Rating: ${stars(fromRating)} → ${stars(toRating)}"
    }
}
