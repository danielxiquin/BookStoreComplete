package org.example;

import org.json.JSONObject;
import java.util.HashMap;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;


class BTreeNode {
    List<JSONObject> books;
    List<BTreeNode> children;
    boolean isLeaf;

    public BTreeNode(boolean isLeaf) {
        this.books = new ArrayList<>();
        this.children = new ArrayList<>();
        this.isLeaf = isLeaf;
    }

    // Metodo de incertar
    public void insertNonFull(JSONObject book) {
        String isbn = book.getString("isbn");
        int i = this.books.size() - 1;


        if (this.isLeaf) {
            while (i >= 0 && this.books.get(i).getString("isbn").compareTo(isbn) > 0) {
                i--;
            }
            this.books.add(i + 1, book);
        } else {
            while (i >= 0 && this.books.get(i).getString("isbn").compareTo(isbn) > 0) {
                i--;
            }

            if (this.children.get(i + 1).books.size() == 4) {
                this.splitChild(i + 1, this.children.get(i + 1));
                if (this.books.get(i + 1).getString("isbn").compareTo(isbn) < 0) {
                    i++;
                }
            }
            this.children.get(i + 1).insertNonFull(book);
        }
    }

    public void splitChild(int i, BTreeNode y) {
        BTreeNode z = new BTreeNode(y.isLeaf);

        z.books.addAll(y.books.subList(2, y.books.size()));
        y.books.subList(2, y.books.size()).clear();

        if (!y.isLeaf) {
            z.children.addAll(y.children.subList(2, y.children.size()));
            y.children.subList(2, y.children.size()).clear();
        }

        this.children.add(i + 1, z);
        this.books.add(i, y.books.remove(1));

    }


    public int findKey(String isbn) {
        for (int idx = 0; idx < books.size(); idx++) {
            String currentIsbn = books.get(idx).getString("isbn");
            if (currentIsbn.equals(isbn)) {
                return idx;
            }
            if (currentIsbn.compareTo(isbn) > 0) {
                return idx;
            }
        }
        return books.size();
    }


    public boolean updateBook(String isbn, Map<String, Object> updateData) {
        int idx = findKey(isbn);


        if (idx < books.size() && books.get(idx).getString("isbn").equals(isbn)) {
            JSONObject book = books.get(idx);

            // Update each field in the book
            for (Map.Entry<String, Object> entry : updateData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                book.put(key, value);
            }

            return true;
        }

        if (!isLeaf) {

            if (idx < books.size() && isbn.compareTo(books.get(idx).getString("isbn")) > 0) {
                idx++;
            }

            if (idx < children.size()) {
                return children.get(idx).updateBook(isbn, updateData);
            }
        }

        return false;
    }


    public void removeBook(String isbn) {
        int idx = findKey(isbn);

        if (idx < books.size() && books.get(idx).getString("isbn").compareTo(isbn) == 0) {
            if (isLeaf) {
                books.remove(idx);
            } else {
                removeFromNonLeaf(idx);
            }
        } else {
            if (isLeaf) {
                return;
            }

            boolean flag = (idx == books.size());

            if (children.get(idx).books.size() < 2) {
                fill(idx);
            }

            if (flag && idx > books.size()) {
                children.get(idx - 1).removeBook(isbn);
            } else {
                children.get(idx).removeBook(isbn);
            }
        }
    }

    public void fill(int idx) {
        if (idx != 0 && children.get(idx - 1).books.size() >= 2) {
            borrowFromPrev(idx);
        } else if (idx != books.size() && children.get(idx + 1).books.size() >= 2) {
            borrowFromNext(idx);
        } else {
            if (idx != books.size()) {
                merge(idx);
            } else {
                merge(idx - 1);
            }
        }
    }

    public void removeFromNonLeaf(int idx) {
        String isbnToRemove = books.get(idx).getString("isbn");

        if (children.get(idx).books.size() >= 2) {
            JSONObject pred = getPredecessor(idx);
            books.set(idx, pred);
            children.get(idx).removeBook(pred.getString("isbn"));
        } else if (children.get(idx + 1).books.size() >= 2) {
            JSONObject succ = getSuccessor(idx);
            books.set(idx, succ);
            children.get(idx + 1).removeBook(succ.getString("isbn"));
        }

        else {
            merge(idx);
            children.get(idx).removeBook(isbnToRemove);
        }
    }

