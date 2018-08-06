import java.io.IOException;
import java.util.List;

public class LeaderElectionAlgorithm {
	Node dsNode;

	public LeaderElectionAlgorithm(Node _dsNode) {
		this.dsNode = _dsNode;
	}

	public void InitiateElection() {
		
		System.out.println("Initiating Leader Election on this node..........................");
		this.dsNode.phase = 0;
		while (!this.dsNode.isLeader && !this.dsNode.leaderElected) {
			BroadcastToAllClients();
		}
		if (this.dsNode.leaderElected) {
			System.out.println("leader elected , now leader is broadcasted");
			BroadcastToAllClientsIsLeader();
		}
	}

	public void BroadcastToAllClients() {
		Message msg;
		if (this.dsNode.phase == 0) {
			msg = new Message(this.dsNode.UID, 0, 0, this.dsNode.UID, MessageType.SEND);
			this.dsNode.prevTwoStates.add(new PreviousState(0, this.dsNode.UID));
			BroadcastToAllClients(msg);
			this.dsNode.phase = this.dsNode.phase + 1;
		} else {
			if (this.dsNode.msgQueue != null && this.dsNode.msgQueue.size() > 0) {
				long msgCountforPhase = this.dsNode.msgQueue.stream()
						.filter(t -> t.getMessagePhase() == this.dsNode.phase - 1).count();
				if (msgCountforPhase == this.dsNode.clientsOnMachineCount && !this.dsNode.leaderElected) {

					int[] highestUIDAndDistance = this.dsNode.getHighestUIDAndDistanceSoFar(this.dsNode.phase - 1);

					if (highestUIDAndDistance != null && highestUIDAndDistance.length > 0 && !this.dsNode.isLeader) {
						msg = new Message(this.dsNode.UID, this.dsNode.phase, highestUIDAndDistance[1],
								highestUIDAndDistance[0], MessageType.SEND);

						BroadcastToAllClients(msg);
						this.dsNode.phase = this.dsNode.phase + 1;
					}
				}
			}
		}
	}

	public void BroadcastToAllClients(Message msg) {
		dsNode.connectedClients.forEach((clientHandler) -> {
			try {

				if (this.dsNode.phase == 0) {
					System.out.println(
							"Broadcast message sent to " + clientHandler.getClientUID() + " - { UID:" + this.dsNode.UID
									+ ", Distance:" + this.dsNode.getDistance() + ", Phase:" + this.dsNode.phase
									+ " ,HighestUID:" + this.dsNode.UID + " MessageType:" + MessageType.SEND + "}");
				} else {
					System.out.println("Broadcast message sent to " + clientHandler.getClientUID() + " - { UID:"
							+ this.dsNode.UID + ", Distance: " + this.dsNode.getDistance() + ", Phase:"
							+ this.dsNode.phase + ", HighestUID: " + this.dsNode.highestUID + ", MessageType:"
							+ MessageType.SEND + "}");
				}

				clientHandler.getOutputWriter().writeObject(msg);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	// Sending message that this node is the leader
	public void BroadcastToAllClientsIsLeader() {
		Message msg;
		msg = new Message(dsNode.UID, dsNode.phase, dsNode.getDistance(), dsNode.getLeaderUID(),
				MessageType.LEADERELECTED);
		if (this.dsNode.isLeader) {
			System.out.println("I am the leader " + dsNode.UID);
		} else {
			System.out.println("leader elected: " + dsNode.getLeaderUID());
		}
		dsNode.connectedClients.forEach((clientHandler) -> {
			try {
				clientHandler.getOutputWriter().writeObject(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
}
