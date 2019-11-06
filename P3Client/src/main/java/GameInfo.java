import java.io.Serializable;
//Code for Client GameInfo
public class GameInfo implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	boolean isMessage;
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
