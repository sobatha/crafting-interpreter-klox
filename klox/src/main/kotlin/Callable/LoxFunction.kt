package org.example.Callable

import org.example.Interpreter.Interpreter
import org.example.abstractSyntaxTree.Environment
import org.example.abstractSyntaxTree.Stmt

class LoxFunction(private val declaration: Stmt.Function, private val closure: Environment) : LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        (declaration.params zip arguments).map { (param, argument) ->
            environment.define(param.lexeme, argument)
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }
        return null
    }

    override fun arity() = declaration.params.size

    override fun toString() = "<fn ${declaration.name.lexeme} >"
}