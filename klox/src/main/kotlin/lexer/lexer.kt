package org.example.lexer

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

fun runFile(path: String): Int {
    return try {
        val bytes = Files.readAllBytes(Paths.get(path))
        printCommand(String(bytes, Charset.defaultCharset()))
        0
    } catch (e: Exception) {
        1
    }
}

fun printCommand(source: String) {
    val lexer = Scanner(source)
    lexer.scanTokens().forEach { token ->
        println(token)
    }
}


fun runPrompt(): Int {
    while (true) {
        print("> ")
        val input = readlnOrNull()
        if (input.isNullOrBlank()) break
        printCommand(input)
    }

    return 0
}



