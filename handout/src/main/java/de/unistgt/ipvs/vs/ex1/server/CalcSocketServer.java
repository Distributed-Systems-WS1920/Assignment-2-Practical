package de.unistgt.ipvs.vs.ex1.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;

/**
 * Extend the run-method of this class as necessary to complete the assignment.
 * You may also add some fields, methods, or further classes.
 */
public class CalcSocketServer extends Thread {
	private ServerSocket srvSocket;
	private Socket cliSocket;
	private int port;

	public CalcSocketServer(int port) {
		this.srvSocket = null;
		this.cliSocket = null;
		this.port = port;
	}

	@Override
	public void interrupt() {
		try {
			if (srvSocket != null)
				srvSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {	
		// If invalid port -> crash
		if (port <= 0) {
			System.exit(-1);
		}

		try {
			// Start new server socket at given port
			srvSocket = new ServerSocket(port);
		    System.out.println("Server-Socket opened at port: " + port);
		} catch (IOException e) {
			// Crash if you cant open server socket
			System.err.println("Could not open Server Socket at port: " + port);
			e.printStackTrace();
			System.exit(-1);
		}

		// Start listening server socket ..
		while (true) {
			try {
				// Wait for incoming clients
				cliSocket = srvSocket.accept();
			} catch (IOException e) {
				// Can't establish connection with new client
				e.printStackTrace();
				continue;
			}
			
			// Create and start a new session for new client
			System.out.println("Connected to new client");
			CalculationSession newSession;
			try {
				newSession = new CalculationSession(cliSocket);
				newSession.start();
				System.out.println("Started a new Session for client");
			} catch (RemoteException e) {
				// Print remote exception for debugging purposes
				e.printStackTrace();
			}
		}
	}

	public void waitUnitlRunnig() {
		while (this.srvSocket == null) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException ex) {
			}
		}
	}
}