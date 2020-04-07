package student_player;

/*
 * This class is used for the pathfinding algorithm and is based on content from
 * https://www.geeksforgeeks.org/shortest-path-in-a-binary-maze/
 */

public class QueueNode {
	Point pt;
    int dist;
  
    public QueueNode(Point pt, int dist) 
    { 
        this.pt = pt; 
        this.dist = dist; 
    } 

}
