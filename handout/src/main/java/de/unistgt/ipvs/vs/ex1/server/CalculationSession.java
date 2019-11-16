package de.unistgt.ipvs.vs.ex1.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Add fields and methods to this class as necessary to fulfill the assignment.
 */
public class CalculationSession implements Runnable {

	private Socket cliSocket; // --> Socket of the connected client of current thread
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private CalculationImpl calc;
	private String calcOp;
	// Pattern for messages ("...<dd:message>...)
	Pattern patternMessage = Pattern.compile(".*(<(\\d{2}):(.+?)>).*", Pattern.CASE_INSENSITIVE);

	/**
	 * Constructor that initializes the Session-Object.
	 * 
	 * @param cliSocket
	 * @throws RemoteException
	 */
	public CalculationSession(Socket cliSocket) throws RemoteException {
		this.cliSocket = cliSocket;
		this.calcOp = null;
		this.calc = new CalculationImpl();
	}

	/**
	 * Method that gets executed when the thread is started.
	 */
	public void run() {
		try {
			// Prepare input and output streams
			out = new ObjectOutputStream(cliSocket.getOutputStream());
			in = new ObjectInputStream(cliSocket.getInputStream());
			// Send READY-Message to client
			out.writeObject("<08:RDY>");

			// Process incoming messages
			String message = "";
			while (true) {
				message = (String) in.readObject();
				// Catch clients disconnect message to end session
				if (message.equals("DISCONNECT")) {
					in.close();
					out.close();
					cliSocket.close();
					return;
				}
				// Respond with OK to each message (besides disconnect)
				out.writeObject("<07:OK>");
				// Process message and respond to client
				processMessage(message);
			}
		} catch (IOException | ClassNotFoundException e) {
			// End session if failures occur
			e.printStackTrace();
			return;
		}
	}

	/**
	 * This method processes incoming messages by checking it's format, content and
	 * sending appropriate responses to the client.
	 * 
	 * @param message
	 * @throws IOException
	 */
	private void processMessage(String message) throws IOException {
		// Create matcher for incoming message
		Matcher matcher = patternMessage.matcher(message);
		// If message valid
		if (matcher.matches()) {
			String inputMessage = matcher.group(1); // Full message (without stuff before "<" and after ">"
			String inputLength = matcher.group(2); // Length of Message (digits at beginning)
			String inputContent = matcher.group(3).toUpperCase(); // Contents of message

			// Return basic error if given message length does not match the message
			if (inputMessage.length() != Integer.parseInt(inputLength)) {
				out.writeObject("<08:ERR>");
				return;
			}

			// Split contents at whitespace(s)
			String[] contents = inputContent.split("\\s+");

			// For each operator inside message content
			for (String content : contents) {
				// Try to parse supported operations
				switch (content) {
				case "ADD":
					calcOp = content;
					replyToMessageContent(content, "OK");
					break;
				case "SUB":
					calcOp = content;
					replyToMessageContent(content, "OK");
					break;
				case "MUL":
					calcOp = content;
					replyToMessageContent(content, "OK");
					break;
				case "RES":
					calcOp = content;
					replyToMessageContent(content, "OK");
					break;
				case "":
					// Ignore empty strings that are not removed during the string-split
					break;
				default:
					// Try to parse operation as number
					try {
						int value = Integer.valueOf(content);
						// If content is number, then check if calculation operation is set
						if (calcOp == null) {
							// Error if content is neither number nor calculation operation
							replyToMessageContent(content, "ERR");
							break;
						}
						// Perform calculation with number
						performCalcOperation(value, calcOp);
						replyToMessageContent(content, "OK");
					} catch (NumberFormatException e) {
						// Error if content is neither number nor calculation operation
						replyToMessageContent(content, "ERR");
					}
				}
			}
		}

		// Reply to client with 'FIN' after processing the whole message
		out.writeObject("<08:FIN>");
	}

	/**
	 * This method performs a calculation operation with the number from the message
	 * content and the last set calculation operation (ADD, SUB, MUL).
	 * 
	 * @param value
	 * @param calcOp
	 * @throws RemoteException
	 */
	private void performCalcOperation(int value, String calcOp) throws RemoteException {
		switch (calcOp) {
		case "ADD":
			calc.add(value);
			break;
		case "SUB":
			calc.subtract(value);
			break;
		case "MUL":
			calc.multiply(value);
			break;
		}
	}

	/**
	 * This method creates a appropriate reply message according to the content and
	 * the operator it's validity.
	 * 
	 * @param content
	 * @param status
	 * @throws IOException
	 */
	public void replyToMessageContent(String content, String status) throws IOException {
		int headerLength = 0;

		if (status.equals("OK") && !content.equals("RES")) {
			headerLength = 6 + status.length() + content.length(); // 6 -> because of <, 2xDigit, :, whitespace, >
			out.writeObject("<" + getHeaderLengthText(headerLength) + ":" + status + " " + content + ">");
		}
		if (status.equals("ERR")) {
			headerLength = 6 + status.length() + content.length(); // 6 -> because of <, 2xDigit, :, whitespace, >
			out.writeObject("<" + getHeaderLengthText(headerLength) + ":" + status + " " + content + ">");
		}
		if (status.equals("OK") && content.equals("RES")) {
			int result = calc.getResult();
			// 7 -> because of<, 2xDigit, :, 2x whitespace, >
			headerLength = 7 + status.length() + content.length() + String.valueOf(result).length();
			out.writeObject(
					"<" + getHeaderLengthText(headerLength) + ":" + status + " " + content + " " + result + ">");
		}
	}

	/**
	 * This method returns the String representation of the length of the respnse
	 * message.
	 * 
	 * @param headerLength
	 * @return lengthAsString
	 */
	public String getHeaderLengthText(int headerLength) {
		return headerLength < 10 ? "0" + headerLength : String.valueOf(headerLength);
	}

}