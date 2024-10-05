package org.example.lexer

import org.example.Interpreter.Interpreter
import org.example.Parser.Parser
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths


fun runFile(path: String): Int {
    val bytes = Files.readAllBytes(Paths.get(path))
    run(String(bytes, Charset.defaultCharset()))
    return 0
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

    interpreter.interpret(statements)
}

private var hadError = false
