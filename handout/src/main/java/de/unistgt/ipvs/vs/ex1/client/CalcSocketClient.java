package de.unistgt.ipvs.vs.ex1.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
/**
 * Implement the connectTo-, disconnect-, and calculate-method of this class
 * as necessary to complete the assignment. You may also add some fields or methods.
 */
public class CalcSocketClient {
	private Socket cliSocket;
    private ObjectInputStream oisIn;
    private ObjectOutputStream oosOut;
	private int    rcvdOKs;		// --> Number of valid message contents
	private int    rcvdErs;		// --> Number of invalid message contents
	private int    calcRes;		// --> Calculation result (cf.  'RES')

    private boolean shutdown;
    private Queue<String> queue = new LinkedList<String>();
	
	public CalcSocketClient() {
		this.cliSocket = null;
		this.rcvdOKs   = 0;
		this.rcvdErs   = 0;
		this.calcRes   = 0;
		this.shutdown = false;
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

	public boolean connectTo(String srvIP, int srvPort) {
               
		//Solution here
		try{
			this.cliSocket = new Socket(srvIP, srvPort);
            System.out.println("Sending request to Socket Server");
            this.oosOut = new ObjectOutputStream(cliSocket.getOutputStream());
            this.oisIn = new ObjectInputStream(cliSocket.getInputStream());

            String msg;
            while(true){
                msg = (String)oisIn.readObject();
                msgFilterSplitQueue(msg);
                if(!queue.isEmpty()) {
                    if(queue.poll().equals("RDY"))
                        System.out.println("Received RDY");
                        break;
                }
            }

		} catch(ClassNotFoundException | IOException e) {
            e.printStackTrace();
		}

		
		return true;
	}

	public boolean disconnect() {
               
	    //Solution here
        try {
            this.oosOut.writeObject("DISCONNECTED");
            oosOut.close();
            oisIn.close();
            this.cliSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

		return true;
	}

	public boolean calculate(String request) {
               
		if (cliSocket == null) {
			System.err.println("Client not connected!");
			return false;
		}

		//Solution here
        try {

            oosOut.writeObject(request);
            //oosOut.flush();
            System.out.println("client send request");
            String msg;
            while(true) {
                msg = (String) oisIn.readObject();
                msgFilterSplitQueue(msg);
                decodeMsg();
                if(this.shutdown)
                    break;
            }
            this.shutdown = false;

        } catch(ClassNotFoundException|IOException e) {
            e.printStackTrace();
        }


        return true;
	}

	public void decodeMsg() {
		if(!queue.isEmpty()) {
		    String p = queue.poll();
            if(p.equals("OK")) {
                this.rcvdOKs++;
                if(queue.isEmpty())
                    return;
                else {
                    if(queue.poll().equals("RES"))
                        this.calcRes = Integer.parseInt(queue.poll());
                }
            }
            if(p.equals("ERR")) {
                queue.remove();
                this.rcvdErs++;
            }
            if(p.equals("FIN")) {
                this.shutdown = true;
            }
        }
    }

    public void msgFilterSplitQueue(String msg){
        String[] strArray = null;
        String content = msg.substring(msg.indexOf(":")+1, msg.indexOf(">"));
        strArray = content.split(" ");
        for (String e : strArray){
            this.queue.offer(e);
        }
    }

    public static void main(String[] args) {
        CalcSocketClient cCli = new CalcSocketClient();
        cCli.connectTo("localhost", 12346);

        //String req1 = "ADD 1 2 3 SUB 3 2 0";
        //cCli.calculate("<" + (req1.length() + 5) + ":" + req1 + ">");
        //cCli.calculate("<08:rEs>");

        String req31 = "  MUL  1   ASM  ADD ABC 10    5  SUB 100 ADD10   ADD";
        cCli.calculate("24 foo 42 <" + (req31.length() + 5) + ":" + req31 + ">");

        String req32 = "60 4 MUL -2 RES  ";
        cCli.calculate("a faq 23 <" + (req32.length() + 5) + ":" + req32 + "> bla 42 ");

        System.out.println(cCli.getCalcRes());
        System.out.println(cCli.getRcvdOKs());


        cCli.disconnect();
    }
}

