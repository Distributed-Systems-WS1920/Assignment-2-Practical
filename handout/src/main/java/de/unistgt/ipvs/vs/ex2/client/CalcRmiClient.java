package de.unistgt.ipvs.vs.ex2.client;

import de.unistgt.ipvs.vs.ex2.common.ICalculation;
import de.unistgt.ipvs.vs.ex2.common.ICalculationFactory;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Collection;

/**
 * Implement the getCalcRes-, init-, and calculate-method of this class as
 * necessary to complete the assignment. You may also add some fields or
 * methods.
 */
public class CalcRmiClient {
	private ICalculation calc = null;

	public CalcRmiClient() {
		this.calc = null;
	}

	public int getCalcRes() {
		try {
			return calc.getResult();
		} catch (RemoteException | NullPointerException e) {
			// Print error for debugging purposes
			e.printStackTrace();
			// Return -1 if something went wrong
			return -1;
		}
	}

	public boolean init(String url) {
		if(this.calc != null) {
			// Can't init twice
			return false;
		}
		
		try {
			ICalculationFactory factory = (ICalculationFactory) Naming.lookup(url);
			this.calc = factory.getSession();
			
		} catch (Exception e) {
			// Log error if something went wrong
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean calculate(CalculationMode calcMode, Collection<Integer> numbers) {
		try {
			// Perform given operation for every number in collection
			for (Integer number : numbers) {
				// Perform appropriate operation according to calcMode
				switch (calcMode) {
				case ADD:
					calc.add(number);
				    break;
				case SUB:
					calc.subtract(number);
				    break;
				case MUL:
					calc.multiply(number);
					break;
				}
			}
		} catch (RemoteException e) {
			// Print remote exception for debugging purposes
			e.printStackTrace();
			// Return that something went wrong
			return false;
		}
		// Return success
		return true;
	}
}
