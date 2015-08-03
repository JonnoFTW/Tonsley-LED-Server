
public class GameOfLifeThing {

	public int x,y,z;
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getZ() {
		return z;
	}
	public int[][] getBoard() {
		return board;
	}

	public int[][] board;
	public GameOfLifeThing(int x,int y, int z, int[][] board) {
		this.x =x;
		this.y =y;
		this.z =z;
		this.board = board;
	}

}
