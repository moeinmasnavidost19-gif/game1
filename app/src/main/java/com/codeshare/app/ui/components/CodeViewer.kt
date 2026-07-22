package com.codeshare.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** تم‌های نمایش کد */
data class CodeTheme(
    val name: String,
    val background: Color,
    val text: Color,
    val keyword: Color,
    val string: Color,
    val comment: Color,
    val number: Color,
    val function: Color,
    val lineNumber: Color,
    val lineNumberBg: Color,
)

object CodeThemes {
    val Dark = CodeTheme(
        name = "Dark",
        background = Color(0xFF1E1E1E), text = Color(0xFFD4D4D4),
        keyword = Color(0xFF569CD6), string = Color(0xFFCE9178),
        comment = Color(0xFF6A9955), number = Color(0xFFB5CEA8),
        function = Color(0xFFDCDCAA), lineNumber = Color(0xFF858585),
        lineNumberBg = Color(0xFF252526),
    )
    val Light = CodeTheme(
        name = "Light",
        background = Color(0xFFFFFFFF), text = Color(0xFF1F2328),
        keyword = Color(0xFF0000FF), string = Color(0xFFA31515),
        comment = Color(0xFF008000), number = Color(0xFF098658),
        function = Color(0xFF795E26), lineNumber = Color(0xFF6E7781),
        lineNumberBg = Color(0xFFF6F8FA),
    )
    val Dracula = CodeTheme(
        name = "Dracula",
        background = Color(0xFF282A36), text = Color(0xFFF8F8F2),
        keyword = Color(0xFFFF79C6), string = Color(0xFFF1FA8C),
        comment = Color(0xFF6272A4), number = Color(0xFFBD93F9),
        function = Color(0xFF50FA7B), lineNumber = Color(0xFF6272A4),
        lineNumberBg = Color(0xFF21222C),
    )
    val Monokai = CodeTheme(
        name = "Monokai",
        background = Color(0xFF272822), text = Color(0xFFF8F8F2),
        keyword = Color(0xFFF92672), string = Color(0xFFE6DB74),
        comment = Color(0xFF75715E), number = Color(0xFFAE81FF),
        function = Color(0xFFA6E22E), lineNumber = Color(0xFF90908A),
        lineNumberBg = Color(0xFF2D2E27),
    )

    val all = listOf(Dark, Light, Dracula, Monokai)
    fun byName(name: String): CodeTheme = all.find { it.name == name } ?: Dark
}

