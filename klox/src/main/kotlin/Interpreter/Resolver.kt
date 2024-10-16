package org.example.Interpreter

import org.example.abstractSyntaxTree.Expr
import org.example.abstractSyntaxTree.Stmt
import org.example.lexer.Token
import org.example.lexer.TokenType
import java.util.Stack

class Resolver(private val interpreter: Interpreter) : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {
    private enum class FunctionType {
        NONE, FUNCTION, METHOD, INITIALIZER
    }

    private enum class ClassType {
        NONE, CLASS
    }

    private var currentClass: ClassType = ClassType.NONE

    private val scopes: Stack<MutableMap<String, Boolean>> = Stack()
    private var currentFunction = FunctionType.NONE

    override fun visitAssignExpr(expr: Expr.Assign) {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
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
        resolve(expr.obj)
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
        resolve(expr.value)
        resolve(expr.obj)
    }

    override fun visitSuperExpr(expr: Expr.Super) {
        resolveLocal(expr, expr.keyword)
    }

    override fun visitThisExpr(expr: Expr.This) {
        if (currentClass == ClassType.NONE) throw Exception("Can't use 'this' outside of a class")
        resolveLocal(expr, expr.keyword)
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

    private fun resolveLocal(expr: Expr, name: Token) {
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
        val enclosingClass = currentClass
        currentClass = ClassType.CLASS

        declare(stmt.name)
        define(stmt.name)
        if (stmt.superclass != null && stmt.superclass.name.lexeme == stmt.name.lexeme)
            throw Error("class cannot inherit from itself")
        stmt.superclass?.let { resolve(it) }

        if (stmt.superclass != null) {
            beginScope()
            scopes.peek().put("super", true)
        }

        beginScope()
        scopes.peek()["this"] = true

        stmt.methods.map {
            val type = if (it.name.lexeme == "init") FunctionType.INITIALIZER else FunctionType.METHOD
            resolveFunction(it, type)
        }

        endScope()

        stmt.superclass?.let { endScope() }

        currentClass = enclosingClass
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
        stmt.value?.let {
            if (currentFunction == FunctionType.INITIALIZER) throw Error("Can't return a value from an initializer")
            resolve(it)
        }
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