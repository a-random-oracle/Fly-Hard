package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

public class NetworkManager {

	/** The URL to the server */
	public static final String SERVER_URL =
			"http://tomcat-teamgoa.rhcloud.com";

	/** The post extension */
	public static final String POST_EXT = "/post";

	/** The number of attempts before aborting a connection */
	private static final int ABORT_AFTER = 5;
	
	/** Whether to output data to the standard output */
	private static boolean verbose;
	
	/** The thread to send data on */
	private Thread sendThread;


	// Constructor ----------------------------------------------------------------------

	/**
	 * Constructs a new network manager.
	 * @param verbose
	 * 			<code>true</code> indicates that the network manager
	 * 			should output status and connection information to
	 * 			the standard output
	 */
	public NetworkManager(boolean verbose) {
		NetworkManager.verbose = verbose;
	}


	// HTTP Methods ---------------------------------------------------------------------
	
	public static ArrayList<String> httpGet(String url, HashMap<String, String> headers)
			throws ClientProtocolException, IOException {
		// The array of stings to return
		ArrayList<String> responseArray = new ArrayList<String>();

		// Create a client to manage the request to the server
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);

		// Track the number of attempts made to connect to the server
		int attempts = 0;

		// Set up a boolean to break out of the while loop after
		// a successful connection
		boolean success = false;

		do {
			// Create an iterator for the list of headers
			Iterator<String> iterator = headers.keySet().iterator();
			
			// Add headers to the request
			print("<GET>");
			String currentHeader = null;
			while (iterator.hasNext()) {
				currentHeader = iterator.next();
				request.addHeader(currentHeader, headers.get(currentHeader));
				print("         " + currentHeader + " = "
						+ headers.get(currentHeader));
			}
			print("</GET>");

			// Send the request to the server
			HttpResponse response = client.execute(request);

			if (response != null
					&& response.getStatusLine().getStatusCode() == 200) {
				// If the response is valid, try to read it
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity()
								.getContent()));

				// Retrieve the body of the response
				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						responseArray.add(line);
					}

					// Response has been read successfuly
					success = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				// Couldn't connect to the server
				Exception e = new Exception("Connection to server "
						+ "could not be established.");
				e.printStackTrace();
			}

			attempts++;
		} while (!success && (attempts < ABORT_AFTER));

		return responseArray;
	}

	public static ArrayList<String> httpPost(String url, HashMap<String, String> headers)
			throws ClientProtocolException, IOException {
		// The array of stings to return
		ArrayList<String> responseArray = new ArrayList<String>();

		// Create a client to manage the request to the server
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(url);

		// Track the number of attempts made to connect to the server
		int attempts = 0;

		// Set up a boolean to break out of the while loop after
		// a successful connection
		boolean success = false;

		do {
			// Create an iterator for the list of headers
			Iterator<String> iterator = headers.keySet().iterator();
			
			// Add headers to the request
			print("<POST>");
			String currentHeader = null;
			while (iterator.hasNext()) {
				currentHeader = iterator.next();
				request.addHeader(currentHeader, headers.get(currentHeader));
				print("         " + currentHeader + " = "
						+ headers.get(currentHeader));
			}
			print("</POST>");

			// Send the request to the server
			HttpResponse response = client.execute(request);

			if (response != null
					&& response.getStatusLine().getStatusCode() == 200) {
				// If the response is valid, try to read it
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity()
								.getContent()));

				// Retrieve the body of the response
				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						responseArray.add(line);
					}

					// Response has been read successfuly
					success = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				// Couldn't connect to the server
				Exception e = new Exception("Connection to server "
						+ "could not be established.");
				e.printStackTrace();
			}

			attempts++;
		} while (!success && (attempts < ABORT_AFTER));

		return responseArray;
	}


	// Close ----------------------------------------------------------------------------

	/**
	 * Closes any open connections.
	 */
	public void close() {
		// TODO
	}


	// Helper Methods -------------------------------------------------------------------

	/**
	 * Prints strings to the standard output.
	 * <p>
	 * If {@link #verbose} is set to <code>true</code>, this will
	 * function in the same was as {@link System.out#println()}.
	 * </p>
	 * <p>
	 * Otherwise this will do nothing.
	 * </p>
	 * @param string
	 * 			the string to output
	 */
	public static void print(String string) {
		if (verbose) System.out.println(string);
	}

}
