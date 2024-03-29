package de.unistgt.ipvs.vs.ex2.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import de.unistgt.ipvs.vs.ex2.common.ICalculation;
import de.unistgt.ipvs.vs.ex2.common.ICalculationFactory;

/**
 * Change this class (implementation/signature/...) as necessary to complete the
 * assignment. You may also add some fields or methods.
 */

public class CalculationImplFactory extends UnicastRemoteObject implements ICalculationFactory {
	protected CalculationImplFactory() throws RemoteException {
		super();
	}

	private static final long serialVersionUID = 8409100566761383094L;

	/**
	 * This method returns a new Calculation Session
	 */
	@Override
	public ICalculation getSession() throws RemoteException {
		return new CalculationImpl();
	}

}
