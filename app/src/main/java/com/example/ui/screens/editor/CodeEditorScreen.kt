package com.example.ui.screens.editor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileEntity
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.editor.SyntaxHighlighter
import com.example.ui.localization.LocalAppStrings
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorScreen(
  file: FileEntity?,
  initialContent: String,
  targetLine: Int = 1,
  fontSizeSp: Int = 14,
  lineNumbersEnabled: Boolean = true,
  wordWrapEnabled: Boolean = false,
  syntaxHighlighting: Boolean = true,
  autoSaveEnabled: Boolean = true,
  onBack: () -> Unit,
  onNavigate: (String) -> Unit,
  onSaveFile: (fileId: String, content: String, createSnapshot: Boolean) -> Unit
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val coroutineScope = rememberCoroutineScope()
  val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

  var editorState by remember(file?.id, initialContent) {
    mutableStateOf(TextFieldValue(initialContent, TextRange.Zero))
  }

  var isDirty by remember { mutableStateOf(false) }
  var showFindReplace by remember { mutableStateOf(false) }
  var findQuery by remember { mutableStateOf("") }
  var replaceQuery by remember { mutableStateOf("") }
  var showGoToLineDialog by remember { mutableStateOf(false) }
  var goToLineInput by remember { mutableStateOf("") }
  var currentFontSize by remember { mutableIntStateOf(fontSizeSp) }
  var isWordWrap by remember { mutableStateOf(wordWrapEnabled) }

  // Undo / Redo history stacks
  val undoStack = remember { mutableStateListOf<String>() }
  val redoStack = remember { mutableStateListOf<String>() }

  val verticalScrollState = rememberScrollState()
  val horizontalScrollState = rememberScrollState()
  val focusRequester = remember { FocusRequester() }

  // Auto-save effect
  LaunchedEffect(editorState.text, autoSaveEnabled) {
    if (autoSaveEnabled && isDirty && file != null) {
      delay(3000) // Debounce auto-save by 3 seconds
      onSaveFile(file.id, editorState.text, false)
      isDirty = false
    }
  }

  // Jump to targetLine if passed
  LaunchedEffect(targetLine, initialContent) {
    if (targetLine > 1) {
      val lines = initialContent.lines()
      var charOffset = 0
      for (i in 0 until minOf(targetLine - 1, lines.size)) {
        charOffset += lines[i].length + 1
      }
      editorState = editorState.copy(selection = TextRange(charOffset))
    }
  }

  // Helper functions
  fun pushToUndo(text: String) {
    if (undoStack.isEmpty() || undoStack.last() != text) {
      if (undoStack.size > 50) undoStack.removeAt(0)
      undoStack.add(text)
      redoStack.clear()
    }
  }

  fun updateTextWithHistory(newVal: TextFieldValue) {
    if (newVal.text != editorState.text) {
      pushToUndo(editorState.text)
      isDirty = true
    }
    editorState = newVal
  }

  fun performUndo() {
    if (undoStack.isNotEmpty()) {
      val prev = undoStack.removeAt(undoStack.lastIndex)
      redoStack.add(editorState.text)
      editorState = TextFieldValue(prev, TextRange(prev.length))
      isDirty = true
    }
  }

  fun performRedo() {
    if (redoStack.isNotEmpty()) {
      val next = redoStack.removeAt(redoStack.lastIndex)
      undoStack.add(editorState.text)
      editorState = TextFieldValue(next, TextRange(next.length))
      isDirty = true
    }
  }

  // CRITICAL REQUIREMENT: "کپی کل کد" - One-touch copy entire file verbatim to clipboard!
  fun copyAllCode() {
    val textToCopy = editorState.text
    val clip = ClipData.newPlainText("codevault_${file?.name ?: "code"}", textToCopy)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, strings.copied, Toast.LENGTH_SHORT).show()
  }

  fun duplicateCurrentLine() {
    val text = editorState.text
    val cursor = editorState.selection.start
    val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
    val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
    val currentLine = text.substring(lineStart, lineEnd)

    val newText = text.substring(0, lineEnd) + "\n" + currentLine + text.substring(lineEnd)
    updateTextWithHistory(TextFieldValue(newText, TextRange(lineEnd + 1 + currentLine.length)))
  }

  fun selectAll() {
    editorState = editorState.copy(selection = TextRange(0, editorState.text.length))
  }

  fun manualSave() {
    if (file != null) {
      onSaveFile(file.id, editorState.text, true)
      isDirty = false
      Toast.makeText(context, strings.saved, Toast.LENGTH_SHORT).show()
    }
  }

  val lineCount = remember(editorState.text) { editorState.text.lines().size.coerceAtLeast(1) }
  val (curLine, curCol) = remember(editorState.selection, editorState.text) {
    val cursor = editorState.selection.start
    val beforeCursor = editorState.text.take(cursor)
    val l = beforeCursor.count { it == '\n' } + 1
    val c = cursor - beforeCursor.lastIndexOf('\n')
    Pair(l, c)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = file?.name ?: strings.codeEditor,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
              )
              if (isDirty) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  color = WarningAmber,
                  shape = RoundedCornerShape(4.dp),
                  modifier = Modifier.size(8.dp)
                ) {}
              }
            }
            Text(
              text = file?.language?.uppercase() ?: "PLAIN",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
          }
        },
        actions = {
          // PROMINENT "کپی کل کد" BUTTON (One touch copy all code)
          FilledTonalButton(
            onClick = { copyAllCode() },
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer,
              contentColor = MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.testTag("copy_all_code_top_button")
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = strings.copyAllCode,
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          IconButton(
            onClick = { manualSave() },
            modifier = Modifier.testTag("editor_save_button")
          ) {
            Icon(
              imageVector = Icons.Default.Save,
              contentDescription = strings.save,
              tint = if (isDirty) WarningAmber else MaterialTheme.colorScheme.onSurface
            )
          }

          IconButton(onClick = {
            if (file != null) onNavigate(Screen.VersionHistory.createRoute(file.id))
          }) {
            Icon(Icons.Default.History, contentDescription = strings.versionHistoryTitle)
          }

          IconButton(onClick = { showFindReplace = !showFindReplace }) {
            Icon(Icons.Default.Search, contentDescription = strings.findAndReplace)
          }
        }
      )
    },
    bottomBar = {
      // Bottom Quick Action Bar & Status Bar
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant)
      ) {
        // Quick editing toolbar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          IconButton(onClick = { performUndo() }, enabled = undoStack.isNotEmpty()) {
            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = strings.undo, modifier = Modifier.size(20.dp))
          }
          IconButton(onClick = { performRedo() }, enabled = redoStack.isNotEmpty()) {
            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = strings.redo, modifier = Modifier.size(20.dp))
          }
          IconButton(onClick = { copyAllCode() }) {
            Icon(Icons.Default.ContentCopy, contentDescription = strings.copyAllCode, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
          }
          IconButton(onClick = { duplicateCurrentLine() }) {
            Icon(Icons.Default.ControlPointDuplicate, contentDescription = strings.duplicateLine, modifier = Modifier.size(20.dp))
          }
          IconButton(onClick = { selectAll() }) {
            Icon(Icons.Default.SelectAll, contentDescription = strings.selectAll, modifier = Modifier.size(20.dp))
          }
          IconButton(onClick = { showGoToLineDialog = true }) {
            Icon(Icons.Default.FormatLineSpacing, contentDescription = strings.goToLine, modifier = Modifier.size(20.dp))
          }
          IconButton(onClick = { isWordWrap = !isWordWrap }) {
            Icon(
              Icons.Default.WrapText,
              contentDescription = strings.wordWrap,
              tint = if (isWordWrap) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }
          IconButton(onClick = { if (currentFontSize < 26) currentFontSize += 2 }) {
            Icon(Icons.Default.ZoomIn, contentDescription = "Font +", modifier = Modifier.size(20.dp))
          }
          IconButton(onClick = { if (currentFontSize > 10) currentFontSize -= 2 }) {
            Icon(Icons.Default.ZoomOut, contentDescription = "Font -", modifier = Modifier.size(20.dp))
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Status bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
              text = "${strings.line}: $curLine, ${strings.column}: $curCol",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "$lineCount ${strings.line}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "${editorState.text.length} ${strings.chars}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Text(
            text = if (isDirty) "تغییرات ذخیره نشده" else strings.autoSaved,
            style = MaterialTheme.typography.labelSmall,
            color = if (isDirty) WarningAmber else SuccessGreen
          )
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
    ) {
      // Find and Replace panel
      if (showFindReplace) {
        FindReplaceBar(
          findQuery = findQuery,
          onFindQueryChange = { findQuery = it },
          replaceQuery = replaceQuery,
          onReplaceQueryChange = { replaceQuery = it },
          onFindNext = {
            if (findQuery.isNotEmpty()) {
              val idx = editorState.text.indexOf(findQuery, editorState.selection.end, ignoreCase = true)
              val targetIdx = if (idx != null && idx != -1) idx else editorState.text.indexOf(findQuery, 0, ignoreCase = true)
              if (targetIdx != -1) {
                editorState = editorState.copy(selection = TextRange(targetIdx, targetIdx + findQuery.length))
              } else {
                Toast.makeText(context, strings.noResultsFound, Toast.LENGTH_SHORT).show()
              }
            }
          },
          onReplace = {
            if (findQuery.isNotEmpty() && editorState.selection.length > 0) {
              val selText = editorState.text.substring(editorState.selection.start, editorState.selection.end)
              if (selText.equals(findQuery, ignoreCase = true)) {
                val newText = editorState.text.replaceRange(editorState.selection.start, editorState.selection.end, replaceQuery)
                updateTextWithHistory(TextFieldValue(newText, TextRange(editorState.selection.start + replaceQuery.length)))
              }
            }
          },
          onReplaceAll = {
            if (findQuery.isNotEmpty()) {
              val newText = editorState.text.replace(findQuery, replaceQuery, ignoreCase = true)
              updateTextWithHistory(TextFieldValue(newText, TextRange.Zero))
            }
          },
          onClose = { showFindReplace = false }
        )
      }

      // Code Editor Canvas with Line Numbers
      Row(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(verticalScrollState)
      ) {
        // Line numbers column
        if (lineNumbersEnabled) {
          Column(
            modifier = Modifier
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .padding(horizontal = 8.dp, vertical = 12.dp)
              .widthIn(min = 36.dp),
            horizontalAlignment = Alignment.End
          ) {
            for (i in 1..lineCount) {
              Text(
                text = "$i",
                style = TextStyle(
                  fontFamily = FontFamily.Monospace,
                  fontSize = currentFontSize.sp,
                  lineHeight = (currentFontSize * 1.4f).sp,
                  color = if (i == curLine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                  fontWeight = if (i == curLine) FontWeight.Bold else FontWeight.Normal
                )
              )
            }
          }

          Box(
            modifier = Modifier
              .width(1.dp)
              .fillMaxHeight()
              .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
          )
        }

        // Real Code TextField (with horizontal scroll if wordWrap is off)
        val textModifier = if (isWordWrap) {
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
        } else {
          Modifier
            .fillMaxHeight()
            .horizontalScroll(horizontalScrollState)
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .widthIn(min = 800.dp)
        }

        BasicTextField(
          value = editorState,
          onValueChange = { newVal ->
            updateTextWithHistory(newVal)
          },
          textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = FontFamily.Monospace,
            fontSize = currentFontSize.sp,
            lineHeight = (currentFontSize * 1.4f).sp
          ),
          cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
          visualTransformation = { annotated ->
            if (syntaxHighlighting && file != null) {
              val highlighted = SyntaxHighlighter.highlight(
                code = annotated.text,
                language = file.language,
                isDark = true
              )
              androidx.compose.ui.text.input.TransformedText(
                highlighted,
                androidx.compose.ui.text.input.OffsetMapping.Identity
              )
            } else {
              androidx.compose.ui.text.input.TransformedText(
                annotated,
                androidx.compose.ui.text.input.OffsetMapping.Identity
              )
            }
          },
          modifier = textModifier
            .focusRequester(focusRequester)
            .testTag("code_editor_text_field")
        )
      }
    }
  }

  // Go To Line Dialog
  if (showGoToLineDialog) {
    AlertDialog(
      onDismissRequest = { showGoToLineDialog = false },
      title = { Text(strings.goToLine) },
      text = {
        Column {
          Text(strings.linePrompt)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = goToLineInput,
            onValueChange = { goToLineInput = it },
            placeholder = { Text("1 - $lineCount") },
            singleLine = true
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val lineNum = goToLineInput.toIntOrNull()
            if (lineNum != null && lineNum in 1..lineCount) {
              val lines = editorState.text.lines()
              var offset = 0
              for (i in 0 until lineNum - 1) {
                offset += lines[i].length + 1
              }
              editorState = editorState.copy(selection = TextRange(offset))
            }
            showGoToLineDialog = false
          }
        ) {
          Text(strings.confirm)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showGoToLineDialog = false }) {
          Text(strings.cancel)
        }
      }
    )
  }
}

@Composable
private fun FindReplaceBar(
  findQuery: String,
  onFindQueryChange: (String) -> Unit,
  replaceQuery: String,
  onReplaceQueryChange: (String) -> Unit,
  onFindNext: () -> Unit,
  onReplace: () -> Unit,
  onReplaceAll: () -> Unit,
  onClose: () -> Unit
) {
  val strings = LocalAppStrings.current

  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        OutlinedTextField(
          value = findQuery,
          onValueChange = onFindQueryChange,
          placeholder = { Text(strings.find) },
          singleLine = true,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(8.dp)
        )
        IconButton(onClick = onFindNext) {
          Icon(Icons.Default.Search, contentDescription = strings.find)
        }
        IconButton(onClick = onClose) {
          Icon(Icons.Default.Close, contentDescription = strings.close)
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        OutlinedTextField(
          value = replaceQuery,
          onValueChange = onReplaceQueryChange,
          placeholder = { Text(strings.replace) },
          singleLine = true,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(8.dp)
        )
        TextButton(onClick = onReplace) {
          Text(strings.replace)
        }
        TextButton(onClick = onReplaceAll) {
          Text(strings.replaceAll)
        }
      }
    }
  }
}
