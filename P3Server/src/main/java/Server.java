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
	GameInfo game = new GameInfo();

	Server(Consumer<Serializable> call, int port){

		callback = call;
		server = new TheServer();
		server.start();
		portNum = port;
		clients.add(null);
	}


	public class TheServer extends Thread{
		ServerSocket mysocket;
		public void run() {

			try{
			mysocket = new ServerSocket(portNum);
		    System.out.println("Server is waiting for a clients!");

		    //while gameInfo.has2players == false
		    //while clients.size() <2
		    while(true) {

		    	//threadCheck();
				ClientThread c = new ClientThread(mysocket.accept(), count);
				callback.accept("client has connected to server: " + "client #" + count);

				if (reuseNumbers.size() != 0){
					Integer freeNum = reuseNumbers.remove(0);
					c.clientNumber = freeNum.intValue();
					clients.add(c.clientNumber, c);
				}
				else{
					clients.add(c);
					count++;
				}
				presentClients++;
				c.start();
			    }
			}//end of try
				catch(Exception e) {
					callback.accept("Server socket did not launch");
				}

			}//end of while
		}

		//every time a new thread is added this
	   //updates the array list checking for threads that are dead
		// removing them from the arrayList
		/*public void threadCheck() {
			for (int i =0; i < clients.size(); i++) {
				ClientThread t = clients.get(i);
				if (!t.isAlive()) clients.remove(i);
				//should we also check getState ? if they are runnable/terminated
			}

		}//end threadCheck*/

		class ClientThread extends Thread{


			Socket connection;
			int clientNumber;
			int opponentIndex;
			boolean informedWait = false;
			GameInfo game;
			ObjectInputStream in;
			ObjectOutputStream out;

			ClientThread(Socket s, int clientNum){
				this.connection = s;
				this.clientNumber = clientNum;
				if (this.clientNumber == 1)
					this.opponentIndex = 2;
				else
					this.opponentIndex = 1;
				game = new GameInfo();
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

			//public void updateClientsEnemy(Game obj)



			public void run(){

				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);
				}
				catch(Exception e) {
					System.out.println("Streams not open");
				}

				//updateClients("new client on server: \nclient #"+clientNumber);

				 while(true) {
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

				    if(presentClients < 2){	//only one client on server
				    	if(informedWait == false){
				    		informedWait = true;
				    		callback.accept("Client "+ clientNumber + " is waiting for opponent..."); //first thread does get this far
				    		game.message = "waiting for opponent...";
				    		try{
				    			out.writeObject(game);
				    		}
				    		catch(Exception e){ //first thread does not cause exception
				    			callback.accept("Could not send info to Client " + clientNumber +". Shutting connection down");
				    			clients.set(clientNumber, null);
				    			reuseNumbers.add(clientNumber);
					    		presentClients--;
					    		break;
				    		}
				    	}
				    	//old code, too scared to delete because hana yells at me.
				    	/*try{
				    		//System.out.println(clientNumber);
					    	if(!informedWait){//sent to client only once, Then client waits for "Opponent has join" to be sent
						    		callback.accept("Client "+ clientNumber + " is waiting for opponent...");
						    		game.message = "waiting for opponent...";
						    		out.writeObject(game);
						    		informedWait = true;
					    	}
                           game.message = "Opponent has join";
					    	out.writeObject(game);
				    	}
			    		catch(Exception e){
			    			callback.accept("Something wrong with the socket from \nclient: " + clientNumber + "....closing down!");
			    			updateClients("Client #"+clientNumber+" has left the server!");
				    		clients.set(clientNumber, null);
				    		//make and save reusable client numbers
				    		reuseNumbers.add(clientNumber);
				    		presentClients--;
				    		break;
			    		}*/
					    }

					    /*
					    *	CURRENTLY ONLY SECOND THREAD IS IN THE ELSE BELOW, and second client DOES get the first clients 'move' form the first client using
					    *	the send function. Need to figure out why first client thread isnt reaching here ): #sad
						*/
					    else{ //client has a live opponent
    					    try {
    					    	//letting client know to change scene (with NEW consumer on client program);
    					    	if (!informedJoined){
    					    		game.message = "Opponent has join";
    					    		out.writeObject(game);
    					    		clients.get(this.opponentIndex).out.writeObject(game);
    					    		informedJoined = true;
    					    	}



    					    	//check for cycle of three? error check

    					    	callback.accept("client "+ clientNumber + " waiting for read from connection");
    					    	game = (GameInfo)in.readObject();
    					    	clients.get(opponentIndex).out.writeObject(game);
    					    	//game = in.readObject();

    					    	//I think.. the client will just send a string of what choice
    					    	//they are making and the server will send the whole gameInfo object w everything else???

    					    	callback.accept("client: " + clientNumber + " selected: " + game.message);

    					    	game = (GameInfo)in.readObject();
    					    	//updateClients("client #"+clientNumber+" said: "+data);

    					    }
    					    catch(Exception e) {
    					    	callback.accept("OPPs...Something wrong with the socket from client: " + clientNumber + "....closing down!");
    					    	updateClients("Client #"+clientNumber+" has left the server!");
    					    	//clients.remove(this);
    					    	clients.set(clientNumber, null);
    					    	presentClients--;
    					    	//make and save reusable client numbers
    					    	reuseNumbers.add(clientNumber);
    					    	break;
    					    }
					    }
					}
				}//end of run


		}//end of client thread
}






