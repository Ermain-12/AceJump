package com.johnlindquist.acejump.view

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors.CARET_COLOR
import com.intellij.openapi.editor.colors.EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES
import com.intellij.openapi.editor.colors.EditorColorsManager.getInstance
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.markup.EffectType.BOXED
import com.intellij.openapi.editor.markup.EffectType.ROUNDED_BOX
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.ProjectManager
import com.johnlindquist.acejump.config.AceConfig
import com.johnlindquist.acejump.config.AceConfig.Companion.settings
import com.johnlindquist.acejump.search.defaultEditor
import com.johnlindquist.acejump.view.Boundary.FullFileBoundary
import java.awt.Color
import java.awt.Color.*
import java.awt.Font
import java.awt.Font.BOLD
import java.awt.Font.PLAIN

/**
 * Data holder for all settings and IDE components needed by AceJump.
 */

object Model {
  var editor = defaultEditor()
    get() = if (field.isDisposed) defaultEditor() else field
    set(value) {
      editorText = value.document.text.toLowerCase()
      if (value == field) return

//      // When the editor is updated, we must update some properties
//      Handler.reset()

      field = value

      EditorSettingsExternalizable.getInstance().run {
        naturalBlock = isBlockCursor
        naturalBlink = isBlinkCaret
      }

      naturalColor = getInstance().globalScheme.getColor(CARET_COLOR) ?: BLACK
    }

  val markup
    get() = editor.markupModel
  val project
    get() = editor.project ?: ProjectManager.getInstance().defaultProject
  val caretOffset
    get() = editor.caretModel.offset
  var editorText = editor.document.text.toLowerCase()

  var globalScheme = getInstance().globalScheme

  var naturalBlock = EditorSettingsExternalizable.getInstance().isBlockCursor
  var naturalBlink = EditorSettingsExternalizable.getInstance().isBlinkCaret
  var naturalColor = globalScheme.getColor(CARET_COLOR) ?: BLACK
  var naturalHighlight = globalScheme
    .getAttributes(TEXT_SEARCH_RESULT_ATTRIBUTES)?.backgroundColor ?: YELLOW

  val targetModeHighlightStyle =
    TextAttributes(null, null, settings.targetModeColor, ROUNDED_BOX, PLAIN)
  val textHighlightStyle =
    TextAttributes(null, GREEN, settings.textHighlightColor, BOXED, PLAIN)

  val scheme
    get() = editor.colorsScheme
  val font
    get() = Font(scheme.editorFontName, BOLD, scheme.editorFontSize)
  val fontWidth
    get() = editor.component.getFontMetrics(font).stringWidth("w")
  val fontHeight
    get() = editor.colorsScheme.editorFontSize
  val lineHeight
    get() = editor.lineHeight
  val rectHeight
    get() = fontHeight + 3
  val rectVOffset
    get() = lineHeight - (editor as EditorImpl).descent - fontHeight
  val arcD = rectHeight - 6
  var viewBounds = 0..0
  const val DEFAULT_BUFFER = 40000
  val LONG_DOCUMENT
    get() = DEFAULT_BUFFER < editorText.length
  const val MAX_TAG_RESULTS = 300

  val DEFAULT_BOUNDARY = FullFileBoundary
  var boundaries: Boundary = DEFAULT_BOUNDARY

  data class Settings(var allowedChars: List<Char> =
                        ('a'..'z').plus('0'..'9').toList(),
                      var jumpModeColor: Color = BLUE,
                      var targetModeColor: Color = RED,
                      var definitionModeColor: Color = GREEN,
                      var textHighlightColor: Color = GREEN,
                      var tagForegroundColor: Color = BLACK,
                      var tagBackgroundColor: Color = YELLOW)

  fun Editor.setupCaret() {
    naturalBlock = settings.isBlockCursor
    settings.isBlockCursor = true

    naturalBlink = settings.isBlinkCaret
    settings.isBlinkCaret = false

    naturalColor = colorsScheme.getColor(CARET_COLOR) ?: BLACK
    colorsScheme.setColor(CARET_COLOR, AceConfig.settings.jumpModeColor)
  }
}
