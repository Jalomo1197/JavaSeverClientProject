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
import javafx.geometry.VPos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


public class RPLS extends Application {
	Server serverConnection;
	Scene startUp_scene, info_scene;
	VBox LayoutOne;
	HBox subLayoutOne, clientInfo, winnerInfo;
	TextField portNumber;
	BorderPane serverInfo;


	Button enterPortNumber_btn, turnOnServer_btn;
	Text gameServer_text, portNumPrompt_text, connectedClients_text, serverInfo_text, winner_text, playing_again, errorMessage;
	ListView<String> client1_listView;


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

		//Wont work because only runs once
		//if the user hasnt entered a port number disable the turnOnServer Button
		//if (portNumber.getText() == null) turnOnServer_btn.setDisable(true);
		//else turnOnServer_btn.setDisable(false);

		//Event Handler for Turn on Server
//FIX ME 	turnOnServer.setOnAction
		//have to adjust to for case: invalid or no port number entered
		//may need to add a port variable in the server class since not determined until user inputs it
		turnOnServer_btn.setOnAction(e ->
									{	if (portNumber.getText().equals("")){
											//TODO*** we need to let user know textfeild blank, Must enter port number
											errorMessage.setText("No Port Number: Plese enter port number");
										}
										else{
											int port;
											try{
												port = Integer.parseInt(portNumber.getText());
											}
											catch(NumberFormatException x){
												//TODO*** In here we need to let the user know that input was not a vaild number
												errorMessage.setText("Invalid Port Number: Plese enter valid port number");
												System.out.println("Error : "+ x.getMessage());
												//somehow stop control flow of lamda; Maybe by just adding a "return;"
												return;
											}
											//System.out.println("This is an integer " + i);

											//Creating new Server and defining parameter with lamdas
											serverConnection = new Server
											(
												//**Lamda for Consumer<Serializable> call in constructor
												data ->
												{
														Platform.runLater
														(
														//**Lamda for Platform.runLater argument
														()->{
															client1_listView.getItems().add(data.toString());
															connectedClients_text.setText("Number of clients connected: " + serverConnection.presentClients);
															//TODO
															//if (serverConnection.game.winner != -1)
															//set the winner text
														});
												}, port);
											//End of new Server creation, (second parameter is port number)
											if (serverConnection.server.isAlive() == true) {
												//go to the next stage
												primaryStage.setScene(infoScene());
											}
										}
									});


		primaryStage.setTitle("RPLS Game Server");
		primaryStage.show();
	}

	void initTextVars(){
		//In CSS, the padding parameters are entered in this order: TOP, RIGHT, BOTTOM, LEFT. Clockwise
		gameServer_text = new Text("Game Server");
		portNumPrompt_text = new Text("Enter port number to listen to: ");
		connectedClients_text = new Text("Number of clients connected: " + 0);
		connectedClients_text.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
		serverInfo_text = new Text("Server Information: ");
		winner_text = new Text("Winner: ");
		winner_text.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
		playing_again = new Text("Playing Again ?:" );
		playing_again.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
		errorMessage = new Text(" ");


		gameServer_text.setTextOrigin(VPos.CENTER);

		//gameServer_text.setLineSpacing(50.0);
		//gameServer_text.setStyle("-fx-padding: 10 50 0 0;");
	}

	void initButtonVars(){
		enterPortNumber_btn = new Button("Enter");
		turnOnServer_btn = new Button("Turn on server");
	}

	void initOtherVars(){
		portNumber = new TextField();
		client1_listView= new ListView<String>();
	}

	//initializing the startUpScene
	public Scene startUpScene() {
		//this HBox has the port number prompt text and text field
		subLayoutOne = new HBox(portNumPrompt_text, portNumber);
		LayoutOne = new VBox(gameServer_text, subLayoutOne, turnOnServer_btn, errorMessage);
		return new Scene(LayoutOne, 600, 500);
	}

	public Scene infoScene() {
		clientInfo = new HBox(client1_listView);
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
		return new Scene(serverInfo, 800, 800);
	//	return new Scene();
	}

}//end start
