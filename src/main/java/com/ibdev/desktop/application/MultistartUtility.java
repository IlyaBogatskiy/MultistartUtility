package com.ibdev.desktop.application;

import com.ibdev.desktop.application.model.FileItem;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * @author VPVXNC
 */
public class MultistartUtility extends Application {

    private static final String EXE_PATHS = "src/main/resources/com/ibdev/desktop/application/exePaths.txt";
    private static final String CONFIGURATION_PROPERTIES =
            "src/main/resources/com/ibdev/desktop/application/configuration.properties";

    private ObservableList<FileItem> exePaths;
    private List<Process> runningProcesses;
    private Preferences preferences;

    private Button removeAllButton;
    private Button startButton;
    private Button stopButton;
    private Button removeButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        exePaths = FXCollections.observableArrayList();
        runningProcesses = new ArrayList<>();

        ListView<FileItem> listView = new ListView<>(exePaths);
        listView.setCellFactory(param -> new CheckBoxListCell());

        Button addButton = new Button("Добавить новый исполняемый файл");
        addButton.setOnAction(event -> addExecutablePath());

        removeButton = new Button("Удалить выделенное");
        removeButton.setOnAction(event -> removeSelectedPaths(listView));
        removeButton.setDisable(true);

        removeAllButton = new Button("Удалить всё");
        removeAllButton.setOnAction(event -> {
            if (confirmRemoveAll()) {
                removeAllPaths();
            }
        });
        removeAllButton.setDisable(true);

        startButton = new Button("Запустить приложения");
        startButton.setOnAction(event -> {
            try {
                startApplications();
            } catch (IOException e) {
                displayError("Ошибка запуска приложений", e.getMessage());
            }
        });
        startButton.setDisable(true);

        stopButton = new Button("Остановить приложения");
        stopButton.setOnAction(event -> stopApplications());
        stopButton.setDisable(true);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.addRow(0, listView);
        gridPane.addRow(1, addButton);
        gridPane.addRow(2, new HBox(10, removeButton, removeAllButton));
        gridPane.addRow(3, new HBox(10, startButton, stopButton));

