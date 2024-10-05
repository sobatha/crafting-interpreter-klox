package org.example.Interpreter

import org.example.abstractSyntaxTree.Environment
import org.example.abstractSyntaxTree.Expr
import org.example.abstractSyntaxTree.Stmt
import org.example.lexer.TokenType.*


class Interpreter : Expr.Visitor<Any>, Stmt.Visitor<Unit> {
    private var environment = Environment()

    fun interpret(statements: List<Stmt>) {
        statements.map { execute(it) }
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val right = evaluate(expr.right)
        val left = evaluate(expr.left)

        return when (expr.operator.type) {
            BANG_EQUAL -> left as Double != right as Double
            EQUAL_EQUAL -> left as Double == right as Double
            GREATER -> left as Double > right as Double
            GREATER_EQUAL -> left as Double >= right as Double
            LESS -> (left as Double) < right as Double
            LESS_EQUAL -> left as Double <= right as Double
            MINUS -> left as Double - right as Double
            SLASH -> left as Double / right as Double
            STAR -> left as Double * right as Double
            PLUS -> when {
                left is Double && right is Double -> left + right
                left is String && right is String -> left + right
                else -> null
            }

            else -> null
        }
    }

    override fun visitCallExpr(expr: Expr.Call): Any {
        TODO("Not yet implemented")
    }

    override fun visitGetExpr(expr: Expr.Get): Any {
        TODO("Not yet implemented")
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? =
        evaluate(expr.expression)

    override fun visitLiteralExpr(expr: Expr.Literal): Any? =
        expr.value

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.operator.type == OR) {
            if (isTruthy(left)) return left
        } else if (isTruthy(left)) return left
        return evaluate(expr.right)
    }

    override fun visitSetExpr(expr: Expr.Set): Any? =
        evaluate(expr)

    override fun visitSuperExpr(expr: Expr.Super): Any? =
        evaluate(expr)

    override fun visitThisExpr(expr: Expr.This): Any {
        TODO("Not yet implemented")
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)
        return when (expr.operator.type) {
            BANG -> !isTruthy(right)
            PLUS -> right
            MINUS -> -(right as Double)
            else -> throw Exception("unary accepts plus or minus or bang")
        }
    }

    private fun isTruthy(obj: Any?): Boolean = when (obj) {
        null -> false
        is Boolean -> obj
        else -> true
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return environment.get(expr.name)
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitClassStmt(stmt: Stmt.Class) {
        TODO("Not yet implemented")
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        TODO("Not yet implemented")
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(stmt.condition)) {
            execute(stmt.thenBranch)
        } else {
            stmt.elseBranch?.let { execute(stmt.elseBranch) }
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        println(evaluate(stmt.expression))
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        TODO("Not yet implemented")
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        val value = stmt.initializer?.let { evaluate(stmt.initializer) }
        environment.define(stmt.name.lexeme, value)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    private fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment

        try {
            this.environment = environment
            statements.map { execute(it) }
        } finally {
            this.environment = previous
        }
    }
}