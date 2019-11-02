import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class RPLS extends Application {
	Server serverConnection;
	Scene startUp_scene, info_scene;
	VBox LayoutOne;
	HBox subLayoutOne, clientInfo, winnerInfo;
	TextField portNumber;
	BorderPane serverInfo;


	Button enterPortNumber_btn, turnOnServer_btn;
	Text gameServer_text, portNumPrompt_text, connectedClients_text, serverInfo_text, winner_text, playing_again;
	ListView<String> client1_listView;
	ListView<String> client2_listView;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		initTextVars();
		initButtonVars();
		initOtherVars();
		
		//set the primary stage as the start up scene
		primaryStage.setScene(startUpScene());
		
		//if the user hasnt entered a port number disable the turnOnServer Button
		if (portNumber.getText() == null) turnOnServer_btn.setDisable(true);
		else turnOnServer_btn.setDisable(false);
	
		//Event Handler for Turn on Server
		turnOnServer_btn.setOnAction(e->{serverConnection = new Server(data -> {
			Platform.runLater(()->{
				//listItems.getItems().add(data.toString()); what should we do here????
			}); }, portNumber.getText());
		
			if (serverConnection.server.isAlive() == true) {
				//go to the next stage
				primaryStage.setScene(infoScene());
			}
		
		});

		
		primaryStage.setTitle("RPLS!!!");
		Scene scene = new Scene(new HBox(),600,600);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	void initTextVars(){
		gameServer_text = new Text("Game Server");
		portNumPrompt_text = new Text("Enter port number to listen to: ");
		connectedClients_text = new Text("Number of clients connected: " + 0);
		serverInfo_text = new Text("Server Information: ");
		winner_text = new Text("Winner: ");
		
		playing_again = new Text("Playing Again ?:" );
	}

	void initButtonVars(){
		enterPortNumber_btn = new Button("Enter");
		turnOnServer_btn = new Button("Turn on server");
	}

	void initOtherVars(){
		portNumber = new TextField("e.g. 5555");
		client1_listView= new ListView<String>();
		client2_listView = new ListView<String>();
	}
	
	//initializing the startUpScene
	public Scene startUpScene() {
		//this HBox has the port number prompt text and text field
		subLayoutOne = new HBox(portNumPrompt_text, portNumber);
		LayoutOne = new VBox(gameServer_text, subLayoutOne, turnOnServer_btn);
		return new Scene(LayoutOne, 500, 800);
	}
	
	public Scene infoScene() {
		clientInfo = new HBox(client1_listView, client2_listView);
		//this will have who won the previous round 
		//and if they are playing again
		winnerInfo= new HBox(winner_text,playing_again);
		clientInfo.setSpacing(20);
		serverInfo = new BorderPane();
		//the center of the border pane is the hBox clientInfo which has the two list views for both clients
		serverInfo.setCenter(clientInfo);
		//set the bottom of the border pane as the winner info hbox
		serverInfo.setBottom(winnerInfo);
		serverInfo.setTop(connectedClients_text);
		return new Scene(serverInfo, 500, 800);
	//	return new Scene();
	}

}//end start
