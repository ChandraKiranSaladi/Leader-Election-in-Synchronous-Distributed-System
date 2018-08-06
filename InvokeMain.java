import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;

public class InvokeMain {
	public static void main(String[] args) {

		// build a node for each terminal

		// logic for assigning nodes - temporary

		// int hostNumIndex = Integer.parseInt(args[0]);
		// int hostNumIndex = 0;
		String clientHostName = "";
		try {
			clientHostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Node dsNode = BuildNode(clientHostName);

		System.out.println("Initializing Server with UID: " + dsNode.UID);

		// Start server thread

		Runnable serverRunnable = new Runnable() {
			public void run() {
				TCPServer server = new TCPServer(dsNode);
				// start listening for client requests
				server.listenSocket();
			}
		};
		Thread serverthread = new Thread(serverRunnable);
		serverthread.start();

		System.out.println("Server started and listening to client requests.........");

		// Start client threads at this node
		// System.out.println("Press any key to start clients.........");
		//
		// BufferedReader bufferRead = new BufferedReader(new
		// InputStreamReader(System.in));
		// String input = bufferRead.readLine();

		// if (input != null) {
		for (Entry<String, Node> node : ParseConfigFile.nodeList.entrySet()) {
			if (node.getValue().HostName.equals(clientHostName) ) {
				node.getValue().uIDofNeighbors.entrySet().forEach((neighbour) -> {
					// TCPClient client = new TCPClient(neighbour.getKey(), dsNode.port,
					// dsNode.HostName,neighbour.getValue().PortNumber,
					// neighbour.getValue().HostName);

						dsNode.clientsOnMachineCount = dsNode.clientsOnMachineCount + 1;
						Runnable clientRunnable = new Runnable() {
							public void run() {
								try {
									Thread.sleep(10000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								TCPClient client = new TCPClient(node.getValue().getNodeUID(), neighbour.getValue().PortNumber,
										neighbour.getValue().HostName, node.getValue().HostName, neighbour.getKey(),
										dsNode);
								client.listenSocket();
								client.sendHandShakeMessage();
								client.listenToBroadCastMessages();
							}
						};
						Thread clientthread = new Thread(clientRunnable);
						clientthread.start();
					
				});
				break;

			}

		}
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// dsNode.phase=0;
		// while(dsNode.phase<=20) {
		// dsNode.Broadcast(new Message(dsNode.phase++));
		// }
		LeaderElectionAlgorithm algorithm = new LeaderElectionAlgorithm(dsNode);
		algorithm.InitiateElection();

		bfsTree bfsTree = new bfsTree(dsNode);
		bfsTree.startBfs();
		System.out.println("BFS Tree is constructed");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bfsTree.initiateDegreeQuery();	
		System.out.println("Execution finished......");
		

	}

	public static Node BuildNode(String clientHostName) {
		Node dsNode = new Node();
		try {
			dsNode = ParseConfigFile.read("/home/010/c/cx/cxs172130/config2.txt",
					// "/home/ck/Downloads/config3.txt",
					clientHostName);
		} catch (Exception e) {
			throw new RuntimeException("Unable to get nodeList", e);
		}
		return dsNode;
	}
}
