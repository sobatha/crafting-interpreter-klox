package org.example.LoxClass

import org.example.lexer.Token

class LoxInstance(private val klass: LoxClass) {
    private val fields = mutableMapOf<String, Any?>()
    override fun toString() = "${klass.name} instance"

    fun get(name: Token): Any? {
        if (name.lexeme in fields) return fields[name.lexeme]
        return klass.findMethod(name.lexeme)?.bind(this) ?: throw Exception("Undefined property ${name.lexeme}")
    }

    fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }
}