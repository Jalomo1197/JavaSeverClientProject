import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.control.ListView;

public class Server{
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	ArrayList<Integer> reuseNumbers = new ArrayList<Integer>();
	private Consumer<Serializable> callback;
    TheServer server;
	GameInfo gameInfo;
    boolean client1sus;
    boolean anotherGame;
    int presentClients;
    int count;
    int portNum;

	Server(Consumer<Serializable> call, int port){
		callback = call;
		server = new TheServer();
		portNum = port;
		reuseNumbers.add(1);
		reuseNumbers.add(2);
		gameInfo = new GameInfo();
        presentClients = 0;
        count = 1;
        client1sus = false;
        anotherGame = false;
        server.start();
	}

	public class TheServer extends Thread{
		ServerSocket mysocket;

		public void run() {
			try{
    			mysocket = new ServerSocket(portNum);
    		    System.out.println("Server is waiting for a clients!");

    		    while(true) {
    		    	//Sanity check
    		    	if (reuseNumbers.size() > 2)
                        System.out.println("Error: Something is wrong with reuse numbers.");

    		    	if(reuseNumbers.size() > 0){
    			    	threadCheck();
    			    	ClientThread c = new ClientThread(mysocket.accept(), reuseNumbers.get(0));
    			    	reuseNumbers.remove(0);
                        clients.add(c);
                        presentClients++;
    			    	System.out.println("Numbers of clients in clientThread arraylist: " + clients.size());
    			    	System.out.println("Number of present Clients: " + presentClients);
                        callback.accept("Clients on server: " + clients.size());
    			    	c.start();
    		    	}//endof if reuseNumbers
    		    	else {/*dont accept any more clients*/}
    			} //end while
			}//end of try
			catch(Exception e) {
				callback.accept("Server socket did not launch");
			}//end catch
		}//end of run


		//every time a new thread is added this
	    //updates the array list checking for threads that are dead
		// removing them from the arrayList and adding them to the reuse Numbers list
		public void threadCheck() {
			for (int i =0; i < clients.size(); i++) {
				ClientThread t = clients.get(i);
				System.out.println("at threadCheck");
				if (!t.isAlive() ) {
					gameInfo.has2Players = false;
					reuseNumbers.add(t.clientNumber);
					System.out.println("adding " + t.clientNumber + " to reuse Numbers");
					clients.remove(t);
				} //end if t is alive
				//this hana: should we also check getState ? if they are runnable/terminated
                //this alex: I dont think so because wont the catch{}'s let us know if those things happen?'
			}//end for int i loop
		}//end threadCheck
	}//end TheServer Class



	class ClientThread extends Thread{
		Socket connection;
		ObjectInputStream in;
		ObjectOutputStream out;
        boolean informedWait;
        int clientNumber;


		ClientThread(Socket s, int clientNum){
			this.connection = s;
			this.clientNumber = clientNum;
            this.informedWait = false;
            callback.accept("Client " + clientNumber + " has joined the server");

		}

		//when each client joins the server this tells
		//all other clients there are new clients on the server
		public void updateClients(String message) {
			for(int i = 0; i < clients.size(); i++) {
				ClientThread t = clients.get(i);
				try {
                //*** HANA from alex: Can we do this? since the client programs are expecting GameInfo objects. ***//
				    t.out.writeObject(message);
				}
				catch(Exception e) {}
			}
		}

        // First thing both clients should read is a game object containing their IDs.
        // once this is set both clients and server knows whos who. #WORKS
        public void setIDs(){
            for (ClientThread client : clients){
                gameInfo.initialID = client.clientNumber;
                try{
                    client.out.writeObject(gameInfo); //FIRST WRITE TO CLIENTS
                }
                catch(Exception e){
                    System.out.println("Could not write to client "+ client.clientNumber + ", ID not initialized.");
                }
            }
        }

