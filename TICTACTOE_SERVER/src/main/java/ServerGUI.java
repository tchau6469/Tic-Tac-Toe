import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.util.HashMap;


public class ServerGUI extends Application {

    Button startServerButton;
    TextField portTextField;
    Label portNumberLabel;
    HashMap<String, Scene> sceneHashMap;
    ListView<String> serverListView;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //GameInfo GUIGameInfo;
        sceneHashMap = new HashMap<>();

        startServerButton = new Button("START SERVER");
        startServerButton.setOnAction( e->  {sceneHashMap.put("serverScene",createServerScene());
            int portNum = Integer.parseInt(portTextField.getText());
            primaryStage.setTitle("This is the server");
            SERVER myServer = new SERVER(data-> {
                Platform.runLater(()-> {serverListView.getItems().add(data.toString());});},  portNum);



            primaryStage.setScene(sceneHashMap.get("serverScene"));
            primaryStage.show();

        });

        portTextField = new TextField("5555");
        portNumberLabel = new Label("Enter port number");
        VBox portVBox = new VBox(portNumberLabel, portTextField, startServerButton);
        Scene beginServerScene = new Scene(portVBox, 500, 500);
        primaryStage.setScene(beginServerScene);
        primaryStage.show();

    } //end start

    public Scene createServerScene() {
        serverListView = new ListView<String>();
        BorderPane bpane = new BorderPane();
        bpane.setCenter(serverListView);
        return new Scene(bpane,500, 500);
    }
}
