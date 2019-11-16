package de.unistgt.ipvs.vs.ex2.server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implement the run-method of this class to complete the assignment. You may
 * also add some fields or methods.
 */
public class CalcRmiServer extends Thread {
	private String regHost;
	private String objName;
	String url; // Please use this variable to bind the object.

	public CalcRmiServer(String regHost, String objName) {
		this.regHost = regHost;
		this.objName = objName;
		this.url = "rmi://" + regHost + "/" + objName;
	}

	@Override
	public void run() {
		if (regHost == null || objName == null) {
			System.err.println("registryHost or objectName is not set");
			return;
		}

		try {
			try {
				// Try to create registry
				LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			} catch (ExportException e) {
				System.out.println("Cannot create Registry. Assume that one already exist.");
			}
			// Bind our service to the given name
			Naming.rebind(url, new CalculationImplFactory());
		} catch (Exception e) {
			System.err.println("Object binding failed!");
			e.printStackTrace();
		}
	}

	public void stopServer() {
		try {
			Naming.unbind(url);
		} catch (RemoteException ex) {
			Logger.getLogger(CalcRmiServer.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NotBoundException ex) {
			Logger.getLogger(CalcRmiServer.class.getName()).log(Level.SEVERE, null, ex);
		} catch (MalformedURLException ex) {
			Logger.getLogger(CalcRmiServer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
