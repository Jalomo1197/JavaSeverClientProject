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

	int count = 1;
	int presentClients = 0;
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	ArrayList<Integer> reuseNumbers = new ArrayList<Integer>();
	TheServer server;
	private Consumer<Serializable> callback;
	int portNum;
	GameInfo gameInfo;
	String client1Move;
	String client2Move;

	//booleans for if client has made their moves
	boolean client1;
	boolean client2;
    int c;

	int c1Points;
	int c2Points;


	Server(Consumer<Serializable> call, int port){
		callback = call;
		server = new TheServer();
		portNum = port;
		reuseNumbers.add(1);
		reuseNumbers.add(2);
		gameInfo = new GameInfo();
		client1 = false;
		client2 = false;
		c1Points = 0;
		c2Points = 0;
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

    		    	//ALEX: one of our issues was indexing, im not sure
    		    	//if this method will work for multiple games but for one game it works
    		    	//Reuse numbers is the same as how you implemented it, if a
    		    	//client exits the game their number is put onto the list
    		    	//I decided against using indices to access clients because its error prone
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
                setIDs(); //contains FIRST WRITE TO CLIENTS
            }

            //FOR HANA: java is weird, cannot have empty while loop or else thread gets stuck.
            while(gameInfo.has2Players == false){
                //blocking process till two players are present, provides order
                System.out.print(".");
            }

            //Both client threads make it this far
            System.out.println("Client " + clientNumber + " has2Players is : " + gameInfo.has2Players);
            System.out.println("Client " + clientNumber + " is out of while(gameInfo.has2Players == false) loop");



		    while(true) {
                //sending each clientthread their moves
                try{
                     /*
                        FOR HANA:
                        Client thread sits here until client uses the send() function. It is
                        read into a temp GameInfo because we dont want it to overwrite saved data.
                        Notes:
                            Clients are sending their moves and the server is indeed saving that data
                            (check by printing game info line 224). But when we try to send them to the
                            clients, he clients get empty string in their opponent moves. Causing their
                            list view to print "Error null move from opponent"
                    */
                    GameInfo temp = new GameInfo();   // temp to prevent overriding info
                    temp = (GameInfo)in.readObject(); // send() function from client FIRST READ.
                    // adjusting fields based on client ID
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
                   // waiting till both fields are filled. Strange while-loop: look at explianation on line 165.
                   while(gameInfo.playerOneMove.equals("") || gameInfo.playerTwoMove.equals("")){
                        System.out.print(".");
                    }
                }
                catch(Exception e){
                    System.out.println("ERROR: could not read moves from clients");
                }

                //Checking/Printing GameInfo. Has appropiate data in both threads. Client however get empty opponent moves.
                System.out.println("ClientThread "+ clientNumber +": Game info has both move fields filled. preparing to send to Clients");
                gameInfo.printGameInfo();

                // if the below try and catch is commented out the clients get stuck waitng for a read for opponents move (in game info)
                /* SO this is the PROBLEM:
                    **********************************************************************************************************************
                    SERVER HAS ALL "MOVE" VALUES BEFORE SENDING TO BOTH CLIENTS BUT WHEN CLIENTS READ IN, OPPONENTS VALUE NOT PRESENT.
                            Server line: 225-230
                            Client side: 76-95
                            go crazy nerd
                    **********************************************************************************************************************
                  FIX : none yet
                */
                try{
                    out.writeObject(gameInfo); //SECOND WRITE TO CLIENTS
                }
                catch(Exception e){
                    System.out.println("ERROR: could not exchange clients' moves");
                }

                //get thread stuck until further progess
                //feel free to commit things out or uncommit things below, but we should fix the pr
                while(true){}//for teasing



			   /* try {
			    	while (true) {
    			    	if (gameInfo.has2players == false) {
                            //if (informedWait == false){
                            //
                            //   callback.accept("Client "+ clientNumber + " is waiting for opponent...");
                            //   informedWait = true;
                           // }
    			    	}//end if presentClients<2


    			    	//ALEX: this is the wierd part, the first thread has presentClients = 1 when it goes through this try
    			    	//the second thread has presentClients = 2
    			    	//even though above I set presentClients = 2 it doesnt work unless
    			    	//i do the for each loop and notify the opponent thread on line
    			    	//237. I have to use this same method when sending game objects at the bottom of the try
    			    	//ME THINKS: instead of making one huge try catch we should be breaking them up into multiple distinct try catches but IDK
				    	if(presentClients == 2) {
				    		//presentClients = 2;
				    		System.out.println("client " + clientNumber + " is at line 207");

				    		//callback.accept("Two players are in the game");
				    		//once we send this message that opponent has joined to both clients
				    		//then we need to let them know with clientNumber they are
				    		//might need to change this to serverGame sending this info not game
				    		//Me thinks: game is just accepted from the clients

				    		gameInfo.message = "Opponent has join";
				    		gameInfo.clientNumber = this.clientNumber;
				    		gameInfo.has2Players = true;
				    		out.writeObject(gameInfo);
				    		for(ClientThread c: clients) {
				    			if (c.clientNumber == this.opponentIndex) {
                                    c.set_GI_message("Opponent has join");
                                    c.set_opponentIndex(this.clientNumber);
                                    c.set_GI_has2Players(true);
				    			}
				    		}//end for each
				    		break;
				    	}//end if presentClients ==2
				    }//end second while
    			}//end try
			    catch(Exception e) {
			    	callback.accept("OPPs...Something wrong with the socket from client: " + clientNumber + "....closing down!");
			    	updateClients("Client #"+clientNumber+" has left the server!");
			    	presentClients--;
			    	break;
			    }

    			try {
                    //the only thing important in this game object that we
                    //read is the clientNumber and the move they make

                    //this while should loop through the three games
                    //we need to send a message to the clients once we start here that
                    //if they arent in the choose scene they have to go there, can do it at the bottom of the while
    				while(c1Points != 3 && c2Points!= 3) {
						//Servergame.isMessage = true;
						//Servergame.message = "choose";
						//out.writeObject(Servergame);
				    	gameInfo = (GameInfo)in.readObject();
				    	System.out.println(this.clientNumber + " gets here");

				    	if (gameInfo.isMessage == true) {
				    		callback.accept("Game message for" +clientNumber+ " :"+ gameInfo.message);
				    		continue;
				    	}
				    	else
				    		callback.accept("Client " + gameInfo.clientNumber + "made the move: " + gameInfo.clientMove);

    				    /////HERE YOU ARE IN THE CLIENT #1/////
				    	if (this.clientNumber == 1) {
				    		client1Move = gameInfo.clientMove;
				    		client1 = true;
				    		if (!client2) {
				    			gameInfo.isMessage = true;
				    			gameInfo.message = "Waiting for other player to make move.";
				    			callback.accept("Client 1 is waiting for client 2 to make their move");
				    			//out.writeObject(game);
				    			//break;
				    		}
				    		//else both have made their moves
				    	}

    					////////HERE YOU ARE IN THE CLIENT #2/////
				    	else if (this.clientNumber == 2) {
				    		client2Move = gameInfo.clientMove;
				    		client2 = true;
				    		//if client 2 has chosen but client 1 hasnt send a message saying
				    		//waiting
				    		if (!client1) {
				    			gameInfo.isMessage = true;
				    			gameInfo.message = "Waiting for other player to make move.";
				    			callback.accept("Client 2 is waiting for client 1 to make their move");
				    			//out.writeObject(game);
				    		}
				    	}

                        //ALEX: that same wierd ass issue here: client1 is true but client2 isnt in the first thread
                        //whoever makes the choice last has client1 and client2 as true, so i just right their game object to them
                        //and create the opponent game object and send it too all within the same thread.. this is wierd shit
                        //again maybe breaking up the try catches might work but im tired and dont want to break it
    					if (client1 && client2) {
					    	callback.accept("Both clients have made their moves!");
    				        if (evaluateMoves(client1Move, client2Move) == "client1") {
    				    		c1Points++;
    				    	}
    				        else if (evaluateMoves(client1Move, client2Move) == "client2") {
    				           c2Points++;
    				        }
    				        else if (evaluateMoves(client1Move, client2Move) == "tie") {
    				           callback.accept("client 1 and client 2 tied");
                            }

					        callback.accept("Client 1 Points: "+ c1Points);
   					    	callback.accept("Client 2 Points: "+ c2Points);

					    	if (this.clientNumber == 1) {
					    		Servergame.clientNumber = 1;
					    		Servergame.clientMove = client1Move;
					    		Servergame.opponentMove = client2Move;
					    		Servergame.yourPoints = c1Points;
					    		Servergame.opponentsPoints = c2Points;
					    		Servergame.isMessage = false;
					    		//Servergame.message = moveMessage(client1Move, client2Move);
					    		out.writeObject(Servergame);

					    		for(ClientThread c: clients) {
					    			if (c.clientNumber == this.opponentIndex) {
					    				Servergame.clientNumber = 2;
	    					    		Servergame.clientMove = client2Move;
	    					    		Servergame.opponentMove = client1Move;
	    					    		Servergame.yourPoints = c2Points;
	    					    		Servergame.opponentsPoints = c1Points;

	    					    		Servergame.isMessage = false;
	    					    		//Servergame.message = moveMessage(client1Move, client2Move);
	    					    		//out.writeObject(Servergame);
					    				c.out.writeObject(Servergame);
					    			}//end of if
					    		}//end for each
					    	}//end of if
					    	else if (this.clientNumber == 2) {
					    		Servergame.clientNumber = 2;
					    		Servergame.clientMove = client2Move;
					    		Servergame.opponentMove = client1Move;
					    		Servergame.yourPoints = c2Points;
					    		Servergame.opponentsPoints = c1Points;

					    		Servergame.isMessage = false;
					    		//Servergame.message = moveMessage(client1Move, client2Move);
					    		out.writeObject(Servergame);

					    		for(ClientThread c: clients) {
					    			if (c.clientNumber == this.opponentIndex) {
					    				Servergame.clientNumber = 1;
	    					    		Servergame.clientMove = client1Move;
	    					    		Servergame.opponentMove = client2Move;
	    					    		Servergame.yourPoints = c1Points;
	    					    		Servergame.opponentsPoints = c2Points;
	    					    		Servergame.isMessage = false;
	    					    		//Servergame.message = moveMessage(client1Move, client2Move);
					    				c.out.writeObject(Servergame);
					    			}
					    		}//end for each
					    	}
    				    }//endif
    			    	//at this point both players have all the info on their moves
    			    	//we should tell them if niether of them have 3 points go back to their choose scenes
    			    }//end while
    			}//end try
			    catch(Exception e) {
			    	callback.accept("OPPs...Something wrong with the socket from client: " + clientNumber + "....closing down!");
			    	updateClients("Client #"+clientNumber+" has left the server!");
			    	//will have to reset
			    	//client variables in socket
			    	presentClients--;
			    	break;
			    } //end catch

				//at this point we got here because either client has three points and we broke out of the try loop
				try {
					if (c1Points == 3) {
						if (this.clientNumber == 1) {
							 game.isMessage = true;
							 game.message = "YOU WON.";
							 game.winner = 1;
							 out.writeObject(game);
							 //send a message to the client that says they won
							 //and to go to the end scene
						}
						else{
							//send a message to the client that says they lost and go to the end scene
							game.isMessage = true;
							game.message = "YOU LOST.";
							game.winner = 1;
							out.writeObject(game);
						}
					}//end if client 1 won

					else if (c2Points ==3) {
						if (this.clientNumber ==2) {
							//send a message to client that says they won and go to the end scene
							game.isMessage = true;
							game.message = "YOU WON.";
							game.winner = 2;
							out.writeObject(game);
						}
					    else {
							game.isMessage = true;
							game.message = "YOU LOST.";
							game.winner = 2;
							out.writeObject(game);
							//send a message to the client that says they lost and go to the end scene
						}
					}//end if client 2 won
				}//end try
				catch (Exception e) {
					callback.accept("OPPs...Something wrong with the socket from client: " + clientNumber + "....closing down!");
				    updateClients("Client #"+clientNumber+" has left the server!");
				    //will have to reset
				    //client variables in socket
				    presentClients--;
				    break;
				 }//end catch

				 //at this point we have one more game object to read in
				 //if the player chooses to play again or quit
				 //TODO
				 try {}
				 catch(Exception e){}
                 */
		    }//end while

    		//this big while is the whole ass game and will loop until there is something wrong in the socket
    		//if a client breaks out of their loop this big while loop will send them back to the
    		//"waiting for opponent" try in theory
	    }//end run

        //returns the winner client1 or client2
        /*String evaluateMoves(String c1, String c2) {
        	String winner;
        	//scissors cuts paper and kills lizard
        	if (c1 == c2) {
        		winner = "tie";
        	}
        	else if (c1 == "scissors" && ( c2 == "paper" || c2 == "lizard")) {
        		winner = "client1";
        	}
        	else if (c1 == "paper" && (c2 == "rock" || c2 == "spock"  )) {
        		winner = "client1";
        	}
        	else if (c1 == "rock" && (c2 == "lizard" || c2 == "scissors"  )) {
        		winner = "client1";
        	}
        	else if (c1 == "lizard" && (c2 == "spock" || c2 == "paper"  )) {
        		winner = "client1";
        	}
        	else if (c1 == "spock"&& (c2 == "rock" || c2 == "scissors" )) {
        		winner = "client1";
        	}
        	else winner = "client2";

        	return winner;
        }
*/
	//TODO this will return a message like scissors beats paper, scissors beats lizard etc
	//kinda tedious im lazy rn
	//the format is c1 is your move the clients move, and c2 is the opponent move
    /*
    	String moveMessage (String c1, String c2) {
    		String message;
    		//scissors cuts paper and kills lizard
    		if (c1 == "scissors" && ( c2 == "paper" || c2 == "lizard")) {
    			message = "client1";
    		}
    		else if (c1 == "paper" && (c2 == "rock" || c2 == "spock"  )) {
    			message = "client1";
    		}
    		else if (c1 == "rock" && (c2 == "lizard" || c2 == "scissors"  )) {
    			message = "client1";
    		}
    		else if (c1 == "lizard" && (c2 == "spock" || c2 == "paper"  )) {
    			message = "client1";
    		}
    		else if (c1 == "spock"&& (c2 == "rock" || c2 == "scissors" )) {
    			message = "client1";
    		}
    		else message = "client2";

    		return message;
    	}
    */
	}//end of ClientThread
}//end server









