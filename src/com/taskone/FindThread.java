package com.taskone;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.*;
import java.util.*;

public class FindThread extends Thread {
    private JTree tree;
    private File file;
    private String searchTextFieldValue;
    private String typeTextFieldValue;
    private Map<String, ArrayList<Integer>> findIndexes;

    public FindThread(JTree tree, File file, String searchTextFieldValue,
                      String typeTextFieldValue, Map<String, ArrayList<Integer>> findIndexes) {
        this.tree = tree;
        this.file = file;
        this.searchTextFieldValue = searchTextFieldValue;
        this.typeTextFieldValue = typeTextFieldValue;
        this.findIndexes = findIndexes;
    }

    // Создание и установка модели treeModel для JTree из App
    @Override
    public void run() {
        tree.setModel(new DefaultTreeModel(null));
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(file.getName());
        DefaultTreeModel treeModel = new DefaultTreeModel(filesInFolder(file, root), true);
        tree.setModel(treeModel);
    }

    // Рекурсивный метод который ищет требуемые файлы в папке folder и создает соответствующие узлы для root
    private DefaultMutableTreeNode filesInFolder(File folder, DefaultMutableTreeNode root) {
        File[] files = folder.listFiles();
        for (int i=0;i<files.length;++i) {
            if (files[i].isFile()) {
                String filePath = checkFile(files[i]);
                if(!filePath.isEmpty()) {
                    root.add( new DefaultMutableTreeNode(filePath, false));
                }
            } else {
                filesInFolder(files[i], root);
            }
        }
        return root;
    }

    // Данный метод отвечает за проверку файла на требуемое разрешение(typeTextFieldValue)
    // и наличие искомого текста(searchTextFieldValue), если этот текст не задан то метод вернет путь к файлу
    // если он требуемого разрешения. А если задан текст для поиска то осуществляется поиск всех вхождений
    // этого текста в файле (String.indexOf(String str, int fromIndex)) и добавления соответствующих индексов в
    // ArrayList<Integer> indexes. После нахождения всех индексов они вместе с путем к соответствующему файлу
    // добавляются к Map<String, ArrayList<Integer>> findIndexes, который потом используется в App

    private String checkFile (File file) {
        String resultFilePath = "";
        if (getFileExtension(file.getName()).equals(typeTextFieldValue)) {
            if (!searchTextFieldValue.isEmpty()) {
                try (BufferedReader fileStream = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(file.toString()), "Cp1251"))) {
                    int it = -1, index = 0;
                    int searchTextLenght = searchTextFieldValue.length();
                    boolean find = false;
                    StringBuilder strBldr = new StringBuilder();
                    ArrayList<Integer> indexes = new ArrayList<>();
                    while ((it = fileStream.read()) != -1) {
                        strBldr.append((char) it);
                    }
                    String searchText = searchTextFieldValue;
                    String s = strBldr.toString().replaceAll("\r\n", " ");
                    while (index != -1) {
                        index = s.indexOf(searchText, index);
                        if (index != -1) {
                            indexes.add(index);
                            index += searchTextLenght;
                            if (!find) {
                                find = true;
                                resultFilePath = file.getPath();
                            }
                        }
                    }
                    findIndexes.put(file.getPath(), indexes);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                resultFilePath = file.getPath();
            }
        }

        return resultFilePath;
    }

    // Метод возвращающий тип файла
    private String getFileExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else return "";
    }
}
