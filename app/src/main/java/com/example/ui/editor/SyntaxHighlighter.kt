package com.example.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.*
import java.util.regex.Pattern

object SyntaxHighlighter {

  private val KOTLIN_KEYWORDS = setOf(
    "package", "import", "class", "interface", "object", "val", "var", "fun", "return",
    "if", "else", "when", "for", "while", "do", "try", "catch", "finally", "throw",
    "null", "true", "false", "this", "super", "is", "as", "in", "override", "public",
    "private", "protected", "internal", "abstract", "open", "final", "enum", "sealed",
    "data", "suspend", "companion", "by", "lazy", "lateinit", "inline", "crossinline"
  )

  private val PYTHON_KEYWORDS = setOf(
    "def", "class", "import", "from", "as", "return", "if", "elif", "else", "for",
    "while", "try", "except", "finally", "raise", "with", "pass", "break", "continue",
    "lambda", "yield", "global", "nonlocal", "True", "False", "None", "and", "or", "not", "is", "in"
  )

  private val JS_KEYWORDS = setOf(
    "function", "const", "let", "var", "return", "if", "else", "for", "while", "do",
    "switch", "case", "break", "continue", "default", "try", "catch", "finally", "throw",
    "class", "extends", "super", "this", "new", "import", "export", "from", "async", "await",
    "typeof", "instanceof", "true", "false", "null", "undefined", "yield"
  )

  fun highlight(code: String, language: String, isDark: Boolean = true): AnnotatedString {
    if (code.isEmpty()) return buildAnnotatedString { append("") }

    val defaultTextColor = if (isDark) MetallicTextPrimary else LightTextPrimary
    val keywordColor = SyntaxKeyword
    val stringColor = SyntaxString
    val commentColor = SyntaxComment
    val numberColor = SyntaxNumber
    val typeColor = SyntaxType
    val functionColor = SyntaxFunction

    return buildAnnotatedString {
      append(code)

      // Apply default font & color
      addStyle(
        SpanStyle(color = defaultTextColor, fontFamily = FontFamily.Monospace),
        0,
        code.length
      )

      try {
        // 1. Comments
        when (language.lowercase()) {
          "python", "shell", "yaml" -> {
            highlightRegex(code, "#.*", commentColor, italic = true)
          }
          "html", "xml" -> {
            highlightRegex(code, "<!--[\\s\\S]*?-->", commentColor, italic = true)
          }
          else -> {
            highlightRegex(code, "//.*", commentColor, italic = true)
            highlightRegex(code, "/\\*[\\s\\S]*?\\*/", commentColor, italic = true)
          }
        }

        // 2. Strings
        highlightRegex(code, "\"(\\\\.|[^\"])*\"", stringColor)
        highlightRegex(code, "'(\\\\.|[^'])*'", stringColor)
        highlightRegex(code, "`(\\\\.|[^`])*`", stringColor)

        // 3. Numbers
        highlightRegex(code, "\\b\\d+(\\.\\d+)?([fFLl])?\\b", numberColor)

        // 4. Keywords
        val keywords = when (language.lowercase()) {
          "kotlin" -> KOTLIN_KEYWORDS
          "python" -> PYTHON_KEYWORDS
          "javascript", "typescript" -> JS_KEYWORDS
          "java" -> KOTLIN_KEYWORDS + setOf("void", "boolean", "int", "double", "float", "char", "byte", "short", "long", "extends", "implements", "instanceof")
          else -> KOTLIN_KEYWORDS
        }

        val kwPattern = "\\b(${keywords.joinToString("|")})\\b"
        highlightRegex(code, kwPattern, keywordColor, bold = true)

        // 5. Function calls: name(...)
        highlightRegex(code, "\\b([a-zA-Z_][a-zA-Z0-9_]*)(?=\\s*\\()", functionColor)

        // 6. Types / Classes: PascalCase
        highlightRegex(code, "\\b[A-Z][a-zA-Z0-9_]*\\b", typeColor, bold = true)

        // 7. Annotations / Decorators
        highlightRegex(code, "@[a-zA-Z_][a-zA-Z0-9_]*", SyntaxOperator)

      } catch (_: Exception) {
        // Fallback gracefully without breaking display
      }
    }
  }

  private fun AnnotatedString.Builder.highlightRegex(
    text: String,
    regex: String,
    color: Color,
    bold: Boolean = false,
    italic: Boolean = false
  ) {
    try {
      val pattern = Pattern.compile(regex)
      val matcher = pattern.matcher(text)
      while (matcher.find()) {
        val start = matcher.start()
        val end = matcher.end()
        if (start in text.indices && end <= text.length) {
          addStyle(
            SpanStyle(
              color = color,
              fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
            ),
            start,
            end
          )
        }
      }
    } catch (_: Exception) {
      // ignore regex matching failure
    }
  }
}
