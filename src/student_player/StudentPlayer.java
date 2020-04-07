package student_player;

import boardgame.Move;

import Saboteur.SaboteurPlayer;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDrop;
import Saboteur.cardClasses.SaboteurTile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;

/** A player file submitted by a student. */
public class StudentPlayer extends SaboteurPlayer {
	
	int[][] board; // BOARD_SIZE * 3 X BOARD_SIZE * 3 to make pathfinding easier with tiles since tiles are int[3][3].
	int turn = 0;
	ArrayList<Point> gold;
	ArrayList<SaboteurMove> moves;
	ArrayList<String> pathTiles;
	ArrayList<String> deadEndTiles;
	ArrayList<SaboteurMove> tileMoves;
	int BOARD_SIZE = SaboteurBoardState.BOARD_SIZE;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260727997");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(SaboteurBoardState boardState) {
    	
    	// Read data for list of path tiles. If exception thrown, default to random moves for rest of game.
    	if(turn==0) {
    		tileMoves = new ArrayList<>();
    		pathTiles = new ArrayList<>();
    		deadEndTiles = new ArrayList<>();
    		
    		try {
    			readData();
    		} catch(Exception e) { // If exception set turn to -1
    			e.printStackTrace();
    			turn = -1;
    			return boardState.getRandomMove();
    		}
    	    
    	} 
    	else if(turn == -1) { // When turn is -1 we default to random moves.
    		return boardState.getRandomMove();
    	}

    	/*
    	 * Variables used for main algorithm
    	 * board: a modified version of the board which shows all walls and all possible locations of paths
    	 * gold: array list of Points. Each point is the (x, y) coordinates of a gold nugget.
    	 * moves: all legal moves a player can make
    	 * tileMoves: all moves that involve a path tile (tile that is not a dead end)
    	 */
    	board = MyTools.updateBoard(boardState.getHiddenIntBoard());
    	gold = MyTools.checkGold(boardState.getHiddenBoard());
    	moves = boardState.getAllLegalMoves();
    	tileMoves.clear();
    	
    	
    	/*
    	 * Start of the main decision making
    	 * Steps (in order of priority; code will only execute one step per move)
    	 * 	1. If we don't know where gold is: use a map
    	 * 	2. If we can place a path tile, choose the one that will get us closest to the gold.
    	 * 	3. Drop a map card if we already know the location of the gold.
    	 *  4. Drop a dead-end card if we can't do anything else
    	 *  5. Pick a random move if all else fails.
    	 */
    	
    	// If the location of gold is not known and we have a map card, play the map card.
    	if(gold.size() > 1) {
    		for(SaboteurMove x : moves) {
            	if(x.getCardPlayed().getName().equals("MAP")) return x;
            }
    	}
    	
    	// If we have any path tile moves and add them to the tileMoves arrayList.
    	for(SaboteurMove x : moves) {
    		if(pathTiles.contains(x.getCardPlayed().getName())) {
    			tileMoves.add(x);
    		}
    	}
    	    	
    	// If we don't have any path tile moves then we drop one of our cards
    	if(tileMoves.size() == 0) {
    		int i = 0;
    		for(SaboteurCard c : boardState.getCurrentPlayerCards()) {
    			// If we have a spare map card drop it
    			if(c.getName().equals("MAP") && gold.size() == 1) {
    				return (new SaboteurMove(new SaboteurDrop(), i, 0, boardState.getTurnPlayer()));
    			} else if(deadEndTiles.contains(c.getName())) {
    				return (new SaboteurMove(new SaboteurDrop(), i, 0, boardState.getTurnPlayer()));
    			}
    			i++;
    		}
    	}
    	
    	// Call the path finding algorithm to find best move to make from tileMoves
    	SaboteurMove m = pickPathTileMove();
    	if(m != null) return m; 
    	
    	// If no bad cards to drop and no path-making moves available, choose a random move
    	return boardState.getRandomMove();
    	
    	// Below are some useful print statements for visualizing the algorithm while playing.
    	//---------------------------TESTING----------------------------
    	
