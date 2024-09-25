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
            "Unary ; Token operator, Expr right"
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
    for (type in types) {
        val className = type.split(";")[0].trim()
        val fields = type.split(";")[1].trim()
        defineType(writer, baseName, className, fields)
    }
    writer.println("}\n")

    writer.close()
}

private fun defineType(writer: PrintWriter, baseName: String, className: String, fields: String) {
    val fieldList = fields.split(",")
    writer.println(" static class $className(")
    for (field in fieldList) {
        writer.println("val $field, ")
    }
    writer.println("): $baseName()")
}

