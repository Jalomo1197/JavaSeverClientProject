import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;


public class Client extends Thread{
	private Consumer<Serializable> callback;
	private Consumer<String> sceneRequest;
	ObjectOutputStream out;
	ObjectInputStream in;
	GameInfo gameInfo;
	String ipAddress;
	boolean playAgain;
	int clientID;
	int portNum;



	Client(Consumer<Serializable> call, String ip, String port, Consumer<String> sceneRequest){
		ipAddress = ip;
		portNum = Integer.parseInt(port);
		callback = call;
		this.sceneRequest = sceneRequest;
		gameInfo = new GameInfo();
		playAgain = false;
	}

	public void run() {
		Socket socketClient;
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
					callback.accept("		Points");
					callback.accept("Your points:      " + gameInfo.playerOnePoints);
					callback.accept("Opponent points:  " + gameInfo.playerTwoPoints);
				}
				else if (clientID == 2){
					sceneRequest.accept(gameInfo.playerOneMove);
					callback.accept("		      Points");
					callback.accept("Your points:      " + gameInfo.playerTwoPoints);
					callback.accept("Opponent points:  " + gameInfo.playerOnePoints);
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
			while(playAgain == false ){
				try{
					sleep(1000);
				}
				catch(Exception e){}
			}//for teasing

			gameInfo = new GameInfo();
			gameInfo.has2Players = true; //probably not but just to not brake anything for now
		}//end while true
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
			out.reset();
			out.writeObject(gameInfo);
			out.flush();
		}
		catch (IOException e) {
			System.out.println("Error: Unable to send players move");
		}
	}

	public void send(){
		try{
			out.reset();
			out.writeObject(gameInfo);
			out.flush();
		}
		catch (IOException e) {
			System.out.println("Error: Unable to send option to play again");
		}
	}

	public void requestReset(){
		if (clientID == 1){
			gameInfo.messageFromPlayerOne = "reset picks";
		}
		else if (clientID == 2){
			gameInfo.messageFromPlayerTwo = "reset picks";
		}
		try{
			out.reset();
			out.writeObject(gameInfo);
			out.flush();
		}
		catch (IOException e) {
			System.out.println("Error: Unable to send \"reset picks\"");
		}
	}


	public void killThread(){
		//IDK
	}

}
