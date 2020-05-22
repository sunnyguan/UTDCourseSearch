package com.mvcApp.test.mvcApp.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;
import com.gargoylesoftware.htmlunit.html.parser.HTMLParserListener;

public class UTDDBUpgrader {
	SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

	static HashMap<String, String> searches = new HashMap<String, String>();
	static HashMap<String, String> profToRating = new HashMap<String, String>();
	static HashMap<String, String> profToGPA = new HashMap<String, String>();
	
	static Object readFile(String name) {
		try {
			FileInputStream fileIn = new FileInputStream(name);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			
			Object obj = in.readObject();
			in.close();
			fileIn.close();
			return obj;
		} catch (IOException i) {
			return null;
		} catch (ClassNotFoundException c) {
			return null;
	    }
	}
	
	static boolean saveFile(String name, Object obj) {
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(name);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(obj);
			out.close();
			fileOut.close();
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}
	
	static void readProfToGPA() {
		profToGPA = (HashMap<String, String>) readFile("profToGPA.ser");
		System.out.println("Read complete! GPA Size = " + profToGPA.size());
	}
	
	static void readProfToRating() {
		profToRating = (HashMap<String, String>) readFile("profToRating.ser");
		System.out.println("Read complete! Rating size = " + profToRating.size());
	}
	
	static void saveProfToRating() {
		boolean status = saveFile("profToRating.ser", profToRating);
		System.out.println("Writing profToRating success: " + status);
	}
	
	static void readSearches() {
		searches = (HashMap<String, String>) readFile("searches.ser");
		System.out.println("Read complete! Search size = " + searches.size());
	}
	
	static void saveSearch() {
		boolean status = saveFile("searches.ser", searches);
		System.out.println("Writing searches success: " + status);
	}
	
	public String padRight(String s, int n) {
		return String.format("%-" + n + "s", s);
	}
	
	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException { new UTDDBUpgrader().run(); }
	
	// flowchart:
	// 1. Need to update searches and RMP ratings
	// 2. Copy over the searches.ser and recordingRatings.ser files
	// 3. Update recordRatings.ser by going through each professor and checking again
	// 4. Update searches
	
	public void run() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		setup();
		readProfToRating(); // load in hash maps
		readSearches(); // load in searches
		System.out.println("INFO: Old ratings and searches loaded.");
		
