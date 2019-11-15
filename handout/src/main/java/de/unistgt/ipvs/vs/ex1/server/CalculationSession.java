package de.unistgt.ipvs.vs.ex1.server;

import de.unistgt.ipvs.vs.ex1.common.ICalculation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Pattern;

/**
 * Add fields and methods to this class as necessary to fulfill the assignment.
 */
public class CalculationSession implements Runnable {
    private Socket socket;
    private ObjectInputStream oisIn;
    private ObjectOutputStream oosOut;

    private boolean needRES;
    private int calState;
    private boolean startCal;  // flag
    private boolean calStateChange;

    private CalculationImpl res;
    private Queue<String> queue = new LinkedList<String>();

    public CalculationSession(Socket socket) {
        this.socket = socket;
        this.needRES = false;
        this.calState = 0;
        this.startCal = false;
        this.calStateChange = false;
        try {
            this.oisIn = new ObjectInputStream(this.socket.getInputStream());
            this.oosOut = new ObjectOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            this.res = new CalculationImpl();
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }
    public void run() {
        try {
            //System.out.println("testrun");
            this.oosOut.writeObject(msgCreat("RDY", null));
            //oosOut.flush();
            String msg = " ";
            while(true){
                msg = (String) this.oisIn.readObject();
                if("DISCONNECTED".equals(msg)) return;
                this.oosOut.writeObject(msgCreat("OK", null));
                msgHandle(msg);
            }

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

    }
    public void msgHandle(String msg) throws IOException {
        msgFilterSplitQueue(msg);
        compute();
        this.oosOut.writeObject(msgCreat("FIN", null));
        System.out.println("finished");
    }

    public String msgCreat(String Ack, String content) {
        String msg = " ";
        Ack = Ack.toUpperCase();
        switch (Ack) {
            case "RDY": {
                msg = "<08:RDY>";
                break;
            }
            case "FIN": {
                msg = "<08:FIN>";
                break;
            }
            case "OK": {
                if(content!=null)
                    msg = "<"+(content.length()+8)+":OK "+content+">";
                else msg = "<07:OK>";
                break;
            }
            case "ERR": {
                if(content!=null)
                    msg = "<"+(content.length()+9)+":ERR "+content+">";
                else msg = null;
                break;
            }
            case "RES": {
                if(content!=null)
                    msg = "<"+(content.length()+12)+":OK RES "+content+">";
                else msg = null;
                break;
            }
            default: System.out.println("Ack error!");break;
        }
        return msg;
    }

    public void msgFilterSplitQueue(String msg){
        String[] strArray = null;
        String content = msg.substring(msg.indexOf(":")+1, msg.indexOf(">"));
        content = content.trim();
        strArray = content.split("\\s+");
        for (String p : strArray){
            this.queue.offer(p);
        }
    }

    public void compute() throws IOException {
        if(queue.isEmpty())
            return;

        while(!queue.isEmpty()) {
            String p = queue.poll();
            updateOperation(p);
            if(calStateChange) {
                startCal = true;
                if(needRES) {
                    this.oosOut.writeObject(msgCreat("RES", Integer.toString(res.getResult())));
                    needRES = false;
                    continue;
                }
                this.oosOut.writeObject(msgCreat("OK", p));
            } else if(!calStateChange) {
                if(!startCal) {
                    this.oosOut.writeObject(msgCreat("ERR", p));
                } else {
                    if(isInteger(p)) {
                        this.oosOut.writeObject(msgCreat("OK", p));
                        calculate(calState, p);
                    }
                    else {
                        this.oosOut.writeObject(msgCreat("ERR", p));
                    }
                }
            }
        }
    }

    public void updateOperation(String p) {
        p = p.toUpperCase();
        switch (p){
            case "ADD": this.calState = 1; this.calStateChange = true; break;
            case "SUB": this.calState = 2; this.calStateChange = true; break;
            case "MUL": this.calState = 3; this.calStateChange = true; break;
            case "RES": needRES = true; this.calStateChange = true; break;
            default: this.calStateChange = false; break;
        }

    }

    public boolean isInteger(String p) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(p).matches();
    }

    public void calculate(int calState, String p){
        try {
            int val = Integer.parseInt(p);
            switch (calState) {
                case 1: res.add(val); break;
                case 2: res.subtract(val); break;
                case 3: res.multiply(val); break;
                default: break;
            }
        } catch (RemoteException e){
            e.printStackTrace();
        }

    }
}