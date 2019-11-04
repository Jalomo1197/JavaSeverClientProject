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
	boolean opponentPresent;
	private Consumer<Serializable> callback;
	private Consumer<String> sceneRequest;

	Client(Consumer<Serializable> call, String ip, String port, Consumer<String> sceneRequest){
		ipAddress = ip;
		portNum = Integer.parseInt(port);
		callback = call;
		this.sceneRequest = sceneRequest;
		game = new GameInfo();
		opponentPresent  = false;
	}



	public void run() {
		Socket socketClient;
		String message = " ";
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
		    callback.accept("Client socket did not launch");
		}

		while(true) {
			if (opponentPresent == false) {
				try {
					game = (GameInfo)in.readObject();
					if(game == null)
						System.out.println("fuck");
				}
				catch(Exception e) {
					System.out.println("Cannot read Info from server.");
				}

				message = game.message;
				callback.accept(message);
				if (message.equals("Opponent has join")){
					opponentPresent = true;
					sceneRequest.accept(message);
				}
			}//end if opponentPresent = false

			else{
				//stuff
				//while( both are alive and no winner)

					try {
						//accept game object
						game = (GameInfo)in.readObject();
						sceneRequest.accept(game.message);
						//message = in.readObject().toString();


					}//end try
					catch(Exception e){
						System.out.println("Cannot read Info from opponent.");
					} //end catch



				}//end while

				//start reading game objects
				//sever should probably send a message befor send game object to show that the other player is still connect
				//if message ==> true
				//then read game object?
			}
		}//end run



	//Will our data will be a String for the move?
	//or will it be a whole game info class object
	public void send(String data) {
		game.message = data;
		try {
			out.writeObject(game);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
