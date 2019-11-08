import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;


public class Client extends Thread{
	ObjectOutputStream out;
	ObjectInputStream in;
	GameInfo gameInfo;
	String opponentMove;
	int clientID;
	int yourPoints;
	int opponentPoints;

	//attributes for portNum and ipAddress user enters
	//initialized in constructor for Client
	int portNum;
	String ipAddress;
	boolean opponentPresent;
	private Consumer<Serializable> callback;
	private Consumer<String> sceneRequest;
	int clientNumber;

	Client(Consumer<Serializable> call, String ip, String port, Consumer<String> sceneRequest){
		ipAddress = ip;
		portNum = Integer.parseInt(port);
		callback = call;
		this.sceneRequest = sceneRequest;
		gameInfo = new GameInfo();
		opponentPresent  = false;
		opponentMove = null;
	}

	public void run() {
		Socket socketClient;
		String message = " ";
		//creating a new socket with the user entered ip/port address
		//inside the try black so that any exceptions are caught in the catch frame
		//TODO: HERE WE NEED TO CHECK IF VALID SOCKET/PORT OR ELSE WE GET A NULLPOINTER EXCEPTION
		//WE can do a try with resources (in the application threa before creating the object Client)
		//		resoures would be "Socket testingSocket;"
		try{
			socketClient= new Socket(ipAddress ,portNum);
			out = new ObjectOutputStream(socketClient.getOutputStream());
			in = new ObjectInputStream(socketClient.getInputStream());
			//callback.accept("Connected to the server!"); // consumer isn't set yet
			socketClient.setTcpNoDelay(true);
		}
		catch(Exception e) {
		    callback.accept("Client socket did not launch");
		}

		while(gameInfo.has2Players == false){
			try{
				gameInfo = (GameInfo)in.readObject(); //FIRST READ, BLOCKING METHOD
			}
			catch(Exception e) {
				System.out.println("could not read from server. ID was not initialized");
				//should end process because of error?
			}
		}

		//	ID is being set and scene change to choose scene (better to have this scene change outside the while loop)
		//  FIRST READ CONTAINS ID NUMBER FOR CLIENT.
		clientID = gameInfo.initialID;
		System.out.println("Player " + clientID + " has ID set: " + clientID);
		sceneRequest.accept(gameInfo.publicMessage); //gameInfo.publicMessage contains "Opponent has joined"

		while(true) {
			callback.accept("You're client: " + clientID); //debugging purposes
			//while no winner
			while(gameInfo.winner == -1){
				try{
					gameInfo = (GameInfo)in.readObject(); //SECOND READ, BLOCKING METHOD. Server SHOULD have both moves saved.
					callback.accept("Opponent made move!");
				}
				catch(Exception e){
					System.out.println("ERROR: could not read opponent move");
				}

				if (clientID == 1){
					sceneRequest.accept(gameInfo.playerTwoMove);
					sceneRequest.accept("		Points");
					sceneRequest.accept("Your points:      " + gameInfo.playerOnePoints);
					sceneRequest.accept("Opponent points:  " + gameInfo.playerTwoPoints);
				}
				else if (clientID == 2){
					sceneRequest.accept(gameInfo.playerOneMove);
					sceneRequest.accept("		      Points");
					sceneRequest.accept("Your points:      " + gameInfo.playerTwoPoints);
					sceneRequest.accept("Opponent points:  " + gameInfo.playerOnePoints);
				}
			}

			//There's a winner. use sceneRequest to notify players
			if (this.clientID == gameInfo.winner){
				callback.accept("You Won!");
				sceneRequest.accept("You Won!");
			}
			else{
				callback.accept("You Lost!");
				sceneRequest.accept("You Lost!");
			}


			while(true){}//for teasing
			//callback.accept("Opponent made their move!");

		/*
			if (opponentPresent == false) {
				try {
					game = (GameInfo)in.readObject();
				}
				catch(Exception e) {
					System.out.println("Cannot read Info from server.");
					opponentPresent = false;
					game.has2Players = false;
					break;
				}

				message = game.message;
				callback.accept(message);

				if (message.equals("Opponent has join")){
					opponentPresent = true;
					clientNumber = game.clientNumber;
					sceneRequest.accept(message);
					game.has2Players = true;
					send(true);
				}
			}//end if opponentPresent = false

			//while(opponentPresent== true) {
			if (opponentPresent == true) {
					try {

						//accept game object with
						game = (GameInfo)in.readObject();
						if (game.isMessage) {
							//System.out.println("the game object was a message!");
							if (game.winner != -1) {
								callback.accept(game.message);
								//this will send the application thread to the ending scene
								sceneRequest.accept("end");
							}
							else {
								sceneRequest.accept(message);
								callback.accept(game.message);
								//callback.accept(game.message);
							}

						} //end if is message

						else {

							//moves made is for testing purposes but i dont think i use it in the end

							//sceneRequest.accept("Moves made");
							opponentMove = game.opponentMove;
							callback.accept("Your opponent chose " + opponentMove);
							opponentPoints = game.opponentsPoints;
							callback.accept("Opponent's Points: " +opponentPoints );
							yourPoints = game.yourPoints;
							callback.accept("Your Points: " + yourPoints);

						}

						if (game.has2Players == false) {
							//do something
						}



					}//end try
					catch(Exception e){
						System.out.println("Cannot read game Info from opponent.");
						opponentPresent = false;
						break;
					} //end catch

			  }//end while
			  */
		}//end while true

				//start reading game objects
				//sever should probably send a message befor send game object to show that the other player is still connect
				//if message ==> true
				//then read game object?
			//}
		}//end run



	//Will our data will be a String for the move?
	//or will it be a whole game info class object
	public void send(String move) {
		if (clientID == 1){
			gameInfo.playerOneMove = move;
			callback.accept("(1)You picked " + gameInfo.playerOneMove + "!");
		}
		else if (clientID == 2){
			gameInfo.playerTwoMove = move;
			callback.accept("(2)You picked " + gameInfo.playerTwoMove + "!");
		}
		else{
			System.out.println("Error: Client ID is not valid. Client ID = " + clientID);
		}

		try{
			out.writeObject(gameInfo);
		}
		catch (IOException e) {
			System.out.println("Error: Unable to send players move");
		}
		/*//game.message = data;
		gameInfo.clientNumber = this.clientNumber;
		gameInfo.clientMove = move;
		//gameInfo.isMessage = false;
		try {
			out.writeObject(game);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	public void send(Boolean p) {
		/*game.has2Players = p;
		game.isMessage = true;
		game.clientNumber = this.clientNumber;

		try {
			out.writeObject(game);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	public void send(GameInfo p) {
		/*p.isMessage = false;
		try {
			out.writeObject(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}


}
