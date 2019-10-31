import java.io.Serializable;
//Code for Client GameInfo
public class GameInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Boolean has2Players;
	String p1Play;
	String p2Play;
	int p1Points;
	int p2Points;
	String winner;
	String message; //maybe to send info about why winner won?? idk 
}