		public void run(){
			try {
				in = new ObjectInputStream(connection.getInputStream());
				out = new ObjectOutputStream(connection.getOutputStream());
				connection.setTcpNoDelay(true);
			}
			catch(Exception e) {
				System.out.println("ERROR: Client thread "+ clientNumber + ", Streams did not open.");
			}
	    	System.out.println("INFO: Client " + clientNumber + " opened streams successfully");

            // Only second process hits this, calling setIDs() once.
            if (clients.size() == 2){
                gameInfo.publicMessage = "Opponent has join";
                gameInfo.has2Players = true;
                setIDs(); //contains FIRST WRITE TO CLIENTS *****
            }

            //FOR HANNA: java is weird, cannot have empty while loop or else thread gets stuck.
            while(gameInfo.has2Players == false){
                //blocking process till two players are present, provides order
                //System.out.print(".");
            	try {
            	Thread.sleep(100);
            	}
            	catch (Exception e) {
            		System.out.println("oops");
            	}
            }

            //Both client threads make it this far
            System.out.println("Client " + clientNumber + " has2Players is : " + gameInfo.has2Players);
            System.out.println("Client " + clientNumber + " is out of while(gameInfo.has2Players == false) loop");



		    while(true) {
                //TODO: *** Reset everything here ****

    		    while (gameInfo.winner == -1) {
                    readClientsMove(); //reading move from client
                    waitForBothMoves(); //waiting till both fields are filled

                    if (clientNumber == 1){
                        suspendClientOne();
                    }
                    else if (clientNumber == 2){
                        waitTillClientOneSuspends();
                        evaluateMoves();
                        evaluateWinner();
                        unsuspendClientOne();
                    }

                    //Checking/Printing GameInfo, has appropiate data in both threads
                    gameInfo.printGameInfo();

                    //This write send gameInfo with both players moves
                    writeObject_GameInfo();
                    tempObjectRead(); //this read is to know if players click "Next Round" button.
                    confirmBothClickedNextRound();

                    if(this.clientNumber == 1){
                        suspendClientOne();
                    }
                    else if (this.clientNumber == 2){
                        waitTillClientOneSuspends();
                        resetPlayerMoves();
                        unsuspendClientOne();
                    }
                    System.out.println("client " + clientNumber + ": ready for next round");
    		    }//while gameInfo.winner

                tempObjectRead(); //this read is to check if players are playing again "Play Again" | "Quit"

                //both are not reading parallel must wait till both decide
                confirmBothClicked_PlayAgainOrQuit();

                if(this.clientNumber == 1){
                    suspendClientOne();
                    System.out.println("client " + clientNumber + " unsuspended");
                }
                else if (this.clientNumber == 2){
                    waitTillClientOneSuspends();
                    checkForAnotherGame();
                    if(anotherGame == true){
                        //reset values
                        gameInfo = new GameInfo();
                        gameInfo.has2Players = true;
                        callback.accept("Clients are going to play another game");
                    }
                    else{ //else kill one that quit and notify other player and catch it in clients TODO *****
                        gameInfo.has2Players = false;
                    }
                    unsuspendClientOne();
                }
                writeObject_GameInfo(); //infoming players if there is another game.

		    }//end while
	    }//end run


        public void confirmBothClicked_PlayAgainOrQuit(){
            while (!gameInfo.messageFromPlayerOne.equals("Play Again") && !gameInfo.messageFromPlayerOne.equals("Quit")){
                try{ sleep(100); }
                catch(Exception e){ System.out.println("Unable to sleep thread. sleep id = 1b");}
            }
            while (!gameInfo.messageFromPlayerTwo.equals("Play Again") && !gameInfo.messageFromPlayerTwo.equals("Quit")){
                try{ sleep(100); }
                catch(Exception e){ System.out.println("Unable to sleep thread. sleep id = 2b");}
            }
        }


        public void confirmBothClickedNextRound(){
            while (!gameInfo.messageFromPlayerOne.equals("reset picks") || !gameInfo.messageFromPlayerTwo.equals("reset picks")){
                try{
                    sleep(100);
                }
                catch(Exception e){
                    System.out.println("Error in reset picks loop");
                }
            }
        }


        public void writeObject_GameInfo(){
            try{
                out.reset();
                out.writeObject(gameInfo);
                out.flush();
            }
            catch(Exception e){
                System.out.println("ERROR: could not write to clients");
            }
        }


        public void tempObjectRead(){
            try{
                GameInfo temp = new GameInfo(); // temp to prevent overriding info
                temp = (GameInfo)in.readObject();
                if (this.clientNumber == 1)
                    gameInfo.messageFromPlayerOne =  temp.messageFromPlayerOne;
                else if (this.clientNumber == 2)
                    gameInfo.messageFromPlayerTwo =  temp.messageFromPlayerTwo;
            }
            catch(Exception e){}
        }


