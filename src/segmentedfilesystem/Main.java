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
    	
    	// Variables used to send initial requestPacket to begin receiving DatagramPackets from the server
    	int port = 6014;
    	byte[] requestBuf = new byte[256];
    	InetAddress address = InetAddress.getByName("heartofgold.morris.umn.edu");
    	DatagramSocket clientSocket = new DatagramSocket(port);
    	DatagramPacket requestPacket = new DatagramPacket(requestBuf, 0, address, port);
    	clientSocket.send(requestPacket);
    	
    	// Three ArrayLists of <SNPair> which denote the three files that we are receiving
    	ArrayList<SNPair> fileOne = new ArrayList<SNPair>(),	
    					  fileTwo = new ArrayList<SNPair>(),				 			
    					  fileThree = new ArrayList<SNPair>();
    	
    	// Variables that show how big each file will be. Decided when we receive a packet with a statusByte of 3
    	int fileOnePacketSize = Integer.MAX_VALUE,
    		fileTwoPacketSize = Integer.MAX_VALUE,
    		fileThreePacketSize = Integer.MAX_VALUE;
    	
    	// Runs until all the packets are received
    	while (fileOnePacketSize != fileOne.size() || fileTwoPacketSize != fileTwo.size() || fileThreePacketSize != fileThree.size()) {
    		
    		// Variables for receiving packet chunks of 1K plus the 4 bytes used for the statusByte, fileNumber, and packetNumber
    		byte[] checkBuf = new byte[1028];
        	DatagramPacket checkPacket = new DatagramPacket(checkBuf, 0, checkBuf.length);
    		clientSocket.receive(checkPacket);
    		checkBuf = checkPacket.getData();
    		byte[] toAdd = checkBuf.clone();
    		
    		// Get the statusByte and fileID of the current packet that was received
    		int statusByte = checkBuf[0];
    		int fileID = Math.abs(checkBuf[1]);
    		
    		if (fileNumber(fileID) == 0) {
    			fileOne.add(makeSNPair(statusByte, toAdd, checkPacket));
    			
    			if (statusByte == 3) {
    				fileOnePacketSize = makePacketNumber(checkBuf) + 2;
    			}
    			
    		} else if (fileNumber(fileID) == 1) {
    			fileTwo.add(makeSNPair(statusByte, toAdd, checkPacket));
    
    			if (statusByte == 3) {
    				fileTwoPacketSize = makePacketNumber(checkBuf) + 2;
    				
    			}
    			
    		} else if (fileNumber(fileID) == 2){
    			fileThree.add(makeSNPair(statusByte, toAdd, checkPacket));
    			
    			if (statusByte == 3) {
    				fileThreePacketSize = makePacketNumber(checkBuf) + 2;
    				
    			}
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
    	
    	clientSocket.close();
    	
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
    	
    	toReturn = new File(fileName);
    	
		return toReturn;

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
