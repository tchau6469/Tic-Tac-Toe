import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SERVER {
    ArrayList<clientHandler> clients = new ArrayList<clientHandler>();
    int port;
    private Consumer<Serializable> callback;
    TheServer server;
    FindNextMove myAI;
    Map<String, Integer> mapOfPlayerScore;


    SERVER(Consumer<Serializable> call, int port) {
        mapOfPlayerScore = new HashMap<String, Integer>();
        callback = call;
        this.port = port;
        server = new TheServer();
        server.start();
        myAI = new FindNextMove();
        myAI.start();
    }


    class TheServer extends Thread {

        public void run() {
            System.out.println("Server Started");

            try (ServerSocket serversocket = new ServerSocket(port);) {
                System.out.println("Server is now open on port: " + port);
                callback.accept("Server is now waiting for clients on port: " + port);

                while (true) {
                    clientHandler c = new clientHandler(serversocket.accept());
                    clients.add(c);
                    c.start();

                }//end while


            }//end try

            catch (Exception e) {
                System.out.println("Server socket did not launch :(");
            }


        }//end run
    }//end TheServer class

    class clientHandler extends Thread {
        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;
        Socket clientSocket;
        String playerName;
        GameInfo serverGameInfo;


        clientHandler(Socket s) { //clientHandler constructor
            clientSocket = s;
            playerName = "";
            serverGameInfo = new GameInfo();
        }


        public void updateClientsLeaderboard () {
            for (clientHandler c : clients) {
                c.serverGameInfo.mapOfPlayers = mapOfPlayerScore;
                c.updateClient();
            }
        }

        public void updateClient() { //send server gameInfo to particular client

            try{
                this.outputStream.flush();
                this.outputStream.writeObject(this.serverGameInfo);
                this.outputStream.reset();
            }
            catch(Exception theseFists){}

        }//end updateClient

        public void run() {
            try {//setting up connection between client and clienthandler
                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                inputStream = new ObjectInputStream(clientSocket.getInputStream());
                clientSocket.setTcpNoDelay(true);

                outputStream.writeObject(serverGameInfo);

                serverGameInfo = (GameInfo)inputStream.readObject();
                playerName = serverGameInfo.playerName;


                if (mapOfPlayerScore.containsKey(playerName)) {
                    callback.accept("A guy tried joining with name: " + playerName +". NAME IS TAKEN HOMIE");
                    clientSocket.close();
                    clients.remove(this);
                    return;
                }

                serverGameInfo.mapOfPlayers.put(playerName, 0);



                mapOfPlayerScore.put(playerName, 0); //update the server's hashmap of scores


                callback.accept("Client that just joined: " + playerName);
                updateClientsLeaderboard();

            }//end try

            catch (Exception e) {
                System.out.println("hehe exception caught. client could not connect");
            }

            while (true) {
                try {
                    serverGameInfo = (GameInfo)inputStream.readObject();

                    if (serverGameInfo.justStartedGame) {//CHECK TO CHECK IF A PLAYER JUST STARTED GAME TO UPDATE SERVER LISTVIEW
                        callback.accept("Player: " + playerName + " has just started a game on difficulty: " + serverGameInfo.difficulty);
                        serverGameInfo.justStartedGame = false;
                        updateClient();
                    }

                    if (serverGameInfo.isInGame) { //check if the client is in a game

                        if (serverGameInfo.ServersTurn) { //if client made a move

                            callback.accept("Player: " + playerName + " picked square # " + (serverGameInfo.playerChoice+1));

                            serverGameInfo.whoWon = myAI.checkIfThereIsWinner(serverGameInfo.gameBoardString); //check if there is a winner



                           if (serverGameInfo.whoWon.equals("")) { //if there's no winner, server gets to move
                               serverGameInfo.serverChoice = myAI.getMove(serverGameInfo.gameBoardString, serverGameInfo.difficulty) - 1;
                               callback.accept("server playing against " + playerName + " picked square # " + (serverGameInfo.serverChoice + 1));

                               String oldString = serverGameInfo.gameBoardString;
                               serverGameInfo.gameBoardString = oldString.substring(0, serverGameInfo.serverChoice * 2) + 'X' + oldString.substring(serverGameInfo.serverChoice * 2 + 1);
                           }

                            serverGameInfo.whoWon = myAI.checkIfThereIsWinner(serverGameInfo.gameBoardString); //check to see if server won after it made a move

                            if (serverGameInfo.whoWon.equals("player") || serverGameInfo.whoWon.equals("server") || serverGameInfo.whoWon.equals("draw")) { //check to see if somebody won after server moved
                                callback.accept("winner of " + serverGameInfo.playerName + "'s game: " + serverGameInfo.whoWon);
                                if (serverGameInfo.whoWon.equals("player")) { //if player won
                                    mapOfPlayerScore.put(serverGameInfo.playerName, (mapOfPlayerScore.get(serverGameInfo.playerName) +1));
                                    updateClientsLeaderboard();

                                }


                            }

                            //theres a win. need to reset stoooof
                            if (serverGameInfo.whoWon.equals("player") || serverGameInfo.whoWon.equals("server") || serverGameInfo.whoWon.equals("draw")){
                                resetGameInfo();
                                updateClient();
                            }

                            else { //send gameinfo back to client after server made move
                                serverGameInfo.ServersTurn = false;
                                serverGameInfo.PlayersTurn = true;
                                updateClient();
                            }
                        }//end if (serverGameInfo.ServersTurn
                    }//end if (serverGameInfo.isInGame)



                }//end try

                catch (Exception e) {
                    callback.accept("OPZ, SOMETHING HAPPENED WITH CLIENT CONNECTION WITH CLIENT: " + playerName); //EDIT THIS LATER
                    clients.remove(this);
                    serverGameInfo.mapOfPlayers.remove(this.playerName); //get rid of playername that just left from the scores hashmap
                    mapOfPlayerScore.remove(this.playerName);
                    updateClientsLeaderboard(); //update everyone's leaderboard when someone left
                    break;
                }
            }//end while(true) infinite loop

        }//end run

        public void resetGameInfo() { //reset things that needa be reset in server gameinfo
            serverGameInfo.difficulty="";
            serverGameInfo.isInGame = false;
            serverGameInfo.playerChoice = -1;
            serverGameInfo.ServersTurn = false;
            serverGameInfo.PlayersTurn = true;
            serverGameInfo.gameBoardString = "b b b b b b b b b";

        }


    }//end clientHandler class



}//end SERVER class






