package org.bundleproject.discover.gui

import dev.isxander.xanderlib.utils.Multithreading
import org.bundleproject.discover.Discover
import org.bundleproject.discover.repo.entry.EntryAction
import org.bundleproject.discover.repo.entry.EntryWarning
import org.bundleproject.discover.repo.entry.ModEntry
import org.bundleproject.discover.utils.FileUtils.getResourceImage
import org.bundleproject.discover.utils.ImageUtils.getScaledImage
import org.bundleproject.discover.utils.StringUtils.wrapText
import org.bundleproject.discover.utils.UpdateHook
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.IOException
import java.util.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.html.HTMLEditorKit
import javax.swing.text.html.StyleSheet


class MainGui(private val discover: Discover) {
    private val modEntries: MutableMap<ModEntry, GuiEntry>
    fun refreshModIcon(mod: ModEntry) {
        modEntries[mod]!!.imageLabel.icon =
            ImageIcon(getScaledImage(discover.repositoryManager.getImage(mod.iconFile), 50, 50, mod.iconScaling))
    }

    fun openGuide(rawText: String?) {
        val guide = JFrame("Guide")
        guide.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        guide.iconImage = getResourceImage("/skyclient.png")
        guide.isResizable = false
        val pane = JPanel()
        val gridBag = GridBagLayout()
        val constraints = GridBagConstraints()
        guide.layout = gridBag
        pane.layout = gridBag
        constraints.fill = GridBagConstraints.HORIZONTAL
        val parser = Parser.builder().build()
        val document = parser.parse(rawText)
        val renderer = HtmlRenderer.builder().build()
        val html = renderer.render(document)
        val editorPane = JEditorPane()
        val kit = HTMLEditorKit()
        val style = StyleSheet()
        style.importStyleSheet(MainGui::class.java.getResource("/md-style.css"))
        kit.styleSheet = style
        editorPane.editorKit = kit
        editorPane.contentType = "text/html"
        editorPane.isEditable = false
        editorPane.text = html
        pane.add(editorPane)
        val sp = JScrollPane(
            pane,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        sp.verticalScrollBar.unitIncrement = 16
        sp.preferredSize = Dimension(800, 600)
        constraints.gridwidth = 0
        constraints.gridy = constraints.gridwidth
        constraints.gridx = constraints.gridy
        gridBag.setConstraints(sp, constraints)
        guide.add(sp)
        guide.pack()
        guide.isVisible = true
    }

    abstract class GuiCheckBox(text: String?) : JCheckBox(text) {
        abstract fun onPress()
    }

    class GuiEntry(val imageLabel: JLabel, val checkbox: GuiCheckBox)
    companion object {
        fun warnDanger(warning: EntryWarning?): Boolean {
            if (warning == null) return true
            val option = JOptionPane.showConfirmDialog(
                null,
                warning.messageHtml.replace("\"".toRegex(), ""),
                "Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            )
            return option == JOptionPane.YES_OPTION
        }

        fun genPopup(actions: Array<EntryAction?>): JPopupMenu {
            val popup = JPopupMenu()
            for (action in actions) {
                val menuItem = JMenuItem(action?.display)
                menuItem.addMouseListener(object : MouseAdapter() {
                    override fun mouseReleased(e: MouseEvent) {
                        Multithreading.runAsync {
                            action?.action?.run()
                        }
                    }
                })
                popup.add(menuItem)
            }
            popup.setSize(popup.width * 4, popup.height * 4)
            return popup
        }
    }

    init {
        modEntries = HashMap()
        discover.repositoryManager.fetchFiles()
        val icon = getResourceImage("/skyclient.png")
        val frame = JFrame("Bundle Discover")
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.iconImage = icon
        frame.isResizable = false
        val container = frame.contentPane
        val modPane = JPanel()
        val packPane = JPanel()
        val gridBag = GridBagLayout()
        val constraints = GridBagConstraints()
        container.layout = gridBag
        modPane.layout = gridBag
        packPane.layout = gridBag
        constraints.fill = GridBagConstraints.HORIZONTAL
        var i = 0
        for (mod in discover.repositoryManager.modEntries) {
            val visible = !mod.isHidden
            val description = wrapText(
                """
                      ${mod.description}
                      
                      - by ${mod.creator}
                      """.trimIndent(), 50
            )
            val imgLabel = JLabel(
                ImageIcon(
                    getScaledImage(
                        discover.repositoryManager.getImage(mod.iconFile),
                        50,
                        50,
                        mod.iconScaling
                    )
                )
            )
            imgLabel.name = mod.id
            imgLabel.preferredSize = Dimension(50, 50)
            imgLabel.toolTipText = description
            constraints.insets = Insets(1, 1, 1, 1)
            constraints.gridx = 0
            constraints.gridy = i
            gridBag.setConstraints(imgLabel, constraints)
            if (visible) modPane.add(imgLabel)
            val checkBox: GuiCheckBox = object : GuiCheckBox(mod.displayName) {
                override fun onPress() {
                    if (!isSelected || warnDanger(mod.warning)) {
                        mod.isEnabled = isSelected
                        if (mod.isEnabled) {
                            for (modId in mod.modRequirements) {
                                val requiredMod = discover.repositoryManager.getMod(modId)
                                if (requiredMod != null) {
                                    requiredMod.isEnabled = true
                                }
                                modEntries[requiredMod]!!.checkbox.isEnabled = true
                            }
                        }
                    }
                    isSelected = mod.isEnabled
                }
            }
            checkBox.name = mod.id
            checkBox.isSelected = mod.isEnabled
            checkBox.toolTipText = description
            checkBox.addActionListener { checkBox.onPress() }
            constraints.gridx = 1
            constraints.gridy = i
            gridBag.setConstraints(checkBox, constraints)
            if (visible) modPane.add(checkBox)
            val actionButton = JButton("^")
            actionButton.preferredSize = Dimension(30, 25)
            actionButton.horizontalAlignment = SwingConstants.CENTER
            actionButton.name = mod.id
            if (mod.actions.isNotEmpty()) {
                actionButton.toolTipText = "Mod Actions"
                actionButton.addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        genPopup(mod.actions).show(e.component, e.x, e.y)
                    }
                })
            } else {
                actionButton.text = ""
                actionButton.isEnabled = false
            }
            constraints.gridx = 3
            constraints.gridy = i
            gridBag.setConstraints(actionButton, constraints)
            if (visible) modPane.add(actionButton)
            modEntries[mod] = GuiEntry(imgLabel, checkBox)
            if (visible) i++
        }
        discover.repositoryManager.getIcons(object : UpdateHook {
            override fun updateMod(mod: ModEntry?) {
                if (mod != null) {
                    refreshModIcon(mod)
                }
            }
        })
        val installButton = JButton("Install")
        installButton.addActionListener {
            try {
                installButton.isEnabled = false
                installButton.text = "Installing Bundle Discover... This may take a while."
                discover.install()
                installButton.isEnabled = true
                installButton.text = "Install"
                JOptionPane.showMessageDialog(
                    null,
                    "Bundle Discover has been successfully installed.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        installButton.preferredSize = Dimension(200, 30)
        constraints.insets = Insets(1, 1, 1, 3)
        constraints.gridwidth = 8
        constraints.gridx = 0
        constraints.gridy = 2
        gridBag.setConstraints(installButton, constraints)
        container.add(installButton)
        val pathDisplayText = JTextField(discover.mcDir!!.absolutePath, 1)
        pathDisplayText.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                onType()
            }

            override fun removeUpdate(e: DocumentEvent) {
                onType()
            }

            override fun changedUpdate(e: DocumentEvent) {
                onType()
            }

            private fun onType() {
                discover.mcDir = File(pathDisplayText.text)
            }
        })
        pathDisplayText.preferredSize = Dimension(150, 30)
        constraints.insets = Insets(1, 1, 1, 3)
        constraints.gridwidth = 3
        constraints.gridx = 0
        constraints.gridy = 3
        gridBag.setConstraints(pathDisplayText, constraints)
        container.add(pathDisplayText)
        val pathButton = JButton("Select Path")
        pathButton.addActionListener {
            val pathFrame = JFrame("Select Path")
            pathFrame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            pathFrame.iconImage = getResourceImage("/skyclient.png")
            pathFrame.isResizable = false
            val fileChooser = JFileChooser()
            fileChooser.addActionListener { listener: ActionEvent ->
                if (listener.actionCommand == JFileChooser.APPROVE_SELECTION) {
                    discover.mcDir = fileChooser.selectedFile
                    if (discover.mcDir != null) {
                        pathDisplayText.text = discover.mcDir!!.absolutePath
                    }
                    pathFrame.dispose()
                } else if (listener.actionCommand == JFileChooser.CANCEL_SELECTION) {
                    pathFrame.dispose()
                }
            }
            fileChooser.currentDirectory = discover.mcDir
            fileChooser.dialogTitle = "Select Minecraft Data Folder"
            fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            fileChooser.isAcceptAllFileFilterUsed = false
            pathFrame.add(fileChooser)
            pathFrame.pack()
            pathFrame.isVisible = true
        }
        pathButton.preferredSize = Dimension(25, 30)
        constraints.insets = Insets(1, 1, 1, 3)
        constraints.gridwidth = 1
        constraints.gridx = 3
        constraints.gridy = 3
        gridBag.setConstraints(pathButton, constraints)
        container.add(pathButton)
        val modLabel = JLabel("Mods", SwingConstants.CENTER)
        modLabel.preferredSize = Dimension(200, 30)
        constraints.gridwidth = 4
        constraints.gridx = 0
        constraints.gridy = 0
        gridBag.setConstraints(modLabel, constraints)
        container.add(modLabel)
        val modScrollPane = JScrollPane(
            modPane,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        )
        modScrollPane.verticalScrollBar.unitIncrement = 16
        modScrollPane.preferredSize = Dimension(740, 500)
        constraints.gridwidth = 4
        constraints.gridx = 0
        constraints.gridy = 1
        gridBag.setConstraints(modScrollPane, constraints)
        container.add(modScrollPane)
        frame.pack()
        frame.isVisible = true
    }
}
