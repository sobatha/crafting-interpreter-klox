package org.example.lexer

enum class TokenType {
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER,
    GREATER_EQUAL, LESS, LESS_EQUAL,

    IDENTIFIER, STRING, NUMBER,

    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL,
    OR, PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    EOF
}

data class Token(
    val type: TokenType,
    val line: Int,
    val lexeme: String = "",
    val literal: Any? = null,
)

val singleCharTokens = mapOf(
    '(' to TokenType.LEFT_PAREN,
    ')' to TokenType.RIGHT_PAREN,
    '{' to TokenType.LEFT_BRACE,
    '}' to TokenType.RIGHT_BRACE,
    ',' to TokenType.COMMA,
    '.' to TokenType.DOT,
    '-' to TokenType.MINUS,
    '+' to TokenType.PLUS,
    ';' to TokenType.SEMICOLON,
    '*' to TokenType.STAR
)

val keywords = mapOf(
    "and" to TokenType.AND,
    "class" to TokenType.CLASS,
    "else" to TokenType.ELSE,
    "false" to TokenType.FALSE,
    "for" to TokenType.FOR,
    "fun" to TokenType.FUN,
    "if" to TokenType.IF,
    "nil" to TokenType.NIL,
    "or" to TokenType.OR,
    "print" to TokenType.PRINT,
    "return" to TokenType.RETURN,
    "super" to TokenType.SUPER,
    "this" to TokenType.THIS,
    "true" to TokenType.TRUE,
    "var" to TokenType.VAR,
    "while" to TokenType.WHILE
)
