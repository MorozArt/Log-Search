package com.taskone;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class App {
    private JPanel mainPanel;
    private JTextField pathTextField;
    private JButton chooseButton;
    private JButton searchButton;
    private JTextField searchTextField;
    private JTextField typeTextField;
    private JScrollPane scrollPane;
    private JTree tree;
    private JScrollPane fileTextScrollPane;
    private JTextPane fileTextArea;
    private JButton previosButton;
    private JButton nextButton;
    private Map<String, ArrayList<Integer>> findIndexes = new HashMap<>();
    private int currentHighlightIndex;


    public App() {
        setupUI(); // Метод для установки интерфейса
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Обработчик для кнопки "Выбрать", который открывает диалог выбора директории
        // и сохраняет выбранную директорию в pathTextField
        chooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter(
                        "*." + typeTextField.getText(), typeTextField.getText()));
                fileChooser.setDialogTitle("Выбор директории");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    pathTextField.setText(fileChooser.getSelectedFile().toString());
                }

            }
        });

        // Обработчик для кнопки "Найти", который запускает поток FindThread для поиска требуемых файлов
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileTextArea.setText("");
                findIndexes.clear();
                File dir = new File(pathTextField.getText());
                FindThread findThread = new FindThread(tree, dir, searchTextField.getText(),
                                                        typeTextField.getText(), findIndexes);
                findThread.setDaemon(true);
                findThread.start();
            }
        });

        // Обработчик события "Выбор" для tree, который запускает OpenThread
        // для открытия выбранного файла в fileTextArea
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {

                OpenThread openThread = new OpenThread(
                        e, currentHighlightIndex, fileTextArea, searchTextField.getText(), findIndexes);
                openThread.setDaemon(true);
                openThread.start();
            }
        });

        // Обработчик для кнопки "Следующее", который при наличии текста для поиска "подсвечивает" следующее
        // вхождение этого текста относительно текущего
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!searchTextField.getText().isEmpty()) {
                    changeHighlightIndex(1);
                }
            }
        });

        // Обработчик для кнопки "Предыдущее", который при наличии текста для поиска "подсвечивает" предыдущее
        // вхождение этого текста относительно текущего
        previosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!searchTextField.getText().isEmpty()) {
                    changeHighlightIndex(-1);
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("TaskOne");
        frame.setContentPane(new App().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    // Метод для изменения текущей "подсветки", если входное значение value положительное, то "подсвечивается"
    // следующее вхождение относительно текущего, если отрицательное то предыдущее
    private void changeHighlightIndex (int value) {
        TreePath selected = tree.getSelectionPath();
        ArrayList<Integer> indexes = findIndexes.get(selected.getLastPathComponent().toString());

        if(value > 0) {
            if (currentHighlightIndex < indexes.size() - 1) {
                ++currentHighlightIndex;
            } else {
                currentHighlightIndex = 0;
            }
        } else {
            if (currentHighlightIndex != 0) {
                --currentHighlightIndex;
            } else {
                currentHighlightIndex = indexes.size() - 1;
            }
        }

        try {
            fileTextArea.getHighlighter().removeAllHighlights();
            fileTextArea.getHighlighter().addHighlight(indexes.get(currentHighlightIndex),
                    indexes.get(currentHighlightIndex) + searchTextField.getText().length(),
                    new DefaultHighlighter.DefaultHighlightPainter(Color.red));
            fileTextArea.moveCaretPosition(indexes.get(currentHighlightIndex));
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
    }

    // Метод создания интерфейса
    private void setupUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NORTH;
        constraints.weightx = 0.0;
        constraints.gridy   = 0  ;

        final JLabel label1 = new JLabel();
        label1.setText("Введите путь");
        constraints.gridx = 0;
        mainPanel.add(label1, constraints);

        pathTextField = new JTextField();
        constraints.gridx = 1;
        constraints.ipadx = 130;
        mainPanel.add(pathTextField, constraints);

        chooseButton = new JButton();
        chooseButton.setText("Выбрать");
        constraints.gridx = 2;
        mainPanel.add(chooseButton, constraints);

        searchButton = new JButton();
        searchButton.setText("Найти");
        constraints.gridx = 3;
        mainPanel.add(searchButton, constraints);

        final JLabel label2 = new JLabel();
        label2.setText("Введите текст для поиска:");
        constraints.gridx = 0;
        constraints.gridy = 1;
        mainPanel.add(label2, constraints);

        searchTextField = new JTextField();
        constraints.ipadx = 130;
        constraints.gridx = 1;
        constraints.gridy = 1;
        mainPanel.add(searchTextField, constraints);

        final JLabel label3 = new JLabel();
        label3.setText("Искать в файлах формата:");
        constraints.gridx = 2;
        constraints.gridy = 1;
        mainPanel.add(label3, constraints);

        typeTextField = new JTextField();
        typeTextField.setText("log");
        constraints.ipadx = 100;
        constraints.gridx = 3;
        constraints.gridy = 1;
        mainPanel.add(typeTextField, constraints);

        scrollPane = new JScrollPane();
        DefaultMutableTreeNode top = new DefaultMutableTreeNode();
        top.removeAllChildren();
        tree = new JTree(new DefaultTreeModel(top));
        scrollPane.setViewportView(tree);
        scrollPane.setBorder(BorderFactory.createTitledBorder(""));
        constraints.gridheight = 4;
        constraints.gridwidth = 2;
        constraints.gridx     = 0;
        constraints.gridy     = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.ipadx = 0;
        mainPanel.add(scrollPane, constraints);

        fileTextScrollPane = new JScrollPane();
        fileTextArea = new JTextPane();
        fileTextArea.setEditable(false);
        fileTextScrollPane.setViewportView(fileTextArea);
        constraints.gridheight = 4;
        constraints.gridwidth = 2;
        constraints.gridx     = 2;
        constraints.gridy     = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.ipady = 400;
        mainPanel.add(fileTextScrollPane, constraints);

        previosButton = new JButton();
        previosButton.setText("Предыдущее");
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 1;
        constraints.gridx = 2;
        constraints.gridy = 6;
        constraints.ipady = 0;
        mainPanel.add(previosButton, constraints);

        nextButton = new JButton();
        nextButton.setText("Следующее");
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 3;
        constraints.gridy = 6;
        mainPanel.add(nextButton, constraints);
    }
}
