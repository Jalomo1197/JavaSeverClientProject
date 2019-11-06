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
	boolean informedJoined = false;
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	ArrayList<Integer> reuseNumbers = new ArrayList<Integer>();
	TheServer server;
	private Consumer<Serializable> callback;
	int portNum;
	GameInfo Servergame;
	String client1Move;
	String client2Move;
	
	//booleans for if client has made their moves
	boolean client1;
	boolean client2;
	
	int c1Points;
	int c2Points;
	
	
	Server(Consumer<Serializable> call, int port){

		callback = call;
		server = new TheServer();
		server.start();
		portNum = port;
		//clients.add(null);
		reuseNumbers.add(1);
		reuseNumbers.add(2);
		Servergame = new GameInfo();
		client1 = false;
		client2 = false;
		c1Points = 0;
		c2Points = 0;
	}

	public class TheServer extends Thread{
		ServerSocket mysocket;
		public void run() {

			try{
			mysocket = new ServerSocket(portNum);
		    System.out.println("Server is waiting for a clients!");

		    //while gameInfo.has2players == false
		    //while clients.size() <2
		   // while(true) {
		    while(true) {
		    	
		    	//sanity check
		    	if (reuseNumbers.size()>2) {System.out.println("Something is wrong with reuse numbers");}
		    	

		    	//ALEX: one of our issues was indexing, im not sure
		    	//if this method will work for multiple games but for one game it works
		    	//Reuse numbers is the same as how you implemented it, if a 
		    	//client exits the game their number is put onto the list
		    	//I decided against using indices to access clients because its error prone
		    	if(reuseNumbers.size()>0) {

			    	System.out.println("HERE at line 57");
			    	
			    	threadCheck();
			    	ClientThread c = new ClientThread(mysocket.accept(), reuseNumbers.get(0));
			    	clients.add(c);
			    	reuseNumbers.remove(0);
		    	
	
			    	System.out.println("Numbers of clients in clientThread arraylist" + clients.size());
			    	presentClients++;
			    	System.out.println("Number of present Clients" + presentClients);
			    	c.start();
		    	}//endof if reuseNumbers
		    	
		    	else {}

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
					Servergame.has2Players = false;
					reuseNumbers.add(t.clientNumber);
					System.out.println("adding " + t.clientNumber + "to reuse Numbers");
					clients.remove(t);
					
				} //end if t is alive
	
				//should we also check getState ? if they are runnable/terminated
			}//end for int i loop

		}//end threadCheck*/
		
	}//end TheServer Class

		class ClientThread extends Thread{


			Socket connection;
			int clientNumber;
			int opponentIndex;
			boolean informedWait = false;
			GameInfo game; //this is what the client sends to the server
			ObjectInputStream in;
			ObjectOutputStream out;

			ClientThread(Socket s, int clientNum){
				this.connection = s;
				
				this.clientNumber = clientNum;
				if (this.clientNumber == 1) this.opponentIndex = 2;
				
				else this.opponentIndex = 1;
				game = new GameInfo();
				System.out.println("Adding client with " + clientNum);
				callback.accept("Client" + clientNumber + "has joined");
			}

			//when each client joins the server this tells
			//all other clients there are new clients on the server
			public void updateClients(String message) {
				for(int i = 0; i < clients.size(); i++) {
					ClientThread t = clients.get(i);
					try {
					 t.out.writeObject(message);
					}
					catch(Exception e) {}
				}
			}


			public void run(){

				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);
				}
				catch(Exception e) {
					System.out.println("Streams not open");
				}

	
				 	//NOTES FOR HANA, dont highlight and delete lol.
				 	//**********************************************************************************************
					/*	•First client thread was the one getting stuck in lines 151-166.
					*
					*	•Client (client 1) class on users side was not updating scene because we never send "Opponent has join"
					*		FIX: This sub issue fixed by making 'boolean informedJoined' a member of the sever class
					*			 instead of the clientthread class. Now either the first or second client thread can
					*			 send that message to both clients once. IN THIS CASE the second client thread is
					*			 sending it to both because first thread stuck.
					*    			Notes: so we know the client classes are working okay, strongly assuming the problem is here.
					*
					*	Ideas:
					*		• making boolean informedWait a member of the server too? (probably not it cheif)
					*		• instead of if make it a while (presentClients < 2)
					*/
					//**********************************************************************************************
				/*
				
				    	if(informedWait == false && presentClients<2){
				    		informedWait = true;
				    		callback.accept("Client "+ clientNumber + " is waiting for opponent..."); //first thread does get this far
				    		
				    	}//end if 
				    */	
				    	System.out.println("client" + clientNumber + "is at line 199");
				    	System.out.println("client" + clientNumber + "number of present Clients" + presentClients);
				    	
				
					   
		while(true) {
    					    try {	
    					    	
    					    	while (true) {
    					    	if (presentClients<2) {
    					    		callback.accept("Client "+ clientNumber + " is waiting for opponent...");
    					    		//here were waiting until the other thread sends this thread
    					    		//the game info object with has2players = true
    					    		game = (GameInfo)in.readObject();
    					    		if (game.has2Players == true) {
    					    			presentClients =2;
    					    		}//end if	
    					    		
    					    		
    					    	  }//end if presentClients<2
    					    	
    					   
    					    	//ALEX: this is the wierd part, the first thread has presentClients =1 when it goes through this try
    					    	//the second thread has presentClients =2
    					    	//even though above I set presentClients = 2 it doesnt work unless
    					    	//i do the for each loop and notify the opponent thread on line
    					    	//237. I have to use this same method when sending game objects at the bottom of the try 
    					    	//ME THINKS: instead of making one huge try catch we should be breaking them up into multiple distinct try catches but IDK
    					    	
    					    	if(presentClients==2) { 
    					    		//presentClients = 2;			    		
    					    		System.out.println("client" + clientNumber + "is at line 241");
    					    		
    					    		//callback.accept("Two players are in the game");
    					    		//once we send this message that opponent has joined to both clients
    					    		//then we need to let them know with clientNumber they are 
    					    		//might need to change this to serverGame sending this info not game
    					    		//Me thinks: game is just accepted from the clients	
    					  
    					    		game.message = "Opponent has join";
    					    		game.clientNumber = this.clientNumber;
    					    		game.has2Players = true;
    					    		out.writeObject(game);
    					    		for(ClientThread c: clients) {
    					    			if (c.clientNumber == this.opponentIndex) {
    					    				game.message = "Opponent has join";
    					    				game.clientNumber = opponentIndex;
    					    				game.has2Players = true;
    					    				c.out.writeObject(game);
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
    							
    					    	game = (GameInfo)in.readObject();
    					    	System.out.println(this.clientNumber + " gets here");
    					    	
    					    	if (game.isMessage == true) {
    					    		callback.accept("Game message for" +clientNumber+ " :"+ game.message);
    					    		continue;
    					    	}
    					    	
    					    	else 
    					    		callback.accept("Client " + game.clientNumber + "made the move: " + game.clientMove);
    					    	
    				   /////HERE YOU ARE IN THE CLIENT #1/////
    					    	if (this.clientNumber == 1) {
    					    		client1Move = game.clientMove;
    					    		client1 = true;
    					    		if (!client2) {
    					    			game.isMessage = true;
    					    			game.message = "Waiting for other player to make move.";
    					    			callback.accept("Client 1 is waiting for client 2 to make their move");
    					    			//out.writeObject(game);
    					    			//break;
    					    		}
    					    		//else both have made their moves
    					    	}
    					    	
    					 ////////HERE YOU ARE IN THE CLIENT #2/////
    					    	else if (this.clientNumber == 2) {
    					    		client2Move = game.clientMove;
    					    		client2 = true;
    					    		//if client 2 has chosen but client 1 hasnt send a message saying 
    					    		//waiting
    					    		if (!client1) {
    					    			game.isMessage = true;
    					    			game.message = "Waiting for other player to make move.";
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
    					    		else c2Points++;
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
    					    			}
    					    		}//end for each
    					    	}
    					    	
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
    						 else {
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
		
		
		
		}//end while
		//this big while is the whole ass game and will loop until there is something wrong in the socket
		//if a client breaks out of their loop this big while loop will send them back to the 
		//"waiting for opponent" try in theory
			
				    
			}//end run
			
	//returns the winner client1 or client2
	String evaluateMoves(String c1, String c2) {
		String winner;
		//scissors cuts paper and kills lizard
		if (c1 == "scissors" && ( c2 == "paper" || c2 == "lizard")) {
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