// ─── کلمات کلیدی هر زبان ───
private val KEYWORDS: Map<String, Set<String>> = run {
    val common = setOf("if", "else", "for", "while", "return", "break", "continue", "true", "false", "null")
    mapOf(
        "Python" to common + setOf("def", "class", "import", "from", "as", "with", "try", "except",
            "finally", "raise", "lambda", "pass", "yield", "global", "nonlocal", "in", "is", "not",
            "and", "or", "None", "True", "False", "self", "elif", "async", "await", "print"),
        "Java" to common + setOf("public", "private", "protected", "static", "final", "void", "int",
            "long", "double", "float", "boolean", "char", "byte", "short", "class", "interface",
            "extends", "implements", "new", "this", "super", "try", "catch", "finally", "throw",
            "throws", "import", "package", "switch", "case", "default", "do", "abstract", "enum", "instanceof", "String"),
        "Kotlin" to common + setOf("fun", "val", "var", "class", "object", "interface", "data",
            "sealed", "open", "override", "private", "public", "protected", "internal", "when",
            "is", "in", "as", "import", "package", "try", "catch", "finally", "throw", "companion",
            "init", "constructor", "this", "super", "suspend", "lateinit", "by", "lazy", "enum", "String", "Int", "Boolean"),
        "JavaScript" to common + setOf("function", "var", "let", "const", "class", "extends", "new",
            "this", "super", "import", "export", "from", "default", "try", "catch", "finally",
            "throw", "switch", "case", "typeof", "instanceof", "in", "of", "async", "await",
            "yield", "delete", "void", "undefined", "console"),
        "TypeScript" to common + setOf("function", "var", "let", "const", "class", "extends", "new",
            "this", "super", "import", "export", "from", "default", "try", "catch", "finally",
            "throw", "switch", "case", "typeof", "instanceof", "in", "of", "async", "await",
            "interface", "type", "enum", "namespace", "declare", "readonly", "public", "private",
            "protected", "implements", "string", "number", "boolean", "any", "undefined", "never", "console"),
        "C++" to common + setOf("int", "long", "double", "float", "char", "bool", "void", "class",
            "struct", "public", "private", "protected", "virtual", "override", "const", "static",
            "new", "delete", "this", "namespace", "using", "template", "typename", "include",
            "define", "switch", "case", "default", "do", "try", "catch", "throw", "auto", "std", "cout", "cin", "endl"),
        "C#" to common + setOf("public", "private", "protected", "internal", "static", "void",
            "int", "long", "double", "float", "bool", "char", "string", "class", "interface",
            "struct", "enum", "namespace", "using", "new", "this", "base", "try", "catch",
            "finally", "throw", "switch", "case", "default", "do", "var", "async", "await", "override", "virtual", "Console"),
        "C" to common + setOf("int", "long", "double", "float", "char", "void", "struct", "union",
            "enum", "typedef", "const", "static", "extern", "sizeof", "switch", "case", "default",
            "do", "include", "define", "printf", "scanf", "malloc", "free", "unsigned", "signed"),
        "PHP" to common + setOf("function", "class", "public", "private", "protected", "static",
            "echo", "print", "new", "this", "extends", "implements", "interface", "namespace",
            "use", "require", "include", "try", "catch", "finally", "throw", "switch", "case",
            "default", "do", "foreach", "as", "array", "isset", "unset", "empty"),
        "Go" to common + setOf("func", "var", "const", "type", "struct", "interface", "map",
            "chan", "go", "defer", "select", "switch", "case", "default", "package", "import",
            "range", "make", "new", "nil", "byte", "int", "int64", "string", "bool", "float64", "error", "fmt"),
        "Rust" to common + setOf("fn", "let", "mut", "const", "struct", "enum", "impl", "trait",
            "pub", "use", "mod", "crate", "self", "super", "match", "loop", "move", "ref",
            "unsafe", "async", "await", "dyn", "Box", "Vec", "String", "Option", "Result", "Some", "None", "Ok", "Err", "println"),
        "Swift" to common + setOf("func", "var", "let", "class", "struct", "enum", "protocol",
            "extension", "import", "guard", "defer", "switch", "case", "default", "do", "try",
            "catch", "throw", "throws", "public", "private", "internal", "static", "override",
            "init", "deinit", "self", "super", "nil", "String", "Int", "Bool", "Double", "print"),
        "Ruby" to common + setOf("def", "end", "class", "module", "require", "include", "attr_accessor",
            "new", "self", "nil", "unless", "until", "do", "then", "yield", "begin", "rescue",
            "ensure", "raise", "puts", "print", "lambda", "proc"),
        "Dart" to common + setOf("void", "int", "double", "String", "bool", "var", "final", "const",
            "class", "extends", "implements", "with", "abstract", "new", "this", "super", "import",
            "library", "part", "try", "catch", "finally", "throw", "switch", "case", "default",
            "do", "async", "await", "yield", "late", "required", "print", "Widget", "BuildContext"),
        "SQL" to setOf("SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES", "UPDATE", "SET",
            "DELETE", "CREATE", "TABLE", "ALTER", "DROP", "INDEX", "JOIN", "INNER", "LEFT",
            "RIGHT", "OUTER", "ON", "AS", "AND", "OR", "NOT", "NULL", "PRIMARY", "KEY",
            "FOREIGN", "REFERENCES", "GROUP", "BY", "ORDER", "HAVING", "LIMIT", "DISTINCT",
            "COUNT", "SUM", "AVG", "MAX", "MIN", "select", "from", "where", "insert", "into",
            "values", "update", "set", "delete", "create", "table", "join", "order", "by", "group"),
        "Shell" to common + setOf("echo", "cd", "ls", "mkdir", "rm", "cp", "mv", "cat", "grep",
            "sed", "awk", "chmod", "chown", "sudo", "export", "source", "alias", "fi", "then",
            "elif", "do", "done", "case", "esac", "function", "local", "read", "exit"),
        "HTML" to setOf("html", "head", "body", "div", "span", "p", "a", "img", "script", "style",
            "link", "meta", "title", "h1", "h2", "h3", "h4", "h5", "h6", "ul", "ol", "li",
            "table", "tr", "td", "th", "form", "input", "button", "select", "option", "textarea",
            "nav", "header", "footer", "section", "article", "main", "class", "id", "href", "src"),
        "CSS" to setOf("color", "background", "margin", "padding", "border", "width", "height",
            "display", "position", "top", "left", "right", "bottom", "flex", "grid", "font",
            "text", "align", "justify", "content", "items", "center", "absolute", "relative",
            "fixed", "none", "block", "inline", "hover", "active", "focus", "important", "media", "root"),
    )
}

