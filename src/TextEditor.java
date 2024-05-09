import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

/**
 * Простий текстовий редактор з можливістю збереження резервних копій файлів.
 */
public class TextEditor extends JFrame implements ActionListener {
    private JTextArea textArea;
    private JTextField backupLocationField;
    private JTextField numberOfCopiesField;

    /**
     * Конструктор для створення вікна текстового редактора.
     */
    public TextEditor() {
        setTitle("Text Editor");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Додавання текстового поля для введення тексту
        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // Додавання панелі з полями введення та кнопкою збереження
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backupLocationField = new JTextField(20);
        backupLocationField.setToolTipText("Enter backup location"); // Підказка для поля розташування резервних копій
        numberOfCopiesField = new JTextField(5);
        numberOfCopiesField.setToolTipText("Enter number of copies"); // Підказка для поля кількості копій

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        bottomPanel.add(new JLabel("Backup Location:"));
        bottomPanel.add(backupLocationField);
        bottomPanel.add(new JLabel("Number of Copies:"));
        bottomPanel.add(numberOfCopiesField);
        bottomPanel.add(saveButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Обробник подій для кнопки збереження.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Save")) {
            saveFile();
        }
    }

    /**
     * Зберігає файл та його резервні копії.
     */
    private void saveFile() {
        String backupLocation = backupLocationField.getText();
        int numberOfCopies = Integer.parseInt(numberOfCopiesField.getText());

        File file = showSaveFileDialog();
        if (file != null) {
            // Створення окремого потока для збереження оригінального файлу
            Thread originalThread = new Thread(() -> saveOriginalFile(file));
            originalThread.start();

            // Створення окремих потоків для збереження копій файлу
            for (int i = 0; i < numberOfCopies; i++) {
                Thread copyThread = new Thread(() -> FileSaver.saveCopy(file, backupLocation, textArea.getText()));
                copyThread.start();
            }

            // Виведення повідомлення про успішне збереження
            JOptionPane.showMessageDialog(this, "File and copies saved successfully!");
        }
    }

    /**
     * Відображає діалогове вікно для вибору місця збереження файлу.
     */
    private File showSaveFileDialog() {
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Зберігає оригінальний файл.
     */
    private void saveOriginalFile(File originalFile) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(originalFile));
            writer.write(textArea.getText());
            writer.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving original file: " + e.getMessage());
        }
    }

    /**
     * Метод main, викликається при запуску програми.
     */
    public static void main(String[] args) {
        // Запуск головного вікна у потоці Swing
        SwingUtilities.invokeLater(() -> {
            TextEditor editor = new TextEditor();
            editor.setVisible(true);
        });
    }
}

/**
 * Клас для збереження резервних копій файлів.
 */
class FileSaver {
    /**
     * Зберігає копію файлу.
     */
    public static void saveCopy(File originalFile, String backupLocation, String content) {
        File backupDir = new File(backupLocation);
        if (!backupDir.exists()) {
            backupDir.mkdirs(); // Створюємо директорію, якщо вона не існує
        }

        String backupFileName = originalFile.getName() + "_backup_" + UUID.randomUUID() + ".txt";
        File backupFile = new File(backupLocation + File.separator + backupFileName);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile));
            writer.write(content); // Використовуємо текст з текстового поля
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
