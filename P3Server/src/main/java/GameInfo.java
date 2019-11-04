import java.io.Serializable;
//Code for Client GameInfo
public class GameInfo implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	boolean winner;
	String message; //maybe to send info about why winner won?? idk

	GameInfo(){
		winner = false;
	}


}
