package segmentedfilesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;

public class Main {
    
    public static void main(String[] args) throws IOException {
    	
    	int port = 6014;
    	byte[] requestBuf = new byte[256];
    	
    	
    	InetAddress address = InetAddress.getByName("heartofgold.morris.umn.edu");
    	DatagramSocket clientSocket = new DatagramSocket(port);
    	DatagramPacket requestPacket = new DatagramPacket(requestBuf, 0, address, port);
    	
    	clientSocket.send(requestPacket);
    	
    	ArrayList<SNPair> 
    	fileOne = new ArrayList<SNPair>(),	
    	fileTwo = new ArrayList<SNPair>(),				 			
    	fileThree = new ArrayList<SNPair>();
    	
    	int fileOnePacketSize = Integer.MAX_VALUE;
    	int fileTwoPacketSize = Integer.MAX_VALUE;
    	int fileThreePacketSize = Integer.MAX_VALUE;
    	
    	System.out.println("Starting Client. . .");
    	
    	while (fileOnePacketSize != fileOne.size() || fileTwoPacketSize != fileTwo.size() || fileThreePacketSize != fileThree.size()) {
    		System.out.println("Running. . .");
    		
    		byte[] checkBuf = new byte[1028];
        	
        	DatagramPacket checkPacket = new DatagramPacket(checkBuf, 0, checkBuf.length);
    		
    		clientSocket.receive(checkPacket);
    		checkBuf = checkPacket.getData();
    		byte[] toAdd = checkBuf.clone();
    		
    		int statusByte = checkBuf[0];
    		int fileID = Math.abs(checkBuf[1]);
    		
    		
    		//System.out.println("Status byte: " + statusByte + "    packet length: " + checkPacket.getLength());

    		
    		if (fileNumber(fileID) == 0) {
    			
    			
    			fileOne.add(makeSNPair(statusByte, toAdd, checkPacket));
    			
    			if (statusByte == 3) {
    				fileOnePacketSize = makePacketNumber(checkBuf) + 2;
    				//System.out.println("File One Packet Size: " + fileOnePacketSize);
    				
    			}
    			//System.out.println("File One size: " + fileOne.size() + "    :   File One Packet Size: " + fileOnePacketSize);
    		} else if (fileNumber(fileID) == 1) {
    			
    			
    			//System.out.println(makePacketNumber(checkBuf));
    			//System.out.println("Status byte: " + statusByte + "  :  Packet number: " + checkBuf[2] + " : " + checkBuf[3]);
    			fileTwo.add(makeSNPair(statusByte, toAdd, checkPacket));
    
    			if (statusByte == 3) {
    				fileTwoPacketSize = makePacketNumber(checkBuf) + 2;
    				//System.out.println("File Two Packet Size: " + fileTwoPacketSize);
    			}
    			//System.out.println("File Two size: " + fileTwo.size() + "     :  File Two Packet Size: " + fileTwoPacketSize);
    		} else if (fileNumber(fileID) == 2){
    			
    			fileThree.add(makeSNPair(statusByte, toAdd, checkPacket));
    			
    			if (statusByte == 3) {
    				fileThreePacketSize = makePacketNumber(checkBuf) + 2;
    				//System.out.println(fileThreePacketSize);
    			}
    			//System.out.println("File Three size: " + fileThree.size() + "       :  File Three Packet Size: " + fileThreePacketSize);
    			
    		}
    		
    	}
    	
    	Collections.sort(fileOne);
    	Collections.sort(fileTwo);
    	Collections.sort(fileThree);
    	
    	File one = writeFileName(fileOne);
    	File two = writeFileName(fileTwo);
    	File three = writeFileName(fileThree);
    	
    	writeFileData(fileOne, one);
    	writeFileData(fileTwo, two);
    	writeFileData(fileThree, three);
    	//printArrayListBytes(fileOne);
    	
    	System.out.println("-Completed");
    	
    	clientSocket.close();
    	
    }
    
    private static void printByteArray(byte[] arr) {
    	System.out.println("Printing Byte Array: ");
    	System.out.println();
    	for (int i = 4; i < arr.length; i++) {
    		System.out.println(arr[i]);
    	}
    	System.out.println();
    	System.out.println("Finished Printing");
    }
    
    private static void printSNPairs(ArrayList<SNPair> arr) {
    	System.out.println("Printing File: ");
    	for (int i = 0; i < arr.size(); i++) {
    		System.out.println(i + ":   StatusByte: " + arr.get(i).getStatusByte() + " :   PacketNumber: " + arr.get(i).getPacketNumber());
    		
    	}
    }
    
    
    private static int fileNumber(int fileID) {
    	int toReturn = -1;
    	
    	toReturn = fileID % 3;
    	
    	return toReturn;
    }
    

	private static SNPair makeSNPair(int statusByte, byte[] checkBuf, DatagramPacket dp) {
		SNPair toReturn;
		int packetNumber = makePacketNumber(checkBuf);
		
		if (statusByte == 1) {
			toReturn = new SNPair(statusByte, packetNumber, checkBuf, dp.getLength());
		} else if (statusByte == 3) {
			toReturn = new SNPair(statusByte, packetNumber, checkBuf, dp.getLength());
			
		} else {
			toReturn = new SNPair(statusByte, -1, checkBuf, dp.getLength());

		}
		
		return toReturn;
	}


	private static int makePacketNumber(byte[] checkBuf) {
		int checkByte = checkBuf[3];
		int toReturn = 0;
		
		if (checkByte > -1) {
			toReturn = checkByte;
		} else if (checkByte < -1) {
			toReturn = checkByte + 256;
		}
		return toReturn;
	}


	public static class SNPair implements Comparable<SNPair> {
		
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
	

    private static File writeFileName(ArrayList<SNPair> arr) {
    	File toReturn;
    	String fileName = "";
    	byte[] header = arr.get(0).getByteArray();

    	for (int i = 2; i < arr.get(0).getByteArrayLen(); i++) {
    		
    		if (header[i] == 0) {
    			break;
    		}

    		fileName = fileName + (char) header[i];

    	}

    	System.out.println("File Name is: " + fileName);
    	toReturn = new File(fileName);
    	
		return toReturn;

    }
    
    private static void printArrayListBytes(ArrayList<SNPair> arr) {
    	for (int i = 1; i < arr.size(); i++) {
    		printByteArray(arr.get(i).getByteArray());
    	}
    }
    
    private static void writeFileData(ArrayList<SNPair> arr, File file) throws IOException {
    	FileOutputStream fileOutput = new FileOutputStream(file);
    	byte[] holderBuf = new byte[1028];
    	int packetLen;
    	
    	for (int i = 1; i < arr.size(); i++) {
    		holderBuf = arr.get(i).getByteArray();
    		packetLen = arr.get(i).getByteArrayLen();
    		
    		
    		fileOutput.write(holderBuf, 4, packetLen - 4);
    	}
    	
    	fileOutput.flush();
    	fileOutput.close();
    }
    
    
    
    
    
    
    
    
    
    
    

}
