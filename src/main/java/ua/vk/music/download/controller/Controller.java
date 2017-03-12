package ua.vk.music.download.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ua.vk.music.download.Main;
import ua.vk.music.download.model.AudioDownloader;
import ua.vk.music.download.model.HttpSender;
import ua.vk.music.download.model.Parser;
import ua.vk.music.download.object.Audio;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller {

    private ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());;
    private HttpSender httpSender = HttpSender.getInstance();
    private Parser parser = new Parser();
    private AudioDownloader downloader = new AudioDownloader();

    private ObservableList<Audio> observableAudioList = FXCollections.observableArrayList();

    @FXML
    private TextField id;

    @FXML
    private TableView audioTable;
    @FXML
    private TableColumn<Audio, String> artist;
    @FXML
    private TableColumn<Audio, String> title;
    @FXML
    private TableColumn<Audio, String> duration;

    @FXML
    private void initialize(){
        artist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        duration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        audioTable.setItems(observableAudioList);
    }

    public void showLoginDialog(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            stage.setTitle("Войти");
            stage.setMinHeight(150);
            stage.setMinWidth(300);
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((MenuItem) actionEvent.getSource()).getParentPopup().getOwnerWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void search(ActionEvent actionEvent) {
        if(id == null || id.getText().isEmpty()) return;

        System.out.println(id.getText());

        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                String response = null;
                try {
                    response = httpSender.searchAudio(id.getText());
                    observableAudioList.addAll(parser.audioParser(response));
                } catch (IOException e) {
                    e.printStackTrace();
                    alert(Alert.AlertType.WARNING, "Ошибка", "Нет подключения к сети");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        service.submit(task);
    }

    public void searchInAudio(ActionEvent actionEvent) {
        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                String response = null;
                try {
                    response = httpSender.searchUserAudio(id.getText());
                    observableAudioList.addAll(parser.audioParser(response));
                } catch (IOException e) {
                    e.printStackTrace();
                    alert(Alert.AlertType.WARNING, "Ошибка", "Нет подключения к сети");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        service.submit(task);
    }

    public void searchOnWall(ActionEvent actionEvent) {
        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                String response = null;
                try {
                    response = httpSender.searchAudioOnWall("-" + id.getText());
                    observableAudioList.addAll(parser.wallParser(response));
                } catch (IOException e) {
                    e.printStackTrace();
                    alert(Alert.AlertType.WARNING, "Ошибка", "Нет подключения к сети");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        service.submit(task);
    }

    public void download(ActionEvent actionEvent) {
        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Audio selectedAudio = (Audio) audioTable.getSelectionModel().getSelectedItem();
                downloader.downloadAudio(selectedAudio);
                return null;
            }
        };
        service.submit(task);
    }

    public void downloadAll(ActionEvent actionEvent) {
        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                downloader.downloadAllAudio(observableAudioList);
                return null;
            }
        };

        service.submit(task);
    }

    private void alert(Alert.AlertType alertType, String title, String text){
        Alert alert = new Alert(alertType);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(Main.getMainStage());
        alert.setTitle(title);
        alert.setContentText(text);
        alert.showAndWait();
    }
}
