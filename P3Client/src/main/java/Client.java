import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;


public class Client extends Thread{

	
	//Socket socketClient;
	
	ObjectOutputStream out;
	ObjectInputStream in;
	GameInfo game;
	
	//attributes for portNum and ipAddress user enters
	//initialized in constructor for Client
	int portNum;
	String ipAddress;
	
	private Consumer<Serializable> callback;
	
	Client(Consumer<Serializable> call, String ip, String port){
		
		ipAddress = ip;
		portNum = Integer.parseInt(port);
		callback = call;
	}
	
	public void run() {
		
		//creating a new socket with the user entered ip/port address
		//inside the try black so that any exceptions are caught in the catch frame 
		try(Socket socketClient = new Socket(ipAddress ,portNum);) {

			
		   //socketClient= new Socket(ipAddress ,portNum);
	       out = new ObjectOutputStream(socketClient.getOutputStream());
	       in = new ObjectInputStream(socketClient.getInputStream());
	       callback.accept("client is connected to the server!");
	       socketClient.setTcpNoDelay(true);
		  }
		  catch(Exception e) {
			  callback.accept("Server socket did not launch");
		  }
		
		while(true) {
			 
			try {
			//cast the input stream to a GameInfo object
			game = (GameInfo)in.readObject();
			callback.accept(game);
			}
			catch(Exception e) {}
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
