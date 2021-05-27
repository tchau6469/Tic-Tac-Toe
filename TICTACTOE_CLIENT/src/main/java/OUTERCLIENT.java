import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.stage.WindowEvent;
import javafx.scene.paint.Color;

import javafx.scene.shape.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.util.Duration;

/*REFERENCE: https://github.com/AlmasB/FXTutorials/blob/master/src/com/almasb/tictactoe/TicTacToeApp.java
  had a cool looking gameboard
  REFERENCE: https://www.mkyong.com/java8/java-8-how-to-sort-a-map/
  we dont know how to sort a hashmap :(
 */

public class OUTERCLIENT extends Application {

    BorderPane bpane;
    GameInfo clientGameInfo;
    ListView<String> clientListView = new ListView<>();
    Stage currentWindow;
    int tileCount;
    Button playAgainButton;
    ArrayList<Tile> gameBoardList;
    int portNum;
    String IPAddress;
    String desiredName;
    Client myClient;
    Label resultLabel;
    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        currentWindow = primaryStage;
        clientGameInfo = new GameInfo();

        primaryStage.setTitle("Lets play some tic tac toe!!");
        //setting up client connection screen
        Button startClientButton = new Button("START CLIENT UP!!!");
        Label portNumLabel = new Label("Enter port number ");
        Label IPAddresslabel = new Label("Enter IP Address");
        Label nameLabel = new Label("Enter a name");
        TextField portNumTextField = new TextField("5555");
        TextField IPAddressTextField = new TextField("127.0.0.1");
        TextField nameTextField = new TextField();
        VBox inputVBox = new VBox(nameLabel, nameTextField, portNumLabel, portNumTextField, IPAddresslabel, IPAddressTextField, startClientButton);
        bpane = new BorderPane();
        bpane.setCenter(inputVBox);
        primaryStage.setScene(new Scene(bpane, 600, 600));
        primaryStage.show();

