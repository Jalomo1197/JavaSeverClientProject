import java.beans.EventHandler;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.util.function.Consumer;

public class RPLS extends Application {
	Client clientConnection;
	ListView<String> information_listView;
	//this hashmap will have all of the scenes
	//denoted by a string
	//intro//waiting//choose//show//end
	HashMap<String, Scene> sceneMap;
	Consumer<String> sceneRequest;
	//this hashmap will have all the images for the moves denoted by a string
	//rock//paper//scissors//lizzard//spock
	HashMap<String, ImageView> imageMap;
	String move;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		primaryStage.setTitle("RPLS!!!");
		createTextObjects();
		createButtonObjects();
		createSceneMap();

		sceneRequest = message -> {
			Platform.runLater(()->{
				if (message.equals("Opponent has join")){
					primaryStage.setScene(sceneMap.get("choose"));
					primaryStage.show();
				}

				//
			});
		};


		primaryStage.setScene(sceneMap.get("intro"));
		moveButtons = new HBox(rock, paper, scissors, lizard, spock, sendMove);

		//*************//

		//not sure about the below code..how to handle closing out of the box
		//from ALEX: we need to make sure we kill the client thread connection
		//then close the applications

		/*
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
		*/
		primaryStage.show();

		//for intro scene
		//if the user hasnt entered anything in the port or ip fields the
		//connect button is disabled
		//if (port.getText() == null || ipAddress.getText() == null){
		//	connectToServer.setDisable(true);
		//}
		//else enable the connectToServer Button
		//else connectToServer.setDisable(false);

		//creating a new client connection when user clicks on the connectToServer Button
		//the client constructor takes a serializable consumer and the ip/port address entered by user

		connectToServer.setOnAction(e->
		{
			if(port.getText().equals("") || ipAddress.getText().equals(""))
			{
				errorMessage.setText("All fields need entries: Port Number and IP Address");
				return;
			}
			else
			{
				clientConnection = new Client(data -> {
				Platform.runLater(()->{information_listView.getItems().add(data.toString());});}, ipAddress.getText(), port.getText(), sceneRequest);

				//now start the clientConnection client object, this calls the run method
			    clientConnection.start();

			    if (clientConnection.isAlive() == false)
			    {}//do something go back to intro scene..send an error message
			    else
			    {
			    	//while (clientConnection.game.has2Players == false)
			    	//{
			    		//if there arent two players set the scene to the waiting stage
			    		primaryStage.setScene(sceneMap.get("waiting"));
			    	//}
			    	//if there are two players, then go to the choose scene
			    	//primaryStage.setScene(sceneMap.get("choose"));
			    	//set the move string to "null" every time you enter the choice scene
			    	//move = "null";

			    }//end inner else
			}//end else
		});

		//setting up the Move Buttons//
		rock.setOnAction(e->{
			move= "rock";
			//set the clientMove imageview
			//clientMove= imageMap.get("rock");
		});
		paper.setOnAction(e->{
			move= "paper";
			//set the clientMove imageview
			//clientMove= imageMap.get("rock");
		});
		scissors.setOnAction(e->{
			move= "scissors";
			//set the clientMove imageview
			//clientMove= imageMap.get("rock");
		});
		lizard.setOnAction(e->{
			move= "lizard";
			//set the clientMove imageview
			//clientMove= imageMap.get("rock");
		});
		spock.setOnAction(e->{
			move= "spock";
			//set the clientMove imageview
			//clientMove= imageMap.get("rock");
		});

		//make sure the player doesnt send an empty move by disabling the send move button if nothing is chosen
		if (move == "null") sendMove.setDisable(true);
		else sendMove.setDisable(false);

		sendMove.setOnAction(e->{
				clientConnection.send(move);
				//move on to the next scene
				primaryStage.setScene(sceneMap.get("show"));

				opponentMove= imageMap.get(clientConnection.game.p1Play); //how does this work// should to gameInfo sent from the server just send the opponents move?
				clientMove = imageMap.get("move");
		});





	}//end start

	TextField port, ipAddress;
	Button connectToServer, rock, paper, scissors, lizard, spock, playAgain, quit, sendMove;
	VBox introInputs;
	Text portText, ipAddressText, winner, errorMessage;
	HBox waitingBox, moveButtons, moveImageBox;
	Text waiting; //for the waiting scene
	ListView<String> scores; //this will go in the choose and show scenes
	ImageView clientMove; // this is for the player will = imageMap.get("clients move string")
	ImageView opponentMove = new ImageView();// this is for the opponent
	BorderPane choosePane, showPane; //this is for the choose scene and show scene
	Text moveEvaluation; //this will display during the show scene and will display why your move won/lost "Rock Beats Scissors! You lose"
	HBox endButtons;
	VBox endBox;


	public void createSceneMap(){
		sceneMap = new HashMap<String, Scene>();
		sceneMap.put("intro",  createIntro());
		sceneMap.put("waiting", createWaitingScene());
		sceneMap.put("choose",  createChooseScene());
		sceneMap.put("show",  createShowScene());
		sceneMap.put("end",  createEndScene());
	}


	//Initializing all the scenes for the To be put into the scene hashmap
	public Scene createIntro() {
		introInputs = new VBox(portText, port, ipAddressText, ipAddress, connectToServer, errorMessage);
		//this is to set padding between the items in the vbox
		introInputs.setSpacing(5);
		return new Scene(introInputs, 500, 400);
	}

	public Scene createWaitingScene() {
		waitingBox = new HBox(waiting);
		return new Scene(waitingBox, 300, 200);
	}


	public Scene createChooseScene() {
		//will have all the buttons, ListView,ImageView
		choosePane = new BorderPane();
		//the center will be an imageView
		clientMove = new ImageView();
		choosePane.setCenter(clientMove);
		//the Bottoms is an HBox with the move buttons
		moveButtons = new HBox();
		choosePane.setBottom(moveButtons);
		//the right is a ListView with the scores
		scores = new ListView<String>();
		choosePane.setRight(scores);

		return new Scene(choosePane, 500, 400);
	}

	public Scene createShowScene() {
		//this is an HBox with a view of both moves
		moveImageBox = new HBox(clientMove, opponentMove);
		showPane = new BorderPane();
		showPane.setCenter(moveImageBox);
		//the right is a ListView with the scores
		showPane.setRight(scores);
		return new Scene(showPane, 500, 400);
	}

	public Scene createEndScene() {
		//this hBox has the play again and quit buttons
		endButtons = new HBox(playAgain, quit);
		//this vBox has the winning text and the buttons
		endBox = new VBox(winner,endButtons);
		return new Scene(endBox, 500, 400);
	}

	public void createTextObjects(){
		port = new TextField("");
		portText = new Text("Please Enter a Port #: ");
		ipAddress = new TextField("");
		ipAddressText = new Text("Please Enter an IP Address: ");
		waiting = new Text("Waiting for Another Player...");
		errorMessage = new Text("");
		winner = new Text("");
		move = new String();
		information_listView = new ListView<String>();
	}

	public void createButtonObjects(){
		//for choose scene
		//buttons for the image moves.. we will but image views inside these buttons
		//using .setGraphic()
		rock = new Button("rock");
		paper = new Button("paper");
		scissors = new Button("scissors");
		lizard = new Button("lizard");
		spock = new Button("spock");
		sendMove = new Button("Send Move");
		playAgain = new Button("PLAY AGAIN");
		quit = new Button("QUIT");
		connectToServer = new Button("Connect To Server");
		//moveButtons
	}
}