        public void readClientsMove(){
            try{
                GameInfo temp = new GameInfo();   // temp to prevent overriding info
                temp = (GameInfo)in.readObject(); // send() function from client FIRST READ *****

                if(this.clientNumber == 1){
                    gameInfo.playerOneMove = temp.playerOneMove;
                    callback.accept("Client 1 picked " + gameInfo.playerOneMove);
                }
                else if(this.clientNumber == 2){
                    gameInfo.playerTwoMove = temp.playerTwoMove;
                    callback.accept("Client 2 picked " + gameInfo.playerTwoMove);
                }
                else{
                    System.out.println("ERROR: invalid client number, client number = " + this.clientNumber);
                }
            }//end try
            catch(Exception e){
                System.out.println("ERROR: could not read moves from clients");
            }
        }


        public void waitForBothMoves(){
             while(gameInfo.playerOneMove.equals("") || gameInfo.playerTwoMove.equals("")){
                try { sleep(10); }
                catch (Exception e) { System.out.println("*** Error: sleep() failed while waiting for both moves"); }
            }
        }


        public void waitTillClientOneSuspends(){
            while (client1sus == false){
                try{sleep(300);}
                catch(Exception e){ System.out.println("*** Error: sleep() failed while waiting for suspend"); }
            }
        }


        public void suspendClientOne(){
            client1sus = true;
            suspend();
        }


        public void unsuspendClientOne(){
            for(ClientThread c : clients){
                if (c.clientNumber == 1){
                    try{
                        client1sus = false;
                        c.resume();
                    }
                    catch(Exception e){
                        System.out.println("*** Error: unable to resume client thread with ID = 1");
                    }
                }
            }
        }


    	void resetPlayerMoves() {
    		gameInfo.playerOneMove = "";
    		gameInfo.playerTwoMove = "";
    	}


        void evaluateMoves() {
        	if (gameInfo.playerOneMove.equals(gameInfo.playerTwoMove)) {
        		callback.accept("Both players have tied");
        		return;
        	}
        	else if ((gameInfo.playerOneMove.equals("scissors"))&& ( gameInfo.playerTwoMove.equals("paper") || gameInfo.playerTwoMove.equals("lizard"))){
        		gameInfo.playerOnePoints++;
        		return;
        	}
        	else if ( (gameInfo.playerOneMove.equals("paper") ) && (gameInfo.playerTwoMove.equals("rock") || gameInfo.playerTwoMove.equals("spock") )) {
        		gameInfo.playerOnePoints++;
        		return;
        	}
        	else if ((gameInfo.playerOneMove.equals("rock")) && (gameInfo.playerTwoMove.equals("lizard")|| gameInfo.playerTwoMove.equals("scissors") )) {
        		gameInfo.playerOnePoints++;
        		return;
        	}
        	else if ((gameInfo.playerOneMove.equals("lizard")) && (gameInfo.playerTwoMove.equals("spock") || gameInfo.playerTwoMove.equals("paper")  )) {
        		gameInfo.playerOnePoints++;
        		return;
        	}
        	else if ((gameInfo.playerOneMove.equals("spock")) && (gameInfo.playerTwoMove.equals("rock")|| gameInfo.playerTwoMove.equals("scissors") )) {
        		gameInfo.playerOnePoints++;
        		return;
        	}
        	else
                gameInfo.playerTwoPoints++; //error if empty strings???
        }//evaluateMoves


        void evaluateWinner(){
        	if (gameInfo.playerOnePoints == 3)
        		gameInfo.winner = 1;
        	else if (gameInfo.playerTwoPoints == 3)
        		gameInfo.winner = 2;

            if (gameInfo.winner != -1)
                callbackWinner();
        }


        public void callbackWinner(){
            if(gameInfo.winner == 1){
                callback.accept("Client 1 Won");
            }
            else if(gameInfo.winner == 2){
                callback.accept("Client 2 Won");
            }
        }//end of callbackWinner


        public void checkForAnotherGame(){
            if (gameInfo.messageFromPlayerOne.equals("Play Again") && gameInfo.messageFromPlayerTwo.equals("Play Again"))
                anotherGame = true;
        }


	}//end of ClientThread
}//end server