    public void borrowFromPrev(int idx) {
        BTreeNode child = children.get(idx);
        BTreeNode sibling = children.get(idx - 1);

        child.books.add(0, books.get(idx - 1));

        books.set(idx - 1, sibling.books.remove(sibling.books.size() - 1));

        if (!child.isLeaf) {
            child.children.add(0, sibling.children.remove(sibling.children.size() - 1));
        }
    }

    public void borrowFromNext(int idx) {
        BTreeNode child = children.get(idx);
        BTreeNode sibling = children.get(idx + 1);

        child.books.add(books.get(idx));

        books.set(idx, sibling.books.remove(0));

        if (!child.isLeaf) {
            child.children.add(sibling.children.remove(0));
        }
    }

    public void merge(int idx) {
        BTreeNode child = children.get(idx);
        BTreeNode sibling = children.get(idx + 1);

        child.books.add(books.remove(idx));

        child.books.addAll(sibling.books);

        if (!child.isLeaf) {
            child.children.addAll(sibling.children);
        }

        children.remove(idx + 1);
    }

    public JSONObject getPredecessor(int idx) {
        BTreeNode current = children.get(idx);
        while (!current.isLeaf) {
            current = current.children.get(current.books.size());
        }
        return current.books.get(current.books.size() - 1);
    }

    public JSONObject getSuccessor(int idx) {
        BTreeNode current = children.get(idx + 1);
        while (!current.isLeaf) {
            current = current.children.get(0);
        }
        return current.books.get(0);
    }

}

class BTree {
    BTreeNode root;
    int t;
    HashMap<String, JSONObject> bookIndexByName;
    HashMap<String, JSONObject> bookIndexByIsbn;

    public BTree() {
        this.t = 5;
        this.root = null;
        this.bookIndexByName = new HashMap<>();
        this.bookIndexByIsbn = new HashMap<>();
    }

    public void insert(JSONObject book) {
        String name = book.getString("name");
        String isbn = book.getString("isbn");


        if (bookIndexByName.containsKey(name)) {
            return;
        }

        if (bookIndexByIsbn.containsKey(isbn)) {
            return;
        }

        bookIndexByName.put(name, book);
        bookIndexByIsbn.put(isbn, book);

        if (root == null) {
            root = new BTreeNode(true);
            root.books.add(book);
        } else {
            if (root.books.size() == 4) {
                BTreeNode newNode = new BTreeNode(false);
                newNode.children.add(root);
                newNode.splitChild(0, root);
                root = newNode;
            }
            root.insertNonFull(book);
        }

    }

    public boolean updateBook(String isbn, Map<String, Object> updateData) {

        if (root != null) {
            JSONObject originalBook = findBookByIsbn(isbn);
            if (originalBook == null) {
                return false;
            }

            String oldName = originalBook.getString("name");

            // Update the book in the B-tree node.
            boolean updated = root.updateBook(isbn, updateData);

            if (updated) {
                JSONObject updatedBook = findBookByIsbn(isbn);

                if (updatedBook != null) {
                    String newName = updatedBook.getString("name");


                    // If the name has changed, update the bookIndexByName map.
                    if (!oldName.equals(newName)) {
                        bookIndexByName.remove(oldName); // Remove old name
                        bookIndexByName.put(newName, updatedBook); // Insert new name
                    }

                    // Ensure that the bookIndexByIsbn map is still accurate.
                    bookIndexByIsbn.put(isbn, updatedBook);

                    return true;
                }
            }
        }

        return false;
    }

    public void removeBook(String isbn) {
        if (root != null) {
            JSONObject bookToRemove = findBookByIsbn(isbn);

            if (bookToRemove != null) {
                String name = bookToRemove.getString("name");

                // Remove the book from both indexes.
                bookIndexByName.remove(name);
                bookIndexByIsbn.remove(isbn);
            }

            root.removeBook(isbn);
            if (root.books.size() == 0) {
                if (root.isLeaf) {
                    root = null;
                } else {
                    root = root.children.get(0);
                }
            }
        }
    }

