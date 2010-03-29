/**
 * Factory class to create each of the Ttftp packet types.
 * 
 * @author Joel
 * 
 */
public class TtftpPacketFactory {

	public static TtftpPacket createPacketGET(String fileName) {
		return new TtftpPacket(0, TtftpPacket.GET, 0, null, fileName);
	}

	public static TtftpPacket createPacketFAIL() {
		return new TtftpPacket(0, TtftpPacket.FAIL, 0, null, null);
	}

	public static TtftpPacket createPacketDATA(int length, long seqNo,
			byte[] data) {
		return new TtftpPacket(length, TtftpPacket.DATA, seqNo, data, null);
	}

	public static TtftpPacket createPacketTHX(long seqNo) {
		return new TtftpPacket(0, TtftpPacket.THX, seqNo, null, null);
	}

	public static TtftpPacket createPacketEOF(long seqNo) {
		return new TtftpPacket(0, TtftpPacket.EOF, seqNo, null, null);
	}

	public static TtftpPacket createPacketFromBuff(int length, byte[] buff) {
		return new TtftpPacket(length, buff);
	}
}
