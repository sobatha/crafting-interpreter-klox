package org.example.Callable

import org.example.Interpreter.Interpreter
import org.example.LoxClass.LoxInstance
import org.example.abstractSyntaxTree.Environment
import org.example.abstractSyntaxTree.Stmt

class LoxFunction(
    private val declaration: Stmt.Function,
    private val closure: Environment,
    private val isInitializer: Boolean = false
) : LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        (declaration.params zip arguments).map { (param, argument) ->
            environment.define(param.lexeme, argument)
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            if (isInitializer) return closure.getAt(0, "this")
            return returnValue.value
        }
        if (isInitializer) return closure.getAt(0, "this")
        return null
    }

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment, isInitializer)
    }

    override fun arity() = declaration.params.size

    override fun toString() = "<fn ${declaration.name.lexeme} >"
}