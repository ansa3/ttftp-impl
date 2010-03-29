import java.net.*;
import java.io.*;

public class TtftpClient {

	private static long seqNo = 0;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// check all the args have been given
		if (args.length < 3) {
			System.out.println("Usage: TtftpClient server port filename");
			System.exit(1);
		}

		try {
			InetAddress IP = InetAddress.getByName(args[0]);
			DatagramSocket ds = new DatagramSocket();
			TtftpPacket packet = TtftpPacketFactory.createPacketGET(args[2]);
			FileOutputStream in = new FileOutputStream(new File("in/"
					+ packet.getFileName()));

			// create and send a GET packet
			byte[] buff = packet.getBuffer();
			DatagramPacket dp = new DatagramPacket(buff, buff.length, IP,
					Integer.parseInt(args[1]));
			ds.send(dp);

			for (;;) {
				// receive a packet
				buff = new byte[576];
				dp = new DatagramPacket(buff, buff.length);
				ds.receive(dp);
				TtftpPacket tp = TtftpPacketFactory.createPacketFromBuff(dp
						.getLength(), buff);

				if (tp.isFAIL()) {
					System.out
							.println("The server could not send the specified file");
					System.exit(1);
				}

				if (tp.isDATA()) {
					// check sequence number
					if (tp.getSeqNo() == seqNo) {					
						// write the data to disk
						in.write(tp.getData(), 0, dp.getLength()
								- TtftpPacket.DATA_START);
	
						// send back a thx
						sendTHX(ds, dp);	
						
						// incr seqNo
						seqNo++;
					// the THX must have been lost
					} else {
						// send back another THX
						sendTHX(ds, dp);	
					}					
				}

				if (tp.isEOF()) {
					System.out.println("The file was received");

					// send back a thx
					TtftpPacket p = TtftpPacketFactory.createPacketTHX(seqNo);
					ds.send(new DatagramPacket(p.getBuffer(),
							p.getBuffer().length, dp.getSocketAddress()));

					// terminate and return
					in.close();
					break;
				}

			}

		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (SocketException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}

	private static void sendTHX(DatagramSocket ds, DatagramPacket dp) {
		TtftpPacket p = TtftpPacketFactory.createPacketTHX(seqNo);
		try {
			ds.send(new DatagramPacket(p.getBuffer(), p.getBuffer().length, dp
					.getSocketAddress()));
		} catch (Exception e) {
			System.err.println("Exception: " + e);
			System.exit(1);
		}
	}
}
