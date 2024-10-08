package org.example.Interpreter

import org.example.abstractSyntaxTree.Expr
import org.example.abstractSyntaxTree.Stmt
import org.example.lexer.Token
import org.example.lexer.TokenType
import java.util.Stack

class Resolver(private val interpreter: Interpreter) : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {
    private enum class FunctionType {
        NONE, FUNCTION
    }

    private val scopes: Stack<MutableMap<String, Boolean>> = Stack()
    private var currentFunction = FunctionType.NONE

    override fun visitAssignExpr(expr: Expr.Assign): Unit? {
        TODO()
    }

    override fun visitBinaryExpr(expr: Expr.Binary) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitCallExpr(expr: Expr.Call) {
        resolve(expr.callee)
        expr.arguments.map { resolve(it) }
    }

    override fun visitGetExpr(expr: Expr.Get) {
        TODO("Not yet implemented")
    }

    override fun visitGroupingExpr(expr: Expr.Grouping) {
        resolve(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal) {}

    override fun visitLogicalExpr(expr: Expr.Logical) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitSetExpr(expr: Expr.Set) {
        TODO("Not yet implemented")
    }

    override fun visitSuperExpr(expr: Expr.Super) {
        TODO("Not yet implemented")
    }

    override fun visitThisExpr(expr: Expr.This) {
        TODO("Not yet implemented")
    }

    override fun visitUnaryExpr(expr: Expr.Unary) {
        resolve(expr.right)
    }

    override fun visitVariableExpr(expr: Expr.Variable): Unit {
        if (!scopes.isEmpty() && scopes.peek()[expr.name.lexeme] == false) {
            throw Exception("Can't read local variable in its own initializer.")
        }
        resolveLocal(expr, expr.name)
    }

    private fun resolveLocal(expr: Expr.Variable, name: Token) {
        for (i in scopes.size downTo 0) {
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        beginScope()
        stmt.statements.map { resolve(it) }
        endScope()
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun beginScope() {
        scopes.push(mutableMapOf())
    }

    fun resolve(statement: Any) {
        when (statement) {
            is Stmt -> statement.accept(this)
            is Expr -> statement.accept(this)
        }
    }

    override fun visitClassStmt(stmt: Stmt.Class) {
        TODO("Not yet implemented")
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        resolve(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        declare(stmt.name)
        define(stmt.name)
        resolveFunction(stmt, FunctionType.FUNCTION)
    }

    private fun resolveFunction(function: Stmt.Function, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type
        beginScope()
        function.params.map {
            declare(it)
            define(it)
        }
        resolve(function.body)
        endScope()
        currentFunction = enclosingFunction
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        stmt.elseBranch?.let { resolve(it) }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        resolve(stmt.expression)
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        if (currentFunction == FunctionType.NONE) throw Exception("Can't return from top-level code")
        stmt.value?.let { resolve(it) }
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        declare(stmt.name)
        if (stmt.initializer != null) resolve(stmt.initializer)
        define(stmt.name)
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return;

        val scope = scopes.peek()
        scope[name.lexeme] = true
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return;

        val scope = scopes.peek()
        if (scope.containsKey(name.lexeme)) throw Exception("$name is already with this name in this scope")
        scope[name.lexeme] = false
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        resolve(stmt.condition)
        resolve(stmt.body)
    }

}