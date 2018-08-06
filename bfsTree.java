import java.io.IOException;

public class bfsTree {
	int totalAcksReceived = 0;
	Message msg;
	Node node;
	int newDegree, highestDegreeUID = 0;

	public bfsTree(Node node1) {
		this.node = node1;
	}

	public void startBfs() {
		if (this.node.isLeader) {
			this.node.isMarked = true;
			this.node.msgQueue.clear();
			sendSearch(MessageType.SEARCH);
			System.out.println("Bfs search initiated................................");
		}

		while (true) {
			msg = node.getHeadMessageFromQueue();
			if (msg != null && msg.getMsgType() == MessageType.SEARCH && !this.node.isMarked) {
				this.node.parentUID = msg.senderUID;
				this.node.isMarked = true;
				sendSearch(MessageType.SEARCH);
			} else if (msg != null && msg.getMsgType() == MessageType.SEARCH && this.node.isMarked) {
				sendNEGACK();
			} else if (msg != null && msg.getMsgType() == MessageType.NEGACK && this.node.isMarked) {
				totalAcksReceived = totalAcksReceived + 1;
				if (totalAcksReceived == this.node.uIDofNeighbors.size()) {
					sendPOSACK();
					break;
				}
			} else if (msg != null && msg.getMsgType() == MessageType.POSACK && this.node.isMarked) {
				totalAcksReceived = totalAcksReceived + 1;
				node.childList.add(msg.senderUID);
				if (totalAcksReceived == this.node.uIDofNeighbors.size()) {
					sendPOSACK();
					break;
				}
			}
		}
		if (!node.isLeader) {
			this.node.nodeDegree = node.childList.size() + 1;
			System.out.println("Node: " + node.UID + " , its parent: " + this.node.parentUID);
			System.out.println("Degree of node :" + this.node.nodeDegree);
		} else {
			this.node.nodeDegree = node.childList.size();
			System.out.println("Node: " + node.UID + " is elected as the leader and is the bfs root node");
			System.out.println("Degree of root node :" + this.node.nodeDegree);
		}
		for (ClientRequestHandler y : this.node.connectedClients) {
			if (node.childList != null && !node.childList.isEmpty() && node.childList.contains(y.getClientUID())) {
				System.out.println(" Node: " + node.UID + " , its Child: " + y.getClientUID());
			} else {
				y.isChild = false;
			}
		}

		/*
		 * for (ClientRequestHandler y : this.node.connectedClients) { if (!y.isChild) {
		 * try { y.out.writeObject(new Message(this.node.UID, MessageType.BYE));
		 * y.getClientSocket().close(); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); } } }
		 */

		// this.node.connectedClients.removeIf(s -> s.isChild == false);
	}

	public void sendNEGACK() {
		for (ClientRequestHandler x : node.connectedClients) {
			try {
				if (x.getClientUID() == msg.senderUID) {
					System.out.println("BFS negative ACK message sent to " + x.getClientUID() + " - { UID:"
							+ this.node.UID + " ,MessageType:" + MessageType.NEGACK + "}");
					x.out.writeObject(new Message(this.node.UID, MessageType.NEGACK));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void sendPOSACK() {
		// TODO Auto-generated method stub
		for (ClientRequestHandler x : node.connectedClients) {
			if (x.getClientUID() == this.node.parentUID)
				try {
					System.out.println("BFS positive ACK message sent to " + x.getClientUID() + " - { UID:"
							+ this.node.UID + " ,MessageType:" + MessageType.POSACK + "}");
					x.out.writeObject(new Message(this.node.UID, MessageType.POSACK));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	public void sendPOSACKDegree() {
		for (ClientRequestHandler x : node.connectedClients) {
			if (x.getClientUID() == this.node.parentUID)
				try {
					System.out.println("Degree broadcast message sent to " + x.getClientUID() + " - { UID:"
							+ this.node.UID + " ,HighestDegree:" + this.newDegree + " ,HighestDegreeUID: "
							+ this.highestDegreeUID + " ,MessageType:" + MessageType.POSDEGREE + "}");
					x.out.writeObject(
							new Message(this.node.UID, MessageType.POSDEGREE, this.newDegree, this.highestDegreeUID));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	public void sendSearch(MessageType message) {
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} 				
			for (ClientRequestHandler x : node.connectedClients) {
			try {
				if (message == MessageType.SEARCH) {
					x.out.writeObject(new Message(this.node.UID, MessageType.SEARCH));
				} else if (message == MessageType.DEGREE) {
					if (x.isChild) {
						System.out.println("Degree broadcast message sent to " + x.getClientUID() + " - { UID:"
								+ this.node.UID + " ,HighestDegree:" + this.newDegree + " ,HighestDegreeUID: "
								+ this.highestDegreeUID + " ,MessageType:" + MessageType.DEGREE + "}");
						x.out.writeObject(
								new Message(this.node.UID, MessageType.DEGREE, this.newDegree, this.highestDegreeUID));
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void initiateDegreeQuery() {
		this.totalAcksReceived = 0;
		if (this.node.isLeader) {
			this.node.msgQueue.clear();
			this.newDegree = node.childList.size();
			this.highestDegreeUID = this.node.UID;
			System.out.println("Broadcast initiated to find max degree...............................");
			sendSearch(MessageType.DEGREE);
		}

		while (true) {
			int newDegree = 0;
			msg = this.node.getHeadMessageFromQueue();
			if (msg != null && msg.getMsgType() == MessageType.DEGREE) {
				if (!this.node.childList.isEmpty() && msg.nodeDegree < this.node.childList.size() + 1) {
					this.highestDegreeUID = node.UID;
					this.newDegree = node.childList.size() + 1;
					sendSearch(MessageType.DEGREE);
				} else if (!node.childList.isEmpty() && msg.nodeDegree >= node.childList.size() + 1) {
					this.highestDegreeUID = msg.highestDegreeUID;
					this.newDegree = msg.nodeDegree;
					sendSearch(MessageType.DEGREE);
				} else if (msg != null && node.childList.isEmpty()) {
					this.newDegree = msg.nodeDegree;
					this.highestDegreeUID = msg.highestDegreeUID;
					System.out.println("Convergecast initiated to find max degree.....");
					sendPOSACKDegree();
					break;
				}
			} else if (msg != null && msg.getMsgType() == MessageType.POSDEGREE && this.node.isLeader) {
				totalAcksReceived = totalAcksReceived + 1;
				System.out.println("TotalAck :" + totalAcksReceived + " MessageUID : " + msg.senderUID);
				if (msg.nodeDegree > this.newDegree) {
					this.newDegree = msg.nodeDegree;
					this.highestDegreeUID = msg.highestDegreeUID;
				}

				if (totalAcksReceived == this.node.childList.size()) {
					System.out.println("Maximum degree of any node in the BFS tree is " + this.newDegree + " for node "
							+ this.highestDegreeUID);
					break;
				}
			} else if (msg != null && msg.getMsgType() == MessageType.POSDEGREE && !this.node.isLeader) {
				totalAcksReceived = totalAcksReceived + 1;
				if (msg.nodeDegree > this.newDegree) {
					this.newDegree = msg.nodeDegree;
					this.highestDegreeUID = msg.highestDegreeUID;
				}
				if (totalAcksReceived == this.node.childList.size()) {
					System.out.println("Convergecast initiated to find max degree.....");
					sendPOSACKDegree();
					break;
				}
			}
		}
	}
}