        // Testing all moves.
    	/*
    	System.out.println("------PRINING ALL LEGAL MOVES---------");
        for(SaboteurMove y : moves) {
        	System.out.println(y.toPrettyString());
        }
        
        // Print path board.
        System.out.println("--------PRINTING PATH-----------");
        MyTools.printBoardPath(board);
        
        
        // Print tile board.
        System.out.println("-----------PRINTING TILES------------");
        MyTools.printBoardTiles(boardState);*/
        
    }
    
    // ----------------------------------- Below are some helper functions ---------------------------------- //
    
    /*
     * Reads the names of path tiles and dead-end tiles from input_data.txt and adds them to pathTiles 
     */
    private void readData() throws IOException{
    	File file = new File("data/input_data.txt"); 
	    Scanner sc = new Scanner(file);
	  
	    for(int i = 0; i < 13; i++) {
	    	pathTiles.add(sc.nextLine());
	    }
	    while(sc.hasNext()) {
	    	deadEndTiles.add(sc.nextLine());
	    }
	    sc.close();
    }
    
    /*
     * Picks the "best" path tile move
     * I.e. the move that gets the player closest to the gold.
     */
    private SaboteurMove pickPathTileMove() {
    	int lowestMoveDistance = Integer.MAX_VALUE;
    	SaboteurMove moveToPick = null;
    	
    	// Loops over all tile moves and calculates shortest path to gold.
    	for(SaboteurMove z : tileMoves) {
    		
    		//Setup: get tile layout and position to add to our board.
    		SaboteurTile tile = (SaboteurTile) z.getCardPlayed();
    		int[][] path = tile.getPath();
    		Point src = new Point(z.getPosPlayed()[0]*3, z.getPosPlayed()[1]*3);
    		
    		// Have to rotate the path tile to align with the orientation of the board.
    		rotateMatrix(path);
    		
    		// Add tile to board.
    		int tmp = 0;
    		for(int i = src.x; i < (src.x)+3; i++) {
    			for(int j = src.y; j < (src.y)+3; j++) {
    				tmp = board[i][j];
    				board[i][j] = path[i-(src.x)][j-(src.y)];
    				path[i-(src.x)][j-(src.y)] = tmp;
    			}
    		}
    		
    		// Find the shortest path
    		int newMoveDistance = pathDistance(src);
    		
    		// Remove tile from board.
    		for(int i = src.x; i < (src.x)+3; i++) {
    			for(int j = src.y; j < (src.y)+3; j++) {
    				board[i][j] = path[i-(src.x)][j-(src.y)];
    			}
    		}
    		
    		// Check if move is better than current move if not keep the current move.
    		if(newMoveDistance == -1) continue;
    		else if(lowestMoveDistance > newMoveDistance) {
    			lowestMoveDistance = newMoveDistance;
    			moveToPick = z;
    		}
    		
    		
    	}
    	return moveToPick;
    }
    
    /*
     * Pathfinding algorithm based on: https://www.geeksforgeeks.org/shortest-path-in-a-binary-maze/
     * Finds the shortest path in a binary maze.
     */
    private int pathDistance(Point src) {
    	
		int rowNum[] = {-1, 0, 0, 1}; 
		int colNum[] = {0, -1, 1, 0}; 
		
		// Make sure src is valid.
		if(board[src.x+1][src.y+1] != 1) {
			System.err.println("Line 204: source was not valid");
			return -1;
		}
		
		// Visited array of the board.
		boolean[][] visited = new boolean[BOARD_SIZE*3][BOARD_SIZE*3];
		visited[src.x+1][src.y+1] = true;
		
		// Main BFS queue
		Queue<QueueNode> q = new LinkedList<>();
		
		QueueNode s = new QueueNode(new Point(src.x+1, src.y+1), 0);
		q.add(s);
		
		while(!q.isEmpty()) {
			QueueNode curr = q.peek();
			Point pt = curr.pt;
			
			// If any of the potential gold cells are reached we stop
			for(Point g : gold) {
				if(pt.x == g.x && pt.y == g.y) return curr.dist;
			}
			
			// Else we dequeue the current cell and enqueue all adjacent cells that are valid
			q.remove();
			
			for(int i = 0; i < 4; i++) {
				int row = pt.x + rowNum[i];
				int col = pt.y + colNum[i];
				
				if(isValid(row, col) && board[row][col] != 0 && !visited[row][col]) {
					// Mark cell as visited and add it to queue.
					visited[row][col] = true;
					q.add(new QueueNode(new Point(row, col), curr.dist + 1));
					
				}
			}
		}
		
		return -1;
    }
    
    /*
     * Make sure an index is within the range of the board
     * Code from: https://www.geeksforgeeks.org/shortest-path-in-a-binary-maze/
     */
    private boolean isValid(int row, int col) 
    { 
        // return true if row number and  
        // column number is in board 
        return (row >= 0) && (row < BOARD_SIZE*3) && 
               (col >= 0) && (col < BOARD_SIZE*3); 
    } 
    
    /*
     * Rotate a matrix 90 degrees counter-clockwise
     * Used to align the board and the tiles
     * Code from https://www.geeksforgeeks.org/inplace-rotate-square-matrix-by-90-degrees/
     */
    private void rotateMatrix(int[][] mat)
    {
        int N = 3;
        // Consider all squares one by one
        for (int x = 0; x < N / 2; x++) {
            // Consider elements in group of 4 in
            // current square
            for (int y = x; y < N - x - 1; y++) {
                // store current cell in temp variable
                int temp = mat[x][y];

                // move values from right to top
                mat[x][y] = mat[y][N - 1 - x];

                // move values from bottom to right
                mat[y][N - 1 - x] = mat[N - 1 - x][N - 1 - y];

                // move values from left to bottom
                mat[N - 1 - x][N - 1 - y] = mat[N - 1 - y][x];

                // assign temp to left
                mat[N - 1 - y][x] = temp;
            }
        }
    }
}