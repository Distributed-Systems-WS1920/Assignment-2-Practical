package de.unistgt.ipvs.vs.ex1.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Implement the connectTo-, disconnect-, and calculate-method of this class as
 * necessary to complete the assignment. You may also add some fields or
 * methods.
 */
public class CalcSocketClient {
	private Socket cliSocket;
	private int rcvdOKs; // --> Number of valid message contents
	private int rcvdErs; // --> Number of invalid message contents
	private int calcRes; // --> Calculation result (cf. 'RES')
	private ObjectOutputStream out;
	private ObjectInputStream in;

	/**
	 * Constructor that initiates the object
	 */
	public CalcSocketClient() {
		this.cliSocket = null;
		this.rcvdOKs = 0;
		this.rcvdErs = 0;
		this.calcRes = 0;
		in = null;
		out = null;
	}

	public int getRcvdOKs() {
		return rcvdOKs;
	}

	public int getRcvdErs() {
		return rcvdErs;
	}

	public int getCalcRes() {
		return calcRes;
	}

	/**
	 * This method connects the Client to the Server with IP + Port. Also this
	 * method waits for the RDY message from Server until returning a successfull
	 * connection.
	 * 
	 * @param srvIP
	 * @param srvPort
	 * @return connectionEstablished
	 */
	public boolean connectTo(String srvIP, int srvPort) {

		try {
			// Connect to server with IP + Port
			cliSocket = new Socket(srvIP, srvPort);
			// Setup In-/OutputStream for connection
			out = new ObjectOutputStream(cliSocket.getOutputStream());
			in = new ObjectInputStream(cliSocket.getInputStream());
		} catch (UnknownHostException e) {
			// Return false if IP of server cannot be determined
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// Return false if I/O error occurs
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// After connecting, wait for server to become ready
		waitForReady();

		// Return that connection was successfully established
		return true;
	}

	/**
	 * This method forces the client to wait until the Server signals that it's
	 * ready to receive messages. This way, the client won't send any messages too
	 * early. (Ready message = <08:RDY>)
	 */
	private void waitForReady() {
		// Wait until ready message arrives
		while (true) {
			String message = "";
			try {
				// Read incomin messages
				message = (String) in.readObject();
			} catch (IOException | ClassNotFoundException e) {
				// Print IO Exceptions for debug purposes and continue processing
				e.printStackTrace();
			}

			// Break if ready message is received
			if (message.equals("<08:RDY>")) {
				return;
			}

		}
	}

	/**
	 * This method disconnects the client
	 * 
	 * @return disconnectSuccessfull (true/false)
	 */
	public boolean disconnect() {
		try {
			// Close In-/OutputStream and Socket
			out.writeObject("DISCONNECT");
			in.close();
			out.close();
			cliSocket.close();
		} catch (IOException e) {
			// Return false if I/O error occurs
			return false;
		}
		// Return that disconnect was successful
		return true;
	}

	/**
	 * This method uses the incoming messages from the server to calculate the
	 * result, #Errors and #OKs.
	 * 
	 * @param request
	 * @return calculationFinished (true/false)
	 */
	public boolean calculate(String request) {

		// Send message to Server
		try {
			out.writeObject(request);
		} catch (IOException e1) {
			// Print exception for debugging purposes
			e1.printStackTrace();
			return false;
		}

		while (true) {
			String message = "";
			try {
				// read message
				message = (String) in.readObject();
			} catch (IOException | ClassNotFoundException e) {
				// Print IO Exceptions for debug purposes and continue processing
				e.printStackTrace();
			}

			// Count received 'OK' messages
			if (message.contains("OK")) {
				rcvdOKs++;
			}

			// Count received 'ERR' messages
			if (message.contains("ERR")) {
				rcvdErs++;
			}

			// Set Result
			if (message.contains("RES")) {
				// Read the last content of RES message (the result) and store it into calcRes
				String[] splitMsg = message.split(">")[0].split(" ");
				calcRes = Integer.valueOf(splitMsg[splitMsg.length - 1]);
			}

			// End calculation if 'FIN' message is received
			if (message.equals("<08:FIN>")) {
				return true;
			}
		}
	}
}
