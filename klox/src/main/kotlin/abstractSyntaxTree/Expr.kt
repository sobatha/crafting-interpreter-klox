package org.example.abstractSyntaxTree

import org.example.lexer.Token

abstract class Expr {
    class Binary(
        val left: Expr,
        val operator: Token,
        val right: Expr,
    ) : Expr()

    class Grouping(
        val value: Any,
    ) : Expr()

    class Unary(
        val operator: Token,
        val right: Expr,
    ) : Expr()
}

