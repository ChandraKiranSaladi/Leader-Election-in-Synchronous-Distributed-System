import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Stream;
import java.net.*;

public class Node {
	int UID, port, highestUID, leaderUID, distanceKnown, phase;
	String HostName;
	HashMap<Integer, NeighbourNode> uIDofNeighbors;
	ServerSocket serverSocket;
	List<ClientRequestHandler> connectedClients = Collections.synchronizedList(new ArrayList<ClientRequestHandler>());
	List<TCPClient> clientsOnMachine = Collections.synchronizedList(new ArrayList<TCPClient>());
	int clientsOnMachineCount;
	BlockingQueue<Message> msgQueue;
	Boolean isLeader;
	Boolean leaderElected;
	Deque<PreviousState> prevTwoStates;
	int parentUID;
	public boolean isMarked;
	int nodeDegree = 0;
	public List<Integer> childList = new ArrayList<>();

	public Node(int UID, int port, String hostName, HashMap<Integer, NeighbourNode> uIDofNeighbors, int phase) {
		this.UID = UID;
		this.port = port;
		this.HostName = hostName;
		this.uIDofNeighbors = uIDofNeighbors;
		this.isLeader = false;
		this.msgQueue = new PriorityBlockingQueue<Message>();
		this.distanceKnown = 0;// add
		this.highestUID = UID;// add
		this.leaderElected = false;
		this.phase = phase;
		this.clientsOnMachineCount = 0;
		this.prevTwoStates = new ArrayDeque<>();
	}

	public Node() {
		// TODO Auto-generated constructor stub
	}

	public int getLeaderUID() {
		return this.leaderUID = this.highestUID;

	}

	public int getDistance() {
		return this.distanceKnown;
	}

	public void setNodeAsLeader() {
		this.isLeader = true;
		this.leaderElected = true;
	}

	public int[] getHighestUIDAndDistanceSoFar(int phase) {
		int[] result = new int[2];
		if (this.msgQueue != null && this.msgQueue.size() > 0) {
			if (!this.msgQueue.stream()
					.anyMatch(t -> t.getMsgType() == MessageType.LEADERELECTED && t.getMessagePhase() == phase)) {
				Message msg = getHighestUIDMsgReceived(phase);
				checkTerminationAndAdd(msg);
				if (msg.highestUIDReceived > this.highestUID) {
					this.highestUID = msg.highestUIDReceived;
					this.distanceKnown = msg.distanceKnown + 1;
				} else if (msg.highestUIDReceived == this.highestUID) {
					this.highestUID = msg.highestUIDReceived;
					this.distanceKnown = msg.distanceKnown;
				}
			} else {
				Optional<Message> message = this.msgQueue.stream()
						.filter(t -> t.getMsgType() == MessageType.LEADERELECTED && t.getMessagePhase() == phase)
						.findFirst();
				this.highestUID = message.get().gethighestUIDReceived();
				this.distanceKnown = message.get().getDistanceKnown();
				this.leaderElected = true;
				if (this.UID == this.highestUID) {
					this.isLeader = true;
				}
			}
			result[0] = this.highestUID;
			result[1] = this.distanceKnown;
		}
		return result;
	}

	public Message getHighestUIDMsgReceived(int phase) {
		Message message = this.msgQueue.stream().filter(t -> t.getMessagePhase() == phase)
				.max(Comparator.comparing(msg -> msg.highestUIDReceived)).get();
		return message;
	}

	public Boolean checkTerminationAndAdd(Message msg) {
		Boolean isterminationReached = false;
		if (prevTwoStates.size() == 2) {
			isterminationReached = this.prevTwoStates.stream()
					.allMatch(state -> state.distance == this.distanceKnown && state.UID == this.highestUID);
			if (isterminationReached) {
				this.leaderElected = true;
				if (this.UID == this.highestUID) {
					this.isLeader = true;
				}
			} else {
				this.prevTwoStates.removeLast();
			}
			return isterminationReached;
		} else if (prevTwoStates.size() < 2) {
			this.prevTwoStates.addFirst(new PreviousState(msg.getDistanceKnown(), msg.highestUIDReceived));
		}
		return isterminationReached;
	}

	public Message getHeadMessageFromQueue() {
		if (this.msgQueue.peek() != null) {
			Message msg = this.msgQueue.peek();
			this.msgQueue.remove();
			return msg;
		}
		return null;
	}

	public void addMessageToQueue(Message msg) {
		this.msgQueue.add(msg);
	}

	public void attachServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public int getNodeUID() {
		return this.UID;
	}

	public int getNodePort() {
		return this.port;
	}

	public String getNodeHostName() {
		return this.HostName;
	}

	public HashMap<Integer, NeighbourNode> getNeighbors() {
		return this.uIDofNeighbors;
	}

	public void addClient(ClientRequestHandler client) {
		synchronized (connectedClients) {
			connectedClients.add(client);
		}
	}

	public List<ClientRequestHandler> getAllConnectedClients() {
		return this.connectedClients;
	}
}

class PreviousState {
	long distance;
	int UID;

	PreviousState(long dist, int UID) {
		this.distance = dist;
		this.UID = UID;
	}
}
