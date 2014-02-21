package com.paylogic.scanwarelite;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.paylogic.scanwarelite.models.User;

public class APIFacade {
	private String urlBase = "https://api.paylogic.nl/API/?command=";
	private String command;
	private String urlParams;
	private String urlString;
	private URL url;
	private HttpURLConnection conn;
	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	private Document doc;

	private Document callAPI(String urlString) {

		try {
			url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			if (conn.getResponseCode() == 200) {
				dbFactory = DocumentBuilderFactory.newInstance();
				dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(conn.getInputStream());
			} else {
				return null;
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return doc;
	}

	public Document login(String username, String password) {
		command = "sparqLogin";
		urlParams = "&username=" + username + "&password=" + password;
		urlString = urlBase + command + urlParams;

		return callAPI(urlString);

	}

	public Document getEvents(String username, String password) {
		command = "sparqMMList";
		urlParams = "&username=" + username + "&password="
				+ password;
		urlString = urlBase + command + urlParams;

		Document response  = callAPI(urlString);
		return response;
	}
}
