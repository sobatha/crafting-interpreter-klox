package org.example.Parser

import org.example.abstractSyntaxTree.Expr
import org.example.abstractSyntaxTree.Stmt
import org.example.lexer.Token
import org.example.lexer.TokenType
import org.example.lexer.TokenType.*


class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): List<Stmt> {
        val statements: MutableList<Stmt> = mutableListOf()
        while (!isAtEnd()) {
            statements.add(declaration())
        }

        return statements.toList()
    }

    private fun expression(): Expr = assignment()

    private fun assignment(): Expr {
        val expr = or()

        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()

            return when (expr) {
                is Expr.Variable -> Expr.Assign(expr.name, value)
                is Expr.Get -> Expr.Set(expr.obj, expr.name, value)
                else -> throw Error("$equals, Invalid assignment target")
            }
        }
        return expr
    }

    private fun or(): Expr {
        var expr = and()

        while (match(OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (match(AND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun declaration(): Stmt = when {
        (match(CLASS)) -> classDeclaration()
        (match(FUN)) -> function("function");
        (match(VAR)) -> varDeclaration()
        else -> statement()
    }

    private fun classDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expect class name after class.")

        val superClass = if (match(LESS)) {
            consume(IDENTIFIER, "Expect superclass name")
            Expr.Variable(previous())
        } else null

        consume(LEFT_BRACE, "Expect '{' before class body.")

        val methods = mutableListOf<Stmt.Function>()
        while (!check(RIGHT_BRACE) && !isAtEnd()) methods.add(function("method"))
        consume(RIGHT_BRACE, "Expect '}' after class body.")
        return Stmt.Class(name, superClass, methods)
    }

    private fun varDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "expect var name")

        val initializer = if (match(EQUAL)) expression() else null
        consume(SEMICOLON, "Expect ';' after variable declaration")
        return Stmt.Var(name, initializer)
    }

    private fun statement(): Stmt = when {
        match(FOR) -> forStatement()
        match(IF) -> ifStatement()
        match(PRINT) -> printStatement()
        match(RETURN) -> returnStatement()
        match(WHILE) -> whileStatement()
        match(LEFT_BRACE) -> Stmt.Block(block())
        else -> expressionStatement()
    }

    private fun returnStatement(): Stmt {
        val keyword = previous()
        var value = if (!check(SEMICOLON)) expression() else null
        consume(SEMICOLON, "Expect ';' after return value")
        return Stmt.Return(keyword, value)
    }

    private fun forStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after for")
        val initializer = when {
            match(SEMICOLON) -> null
            match(VAR) -> varDeclaration()
            else -> expressionStatement()
        }

        var condition = if (!check(SEMICOLON)) expression() else null
        consume(SEMICOLON, "Expect ';' after loop condition")

        val increment = if (!check(RIGHT_PAREN)) expression() else null
        consume(RIGHT_PAREN, "Expect ')' after for clause")

        var body = statement()

        if (increment != null) {
            body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
        }

        if (condition == null) condition = Expr.Literal(true)
        body = Stmt.While(condition, body)

        if (initializer != null) body = Stmt.Block(listOf(initializer, body))

        return body
    }

    private fun whileStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after while")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after condition")

        val body = statement()

        return Stmt.While(condition, body)
    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after if")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after if")

        val thenBranch = statement()
        val elseBranch = if (match(ELSE)) statement() else null
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun block(): List<Stmt> {
        val statement = mutableListOf<Stmt>()

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statement.add(declaration())
        }

        consume(RIGHT_BRACE, "Expect '}' after block")
        return statement
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after value")
        return Stmt.Expression(expr)
    }

    private fun function(kind: String): Stmt.Function {
        val name = consume(IDENTIFIER, "Expect $kind name.")
        consume(LEFT_PAREN, "Expect '(' after $kind name.")
        val parameters = mutableListOf<Token>()
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size >= 255) throw Exception("Can't have more than 255 parameters")
                parameters.add(consume(IDENTIFIER, "Expect parameter name."))
            } while (match(COMMA))
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters")
        consume(LEFT_BRACE, "Expect { before $kind body.")
        val body = block()
        return Stmt.Function(name, parameters, body)
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value")
        return Stmt.Print(value)
    }

    private fun equality(): Expr {
        var expr = comparison()
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun comparison(): Expr {
        var expr: Expr = term()

        while (match(TokenType.GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right: Expr = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): Expr {
        var expr = factor()

        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): Expr {
        var expr = unary()

        while (match(STAR, SLASH)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun unary(): Expr {
        while (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return call()
    }

    private fun call(): Expr {
        var expr = primary()

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr)
            } else if (match(DOT)) {
                val name = consume(IDENTIFIER, "Expect property name after '.'")
                expr = Expr.Get(expr, name)
            } else {
                break
            }
        }

        return expr
    }

    private fun finishCall(callee: Expr): Expr {
        val arguments: MutableList<Expr> = mutableListOf()
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size >= 255) throw Exception("Can't have more than 255 arguments")
                arguments.add(expression())
            } while (match(COMMA))
        }

        val paren = consume(RIGHT_PAREN, "Expect ')' after arguments")

        return Expr.Call(callee, paren, arguments)
    }

    private fun primary(): Expr = when {
        (match(FALSE)) -> Expr.Literal(false)
        (match(TRUE)) -> Expr.Literal(true)
        (match(NIL)) -> Expr.Literal(null)
        (match(NUMBER, STRING)) -> Expr.Literal(previous().literal)
        (match(SUPER)) -> {
            val keyword = previous()
            consume(DOT, "Expect '.' after super")
            val method = consume(IDENTIFIER, "Expect superclass method name")
            Expr.Super(keyword, method)
        }
        (match(THIS)) -> Expr.This(previous())
        (match(IDENTIFIER)) -> Expr.Variable(previous())

        (match(LEFT_PAREN)) -> {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            Expr.Grouping(expr)
        }

        else -> throw Exception()
    }

    private fun match(vararg types: TokenType): Boolean {
        types.forEach {
            if (check(it)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type === type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type === EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw Exception(message)
    }

}




