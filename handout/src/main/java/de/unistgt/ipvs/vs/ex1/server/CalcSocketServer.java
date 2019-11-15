package de.unistgt.ipvs.vs.ex1.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Extend the run-method of this class as necessary to complete the assignment.
 * You may also add some fields, methods, or further classes.
 */
public class CalcSocketServer extends Thread {
	private ServerSocket srvSocket;
	private int port;

	public CalcSocketServer(int port) {
	    this.port = port;
        this.srvSocket = null;
	}
	
	@Override
	public void interrupt() {
		try {
			if (srvSocket != null) srvSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
           
		if (this.port <= 0) {
			System.err.println("Wrong number of arguments.\nUsage: SocketServer <listenPort>\n");
			System.exit(-1);
		}
        try {
            this.srvSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
		// Start listening server socket ..
        System.out.println("S:Listening...");
        while(true) {
            try {
                Socket socket = srvSocket.accept();
                System.out.println("New Client is connected, begin a new Thread.");
                CalculationSession session = new CalculationSession(socket);
                new Thread(session).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


	}

    public void waitUnitlRunnig(){
        while(this.srvSocket == null){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    public static void main(String[] args) {
        CalcSocketServer cSrv = new CalcSocketServer(12346);
        cSrv.start();
        cSrv.waitUnitlRunnig();
    }
}