		// update hashmaps
		HashMap<String, String> newProfToRating = new HashMap<String, String>();
		int i = 0;
		for (Map.Entry<String, String> s : profToRating.entrySet()) {
			String name = s.getKey();
			String newOutput = rating(name);
			System.out.println("Updating professor " + padRight(i++ + "", 5) + "/" + profToRating.size() + ": " + padRight(name, 40) + " --> " + newOutput);
			newProfToRating.put(name, newOutput);
		}
		profToRating = newProfToRating;
		saveProfToRating(); // saves searches
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
		client.getOptions().setThrowExceptionOnFailingStatusCode(false);
		client.getOptions().setPrintContentOnFailingStatusCode(false);
		client.getOptions().setThrowExceptionOnScriptError(false);
		client.getOptions().setJavaScriptEnabled(false);
		client.setCssErrorHandler(new SilentCssErrorHandler());
		client.setHTMLParserListener(new HTMLParserListener() {
			@Override
			public void error(String message, java.net.URL url, String html, int line, int column, String key) {
				
			}

			@Override
			public void warning(String message, java.net.URL url, String html, int line, int column, String key) {
				
			}
		});
	}
	
	public String rating(String prof) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		String rating = "0 Ratings Found";
		if (!prof.toLowerCase().contains("staff")) {
			// Rate My Professor Scan
			String url = "https://www.ratemyprofessors.com/search.jsp?query=" + prof;
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
					String tid = rmp.getUrl().toString().split("=")[1];
					rating = ((HtmlDivision) rmp
							.getFirstByXPath("//*[@id=\"root\"]/div/div/div[2]/div[1]/div[1]/div[1]/div[1]/div/div[1]"))
									.asText();
					
					if(!rating.contains("N/A")) {
						rating += " based on " + ((HtmlAnchor) rmp
								.getFirstByXPath("//*[@id=\"root\"]/div/div/div[2]/div[1]/div[1]/div[1]/div[2]/div/a"))
										.asText();
					} else {
						rating = "0 Ratings Found";
					}
					rating += "@@" + tid;
				} catch (Exception e) {}
			}
		}
		profToRating.put(prof, rating);
		return rating;
	}
	
	public String newSearch(String course, String term) {
		System.out.println(dateFormatLocal.format(new Date()) + "\tRequested Course: " + course);
		
		String output = "<table style=\"width: 100%;\" id=\"professors\">"
				+ "<colgroup>\r\n" + 
				"       <col span=\"1\" style=\"width: 5%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 8%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 17%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 10%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 10%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 10%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 7%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 33%;\">\r\n" + 
				"  </colgroup>"
				+ "<thead><tr data-sort-method=\"none\"><th>Status</th>"
				+ "<th role=\"columnheader\">Course</th>"
				+ "<th role=\"columnheader\">Name</th>"
				+ "<th role=\"columnheader\">Professor</th>"
				+ "<th role=\"columnheader\">Rating</th>"
				+ "<th role=\"columnheader\">Avg. GPA</th>"
				+ "<th role=\"columnheader\">Overall</th>"
				+ "<th role=\"columnheader\">Schedule</th></tr></thead>";

		// String term = "term_20f?";

		String searchQuery = course.trim().replace(" ", "/") + "/" + term + "?";

		WebClient client = new WebClient();
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		
		profToRating.put("-Staff-", "0 Ratings Found");
		try {
			String searchUrl = "https://coursebook.utdallas.edu/" + searchQuery;
			HtmlPage page = client.getPage(searchUrl);

			List l = page.getByXPath("//*/td[4]");

			for (int section = 1; section <= l.size(); section++) {
				long timeTrack = System.currentTimeMillis();
				
				HtmlTableRow tr = (HtmlTableRow) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]");
				
				HtmlSpan openSpan = (HtmlSpan) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]/td[1]/span");
				String open = "Unknown";
				if(openSpan != null) open = openSpan.asText();
				
				String name = tr.getCell(2).asText();
				
				String prof = tr.getCell(3).asText();
				// add multiple professor support
				if(prof.contains(",")) prof = prof.split(",")[0];
				// System.out.println("trimmed from: " + tr.getCell(2).asText() + " to " + prof);
				
				String time = tr.getCell(4).asText();

				String rating = "0 Ratings Found";
				
				if(!profToRating.containsKey(prof)) {
					System.out.println("Uh oh, checking web...:" + prof);
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
								String tid = rmp.getUrl().toString().split("=")[1];
								rating = ((HtmlDivision) rmp
										.getFirstByXPath("//*[@id=\"root\"]/div/div/div[2]/div[1]/div[1]/div[1]/div[1]/div/div[1]"))
												.asText();
								
								if(!rating.contains("N/A")) {
									rating += " based on " + ((HtmlAnchor) rmp
											.getFirstByXPath("//*[@id=\"root\"]/div/div/div[2]/div[1]/div[1]/div[1]/div[2]/div/a"))
													.asText();
								} else {
									rating = "0 Ratings Found";
								}
								
								rating += "@@" + tid;
								
								profToRating.put(prof, rating);
								saveProfToRating();
							} catch (Exception e) {
								profToRating.put(prof, rating);
								saveProfToRating();
							}
						} else {
							profToRating.put(prof, rating);
							saveProfToRating();
						}
					}
				} else {
					rating = profToRating.get(prof);
				}
				
				String avgGPA = "0 Records Found";
				if(profToGPA.containsKey(prof)) {
					avgGPA = profToGPA.get(prof);
				}
				
				String overallRating = "0 (N/A)";
				double gpaWeight = 70;
				if(rating.contains("based on") && !avgGPA.contentEquals("N/A")) {
					double info0 = 2;
					try {
						info0 = Double.parseDouble(avgGPA.split(" ")[0]);
					} catch (Exception e) {}
					double scores = Double.parseDouble(rating.split(" ")[0]) / 5 * (100 - gpaWeight) + info0 / 4 * gpaWeight;
					scores = (double) Math.round(scores * 100d) / 100d; // adjust scaling !
					overallRating = scores + "";
				}
				
				String formatName = name.replaceAll("\\(.*\\)", "").replace("CV Honors", "CV");
				
				HtmlAnchor a = (HtmlAnchor) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]/td[2]/a");
				String url = a.getAttribute("href").split("https://coursebook.utdallas.edu/search/")[1];
				
				String sect = "N/A";
				try {
					String[] cc = url.split("\\.")[0].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
					sect = cc[0].toUpperCase() + " " + cc[1]; 
				} catch (Exception e) {}

				
				/*if(rating.contains("No")) {
					output += "<tr data-sort-method='none'>";
				}*/
				
				output += "<tr>";
				
				output += "<td>" + open + "</td>";
				
				output += "<td>" + sect + "</td>";
				
				output += "<td><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://coursebook.utdallas.edu/clips/clip-section-v2.zog?id=" + url + "\">";
				if (name.contains("CV Honors"))
					output += "<b>" + formatName + "</b></a></td>";
				else
					output += formatName + "</a></td>";
				output += "<td>" + prof + "</td>";
				
				// System.out.println("Rating: " + rating);
				if(rating.contains("0 Ratings")) {
					output += "<td>" + rating + "</td>";
				} else {
					String add = "";
					String gtid = "";
					try {
						double r = Double.parseDouble(rating.split(" ")[0]);
						gtid = rating.split("@@")[1];
						rating = rating.split("@@")[0];
						add = " class='";
						if(r <= 2.5) {
							add += "uhoh'";
						} else if(r >= 4.5) {
							add += "ahhh'";
						} else {
							add += "normal'";
						}
					}catch(Exception e) {}
					output += "<td" + add + "><a href=\"https://www.ratemyprofessors.com/ShowRatings.jsp?tid=" + gtid + "\">" + rating + "</a></td>";
				}
				
				output += "<td><a href=\"https://utdgrades.com/results?search=" + prof + "\">" + avgGPA + "</a></td>";
				
				output += "<td>" + overallRating + "</td>"; //  data-sort-method='number'
				
				output += "<td>" + time + "</td>";
				output += "</tr>";
				
				System.out.println("Processing " + padRight(prof, 40) + (System.currentTimeMillis() - timeTrack) + "ms");
			}
			
			output += "</table>";
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	public String newSearch_old(String course, String term) {
		// System.out.println(dateFormatLocal.format(new Date()) + "\tRequested Course: " + course);
		
		String output = "<table style=\"width: 100%;\" id=\"professors\">"
				+ "<colgroup>\r\n" + 
				"       <col span=\"1\" style=\"width: 5%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 20%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 10%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 10%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 15%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 7%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 33%;\">\r\n" + 
				"  </colgroup>"
				+ "<thead><tr data-sort-method=\"none\"><th>Status</th>"
				+ "<th>Name</th>"
				+ "<th>Professor</th>"
				+ "<th>Rating</th>"
				+ "<th>Avg. GPA</th>"
				+ "<th>SG Rank</th>"
				+ "<th>Schedule</th></tr></thead>";

		// String term = "term_20f?";

		String searchQuery = course.trim().replace(" ", "/") + "/" + term + "?";

		WebClient client = new WebClient();
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		
		profToRating.put("-Staff-", "0 Ratings Found");
		try {
			String searchUrl = "https://coursebook.utdallas.edu/" + searchQuery;
			HtmlPage page = client.getPage(searchUrl);

			List l = page.getByXPath("//*/td[4]");

			for (int section = 1; section <= l.size(); section++) {
				long timeTrack = System.currentTimeMillis();
				
				HtmlTableRow tr = (HtmlTableRow) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]");
				
				HtmlSpan openSpan = (HtmlSpan) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]/td[1]/span");
				String open = "Unknown";
				if(openSpan != null) open = openSpan.asText();
				
				String name = tr.getCell(2).asText();
				String prof = tr.getCell(3).asText();
				String time = tr.getCell(4).asText();

				String currentRating = "0 Ratings Found"; // always check web since we're updating DB
				
				String avgGPA = "N/A";
				if(profToGPA.containsKey(prof)) 
					avgGPA = profToGPA.get(prof);
				
				String overallRating = "N/A";
				double gpaWeight = 70;
				if(currentRating.contains("based on") && !avgGPA.contentEquals("N/A")) {
					double info0 = 2;
					try {
						info0 = Double.parseDouble(avgGPA.split(" ")[0]);
					} catch (Exception e) {}
					double scores = Double.parseDouble(currentRating.split(" ")[0]) / 5 * (100 - gpaWeight) + info0 / 4 * gpaWeight;
					scores = (double) Math.round(scores * 100d) / 100d; // adjust scaling !
					overallRating = scores + "";
				}
				
				String formatName = name.replaceAll("\\(.*\\)", "").replace("CV Honors", "CV");
				
				HtmlAnchor a = (HtmlAnchor) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]/td[2]/a");
				String url = a.getAttribute("href").split("https://coursebook.utdallas.edu/search/")[1];
				
				output += "<tr>";
				output += "<td>" + open + "</td>";
				output += "<td><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://coursebook.utdallas.edu/clips/clip-section-v2.zog?id=" + url + "\">";
				if (name.contains("CV Honors"))
					output += "<b>" + formatName + "</b></a></td>";
				else
					output += formatName + "</a></td>";
				
				output += "<td>" + prof + "</td>";
				if(currentRating.contains("0 Ratings")) {
					output += "<td>" + currentRating + "</td>";
				} else {
					String add = "";
					try {
						double r = Double.parseDouble(currentRating.split(" ")[0]);
						add = " class='";
						if(r <= 2.5) {
							add += "uhoh'";
						} else if(r >= 4.5) {
							add += "ahhh'";
						} else {
							add += "normal'";
						}
					}catch(Exception e) {}
					output += "<td" + add + ">" + currentRating + "</td>";
				}
				
				output += "<td>" + avgGPA + "</td>";
				output += "<td data-sort-method='number'>" + overallRating + "</td>";
				output += "<td>" + time + "</td>";
				output += "</tr>";
				System.out.println("Processing " + padRight(prof, 40) + (System.currentTimeMillis() - timeTrack) + "ms");
			}
			output += "</table>";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
}
