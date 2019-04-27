package com.taskone;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OpenThread extends Thread {

    private TreeSelectionEvent e;
    private int currentHighlightIndex;
    private JTextPane fileTextArea;
    private String searchTextFieldValue;
    private Map<String, ArrayList<Integer>> findIndexes = new HashMap<>();

    public OpenThread(TreeSelectionEvent e, int currentHighlightIndex, JTextPane fileTextArea, String searchTextFieldValue, Map<String, ArrayList<Integer>> findIndexes) {
        this.e = e;
        this.currentHighlightIndex = currentHighlightIndex;
        this.fileTextArea = fileTextArea;
        this.searchTextFieldValue = searchTextFieldValue;
        this.findIndexes = findIndexes;
    }

    // Получение выбранного в tree файла и если он существует вывод его содержимого в соответствующее
    // окно (fileTextArea), также если задан текст для поиска (searchTextFieldValue), то устанавливается "подсветка"
    // на первое вхождение этого текста в файле
    @Override
    public void run() {
        currentHighlightIndex = 0;
        JTree tree = (JTree) e.getSource();
        TreePath selected = tree.getSelectionPath();
        if(selected != null) {
            fileTextArea.setText("");

            File file = new File(selected.getLastPathComponent().toString());
            if(file.exists()) {
                fileTextArea.setText(readText(file));
            }

            if (!searchTextFieldValue.isEmpty()) {
                ArrayList<Integer> indexes = findIndexes.get(selected.getLastPathComponent().toString());
                try {
                    fileTextArea.getHighlighter().addHighlight(indexes.get(currentHighlightIndex), indexes.get(currentHighlightIndex) + searchTextFieldValue.length(),
                            new DefaultHighlighter.DefaultHighlightPainter(Color.red));
                    fileTextArea.moveCaretPosition(indexes.get(currentHighlightIndex));
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    // Метод для чтения текста из файла
    private String readText (File file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file.getPath()), "Cp1251"))) {
            StringBuilder line = new StringBuilder();
            String text = "";
            int it = -1;
            while ((it = reader.read()) != -1) {
                line.append((char) it);
            }
            text = line.toString();
            return text;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}