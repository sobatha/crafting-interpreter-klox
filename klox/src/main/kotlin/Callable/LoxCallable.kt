package org.example.Callable

import org.example.Interpreter.Interpreter

interface LoxCallable {
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
    fun arity() : Int
}