private enum class TokKind { KEYWORD, STRING, COMMENT, NUMBER, FUNCTION, PLAIN }

/**
 * توکنایزر ساده و سریع خط به خط.
 */
private fun highlightLine(line: String, lang: String, theme: CodeTheme): AnnotatedString {
    val keywords = KEYWORDS[lang] ?: KEYWORDS["Kotlin"]!!
    return buildAnnotatedString {
        var i = 0
        val n = line.length
        while (i < n) {
            val c = line[i]
            when {
                // کامنت‌ها
                (c == '/' && i + 1 < n && (line[i + 1] == '/' || line[i + 1] == '*')) ||
                    (c == '#' && lang in setOf("Python", "Shell", "Ruby")) ||
                    (c == '-' && i + 1 < n && line[i + 1] == '-' && lang == "SQL") -> {
                    withStyle(SpanStyle(color = theme.comment, fontStyle = FontStyle.Italic)) {
                        append(line.substring(i))
                    }
                    i = n
                }
                // رشته‌ها
                c == '"' || c == '\'' || c == '`' -> {
                    val quote = c
                    var j = i + 1
                    while (j < n && line[j] != quote) {
                        if (line[j] == '\\') j++
                        j++
                    }
                    val end = (j + 1).coerceAtMost(n)
                    withStyle(SpanStyle(color = theme.string)) { append(line.substring(i, end)) }
                    i = end
                }
                // اعداد
                c.isDigit() -> {
                    var j = i
                    while (j < n && (line[j].isDigit() || line[j] == '.' || line[j] == 'x' ||
                            line[j] == 'f' || line[j] == 'L' || line[j] in 'a'..'e' || line[j] in 'A'..'F')) j++
                    withStyle(SpanStyle(color = theme.number)) { append(line.substring(i, j)) }
                    i = j
                }
                // شناسه‌ها
                c.isLetter() || c == '_' -> {
                    var j = i
                    while (j < n && (line[j].isLetterOrDigit() || line[j] == '_')) j++
                    val word = line.substring(i, j)
                    val isFunc = j < n && line[j] == '('
                    when {
                        word in keywords -> withStyle(SpanStyle(color = theme.keyword)) { append(word) }
                        isFunc -> withStyle(SpanStyle(color = theme.function)) { append(word) }
                        else -> withStyle(SpanStyle(color = theme.text)) { append(word) }
                    }
                    i = j
                }
                else -> {
                    withStyle(SpanStyle(color = theme.text)) { append(c.toString()) }
                    i++
                }
            }
        }
    }
}

/**
 * نمایشگر کد با شماره خط، اسکرول افقی و تم قابل تغییر.
 */
@Composable
fun CodeViewer(
    code: String,
    language: String,
    theme: CodeTheme,
    modifier: Modifier = Modifier,
    fontSize: Int = 13,
) {
    val lines = remember(code) { code.replace("\t", "    ").lines() }
    val highlighted = remember(code, language, theme) {
        lines.map { highlightLine(it, language, theme) }
    }
    val hScroll = rememberScrollState()
    val numWidth = (lines.size.toString().length * (fontSize * 0.75)).dp

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(theme.background),
    ) {
        // ستون شماره خط (سمت چپ در LTR — کد همیشه چپ‌چین است)
        Column(
            modifier = Modifier
                .background(theme.lineNumberBg)
                .padding(vertical = 10.dp, horizontal = 8.dp),
        ) {
            lines.forEachIndexed { idx, _ ->
                Text(
                    "${idx + 1}",
                    color = theme.lineNumber,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.5).sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(numWidth),
                )
            }
        }
        // خطوط کد
        Column(
            modifier = Modifier
                .horizontalScroll(hScroll)
                .padding(vertical = 10.dp, horizontal = 12.dp),
        ) {
            highlighted.forEach { line ->
                Text(
                    text = if (line.isEmpty()) AnnotatedString(" ") else line,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.5).sp,
                    fontFamily = FontFamily.Monospace,
                    softWrap = false,
                )
            }
        }
    }
}
