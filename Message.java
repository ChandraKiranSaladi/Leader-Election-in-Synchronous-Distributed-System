import java.io.Serializable;

public class Message implements Serializable, Comparable<Message> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int senderUID;
	int highestUIDReceived;
	int phase;
	// long timestamp; Not used anywhere
	int distanceKnown;// changed diameter to distance
	MessageType msgtype;
	int nodeDegree, highestDegreeUID;

	public Message(int senderUID, int phase, int distanceKnown, int highestUIDReceived, MessageType msgtype) {
		this.senderUID = senderUID;
		this.phase = phase;
		this.distanceKnown = distanceKnown;// Make this changes
		this.highestUIDReceived = highestUIDReceived;
		this.msgtype = msgtype;
		this.phase = phase;
		// this.timestamp = System.currentTimeMillis(); Not used anywhere
	}

	public Message(Message message) {
		this(message.senderUID, message.phase, message.distanceKnown, message.highestUIDReceived, message.msgtype);
	}

	public Message(int senderUID, MessageType bfsMsgType) {
		this.senderUID = senderUID;
		this.msgtype = bfsMsgType;
	}

	public Message(int senderUID, MessageType bfsMsgType, int degree, int highestDegreeUID) {
		this.senderUID = senderUID;
		this.msgtype = bfsMsgType;
		this.nodeDegree = degree;
		this.highestDegreeUID = highestDegreeUID;
	}

	public int getSenderUID() {
		return this.senderUID;
	}

	public int getMessagePhase() {
		return this.phase;
	}

	public int getDistanceKnown() {// change
		return this.distanceKnown;// change
	}

	public int gethighestUIDReceived() {
		return this.highestUIDReceived;
	}

	public MessageType getMsgType() {
		return this.msgtype;
	}

	@Override
	public int compareTo(Message msg) {
		;
		if (this.phase < msg.phase) {
			return -1;
		}

		if (this.phase > msg.phase) {
			return 1;
		}
		// Should not reach here
		return 0;
	}
}
