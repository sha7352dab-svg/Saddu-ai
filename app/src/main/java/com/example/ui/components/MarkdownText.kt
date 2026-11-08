package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeBlockBackground
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.VioletGlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class Code(val language: String, val content: String) : MarkdownBlock()
    data class Bullet(val text: String) : MarkdownBlock()
    data class Numbered(val number: String, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val blocks = remember(text) { parseMarkdown(text) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val fontSize = when (block.level) {
                        1 -> 20.sp
                        2 -> 18.sp
                        else -> 16.sp
                    }
                    val topPad = if (block.level == 1) 10.dp else 6.dp
                    Spacer(modifier = Modifier.height(topPad))
                    Text(
                        text = buildInlineAnnotatedString(block.text, textColor),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            color = if (block.level == 1) CyanNeon else textColor
                        )
                    )
                }
                is MarkdownBlock.Code -> {
                    CodeSnippetCard(language = block.language, code = block.content)
                }
                is MarkdownBlock.Bullet -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = CyanNeon,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = buildInlineAnnotatedString(block.text, textColor),
                            style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownBlock.Numbered -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            color = VioletGlow,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            text = buildInlineAnnotatedString(block.text, textColor),
                            style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    if (block.text.isNotBlank()) {
                        Text(
                            text = buildInlineAnnotatedString(block.text, textColor),
                            style = MaterialTheme.typography.bodyLarge.copy(color = textColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CodeSnippetCard(language: String, code: String) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CodeBlockBackground)
            .border(1.dp, SurfaceBorderDark, RoundedCornerShape(10.dp))
    ) {
        Column {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerDark)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.ifBlank { "code" }.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        color = CyanNeon,
                        letterSpacing = 1.sp
                    )
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("code", code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                        copied = true
                        scope.launch {
                            delay(2000)
                            copied = false
                        }
                    },
                    modifier = Modifier.size(32.dp).testTag("copy_code_button")
                ) {
                    if (copied) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Copied",
                            tint = CyanNeon,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Code Text with JetBrains Mono
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFFE2E8F0)
                    )
                )
            }
        }
    }
}

private fun parseMarkdown(raw: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = raw.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Code block start
        if (line.trimStart().startsWith("```")) {
            val lang = line.trimStart().removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            blocks.add(MarkdownBlock.Code(lang, codeLines.joinToString("\n")))
            i++
            continue
        }

        // Header
        val trimmed = line.trim()
        if (trimmed.startsWith("#")) {
            val level = trimmed.takeWhile { it == '#' }.length
            val headerText = trimmed.removePrefix("#".repeat(level)).trim()
            blocks.add(MarkdownBlock.Header(level, headerText))
            i++
            continue
        }

        // Bullet point
        if (trimmed.startsWith("* ") || trimmed.startsWith("- ")) {
            val bulletText = trimmed.drop(2).trim()
            blocks.add(MarkdownBlock.Bullet(bulletText))
            i++
            continue
        }

        // Numbered list (e.g. "1. ")
        val numMatch = Regex("^(\\d+)\\.\\s+(.*)").find(trimmed)
        if (numMatch != null) {
            val num = numMatch.groupValues[1]
            val rest = numMatch.groupValues[2]
            blocks.add(MarkdownBlock.Numbered(num, rest))
            i++
            continue
        }

        // Paragraph
        if (trimmed.isNotBlank()) {
            blocks.add(MarkdownBlock.Paragraph(line))
        }
        i++
    }

    return blocks
}

private fun buildInlineAnnotatedString(text: String, defaultColor: Color) = buildAnnotatedString {
    var cursor = 0

    while (cursor < text.length) {
        // Check bold: **text**
        if (cursor + 1 < text.length && text[cursor] == '*' && text[cursor + 1] == '*') {
            val end = text.indexOf("**", cursor + 2)
            if (end != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                    append(text.substring(cursor + 2, end))
                }
                cursor = end + 2
                continue
            }
        }

        // Check inline code: `code`
        if (text[cursor] == '`') {
            val end = text.indexOf('`', cursor + 1)
            if (end != -1) {
                withStyle(
                    SpanStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        background = Color(0x3300E5FF),
                        color = CyanNeon,
                        fontSize = 13.sp
                    )
                ) {
                    append(" ${text.substring(cursor + 1, end)} ")
                }
                cursor = end + 1
                continue
            }
        }

        // Check italic: *text*
        if (text[cursor] == '*') {
            val end = text.indexOf('*', cursor + 1)
            if (end != -1) {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = defaultColor)) {
                    append(text.substring(cursor + 1, end))
                }
                cursor = end + 1
                continue
            }
        }

        append(text[cursor])
        cursor++
    }
}
