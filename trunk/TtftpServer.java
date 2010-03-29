import java.net.*;

/**
 * Class to get new Ttftp connections. Each new connection is passed to a worker
 * to complete the transmitting process
 * 
 * @author Joel
 * 
 */
class TtftpServer {

	/**
	 * Keep listening for connections
	 */
	public void start_server() {
		try {
			DatagramSocket ds = new DatagramSocket();			
			System.out.println("TtftpServer on port " + ds.getLocalPort());

			// enter packet receiving state
			for (;;) {				
				// receive packets
				byte[] buf = new byte[576];
				DatagramPacket p = new DatagramPacket(buf, 576);
				ds.receive(p);

				// start a worker thread
				TtftpServerWorker worker = new TtftpServerWorker(p);
				worker.start();
			}
		} catch (Exception e) {
			System.err.println("Exception: " + e);
		}
	}

	/**
	 * TtftpServer main method.
	 * At the moment this just calls the start_server method
	 * @param args
	 */
	public static void main(String args[]) {
		TtftpServer d = new TtftpServer();
		d.start_server();
	}
}
