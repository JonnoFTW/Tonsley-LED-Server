import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

public class DataAcceptThread extends Thread {

	public static void main(String[] args) throws IOException {
		DataAcceptThread dat = new DataAcceptThread(7778, true);
		dat.start();
	}

	private ServerSocket mServerSocket;
	private boolean mRunning = true, mVerbose = false;
	private LinkedList<GameOfLifeThing> mGoLQueue = new LinkedList<>();

	private static final String TAG = DataAcceptThread.class.getSimpleName();

	public DataAcceptThread(int port, boolean verbose) throws IOException {
		mServerSocket = new ServerSocket(port, 512);
		//putThing(0, 0, 0, new int[][]{{1,2,3},{4,5,6},{7,8,9}});
		mVerbose = verbose;
		log("Started thread on port " + port + " with address "
				+ mServerSocket.getLocalSocketAddress());
	}

	private boolean valid(int x, int max) {
		return x >= 0 && x <= max;
	}

	public void putThing(int x, int y, int z, int[][] board) {
		if (valid(x, 165) && valid(y, 17) && valid(z, 2)
				&& valid(board.length, 165)) {
			boolean err = false;
			for (int[] is : board) {
				// only valid length boards and non-ragged arrays
				if (!valid(is.length, 17) || is.length!=board[0].length)
					err = true;
			}
			if (!err) {
				log("Adding board");
				mGoLQueue.add(new GameOfLifeThing(x, y, z, board));
				return;
			}
		}
		log("Invalid board received");

	}

	public boolean hasThings() {
		return !mGoLQueue.isEmpty();
	}
	public GameOfLifeThing[] getThings() {
		GameOfLifeThing[] things = new GameOfLifeThing[mGoLQueue.size()];
		int i =0;
		while (!mGoLQueue.isEmpty()) {
			things[i++] = mGoLQueue.pop();
		}
		return things;
	}

	public void stopThread() throws IOException {
		log("Stopping thread");
		mRunning = false;
		mServerSocket.close();
	}

	private void log(String msg) {
		if (mVerbose)
			System.out.println(TAG + ": " + msg);
	}

	@Override
	public void run() {
		log("Starting thread");
		while (mRunning) {
			try {
				(new Thread(new ParserThread(mServerSocket.accept()))).start();
			} catch (IOException e) {
				log(e.getMessage());
			}
		}
	}
	public static String readAll(Socket socket) throws IOException {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    StringBuilder sb = new StringBuilder();
	    String line;
	    while ((line = reader.readLine()) != null)
	        sb.append(line).append(" ");
	    return sb.toString().trim();
	}
	private class ParserThread implements Runnable {
		private Socket mmSocket;

		public ParserThread(Socket sock) {
			mmSocket = sock;
		}

		
		@Override
		public void run() {
			try {
				log("Connection from" + mmSocket.getRemoteSocketAddress());
				String received = readAll(mmSocket);
				log("Received: '"+received+"'");
				
				Scanner s = new Scanner(received);
				// s.useDelimiter("\n");
				int x = s.nextInt();
				int y = s.nextInt();
				int z = s.nextInt();
				String[] rows = s.nextLine().trim().split("-");
				s.close();

				int[][] numBoard = new int[rows.length][];
				for (int i = 0; i < numBoard.length; i++) {
					String[] values = rows[i].split(",");
					numBoard[i] = new int[values.length];
					for (int j = 0; j < values.length; j++) {
						numBoard[i][j] = Integer.parseInt(values[j]);
					}
				}
				putThing(x, y, z, numBoard);
				
			} catch (IOException e) {
				log("IOException" + e.getMessage());
				e.printStackTrace();
			} catch (NumberFormatException e) {
				log("NumberFormatException: " + e.getMessage());
				e.printStackTrace();
			} finally {
				try {
					
					mmSocket.close();
					log("Closed connection from "+mmSocket.getRemoteSocketAddress());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}
