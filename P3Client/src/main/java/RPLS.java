import java.beans.EventHandler;
import java.util.HashMap;
import java.util.ArrayList;
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
import javafx.scene.image.Image;


public class RPLS extends Application {
	Client clientConnection;
	ListView<String> information_listView;

	//this hashmap will have all of the scenes
	//denoted by a string
	//intro//waiting//choose//show//end
	HashMap<String, Scene> sceneMap;
	Consumer<String> sceneRequest;
	ArrayList<String> validMoves;
	//this hashmap will have all the images for the moves denoted by a string
	//rock//paper//scissors//lizzard//spock
	HashMap<String, Image> imageMap;
	String move;
	String oppMove;

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
		createImageMap();

		sceneRequest = message -> {
			Platform.runLater(()->{
				if (message.equals("Opponent has join")){
					primaryStage.setScene(sceneMap.get("choose"));
					primaryStage.show();
					//idk about this.
					//clientConnection.send(true);
				}
				if (message.equals("Opponent has left")) {
					primaryStage.setScene(sceneMap.get("waiting"));
					primaryStage.show();
				}
				for (String x : validMoves){
					if (message.equals(x)){
						information_listView.getItems().add("Opponent picked " + x);
						opponentMove.setImage(imageMap.get(x));
						break;
					}
				}
				if (message.equals("")){
					information_listView.getItems().add("Error null move from opponent");
				}
				if (message.equals("You Won!")){
					winner.setText("YOU WON!");
					primaryStage.setScene(sceneMap.get("end"));
					primaryStage.show();
				}
				if (message.equals("You Lost!")){
					winner.setText("YOU LOST!");
					primaryStage.setScene(sceneMap.get("end"));
					primaryStage.show();
				}
				/*
				if (message.equals("Moves made")) {
					information_listView.getItems().add("Opponent has selected!");
					opponentMove.setImage(imageMap.get(clientConnection.opponentMove));
					primaryStage.setScene(sceneMap.get("show"));
				}
				if (message.equals("end")) {
					primaryStage.setScene(sceneMap.get("end"));
					primaryStage.show();
				}

				if (message.equals("choose")) {
					primaryStage.setScene(sceneMap.get("choose"));
					primaryStage.show();

				}*/
			});
		};
		primaryStage.setScene(sceneMap.get("intro"));
		primaryStage.show();
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

			  //  if (clientConnection.isAlive() == false)
			   // {}//do something go back to intro scene..send an error message


			     //if (clientConnection.opponentPresent == false) {
			    	//{
			    		//if there arent two players set the scene to the waiting stage

			    		primaryStage.setScene(sceneMap.get("waiting"));
			     //}


			     //primaryStage.setScene(sceneMap.get("choose"));

