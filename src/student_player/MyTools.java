package student_player;

import java.util.ArrayList;

import Saboteur.SaboteurBoardState;
import Saboteur.cardClasses.SaboteurTile;

public class MyTools {
	
	static int BOARD_SIZE = SaboteurBoardState.BOARD_SIZE;
    
    /*
     * Updates the 0-1 board so that all unknown parts that would be walls are shown as walls.
     * For example. The row: -1 -1 -1 becomes 0 -1 0.
     */
    public static int[][] updateBoard(int[][] board) {
    	int countCol = 0;
    	int countRow = 0;
    	for(int i = 0; i < BOARD_SIZE*3; i++) {
    		for(int j = 0; j < BOARD_SIZE*3; j++) {
    			if(board[i][j]==-1 && countCol!=1 && countRow!=1) board[i][j] = 0;
    			countCol = (countCol+1)%3;
    		}
    		countRow = (countRow+1)%3;
    	}
    	return board;
    }
    
    /*
     * Returns an array list of the potential gold locations.
     * [12][3]
     * [12][5]
     * [12][7]
     * If gold location is known will return an ArrayList of length 1.
     * Points are multiplied by 3 and 1 is added to align with 1-0 board coordinates.
     */
    public static ArrayList<Point> checkGold(SaboteurTile[][] tileBoard) {
    	ArrayList<Point> gold = new ArrayList<>();
    	for(int i = 0; i < 3; i++) {
    		int x = 1+(12*3);
    		int y = ((3+(i*2))*3)+1;
    		String idx = tileBoard[12][3+(i*2)].getIdx();
        	if(idx.equals("nugget")) {
        		gold.clear();
        		gold.add(new Point(x, y));
        		return gold;
        	}
        	else if (idx.equals("8")) {
        		gold.add(new Point(x, y));
        	}
    	}
    	
    	return gold;
    }
    
    /*
     * Prints the 0-1 int board but replaces '-1' with '-' for easier reading.
     */
    public static void printBoardPath(int[][] board) {
    	for(int i = 0; i < BOARD_SIZE*3; i++) {
    		for(int j = 0; j < BOARD_SIZE*3; j++) {
    			if(board[i][j]==-1) System.out.print("- ");
    			else System.out.print(board[i][j] +" ");
    		}
    		System.out.println();
    	}
    }
    
    /*
     * Prints the tileIdx of the board and replaces null tiles with '-'.
     */
    public static void printBoardTiles(SaboteurBoardState boardState) {
    	SaboteurTile[][] board = boardState.getHiddenBoard();
    	for(int i = 0; i < BOARD_SIZE; i++) {
    		for(int j = 0; j < BOARD_SIZE; j++) {
    			if(board[i][j] != null) System.out.print(board[i][j].getIdx() +" ");
    			else System.out.print("- ");
    			
    		}
    		System.out.println();
    	}
    }
}