    public JSONObject searchByName(String name) {
        return bookIndexByName.get(name);
    }

    public JSONObject findBookByIsbn(String isbn) {
        return bookIndexByIsbn.get(isbn);
    }
}


public class Main {
    public static void ReaderCSV(String file, BTree tree) {
        String filePath = file;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {

                int separatorIndex = line.indexOf(';');
                if (separatorIndex == -1) {
                    System.err.println("Formato incorrecto: " + line);
                    continue;
                }

                String operation = line.substring(0, separatorIndex).trim();
                String jsonData = line.substring(separatorIndex + 1).trim();

                try {
                    JSONObject jsonObject = new JSONObject(jsonData);

                    switch (operation) {
                        case "INSERT":
                            tree.insert(jsonObject);

                            break;
                        case "PATCH":
                            String isbn = jsonObject.getString("isbn");
                            Map<String, Object> updateData = jsonObject.toMap();
                            updateData.remove("isbn");
                            tree.updateBook(isbn, updateData);

                            break;

                        case "DELETE":
                            String isb = jsonObject.getString("isbn");
                            tree.removeBook(isb);
                            break;
                        default:
                            System.err.println("Operación desconocida: " + operation);
                    }

                } catch (Exception e) {

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void Exit(String file, BTree tree) {
        String filepath = file;

        try (BufferedReader reader = new BufferedReader(new FileReader(filepath));
             BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt", true))) {

            String line;

            while ((line = reader.readLine()) != null) {
                int separatorIndex = line.indexOf(';');
                if (separatorIndex == -1) {
                    System.err.println("Formato incorrecto: " + line);
                    continue;
                }

                String jsonData = line.substring(separatorIndex + 1).trim();

                try {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    String name = jsonObject.getString("name");

                    JSONObject foundBook = tree.searchByName(name);

                    if (foundBook != null) {
                        String formattedOutput = String.format(
                                "{\"isbn\":\"%s\",\"name\":\"%s\",\"author\":\"%s\",\"price\":\"%s\",\"quantity\":\"%s\"}",
                                foundBook.getString("isbn"),
                                foundBook.getString("name"),
                                foundBook.getString("author"),
                                foundBook.getString("price"),
                                foundBook.getString("quantity"));

                        writer.write(formattedOutput);
                        writer.newLine();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] encript(String data, String key) throws Exception {
        KeySpec keySpec = new DESKeySpec(key.getBytes(StandardCharsets.UTF_8));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(keySpec);

        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    public static void guardarEncript(byte[] encryptedData, String filePath) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(encryptedData);
        }
    }

    public static byte[] desencriptar(byte[] encryptedData, String key) throws Exception {
        KeySpec keySpec = new DESKeySpec(key.getBytes(StandardCharsets.UTF_8));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(keySpec);

        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return cipher.doFinal(encryptedData);
    }

    public static byte[] leerarchivoencript(String filePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return fis.readAllBytes();
        }
    }

    public static void guardarDesencript(byte[] decryptedData, String filePath) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(decryptedData);
        }
    }


    public static void main(String[] args) throws Exception {
        String file = "100Klab01_books.csv";
        String file2 = "100Klab01_search.csv";
        BTree tree = new BTree();
        ReaderCSV(file, tree);
        Exit(file2, tree);
        String file3 = "output.txt";
        String key = "ok:uo1IN";
        String data = new String(Files.readAllBytes(Paths.get(file3)));
        byte[] encriptData = encript(data, key);
        String encriptFile = "bitacora_encriptada.enc";
        guardarEncript(encriptData, encriptFile);
        String fileEncript = "bitacora_encriptada.enc";
        byte[] dataEncript = leerarchivoencript(fileEncript);
        byte[] dataDencript = desencriptar(dataEncript, key);
        String descripFile = "bitacora_desencriptada.txt";
        guardarDesencript(dataDencript, descripFile);
    }
}