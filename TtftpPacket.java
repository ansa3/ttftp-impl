/**
 * 
 * @author Joel
 *
 */
public class TtftpPacket {
	private int length;
	private int opCode;
	private long seqNo = -1;
	private byte[] data;
	private String fileName;

	public static final int GET = 1;
	public static final int FAIL = 2;
	public static final int DATA = 3;
	public static final int THX = 4;
	public static final int EOF = 5;

	public static final int PACKET_SIZE = 576;
	public static final int DATA_START = 5;

	public TtftpPacket(int length, byte[] buff) {
		opCode = buff[0];
		this.length = length;

		switch (opCode) {
		case GET:
			parseGET(length, buff);
			break;
		case DATA:
			parseDATA(length, buff);
			break;
		case THX:
			parseTHX(buff);
			break;
		case EOF:
			parseEOF(buff);
			break;
		}
	}

	public TtftpPacket(int length, int opCode, long seqNo, byte[] data,
			String fileName) {		
		this.opCode = opCode;

		switch (opCode) {
		case GET:
			this.fileName = fileName;
			this.length = fileName.length() + 1;
			break;
		case DATA:
			this.seqNo = seqNo;
			this.data = data;
			this.length = length + 5;
			break;
		case THX:
			this.seqNo = seqNo;
			this.length = 5;
			break;
		case EOF:
			this.seqNo = seqNo;
			this.length = 5;
			break;
		case FAIL:
			this.length = 1;
			break;
		}
	}

	private static void store_long(byte[] array, int off, long val) {
		array[off + 0] = (byte) ((val & 0xff000000) >> 24);
		array[off + 1] = (byte) ((val & 0x00ff0000) >> 16);
		array[off + 2] = (byte) ((val & 0x0000ff00) >> 8);
		array[off + 3] = (byte) ((val & 0x000000ff));
	}

	private static long extract_long(byte[] array, int off) {
		long a = array[off + 0] & 0xff;
		long b = array[off + 1] & 0xff;
		long c = array[off + 2] & 0xff;
		long d = array[off + 3] & 0xff;
		return (a << 24 | b << 16 | c << 8 | d);
	}

	private void parseGET(int length, byte[] buff) {

		fileName = new String(buff, 1, length - 1);
	}

	private void parseDATA(int length, byte[] buff) {
		byte[] data = new byte[length - 5];

		seqNo = extract_long(buff, 1);

		System.arraycopy(buff, 5, data, 0, data.length);
		this.data = data;
	}

	private void parseTHX(byte[] buff) {
		seqNo = extract_long(buff, 1);
	}

	private void parseEOF(byte[] buff) {
		seqNo = extract_long(buff, 1);
	}

	public byte[] getBuffer() {
		byte[] buff = new byte[length];
		int i = 0;

		if (opCode == 0)
			throw new IllegalArgumentException();

		buff[i] = (byte) opCode;
		i++;

		if (seqNo != -1) {
			store_long(buff, i, seqNo);
			i += 4; // longs take up 4 bytes
		}

		if (fileName != null) {
			// TODO: catch a too longer string error
			byte[] file = fileName.getBytes();

			System.arraycopy(file, 0, buff, i, file.length);
		}

		if (data != null) {
			System.arraycopy(data, 0, buff, i, length - 5);
		}

		return buff;
	}

	public int getOpCode() {
		return this.opCode;
	}

	public long getSeqNo() {
		return this.seqNo;
	}

	public String getFileName() {
		return this.fileName;
	}

	public byte[] getData() {
		return this.data;
	}
	
	public int getLength() {
		return this.length;
	}

	public boolean isGET() {
		if (opCode == GET)
			return true;
		return false;
	}

	public boolean isFAIL() {
		if (opCode == FAIL)
			return true;
		return false;
	}

	public boolean isDATA() {
		if (opCode == DATA)
			return true;
		return false;
	}

	public boolean isTHX() {
		if (opCode == THX)
			return true;
		return false;
	}

	public boolean isEOF() {
		if (opCode == EOF)
			return true;
		return false;
	}

}
