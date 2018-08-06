import java.io.*;
import java.net.*;

public class TCPClient {

	String serverHostName, clientHostName;
	int serverPortNumber, UID, serverUID;
	Socket clientsocket;
	ObjectInputStream in;
	ObjectOutputStream out;
	Node dsNode;

	public TCPClient(int UID, int serverPort, String serverHostName, String clientHostName, int serverUID,
			Node _dsNode) {
		this.serverHostName = serverHostName;
		System.out.println("ServerHostName: " + serverHostName);
		this.serverPortNumber = serverPort;
		System.out.println("ServerPort: " + serverPort);
		this.UID = UID;
		System.out.println("UID: " + UID);
		this.clientHostName = clientHostName;
		System.out.println("clientHostName: " + clientHostName);
		this.serverUID = serverUID;
		System.out.println("ServerUID: " + serverUID);
		this.dsNode = _dsNode;
	}

	public void listenSocket() {
		// Create socket connection
		try {
			clientsocket = new Socket(serverHostName, serverPortNumber, InetAddress.getByName(clientHostName), 0);
			out = new ObjectOutputStream(clientsocket.getOutputStream());
			out.flush();

			in = new ObjectInputStream(clientsocket.getInputStream());
			System.out.println("After inputStream, listenSocket");
		} catch (UnknownHostException e) {
			System.out.println("Unknown host:" + serverHostName);
			System.exit(1);
		} catch (IOException e) {
			System.out.println("No I/O" + e);
			System.exit(1);
		}
	}

	public void sendHandShakeMessage() {

		try {
			// Send text to server
			System.out.println("Sending data to server " + this.serverUID + ".....");
			String msg = "Hi!" + this.UID;
			out.writeObject(msg);
			out.flush();
		} catch (IOException e) {
			System.out.println("failed transmission" + e);
			System.exit(1);
		}
	}

	public void listenToBroadCastMessages() {
		try {
			while (true) {
				Message broadcastMessage = (Message) in.readObject();
				// add received messages to Blocking queue
				this.dsNode.addMessageToQueue(broadcastMessage);

				if (broadcastMessage.getMsgType() != MessageType.DEGREE
						&& broadcastMessage.getMsgType() != MessageType.POSDEGREE) {
					System.out.println("Boardcast text recieved from :" + broadcastMessage.senderUID + " - { UID:"
							+ broadcastMessage.senderUID + ", Distance:" + broadcastMessage.getDistanceKnown()
							+ ", Phase:" + broadcastMessage.getMessagePhase() + " ,HighestUID:"
							+ broadcastMessage.highestUIDReceived + " MessageType:" + broadcastMessage.getMsgType()
							+ "}");
				} else {
					System.out.println("Degree broadcast message received from " + broadcastMessage.senderUID
							+ " - { UID:" + broadcastMessage.senderUID + " ,HighestDegree:"
							+ broadcastMessage.nodeDegree + " ,HighestDegreeUID: " + broadcastMessage.highestDegreeUID
							+ " ,MessageType:" + broadcastMessage.getMsgType() + "}");
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("failed transmission" + e);
			System.exit(1);
		}
	}
}