			    	//}
			    	//if there are two players, then go to the choose scene
			    	//primaryStage.setScene(sceneMap.get("choose"));
			    	//set the move string to "null" every time you enter the choice scene
			    	//move = "null";

			    }//end inner else
			//}//end else
		});

		//setting up the Move Buttons//
		rock.setOnAction(e->{
			move= "rock";
			//set the clientMove imageview
			clientMove.setImage(imageMap.get("rock"));
		});
		paper.setOnAction(e->{
			move= "paper";
			//set the clientMove imageview
			clientMove.setImage(imageMap.get("paper"));
		});
		scissors.setOnAction(e->{
			move= "scissors";
			//set the clientMove imageview
			clientMove.setImage(imageMap.get("scissors"));
		});
		lizard.setOnAction(e->{
			move= "lizard";
			//set the clientMove imageview
			clientMove.setImage(imageMap.get("lizard"));
		});
		spock.setOnAction(e->{
			move= "spock";
			//set the clientMove imageview
			clientMove.setImage(imageMap.get("spock"));
		});

		//make sure the player doesnt send an empty move by disabling the send move button if nothing is chosen
		//if (move == "null") sendMove.setDisable(true);
		//else sendMove.setDisable(false);

		sendMove.setOnAction(e->{
			clientConnection.send(move);
			//move on to the next scene
			moveImageBox = new HBox(cMove, opponentMove);
			showPane.setCenter(moveImageBox);
			showPane.setRight(information_listView);
			cMove.setImage(imageMap.get(move));
			primaryStage.setScene(sceneMap.get("show"));
		});

		//the next botton
		nextRound.setOnAction(e->{
			//should have an if the opponent move hasnt been set you cant go to the next round
			choosePane.setRight(information_listView);
			//reset the image views
			cMove.setImage(null);
			clientMove.setImage(null);
			opponentMove.setImage(null);;
			primaryStage.setScene(sceneMap.get("choose"));
		});

		//TODO: alex assignment.
		playAgain.setOnAction(e->{
			if (clientConnection.clientID == 1)
				clientConnection.gameInfo.messageFromPlayerOne = "Play Again";
			if (clientConnection.clientID == 2)
				clientConnection.gameInfo.messageFromPlayerTwo = "Play Again";
			clientConnection.send();
		});
		quit.setOnAction(e->{
			if (clientConnection.clientID == 1)
				clientConnection.gameInfo.messageFromPlayerOne = "Quit";
			if (clientConnection.clientID == 2)
				clientConnection.gameInfo.messageFromPlayerTwo = "Quit";
			clientConnection.send();
		});
	}//end start

	TextField port, ipAddress;
	Button connectToServer, rock, paper, scissors, lizard, spock, playAgain, quit, sendMove, nextRound;
	VBox introInputs;
	Text portText, ipAddressText, winner, errorMessage, yourPick_text, oppPick_text;
	HBox waitingBox, moveButtons, moveImageBox;
	Text waiting; //for the waiting scene
	ListView<String> scores; //this will go in the choose and show scenes
	ImageView clientMove, cMove; // this is for the player will = imageMap.get("clients move string")
	ImageView opponentMove;// this is for the opponent
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

	public void createImageMap() {
		imageMap = new HashMap<String, Image>();
		Image rock = new Image("file:src/test/resources/rock.jpg", 90, 90, false, false);
		imageMap.put("rock", rock);
		Image paper = new Image("file:src/test/resources/paper.jpg", 90, 90, false, false);
		imageMap.put("paper", paper);
		Image scissors = new Image("file:src/test/resources/scissors.jpg", 90, 90, false, false);
		imageMap.put("scissors", scissors);
		Image spock = new Image("file:src/test/resources/spock.jpg", 90, 90, false, false);
		imageMap.put("spock", spock);
		Image lizard = new Image("file:src/test/resources/lizard.jpg", 90, 90, false, false);
		imageMap.put("lizard", lizard);
	}


	//Initializing all the scenes for the To be put into the scene hashmap
	public Scene createIntro() {
		introInputs = new VBox(portText, port, ipAddressText, ipAddress, connectToServer, errorMessage);
		//this is to set padding between the items in the vbox
		introInputs.setSpacing(5);
		return new Scene(introInputs, 400, 400);
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
		choosePane.setCenter(clientMove); //*************HAVE TO RESET IN EVENT HANDLER
		//the Bottoms is an HBox with the move buttons
		moveButtons = new HBox(rock, paper, scissors, lizard, spock, sendMove);
		choosePane.setBottom(moveButtons);
		//the right is a ListView with the scores
		choosePane.setRight(information_listView);

		return new Scene(choosePane, 600, 600);
	}

	public Scene createShowScene() {
		//this is an HBox with a view of both moves
		//moveImageBox = new HBox(clientMove, opponentMove);
		showPane = new BorderPane();
		cMove = new ImageView();

		opponentMove = new ImageView();
		HBox clientDisplay = new HBox(yourPick_text, cMove);
		HBox opponentDisplay = new HBox(oppPick_text, opponentMove);
		HBox next = new HBox(nextRound);
		VBox gD =new VBox(clientDisplay,opponentDisplay);
		showPane.setLeft(gD);
		showPane.setBottom(next);
		//showPane.setCenter(moveImageBox);
		//the right is a ListView with the scores
		//showPane.setRight();
		return new Scene(showPane, 600, 600);
	}

	public Scene createEndScene() {
		//this hBox has the play again and quit buttons
		endButtons = new HBox(playAgain, quit);
		//this vBox has the winning text and the buttons
		endBox = new VBox(winner,endButtons);
		return new Scene(endBox, 600, 600);
	}

	public void createTextObjects(){
		port = new TextField("");
		portText = new Text("Please Enter a Port #: ");
		ipAddress = new TextField("");
		ipAddressText = new Text("Please Enter an IP Address: ");
		waiting = new Text("Waiting for Another Player...");
		errorMessage = new Text("");
		winner = new Text("");
		yourPick_text = new Text("\n\n	You: ");
		oppPick_text = new Text("\n\n\n\n	Opponent: ");
		move = new String();
		information_listView = new ListView<String>();
	}

	public void createButtonObjects(){
		//for choose scene
		//buttons for the image moves.. we will but image views inside these buttons
		//using .setGraphic()
		rock = new Button();
		paper = new Button();
		scissors = new Button();
		lizard = new Button();
		spock = new Button();
		rock.setGraphic(new ImageView(new Image("file:src/test/resources/rock.jpg", 90, 90, false, false)));
		paper.setGraphic(new ImageView(new Image("file:src/test/resources/paper.jpg", 90, 90, false, false)));
		scissors.setGraphic(new ImageView(new Image("file:src/test/resources/scissors.jpg", 90, 90, false, false)));
		spock.setGraphic(new ImageView(new Image("file:src/test/resources/spock.jpg", 90, 90, false, false)));
		lizard.setGraphic(new ImageView(new Image("file:src/test/resources/lizard.jpg", 90, 90, false, false)));

		nextRound = new Button("Next Round");
		sendMove = new Button("Send Move");
		playAgain = new Button("PLAY AGAIN");
		quit = new Button("QUIT");
		connectToServer = new Button("Connect To Server");
		validMoves = new ArrayList<>();
		validMoves.add("rock");
		validMoves.add("paper");
		validMoves.add("scissors");
		validMoves.add("lizard");
		validMoves.add("spock");
		//moveButtons
	}
}
