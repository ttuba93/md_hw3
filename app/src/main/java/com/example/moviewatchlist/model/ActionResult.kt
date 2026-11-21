package com.example.moviewatchlist.model

sealed class ActionResult {
    data class Done(val message: String = "") : ActionResult()
    data class Rejected(val reason: String) : ActionResult()
}

fun ActionResult.messageOrNull(): String? = when (this) {
    is ActionResult.Done -> message.ifEmpty { null }
    is ActionResult.Rejected -> reason
}
