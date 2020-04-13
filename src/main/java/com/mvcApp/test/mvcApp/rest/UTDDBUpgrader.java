package com.mvcApp.test.mvcApp.rest;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

public class UTDDBUpgrader {
	SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
	HashMap<String, String> searches = new HashMap<String, String>();
	HashMap<String, String> recordRatings = new HashMap<String, String>();
	void saveSearch() {
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream("searches.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(searches);
			out.close();
			fileOut.close();
			
			System.out.println("Wrote a new search, new size = " + searches.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	void readHashMaps() {
		try {
			FileInputStream fileIn = new FileInputStream("recordedRatings.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			
			recordRatings = (HashMap<String, String>) in.readObject();
			in.close();
			fileIn.close();
			System.err.println("Read complete! Size = " + recordRatings.size());
		} catch (IOException i) {
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("Classes not found");
			return;
	    }
	}
	void saveRecordRatings() {
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream("recordedRatings.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(recordRatings);
			out.close();
			fileOut.close();
			
			System.out.println("Wrote a new professor, new size = " + recordRatings.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	void readSearches() {
		try {
			FileInputStream fileIn = new FileInputStream("searches.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			
			searches = (HashMap<String, String>) in.readObject();
			in.close();
			fileIn.close();
			System.err.println("Read complete! Searches size = " + searches.size());
		} catch (IOException i) {
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("Classes not found");
			return;
	    }
	}
	
	public String padRight(String s, int n) {
		return String.format("%-" + n + "s", s);
	}
	
	public static void main(String[] args) { new UTDDBUpgrader().run(); }
	
	// flowchart:
	// 1. Need to update searches and RMP ratings
	// 2. Copy over the searches.ser and recordingRatings.ser files
	// 3. Update recordRatings.ser by going through each professor and checking again
	// 4. Update searches
	
	public void run() {
		setup();
		readHashMaps(); // load in hash maps
		readSearches(); // load in searches
		System.out.println("INFO: Old ratings and searches loaded.");
		
		// update hashmaps
		HashMap<String, String> newRecordRatings = new HashMap<String, String>();
		int i = 0;
		for (Map.Entry<String, String> s : recordRatings.entrySet()) {
			String name = s.getKey();
			System.out.println("Updating professor " + padRight(i++ + "", 5) + "/" + recordRatings.size() + ": " + name);
			String newOutput = rating(name);
			newRecordRatings.put(name, newOutput);
		}
		recordRatings = newRecordRatings;
		saveRecordRatings(); // saves searches
		System.out.println("INFO: Ratings updated.");
		
		// update searches
		HashMap<String, String> newSearches = new HashMap<String, String>();
		i = 0;
		for(Map.Entry<String, String> s : searches.entrySet()) {
			String search = s.getKey();
			System.out.println("Updating search " + padRight(i++ + "", 5) + "/" + searches.size() + ": " + search);
			String newOutput = newSearch(search.split("%%")[0], search.split("%%")[1]);
			newSearches.put(search, newOutput);
		}
		searches = newSearches;
		saveSearch(); // saves searches
		System.out.println("INFO: Searches and ratings updated. Upgrade complete.");
	}
	static WebClient client;
	
	void setup() {
		client = new WebClient();
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
	}
	
	public String rating(String prof) {
		String rating = "Not Found";
		if (!prof.toLowerCase().contains("staff")) {
			String url = "https://www.ratemyprofessors.com/search.jsp?query=" + prof.trim();
			try {
				HtmlPage rmp = client.getPage(url);
				HtmlUnorderedList allProfs = (HtmlUnorderedList) rmp
						.getFirstByXPath("//*[@id=\"searchResultsBox\"]/div[2]/ul");
				int index = -1;

				if(allProfs != null) {
					for (int i = 1; i <= allProfs.getChildElementCount(); i++) {
						HtmlSpan school = (HtmlSpan) rmp.getFirstByXPath(
								"//*[@id=\"searchResultsBox\"]/div[2]/ul/li[" + i + "]/a/span[2]/span[2]");
						if (school != null && school.asText().contains("The University of Texas at Dallas")) {
							index = i;
							break;
						}
					}

					try {
						HtmlAnchor a = (HtmlAnchor) rmp
								.getFirstByXPath("//*[@id=\"searchResultsBox\"]/div[2]/ul/li[" + index + "]/a");
						
						rmp = a.click();
						rating = ((HtmlDivision) rmp
								.getFirstByXPath("//*[@id=\"root\"]/div/div/div[2]/div[1]/div[1]/div[1]/div[1]/div/div[1]"))
										.asText();
						
						if(!rating.contains("N/A")) {
							rating += " based on " + ((HtmlAnchor) rmp
									.getFirstByXPath("//*[@id=\"root\"]/div/div/div[2]/div[1]/div[1]/div[1]/div[2]/div/a"))
											.asText();
						} else {
							rating = "No Ratings";
						}
						
					} catch (Exception e) {}
					
					
				}
			} catch (Exception e) {
			}
		}
		return rating;
	}
	
	public String newSearch(String course, String term) {
		
		
		System.out.println(dateFormatLocal.format(new Date()) + "\tRequested Course: " + course);
		
		String output = "<table style=\"width: 100%;\" id=\"professors\">"
				+ "<colgroup>\r\n" + 
				"       <col span=\"1\" style=\"width: 15%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 20%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 15%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 20%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 30%;\">\r\n" + 
				"  </colgroup>"
				+ "<thead><tr data-sort-method=\"none\"><th>Open Status</th>"
				+ "<th>Name</th>"
				+ "<th>Professor</th>"
				+ "<th>Rating</th>"
				+ "<th>Schedule</th></tr></thead>";

		// String term = "term_20f?";

		String searchQuery = course.trim().replace(" ", "/") + "/" + term + "?";

		WebClient client = new WebClient();
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		
		recordRatings.put("-Staff-", "Not Found");
		try {
			String searchUrl = "https://coursebook.utdallas.edu/" + searchQuery;
			HtmlPage page = client.getPage(searchUrl);

			List l = page.getByXPath("//*/td[4]");

			for (int section = 1; section <= l.size(); section++) {
				long timeTrack = System.currentTimeMillis();
				
				HtmlTableRow tr = (HtmlTableRow) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]");
				
				HtmlSpan open = (HtmlSpan) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]/td[1]/span");
				String name = tr.getCell(2).asText();
				String prof = tr.getCell(3).asText();
				String time = tr.getCell(4).asText();

				String rating = "Not Found";
				String profLastFirst = "-Staff-";
				
				if(!prof.trim().toLowerCase().contains("staff")) {
					profLastFirst = prof.split(" ")[1] + ", " + prof.split(" ")[0];
				}
				
				if(!recordRatings.containsKey(profLastFirst)) {
					System.out.println("Uh oh, checking web...:" + profLastFirst);
					if (!prof.toLowerCase().contains("staff")) {
						// Rate My Professor Scan
						String url = "https://www.ratemyprofessors.com/search.jsp?query=" + prof;
						HtmlPage rmp = client.getPage(url);
						// *[@id="searchResultsBox"]/div[2]/ul/li[1]/a/span[2]/span[2]
						// *[@id="searchResultsBox"]/div[2]/ul/li[2]/a/span[2]/span[2]
						HtmlUnorderedList allProfs = (HtmlUnorderedList) rmp
								.getFirstByXPath("//*[@id=\"searchResultsBox\"]/div[2]/ul");
						int index = -1;
						
						if(allProfs != null) {
							for (int i = 1; i <= allProfs.getChildElementCount(); i++) {
								HtmlSpan school = (HtmlSpan) rmp.getFirstByXPath(
										"//*[@id=\"searchResultsBox\"]/div[2]/ul/li[" + i + "]/a/span[2]/span[2]");
								if (school != null && school.asText().contains("The University of Texas at Dallas")) {
									index = i;
									break;
								}
							}

							try {
								HtmlAnchor a = (HtmlAnchor) rmp
										.getFirstByXPath("//*[@id=\"searchResultsBox\"]/div[2]/ul/li[" + index + "]/a");
								
								rmp = a.click();
								rating = ((HtmlDivision) rmp
										.getFirstByXPath("//*[@id=\"root\"]/div/div/div[2]/div[1]/div[1]/div[1]/div[1]/div/div[1]"))
												.asText();
								
								if(!rating.contains("N/A")) {
									rating += " based on " + ((HtmlAnchor) rmp
											.getFirstByXPath("//*[@id=\"root\"]/div/div/div[2]/div[1]/div[1]/div[1]/div[2]/div/a"))
													.asText();
								} else {
									rating = "No Ratings";
								}
								
								recordRatings.put(profLastFirst, rating);
								saveRecordRatings();
							} catch (Exception e) {
								recordRatings.put(profLastFirst, rating);
								saveRecordRatings();
							}
						} else {
							recordRatings.put(profLastFirst, rating);
							saveRecordRatings();
						}
					}
				} else {
					rating = recordRatings.get(profLastFirst);
				}
				
				
				
				String formatName = name;//.replaceAll("\\(.*\\)", "").replace("CV Honors", "CV");
				
				// HtmlAnchor a = (HtmlAnchor) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]/td[2]/a");
				String url = "";//a.getAttribute("href").split("https://coursebook.utdallas.edu/search/")[1];
				
				/*if(rating.contains("No")) {
					output += "<tr data-sort-method='none'>";
				}*/
				
				output += "<tr>";
				output += "<td>" + open.asText() + "</td>";
				
				output += "<td><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://coursebook.utdallas.edu/clips/clip-section-v2.zog?id=" + url + "\">";
				if (name.contains("CV Honors"))
					output += "<b>" + formatName + "</b></a></td>";
				else
					output += formatName + "</a></td>";
				output += "<td>" + prof + "</td>";
				
				if(rating.contains("No")) 
					output += "<td data-sort='0'>" + rating + "</td>";
				else {
					String add = "";
					try {
						double r = Double.parseDouble(rating.split(" ")[0]);
						add = " class='";
						if(r <= 2.5) {
							add += "uhoh'";
						} else if(r >= 4.5) {
							add += "ahhh'";
						} else {
							add += "normal'";
						}
					}catch(Exception e) {}
					output += "<td" + add + ">" + rating + "</td>";
				}
				
				output += "<td>" + time + "</td>";
				output += "</tr>";
				
				System.out.println("Processing " + padRight(profLastFirst, 40) + (System.currentTimeMillis() - timeTrack) + "ms");
			}
			
			output += "</table>";
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return output;
	}
}
