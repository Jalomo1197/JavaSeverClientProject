import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class RPLS extends Application {
	Client clientConnection;
	ListView information_listView;
	//this hashmap will have all of the scenes 
	//denoted by a string
	
	//intro
	//waiting
	//choose
	//show
	//end	
	HashMap<String, Scene> sceneMap;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		primaryStage.setTitle("RPLS!!!");
		
		 sceneMap = new HashMap<String, Scene>();
		 sceneMap.put("intro",  createIntro());
		 sceneMap.put("waiting", createWaitingScene());
		//sceneMap.put("client",  createChooseScene()); 
		//sceneMap.put("server",  createShowScene());
	   //sceneMap.put("client",  createEndScene()); 
		
		Scene scene = new Scene(new HBox(),600,600);
		primaryStage.setScene(scene);
		//primaryStage.setScene(sceneMap.get("intro");
		
		primaryStage.show();
		
		//for intro scene
		//if the user hasnt entered anything in the port or ip fields the 
		//connect button is disabled
		if (port.getText() == null || ipAddress.getText() == null){
			connectToServer.setDisable(true);
		}
		//else enable the connectToServer Button
		else connectToServer.setDisable(false);
		
		//creating a new client connection when user clicks on the connectToServer Button
		//the client constructor takes a serializable consumer and the ip/port address entered by user
		
		this.connectToServer.setOnAction(e->{clientConnection = new Client(data -> {
			Platform.runLater(()->{
				//listItems.getItems().add(data.toString()); what should we do here????
			}); }, ipAddress.getText(), port.getText());
		
			//now start the clientConnection client object, this calls the run method 
		    clientConnection.start();
		    //should we have an event here if the clientConnection got caught in the catch
		    //then we have to go back to the intro scene 
		    //otherwise switch to next Scene primaryStage.setScene(sceneMap.get(waiting)); or .get(choose)
		});
		
		
		
	
	}//end start
	
	TextField port;
	TextField ipAddress;
	Button connectToServer;
	VBox introInputs;
	Text portText;
	Text ipAddressText;
	HBox waitingBox;
	Text waiting;
	
	
	
	
	//Initializing all the scenes for the To be put into the scene hashmap
	public Scene createIntro() {

		port = new TextField("Please Enter a Port #: ");
		portText = new Text("Please Enter a Port #: ");
		ipAddress = new TextField("Please Enter an IP Address: ");
		ipAddressText = new Text("Please Enter an IP Address: ");
		
		connectToServer = new Button("Connect To Server");
		introInputs = new VBox(port,portText, ipAddress, ipAddressText, connectToServer);
		//this is to set padding between the items in the vbox
		introInputs.setSpacing(5);
		return new Scene(introInputs, 500, 400);
		
	}
	
	public Scene createWaitingScene() {
		
		waiting = new Text("Waiting for Another Player...");
		waitingBox = new HBox(waiting);
		return new Scene(waitingBox, 300, 200);		
	}
	
	/*
	public Scene createChooseScene() {
		
		return new Scene(pane, 500, 400);
	}
	public Scene createShowScene() {
		
		return new Scene(pane, 500, 400);
	}
	public Scene createEndScene() {
		
		return new Scene(pane, 500, 400);
	}

	*/
	
		
}
