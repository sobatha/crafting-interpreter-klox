package org.example.abstractSyntaxTree

import org.example.lexer.Token

class Environment(val enclosing: Environment? = null) {
    private val values: MutableMap<String, Any?> = mutableMapOf()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? {
        val identifier = name.lexeme
        return if (values.containsKey(identifier)) {
            values[identifier]
        } else if (enclosing != null) {
            enclosing.get(name)
        } else throw Error("undefined value")
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
        } else if (enclosing != null) {
            enclosing.assign(name, value)
        } else throw Error("undefined value")
    }
}