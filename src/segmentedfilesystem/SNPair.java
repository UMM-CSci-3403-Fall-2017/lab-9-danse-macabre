package segmentedfilesystem;

// Class used to store information from the received DatagramPackets to use to construct the three files
public class SNPair implements Comparable<SNPair> {

	private int statusByte;
	private int packetNumber;
	private byte[] byteArray;
	private int byteArrayLen;


	public SNPair(int statusByte, int packetNumber, byte[] byteArray, int byteArrayLen) {
		this.statusByte = statusByte;
		this.packetNumber = packetNumber;
		this.byteArray = byteArray;
		this.byteArrayLen = byteArrayLen;
	}

	public int getStatusByte() {
		return statusByte;
	}

	public int getPacketNumber() {
		return packetNumber;
	}

	public byte[] getByteArray() {
		return byteArray;
	}

	public int getByteArrayLen() {
		return byteArrayLen;
	}

	@Override
	public int compareTo(SNPair o) {
		if (this.getPacketNumber() < o.getPacketNumber()) {
			return -1;
		} else if (this.getPacketNumber() > o.getPacketNumber()) {
			return 1;
		}

		return 0;
	}
}
