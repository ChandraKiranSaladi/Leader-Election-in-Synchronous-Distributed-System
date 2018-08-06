
Implemented a simple message based Synchronous Distributed System using Sockets and Threads. 

Peleg’s Algorithm: A time optimal Leader Election Algorithm in general networks. Elects a leader among a group of nodes in a network using phases and Node Ids. The node with a Larger ID becomes the leader.

SyncBFS Algorithm: Construction of BFS Spanning Tree for Broadcasting and Converge casting from leader to all other nodes. The leader initiates the construction and thereafter, when a node receives a bfs message, the node from which it received will be its parent. Using acknowledgements, a node will find its child nodes. 

Broadcast: Each node sends the data to all its children, till the leaf nodes. 
Convergecast: Each node sends the information to its parent till the leader node. 

Implementation is done using Java and Linux shell scripts. Shell script is used to open multiple terminals and ssh to a server to make each terminal act as a node.






Team Details: Chandra Kiran Saladi      cxs172130
	      Saloni Agarwal	        sxa171531
	      Anusha Reddy Narapureddy  axn160030



Main method class is InvokeMain

Order of compiling the java files. 

1)Message.java 
2)MessageType.java 
3)ParseConfigFile.java 
4)Node.java 
5)ClientRequestHandler.java 
6)TCPClient.java 
7)TCPServer.java 
8)bfsTree.java 
9)LeaderElectionAlgorithm.java 
10)InvokeMain.java


The path mentioned in the Main method in InvokeMain class for configuration file
/home/010/c/cx/cxs172130/config2.txt 

The path mentioned in configuration file for class files 
/home/010/c/cx/cxs172130/src


The config file in the local machine. These are mentioned accordingly in the scripts
CONFIGLOCAL=/home/ck/Downloads/config2.txt
