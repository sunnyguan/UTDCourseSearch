package com.mvcApp.test.mvcApp.rest;

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

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

@EnableScheduling
@Controller
public class HelloRestController {

	final boolean FORCENEW = false;
	SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
	
	static HashMap<String, String> searches = new HashMap<String, String>();
	static HashMap<String, String> profToRating = new HashMap<String, String>();
	static HashMap<String, String> profToGPA = new HashMap<String, String>();
	
	@Scheduled(cron = "0 1 1 * * ?")
	public void scheduleTaskWithFixedRate() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
	    new UTDDBUpgrader().run();
	    readSearches();
		readProfToRating();
	}
	
	@RequestMapping("/")
	public String index() {
		return "index.html";
	}
	
	@RequestMapping("/home")
	public String home() {
		return "home.jsp";
	}
	
	@RequestMapping("/cc")
	public String cc() {
		return "test.html";
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
		Object o = readFile("searches.ser");
		if (o != null) {
			searches = (HashMap<String, String>) o;
			System.out.println("Read complete! Search size = " + searches.size());
		} else {
			System.out.println("File not found, initialized as empty.");
		}
	}
	
	static void saveSearch() {
		boolean status = saveFile("searches.ser", searches);
		System.out.println("Writing searches success: " + status);
	}
	
	public static void startup() {
		readSearches();
		readProfToRating();
		readProfToGPA();
	}
	
	public String padRight(String s, int n) {
		return String.format("%-" + n + "s", s);
	}
	
	@RequestMapping("rmp")
	public ModelAndView rmp(@RequestParam String course, @RequestParam String term) {
		System.out.println(profToRating.size());
		long t = System.currentTimeMillis();
		
		String output = "";
		String identifier = course + "%%" + term;
		if(!searches.containsKey(identifier) || FORCENEW) {
			output = newSearch(course, term);
			searches.put(identifier, output);
			saveSearch();
		} else {
			output = searches.get(identifier);
		}
		
		double time = (System.currentTimeMillis() - t);
		System.out.println("Completed Request in " + time/1000.0 + " seconds.");
		
		ModelAndView model = new ModelAndView("/home");
		model.addObject("output", output);
		model.addObject("course", course);
		model.addObject("time", time / 1000.0);
		model.addObject("numProfs", profToRating.size());
		return model;
	}
	
	public String newSearch(String course, String term) {
		System.out.println(dateFormatLocal.format(new Date()) + "\tRequested Course: " + course);
		
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
				
				String avgGPA = "N/A";
				if(profToGPA.containsKey(prof)) {
					avgGPA = profToGPA.get(prof);
				}
				
				String overallRating = "N/A";
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
				
				// HtmlAnchor a = (HtmlAnchor) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]/td[2]/a");
				String url = "";//a.getAttribute("href").split("https://coursebook.utdallas.edu/search/")[1];
				
				/*if(rating.contains("No")) {
					output += "<tr data-sort-method='none'>";
				}*/
				
				output += "<tr>";
				
				output += "<td>" + open + "</td>";
				
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
	
	
	
	/*
	 <%
    double num = Math.random();
    if (num > 0.95) {
  %>
      <h2>You'll have a luck day!</h2><p>(<%= num %>)</p>
  <%
    } else {
  %>
      <h2>Well, life goes on ... </h2><p>(<%= num %>)</p>
  <%
    }
  %>
  <a href="<%= request.getRequestURI() %>"><h3>Try Again!</h3></a>
	 */

}
