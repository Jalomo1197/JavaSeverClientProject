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
	GameInfo game;

	//attributes for portNum and ipAddress user enters
	//initialized in constructor for Client
	int portNum;
	String ipAddress;
	boolean opponentPresent = false;
	private Consumer<Serializable> callback;
	private Consumer<String> sceneRequest;

	Client(Consumer<Serializable> call, String ip, String port, Consumer<String> sceneRequest){
		ipAddress = ip;
		portNum = Integer.parseInt(port);
		callback = call;
		this.sceneRequest = sceneRequest;
	}



	public void run() {
		Socket socketClient;
		String message;
		//creating a new socket with the user entered ip/port address
		//inside the try black so that any exceptions are caught in the catch frame
		try{
			socketClient= new Socket(ipAddress ,portNum);
			out = new ObjectOutputStream(socketClient.getOutputStream());
			in = new ObjectInputStream(socketClient.getInputStream());
			//callback.accept("Connected to the server!"); // consumer isn't set yet
			socketClient.setTcpNoDelay(true);
		}
		catch(Exception e) {
		//callback.accept("Server socket did not launch");
		}

		while(true) {
			if (opponentPresent == false) {
				try {
					//cant read game because the other player isnt connected yet
					//cast the input stream to a GameInfo object
					message = in.readObject().toString();//blocking method
					callback.accept(message);
					if (message.equals("Opponent has join")){
						opponentPresent = true;
						//callback.accept(message);
						//sceneRequest
						//out.writeObject("Here in client");
						sceneRequest.accept(message);
					}
				}
				catch(Exception e) {
					System.out.println("Error in client");
				}
			}
			else{

				//stuff
				//while( both are alive and no winner)
				//start reading game objects
				//sever should probably send a message befor send game object to show that the other player is still connect
				//if message ==> true
				//then read game object?
			}
		}

    }//end run

	//Will our data will be a String for the move?
	//or will it be a whole game info class object
	public void send(String data) {

		try {
			out.writeObject(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
