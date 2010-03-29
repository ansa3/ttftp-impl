import java.net.*;
import java.io.*;

class TtftpServerWorker extends Thread {
	private DatagramPacket req;
	private long seqNo = 0;

	private void sendfile(TtftpPacket packet) {
		int read = 0;

		// assign a new datagram socket
		DatagramSocket ds;
		try {
			ds = new DatagramSocket();
			ds.setSoTimeout(5000);
		} catch (SocketException e) {
			System.err.println("Exception: " + e);
			return;
		}

		try {

			// must receive a GET request first
			if (!packet.isGET())
				throw new FileNotFoundException();

			TtftpPacket tpOut;
			FileInputStream file = new FileInputStream(new File(packet
					.getFileName()));

			// loop until all data has been sent and THX'ed
			for (;;) {
				// read some data from the file
				byte[] dbuf = new byte[543];

				read = file.read(dbuf);

				// check for end of file
				if (read == -1)
					break;

				// create DATA packet
				tpOut = TtftpPacketFactory.createPacketDATA(read, seqNo, dbuf);

				// send data and receive THX packet
				if (send(ds, tpOut.getBuffer()))
					seqNo++;
				else					
					break;
			}

			// if end of file reached, send EOF packet
			if (read == -1) {
				TtftpPacket tmp = TtftpPacketFactory.createPacketEOF(seqNo);

				// send EOF packet
				send(ds, tmp.getBuffer());
			}

			file.close();

			// we respond to this exception by sending a FAIL
		} catch (FileNotFoundException e) {
			try {
				// create and send FAIL packet
				TtftpPacket tp = TtftpPacketFactory.createPacketFAIL();
				ds.send(new DatagramPacket(tp.getBuffer(),
						tp.getBuffer().length, req.getSocketAddress()));
			} catch (IOException e1) {
				System.err.println("Exception: " + e1);
			}
		} catch (IOException e) {
			System.err.println("Exception: " + e);
		}

	}

	private boolean send(DatagramSocket ds, byte[] obuf) {
		
		// setup THX receiver
		byte[] buf = new byte[TtftpPacket.PACKET_SIZE];
		DatagramPacket dpIn;
		try {			
			dpIn = new DatagramPacket(buf, buf.length, req.getSocketAddress());
		} catch (SocketException e) {
			System.err.println("Exception: " + e);
			return false;
		}

		// wait for a THX to arrive (5 times)
		for (int i = 0; i < 5; i++) {
			try {
				// try to send buffer
				ds.send(new DatagramPacket(obuf, obuf.length, req
						.getSocketAddress()));
				
				// try to receive a THX
				ds.receive(dpIn);

				// make sure the sequence numbers match
				long inSeqNo = TtftpPacketFactory.createPacketFromBuff(
						req.getLength(), buf).getSeqNo();
				if (inSeqNo != seqNo)
					return false;
				else
					return true;
				// THX didn't arrive in time
			} catch (SocketTimeoutException e) {
				continue;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	public void run() {
		TtftpPacket packet = new TtftpPacket(req.getLength(), req.getData());
		System.out.println("Someone is requesting the file:"
				+ packet.getFileName());

		sendfile(packet);
	}

	public TtftpServerWorker(DatagramPacket req) {
		this.req = req;
	}
}
