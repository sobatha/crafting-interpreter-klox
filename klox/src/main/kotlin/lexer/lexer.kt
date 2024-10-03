package org.example.lexer

import org.example.Interpreter.Interpreter
import org.example.Parser.Parser
import java.io.BufferedReader
import java.io.InputStreamReader
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
        run(input)
    }

    return 0
}

private fun run(source: String) {
    val scanner = Scanner(source)
    val tokens: List<Token> = scanner.scanTokens()

    val parser = Parser(tokens)
    val interpreter = Interpreter()
    val statements = parser.parse()

    if (hadError) return

    interpreter.interpret(statements)
}

private val hadError = false
