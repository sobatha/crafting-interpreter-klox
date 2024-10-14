package org.example.LoxClass

import org.example.Callable.LoxCallable
import org.example.Callable.LoxFunction
import org.example.Interpreter.Interpreter

class LoxClass(val name: String, val superClass: LoxClass?, val methods: Map<String, LoxFunction>) : LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): LoxInstance {
        val instance = LoxInstance(this)
        val initializer = findMethod("init")?.bind(instance)?.call(interpreter, arguments)
        return instance
    }

    override fun arity() = findMethod("init")?.arity() ?: 0

    override fun toString(): String = name

    fun findMethod(name: String): LoxFunction? {
        return methods.getOrDefault(name, null) ?: superClass?.findMethod(name)
    }
}