        startClientButton.setOnAction(e-> { //eventhandler for start client button
            desiredName = nameTextField.getText();
            portNum = Integer.parseInt(portNumTextField.getText());
            IPAddress = IPAddressTextField.getText();

            myClient = new Client (portNum, IPAddress);

            myClient.start();

            primaryStage.setScene(createDifficultyScene());
            primaryStage.show();

        });//end of actionevent handler for start client button


    }

    //client class
    class Client extends Thread {
        Socket clientSocket;
        ObjectOutputStream outputStream;
        ObjectInputStream inputStream;
        int portNumber;
        String IPAddress;


        Client( int port, String IP) {//client constructor

            portNumber = port;
            IPAddress = IP;
        }

        public void run() { //run method for threads
            try { //setting up client connection with clienthandler
                clientSocket = new Socket(IPAddress, portNumber);
                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                inputStream = new ObjectInputStream(clientSocket.getInputStream());
                clientSocket.setTcpNoDelay(true);
                //getting an initial gameinfo object from clienthandler and placing name in field
                clientGameInfo = (GameInfo)inputStream.readObject();
                clientGameInfo.playerName = desiredName;

                //sending it back
                this.send();

                //getting another updated gameinfo object
                clientGameInfo = (GameInfo)inputStream.readObject();


                //setting up leaderboard
                Map<String, Integer> hello = anothersort((HashMap<String, Integer>) clientGameInfo.mapOfPlayers);

                for (Map.Entry<String, Integer> key : hello.entrySet()) {
                    clientListView.getItems().add(key.getKey() + ", Score: " + key.getValue());
                }

            }//end try

            catch (Exception e) {
                System.out.println("connection was not made");
            }

            while (true) {
                try {
                    clientGameInfo = (GameInfo)inputStream.readObject();
                    Platform.runLater(()-> {


                        if (!clientGameInfo.whoWon.equals("")) { //results of the game, game is over
                            gameBoardList.get(clientGameInfo.serverChoice).setTextToX();
                            String resultString = clientGameInfo.whoWon;
                            if (resultString.equals("player")){ //player won
                                resultLabel.setText("YOU WON!!!!");
                                resultLabel.setVisible(true);
                            }
                            else if (resultString.equals("server")){ //server won
                                resultLabel.setText("YOU LOSE!!!! :(");
                                resultLabel.setVisible(true);
                            }

                            else {
                                resultLabel.setText("DRAW!!!!! :("); //DRAW
                                resultLabel.setVisible(true);
                            }
                            clientGameInfo.serverChoice = -1;
                            clientGameInfo.whoWon = "";

                            //disable all tiles until new game is made
                            for (Tile tile: gameBoardList) {
                                tile.setOnMouseClicked(null);
                            }
                            playAgainButton.setVisible(true); //making playagainbutton visible


                        }

                        if (clientGameInfo.isInGame) { //filling in square that server chose
                            if (clientGameInfo.PlayersTurn) {
                                if (clientGameInfo.serverChoice != -1) {
                                    gameBoardList.get(clientGameInfo.serverChoice).setTextToX();
                                }
                            }
                        }


                        //updating leaderboard. sometimes server sends in a gameinfo with just updated leaderboard
                        clientListView.getItems().clear();
                        Map<String, Integer> hello = anothersort((HashMap<String, Integer>) clientGameInfo.mapOfPlayers);
                        for (Map.Entry<String, Integer> key : hello.entrySet()) {
                            clientListView.getItems().add(key.getKey() + ", Score: " + key.getValue());
                        }


                    }); //end of platform.runlater

                }//end of try

                catch (Exception e) {}

            }//end of infinite while-loop
        }//end of run method

        public synchronized void send() { //client sends gameinfo back to clienthandler
            try {
                System.out.println("client : " + desiredName + " sending gameinfo to server");
                outputStream.flush();
                outputStream.writeObject(clientGameInfo);
                outputStream.reset();
            }
            catch (Exception e) {}
        }


        //function to sort a hashmap based on key's value
        public Map<String, Integer> anothersort(HashMap<String, Integer> unsortMap) {
            Map<String, Integer> sortedMap = unsortMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (val1, val2) -> val1, LinkedHashMap::new));
            return sortedMap;
        }



    }//end Client class


    class Tile extends StackPane {//tile class to make the gameboard interface
        Text text = new Text();
        int tileNumber;



        Tile(int tileNumber) { //Tile constructor

            Rectangle gameSquare = new Rectangle (200, 200);
            gameSquare.setFill(null);
            gameSquare.setStroke(Color.BLACK);
            text.setStyle("-fx-font-size: 90");


            this.setOnMouseClicked(e-> { //action handler for tile

                System.out.println("just pressed tile: " + (gameBoardList.get(this.tileNumber).tileNumber+1));

                if (clientGameInfo.PlayersTurn) {
                    if (isMoveValid(clientGameInfo.gameBoardString, this.tileNumber * 2)) {//multiplying tileNumber by 2 to get correct index in game string
                        setTextToO();

                        String oldString = clientGameInfo.gameBoardString;
                        clientGameInfo.gameBoardString = oldString.substring(0, tileNumber * 2) + 'O' + oldString.substring(tileNumber * 2 + 1); //replacing gameString with new gamestring
                        //servers turn is true, players turn is false
                        clientGameInfo.ServersTurn = true;
                        clientGameInfo.PlayersTurn = false;
                        clientGameInfo.playerChoice = this.tileNumber;
                        myClient.send();

                    } //end of inner if-statement
                    else {
                        System.out.println("Tile " + (tileNumber+1) + " is already taken. choose another tile");
                        return;
                    }
                }// end of outer if-statement

                else {
                    System.out.println("It is not your turn. wait a lil plz");
                    return;
                }

            }); //end action event for tile

            this.tileNumber = tileNumber;
            setAlignment(Pos.CENTER);
            this.getChildren().addAll(gameSquare, text);


        }//end of Tile constructor


        public void setTextToX(){
            text.setText("X");
        } //method to change the text of square to X

        public void setTextToO(){
            text.setText("O");
        } //method to change the text of square to O


    }//end of Tile class


    public boolean isMoveValid(String gameBoardString, int index) { //function to check if player's desired move is valid. called when player presses a game space on board
        String letterAtIndex = Character.toString(gameBoardString.charAt(index));

        if (letterAtIndex.equals("b")) return true;

        else return false;
    }//end isMoveValid method


    public Scene createDifficultyScene() {
        //creating difficulty buttons
        Button easyButton = new Button("EASY");
        easyButton.setMinSize(200,100);
        Button mediumButton = new Button("MEDIUM");
        mediumButton.setMinSize(200,100);
        Button expertButton = new Button("EXPERT");
        expertButton.setMinSize(200,100);

        Label difficultyLabel = new Label("CHOOSE DIFFICULTY");
        Label topLabel = new Label("PLAYER SCORES");

        clientListView.setPrefSize(200,200);

        VBox topBox = new VBox(topLabel, clientListView);

        //setting eventhandler for difficulty buttons. mostly game initialization stuff
        easyButton.setOnAction(e-> {
            clientGameInfo.difficulty = "easy";
            clientGameInfo.isInGame = true;
            clientGameInfo.justStartedGame = true;
            clientGameInfo.PlayersTurn =true;
            clientGameInfo.ServersTurn = false;
            myClient.send();
            currentWindow.setScene(createGameScene()); //set stage to gamescene
            currentWindow.show();


        });

        mediumButton.setOnAction(e-> {
            clientGameInfo.difficulty = "medium";
            clientGameInfo.isInGame = true;
            clientGameInfo.justStartedGame = true;
            clientGameInfo.PlayersTurn =true;
            clientGameInfo.ServersTurn = false;
            myClient.send();
            currentWindow.setScene(createGameScene());
            currentWindow.show();

        });

        expertButton.setOnAction(e-> {
            clientGameInfo.difficulty = "expert";
            clientGameInfo.isInGame = true;
            clientGameInfo.justStartedGame = true;
            clientGameInfo.PlayersTurn =true;
            clientGameInfo.ServersTurn = false;
            myClient.send();
            currentWindow.setScene(createGameScene());
            currentWindow.show();

        });

        VBox buttonBox = new VBox(40);
        buttonBox.getChildren().addAll(difficultyLabel, easyButton, mediumButton, expertButton);


        BorderPane bpane = new BorderPane();
        bpane.setLeft(buttonBox);
        bpane.setRight(topBox);

        return new Scene(bpane, 600, 600); //return newly created scene



    }//end createDiffucultyScene method

    public Scene createGameScene() { //method to create gamescene
        gameBoardList = new ArrayList<Tile>();
        Pane pane = new Pane();
        for(int i = 0; i < 3; i++) { //making the board and marking each tile with its own number to check its text
            for (int j = 0; j <3; j++) {
                Tile tile = new Tile(tileCount);
                tileCount++;
                tile.setTranslateX(j * 200);
                tile.setTranslateY(i * 200);

                gameBoardList.add(tile);
                pane.getChildren().add(tile);


            }//end inner for-loop

        }//end outer for-loop
        tileCount = 0; //reset tile count for next game for same client
        resultLabel = new Label();
        resultLabel.setStyle("-fx-font-size: 50");
        resultLabel.setTranslateY(200);
        resultLabel.setTranslateX(200);
        resultLabel.setVisible(false);

        //a playagainbutton that sets stage back to difficulty scene
        playAgainButton = new Button("Play again");
        playAgainButton.setTranslateX(300);
        playAgainButton.setTranslateY(300);

        playAgainButton.setVisible(false); //make play button not visible initially

        playAgainButton.setOnAction(e-> {

            currentWindow.setScene(createDifficultyScene());
            currentWindow.show();
        });//end of playagainbutton action handler lambda

        pane.getChildren().addAll(playAgainButton, resultLabel);

        return new Scene(pane, 600, 600);
    }//end createGameScene method


}//end OUTERCLIENT






