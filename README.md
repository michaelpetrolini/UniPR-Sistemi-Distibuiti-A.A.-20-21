# UniPR-Sistemi-Distibuiti-A.A.-20-21

These are the assignments proposed for the "Distributed Systems" course at UniPR A.A. 2020/2021.
  - Assignment 1 - Socket based client-server system for multicast communication: The goal of this assignment is to build a distributed system in Java for the simulation of multicasting among a set of communication nodes.
  - Assignment 2 - RMI based client-server system for mutual exclusion with unreliable nodes
Define and implement a system that provides the centralized algorithm for mutual exclusion. In this system, the coordinator manages a single resource and is elected by using the Bully algorithm. The other nodes submit repeatedly requests with a random delay between two successive deliveries.
  - Assignment 3 – JMS based client-server system for storage replication with the quorum based protocol
Define and implement a distributed system that supports replication through the use of the “quorum based protocol”. The system has different computational nodes divided in clients and coordinators. Clients multicast read and write requests to the coordinators. Coordinators can reply with a vote message. In particular, a coordinator can manage a single request at a time and so it can send a vote message to a client only if it is not serving a request of another client.
