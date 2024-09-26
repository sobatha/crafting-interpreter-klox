package org.example.lexer

import org.example.errors.ErrorReport

class Scanner(private val source: String) {
    private var start = 0
    private var current = 0
    private var line = 1
    private val isAtEnd
        get() = current >= source.length

    private val tokens = mutableListOf<Token>()

    fun scanTokens(): List<Token> {
        while (!isAtEnd) {
            start = current
            scanToken()
        }

        tokens.add(Token(type = TokenType.EOF, line = line))
        return tokens
    }

    private fun scanToken() {
        val c = advance()

        when {
            c in singleCharTokens -> addToken(singleCharTokens[c]!!)
            c == '!' || c == '=' || c == '<' || c == '>' -> handleTwoCharToken(c)
            c == '/' -> handleSlashOrComment()
            c.isWhitespace() -> if (c == '\n') line++
            c.isDigit() -> number()
            c.isLetter() || c == '_' -> identifier()
            c == '"' -> string()
            else -> ErrorReport.reportError(line, where = "", "Unexpected character")
        }
    }

    private fun handleSlashOrComment() {
        if (match('/')) {
            while (peek() != '\n' && !isAtEnd) advance()
        } else {
            addToken(TokenType.SLASH)
        }
    }

    private fun addToken(type: TokenType, literal: Any? = null) =
        tokens.add(Token(type = type, lexeme = source.substring(start, current), literal = literal, line = line))

    private fun handleTwoCharToken(c: Char) {
        val tokenType = when (c) {
            '!' -> if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG
            '=' -> if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL
            '<' -> if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS
            '>' -> if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER
            else -> null
        }
        if (tokenType != null) addToken(tokenType)
    }

    private fun identifier() {
        while (peek().isLetterOrDigit() || peek() == '_') advance()
        val text = source.substring(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun number() {
        while (peek().isDigit()) advance()
        if (peek() == '.' && peekNext().isDigit()) {
            advance() // Consume '.'
            while (peek().isDigit()) advance()
        }
        val value = source.substring(start, current).toDouble()
        addToken(TokenType.NUMBER, value)
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd) {
            if (peek() == '\n') line++
            advance()
        }
        if (isAtEnd) {
            ErrorReport.reportError(line, where = "", "Unterminated string.")
            return
        }
        advance() // Consume closing '"'
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }


    private fun advance(): Char = source.elementAt(current++)

    private fun match(expected: Char): Boolean {
        if (isAtEnd) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    private fun peek(): Char = if (isAtEnd) '\u0000' else source.elementAt(current)

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source[current + 1]
    }
}