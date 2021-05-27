import java.lang.reflect.Array;
import java.util.*;

public class FindNextMove extends Thread{
    private ArrayList<Node> listOfMoves;
    int serverMoveIndex;




    FindNextMove() {
        listOfMoves = new ArrayList<>();
    }

    //synchronized method so only one client can run method to avoid mixing up strings
    public synchronized int getMove(String mystring, String difficulty) {
        AI_MinMax myAI_MinMax = new AI_MinMax(mystring);
        listOfMoves = myAI_MinMax.movesList;

        serverMoveIndex = findMoveIndex(difficulty);
        return serverMoveIndex;

    }//end setBoard

    //returns index that AI should choose based on difficulty
    public synchronized int findMoveIndex(String difficulty) {


        if (difficulty.equals("expert")) {//expert difficulty
            for (Node node : listOfMoves) {
                if (node.getMinMax() == 10 || node.getMinMax() == 0 ) { //prioritizes win,tie
                    return node.getMovedTo();
                }
            }
        }

        if (difficulty.equals("medium")) { //medium difficulty
            for (Node node : listOfMoves) {
                if (node.getMinMax() == 10  || node.getMinMax() == -10 || node.getMinMax()== 0) {//prioritizes win, lose, tie
                    return node.getMovedTo();
                }
            }
        }

        if (difficulty.equals("easy")) { // easy mode. git gud
            for (Node node : listOfMoves) {
                if (node.getMinMax() == -10 || node.getMinMax() == 0 || node.getMinMax() == 10) {//prioritizes lose, tie, win
                    return node.getMovedTo();
                }
            }
        }

        return -1;
    }//end findMoveIndex method

    //checks if there is a draw in current gameBoard string
    public synchronized boolean IsThereDraw(String boardString) {
        for (char c : boardString.toCharArray()) {
            if (c == 'b') { //if a single char in the string is a 'b', then the game is not a tie

                return false;
            }
        }

        return true; //it's a tie
    }

    //checks if there is a winner
    public synchronized String checkIfThereIsWinner (String boardString) {
        char[] boardArray = boardString.toCharArray();
        /*
        if (IsThereDraw(boardString)) {
            return "draw";
        }*/

        if (boardArray[0] == 'O' && boardArray[2] == 'O' && boardArray[4] == 'O'){return "player";} //top horz

        if (boardArray[6] == 'O' && boardArray[8] == 'O' && boardArray[10] == 'O'){return "player";} //mid horz

        if (boardArray[12] == 'O' && boardArray[14] == 'O' && boardArray[16] == 'O'){return "player";} //bottom horz

        if (boardArray[0] == 'O' && boardArray[6] == 'O' && boardArray[12] == 'O'){return "player";} //left vert

        if (boardArray[2] == 'O' && boardArray[8] == 'O' && boardArray[14] == 'O'){return "player";} //mid vert

        if (boardArray[4] == 'O' && boardArray[10] == 'O' && boardArray[16] == 'O'){return "player";} //right vert

        if (boardArray[0] == 'O' && boardArray[8] == 'O' && boardArray[16] == 'O'){return "player";} // diag

        if (boardArray[4] == 'O' && boardArray[8] == 'O' && boardArray[12] == 'O'){return "player";} // other diag

        //THIS IS FOR SERVER WIN VVVV
        if (boardArray[0] == 'X' && boardArray[2] == 'X' && boardArray[4] == 'X'){return "server";} //top horz

        if (boardArray[6] == 'X' && boardArray[8] == 'X' && boardArray[10] == 'X'){return "server";} //mid horz

        if (boardArray[12] == 'X' && boardArray[14] == 'X' && boardArray[16] == 'X'){return "server";} //bottom horz

        if (boardArray[0] == 'X' && boardArray[6] == 'X' && boardArray[12] == 'X'){return "server";} //left vert

        if (boardArray[2] == 'X' && boardArray[8] == 'X' && boardArray[14] == 'X'){return "server";} //mid vert

        if (boardArray[4] == 'X' && boardArray[10] == 'X' && boardArray[16] == 'X'){return "server";} //right vert

        if (boardArray[0] == 'X' && boardArray[8] == 'X' && boardArray[16] == 'X'){return "server";} // diag

        if (boardArray[4] == 'X' && boardArray[8] == 'X' && boardArray[12] == 'X'){return "server";} // other diag

        if (IsThereDraw(boardString)) {
            return "draw";
        }

        return ""; //nobody won yet, so no result
    }//end checkIfThereIsWinner



}
