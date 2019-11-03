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
			ObjectInputStream in;
			ObjectOutputStream out;

			ClientThread(Socket s, int clientNum){
				callback.accept("client "+ clientNum + " created!");
				this.connection = s;
				this.clientNumber = clientNum;
				this.opponentIndex = -1;
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

				updateClients("new client on server: \nclient #"+clientNumber);

				 while(true) {
					    if(presentClients % 2 == 1 && this.opponentIndex == -1){//only one client on server
					    	if(!informedWait){//sent to client only once, Then client waits for "Opponent has join" to be sent
					    		try{
						    		callback.accept("Client "+ clientNumber + " is waiting for opponent...");
						    		out.writeObject("waiting for opponent...");
						    		informedWait = true;
					    		}
					    		catch(Exception e){
					    			callback.accept("Something wrong with the socket from \nclient: " + clientNumber + "....closing down!");
					    			updateClients("Client #"+clientNumber+" has left the server!");
    					    		clients.set(clientNumber, null);
    					    		//make and save reusable client numbers
    					    		reuseNumbers.add(clientNumber);
    					    		presentClients--;
    					    		break;
					    		}
					    	}
					    }
					    else{ //client has a live opponent
    					    try {
    					    	//letting client know to change scene (with NEW consumer on client program);

    					    	if (this.opponentIndex == -1){
    					    		out.writeObject("Opponent has join");
    					    		for(ClientThread x : clients) {
    					    			if (x != null){
    					    				if (x.opponentIndex == -1 && x.clientNumber != this.clientNumber){
    					    					this.opponentIndex = x.clientNumber;
    					    					x.opponentIndex = this.clientNumber;
    					    					break;
    					    				}
    					    			}
    					    	 	}
    					    	 	clients.get(this.opponentIndex).out.writeObject("Opponent has join");
    					    	}


    					    	//check for cycle of three? error check

    					    	callback.accept("client "+ clientNumber + " waiting for read from connection");
    					    	String data = in.readObject().toString();
    					    	//game = in.readObject();

    					    	//I think.. the client will just send a string of what choice
    					    	//they are making and the server will send the whole gameInfo object w everything else???

    					    	callback.accept("client: " + clientNumber + " sent: " + data);
    					    	updateClients("client #"+clientNumber+" said: "+data);

    					    	}
    					    catch(Exception e) {
    					    	callback.accept("OOOOPPs...Something wrong with the socket from client: " + clientNumber + "....closing down!");
    					    	updateClients("Client #"+clientNumber+" has left the server!");
    					    	//clients.remove(this);
    					    	clients.set(clientNumber, null);
    					    	//make and save reusable client numbers
    					    	reuseNumbers.add(clientNumber);
    					    	break;
    					    }
					    }
					}
				}//end of run


		}//end of client thread
}






