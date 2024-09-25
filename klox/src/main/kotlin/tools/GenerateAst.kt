package tools

import java.io.PrintWriter
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    if (args.isEmpty()) exitProcess(68)

    val outputDir = args[0]

    defineAst(
        outputDir, "Expr",
        listOf(
            "Binary ; left: Expr, operator: Token, right: Expr",
            "Grouping ; value: Object",
            "Unary ; operator: Expr, right: Expr"
        )
    )
}

private fun defineAst(outputDir: String, baseName: String, types: List<String>) {
    val path = "$outputDir/$baseName.kt"
    val writer = PrintWriter(path, "UTF-8")

    writer.println(
        "package org.example.abstractSyntaxTree\n" +
                "\n" +
                "import org.example.lexer.Token"
    )
    writer.println("abstract class $baseName {\n")

    defineVisitor(writer, baseName, types)

    for (type in types) {
        val className = type.split(";")[0].trim()
        val fields = type.split(";")[1].trim()
        defineType(writer, baseName, className, fields)
    }

    writer.println("\n")
    writer.println("\tabstract <R> fun accept(visitor: Visitor<R>): R")
    writer.println("}\n")

    writer.close()
}

fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
    writer.println("\tinterface Visitor<R> {")

    for (type in types) {
        val typeName = type.split(";")[0].trim()
        writer.println("\tfun visit$typeName$baseName(${baseName.toLowerCase()}: $typeName): R\n\t}")
    }
}

private fun defineType(writer: PrintWriter, baseName: String, className: String, fields: String) {
    val fieldList = fields.split(",")
    writer.println(" static class $className(")
    for (field in fieldList) {
        writer.println("val $field, ")
    }
    writer.println("): $baseName() {")
    writer.println();
    writer.println("    @Override");
    writer.println("    fun <R> accept(visitor: Visitor<R>): R {");
    writer.println("      return visitor.visit$className$baseName(this)");
    writer.println("    }");
}
