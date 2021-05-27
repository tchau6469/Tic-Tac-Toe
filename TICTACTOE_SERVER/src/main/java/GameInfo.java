import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

class GameInfo implements Serializable {
    int playerChoice;
    int serverChoice;
    Map<String, Integer> mapOfPlayers;
    boolean justStartedGame;
    String playerName;
    boolean ServersTurn;
    boolean PlayersTurn;
    String gameBoardString;
    boolean isInGame;
    String difficulty;
    String whoWon;

    GameInfo() { //GameInfo constructor
        playerChoice = -1;
        serverChoice = -1;
        mapOfPlayers = new HashMap<String, Integer>();
        justStartedGame = false;
        playerName = "";
        ServersTurn = false;
        PlayersTurn = true;
        gameBoardString = "b b b b b b b b b";
        isInGame= false;
        difficulty = "";
        whoWon = "";
    }

}//end GameInfo class