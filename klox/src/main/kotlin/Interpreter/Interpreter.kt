package org.example.Interpreter

import org.example.Callable.LoxCallable
import org.example.Callable.LoxFunction
import org.example.Callable.Return
import org.example.LoxClass.LoxClass
import org.example.LoxClass.LoxInstance
import org.example.abstractSyntaxTree.Environment
import org.example.abstractSyntaxTree.Expr
import org.example.abstractSyntaxTree.Stmt
import org.example.lexer.Token
import org.example.lexer.TokenType.*


class Interpreter : Expr.Visitor<Any>, Stmt.Visitor<Unit> {
    private val globals = Environment()
    private var environment = globals
    private val locals = mutableMapOf<Expr, Int>()

    init {
        class Clock() : LoxCallable {
            override fun arity() = 0
            override fun call(interpreter: Interpreter, arguments: List<Any?>) =
                System.currentTimeMillis() / 1000.0

            override fun toString(): String = "<native fun>"
        }
        globals.define("clock", Clock())
    }

    fun interpret(statements: List<Stmt>) {
        statements.map { execute(it) }
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        val distance = locals[expr]
        distance?.let { environment.assignAt(distance, expr.name, value) }
            ?: globals.assign(expr.name, value)
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

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)

        val arguments = mutableListOf<Any?>()
        expr.arguments.map { arguments.add(evaluate(it)) }
        val function = callee as? LoxCallable ?: throw Exception("$callee is not callable")

        if (arguments.size != function.arity())
            throw Exception("Expected ${function.arity()} arguments but got ${arguments.size}.")
        return function.call(this, arguments)
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        val obj = evaluate(expr.obj) as? LoxInstance ?: throw Exception("only instance can have property")
        return obj.get(expr.name)
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

    override fun visitSetExpr(expr: Expr.Set): Any? {
        val obj = evaluate(expr.obj) as? LoxInstance ?: throw Exception("only instances have fields")
        val value = evaluate(expr.value)
        obj.set(expr.name, value)
        return value
    }

    override fun visitSuperExpr(expr: Expr.Super): Any? {
        val distance = locals[expr]!!
        val superClass = environment.getAt(distance, "super") as LoxClass
        val obj = environment.getAt(distance - 1, "this") as LoxInstance
        val method = superClass.findMethod(expr.method.lexeme) ?: throw Exception("undefined property")
        return method.bind(obj)
    }

    override fun visitThisExpr(expr: Expr.This) = lookUpVariable(expr.keyword, expr)

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
        return lookUpVariable(expr.name, expr)
    }

    private fun lookUpVariable(name: Token, expr: Expr): Any? {
        val distance = locals[expr]
        return distance?.let { environment.getAt(distance, name.lexeme) } ?: globals.get(name)
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitClassStmt(stmt: Stmt.Class) {
        val superClass = stmt.superclass?.let { evaluate(it) }
        superClass?.let { it as? LoxClass ?: throw Exception("Superclass must be a class") }

        environment.define(stmt.name.lexeme, null)

        if (stmt.superclass != null) {
            environment = Environment(environment)
            environment.define("super", superClass)
        }

        val methods = mutableMapOf<String, LoxFunction>()
        stmt.methods.map { methods[it.name.lexeme] = LoxFunction(it, environment, it.name.lexeme == "init") }

        val klass = LoxClass(stmt.name.lexeme, superClass as? LoxClass, methods)
        superClass?.let { environment = environment.enclosing!! }
        environment.assign(stmt.name, klass)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = LoxFunction(stmt, environment)
        environment.define(stmt.name.lexeme, function)
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
        val value = if (stmt.value != null) evaluate(stmt.value) else null
        throw Return(value)
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

    fun resolve(expr: Expr, depth: Int) = locals.put(expr, depth)

    fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment

        try {
            this.environment = environment
            statements.map { execute(it) }
        } finally {
            this.environment = previous
        }
    }
}