        Scene scene = new Scene(gridPane, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("УТИЛИТА МУЛЬТИЗАПУСКА");

        primaryStage.setMinWidth(400);
        primaryStage.setMaxWidth(400);
        primaryStage.setMinHeight(300);
        primaryStage.setMaxHeight(300);

        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Предотвращает автоматическое закрытие окна

            if (!runningProcesses.isEmpty()) {
                stopApplicationsAndClose(primaryStage);
            } else {
                primaryStage.close();
            }
        });

        primaryStage.show();

        // Инициализация настроек приложения
        preferences = Preferences.userRoot().node(this.getClass().getName());
        loadPreferences();
    }

    private void addExecutablePath() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Executable Files", "*.exe");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            FileItem fileItem = new FileItem(selectedFile);

            if (isPathAlreadyExists(fileItem)) {
                displayError("Ошибка добавления пути", "Путь к исполняемому файлу уже существует в списке.");
                return;
            }

            exePaths.add(fileItem);
            savePreferences();
            updateButtonAvailability();
        }
    }

    private boolean isPathAlreadyExists(FileItem fileItem) {
        for (FileItem item : exePaths) {
            if (item.getFile().equals(fileItem.getFile())) {
                return true;
            }
        }
        return false;
    }

    private void removeSelectedPaths(ListView<FileItem> listView) {
        List<FileItem> selectedItems = new ArrayList<>(listView.getItems());
        selectedItems.removeIf(item -> !item.isSelected());

        if (!selectedItems.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Вы уверены, что хотите удалить выбранные элементы?");
            alert.setContentText("Удаление выбранных элементов приведет к их полному удалению из списка.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                exePaths.removeAll(selectedItems);
                savePreferences();
                updateButtonAvailability();
                listView.getSelectionModel().clearSelection();
            }
        }
    }

    private void removeAllPaths() {
        exePaths.clear();
        savePreferences();
        updateButtonAvailability();
    }

    private boolean confirmRemoveAll() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтвердите удаление");
        alert.setHeaderText(null);
        alert.setContentText("Вы уверены, что хотите удалить все пути?");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void startApplications() throws IOException {
        if (exePaths.isEmpty()) {
            displayError("Ошибка запуска приложений", "Нет выбранных исполняемых файлов.");
            return;
        }

        try {
            for (FileItem fileItem : exePaths) {
                ProcessBuilder pb = new ProcessBuilder(fileItem.getFile().getAbsolutePath());
                Process process = pb.start();
                runningProcesses.add(process);
            }
            updateButtonAvailability();
        } catch (IOException e) {
            throw new IOException("Ошибка запуска приложений.", e);
        }
    }

    private void stopApplicationsAndClose(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение закрытия");
        alert.setHeaderText(null);
        alert.setContentText("Вы уверены, что хотите закрыть утилиту? Все запущенные процессы будут остановлены.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            stopApplications();
            primaryStage.close();
        }
    }

    private void stopApplications() {
        if (runningProcesses.isEmpty()) {
            return;
        }

        for (Process process : runningProcesses) {
            process.destroy();
        }

        runningProcesses.clear();
        updateButtonAvailability();
    }

    private void displayError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void savePreferences() {
        preferences.putInt("exePathsCount", exePaths.size());
        for (int i = 0; i < exePaths.size(); i++) {
            FileItem fileItem = exePaths.get(i);
            preferences.put("exePath" + i, fileItem.getFile().getAbsolutePath());
        }
        savePathsToFile(); // Сохранение путей в файл
    }

    private void loadPreferences() {
        int exePathsCount = preferences.getInt("exePathsCount", 0);
        for (int i = 0; i < exePathsCount; i++) {
            String exePath = preferences.get("exePath" + i, null);
            if (exePath != null) {
                FileItem fileItem = new FileItem(new File(exePath));
                exePaths.add(fileItem);
            }
        }
        updateButtonAvailability();
        loadPathsFromFile(); // Загрузка путей из файла
    }

    private void updateButtonAvailability() {
        boolean isPathSelected = exePaths.stream().anyMatch(FileItem::isSelected);
        removeButton.setDisable(!isPathSelected);
        removeAllButton.setDisable(exePaths.size() < 2 || runningProcesses.size() > 0);
        startButton.setDisable(exePaths.size() < 2 || runningProcesses.size() > 0);
        stopButton.setDisable(runningProcesses.isEmpty());
    }

    private void savePathsToFile() {
        Properties properties = new Properties();
        properties.setProperty("exePaths", EXE_PATHS);

        try (OutputStream outputStream = new FileOutputStream("src/main/resources/com/ibdev/desktop/application/configuration.properties")) {
            properties.store(outputStream, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter writer = new PrintWriter(EXE_PATHS)) {
            for (FileItem fileItem : exePaths) {
                writer.println(fileItem.getFile().getAbsolutePath());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadPathsFromFile() {
        Properties properties = new Properties();

        try (InputStream inputStream = new FileInputStream(CONFIGURATION_PROPERTIES)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String exePathsFile = properties.getProperty("exePaths");
        if (exePathsFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(exePathsFile))) {
                String line;
                exePaths.clear();
                while ((line = reader.readLine()) != null) {
                    FileItem fileItem = new FileItem(new File(line));
                    exePaths.add(fileItem);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class CheckBoxListCell extends ListCell<FileItem> {
        private final CheckBox checkBox;

        public CheckBoxListCell() {
            checkBox = new CheckBox();
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                FileItem item = getItem();
                if (item != null) {
                    item.setSelected(newValue);
                    updateButtonAvailability();
                }
            });

            // Добавляем слушатель для изменения состояния выбора ячейки
            selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    getListView().getSelectionModel().select(getIndex());
                } else {
                    getListView().getSelectionModel().clearSelection(getIndex());
                }
            });
        }

        @Override
        protected void updateItem(FileItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                checkBox.setText(item.toString());
                checkBox.setSelected(item.isSelected());
                setGraphic(checkBox);
            }
        }
    }
}
