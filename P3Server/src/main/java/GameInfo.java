import java.io.Serializable;
//Code for server GameInfo
public class GameInfo implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	
	
	//eventually these should be set to private
	boolean isMessage; //is message is used if we are only trying to send a message and ignore all other fields
	int clientNumber;
	boolean has2Players;
	int winner;
	String clientMove;
	String opponentMove;
	int yourPoints;
	int opponentsPoints;
	
	public String message; //maybe to send info about why winner won?? idk

	GameInfo(){
		winner = -1;
		has2Players = false;
	}


}
