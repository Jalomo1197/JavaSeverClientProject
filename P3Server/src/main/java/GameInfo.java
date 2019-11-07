import java.io.Serializable;
//Code for server GameInfo
public class GameInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	String publicMessage;
	String messageFromPlayerOne;
	String messageFromPlayerTwo;
	String messageToPlayerOne;
	String messageToPlayerTwo;
	String playerOneMove;
	String playerTwoMove;
	int playerOnePoints;
	int playerTwoPoints;
	int initialID;
	boolean has2Players;
	int winner;

	GameInfo(){
		winner = -1;
		has2Players = false;
		playerOnePoints = 0;
		playerTwoPoints = 0;
		messageFromPlayerOne = "";
		messageFromPlayerTwo = "";
		messageToPlayerOne = "";
		messageToPlayerTwo = "";
		playerOneMove = "";
		playerTwoMove = "";
	}

	public void printGameInfo(){
        System.out.println("\npublicMessage: " + publicMessage);
        System.out.println("\nmessageFromPlayerOne: " + messageFromPlayerOne);
        System.out.println("\nmessageFromPlayerTwo: " + messageFromPlayerTwo);
        System.out.println("\nmessageToPlayerOne: " + messageToPlayerOne);
        System.out.println("\nmessageToPlayerTwo: " + messageToPlayerTwo);
        System.out.println("\nplayerOneMove: " + playerOneMove);
        System.out.println("\nplayerTwoMove: " + playerTwoMove);
        System.out.println("\nplayerOnePoints: " + playerOnePoints);
        System.out.println("\nplayerTwoPoints: " + playerTwoPoints);
        System.out.println("\nhas2Players: " + has2Players);
        System.out.println("\nwinner: " + winner);
    }
}
