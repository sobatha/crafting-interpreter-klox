package org.example.errors

object ErrorReport {
    fun reportError(line: Int, where: String = "", message: String = "") = report(line, where, message)

    private fun report(line: Int, place: String, message: String) =
        println("[line $line] Error $place : $message")
}