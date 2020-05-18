//Not to be edited
public class Vote {

	private final int participantPort;
	private final String vote;
	
	public Vote(int participantPort, String vote) {
		this.participantPort = participantPort;
		this.vote = vote;
	}

	public int getParticipantPort() {
		return participantPort;
	}

	public String getVote() {
		return vote;
	}

	@Override
	public String toString() {
		return "<" + participantPort + ", " + vote + ">";
	}
	
	
}
