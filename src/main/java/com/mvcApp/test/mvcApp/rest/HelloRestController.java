package com.mvcApp.test.mvcApp.rest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
	static ArrayList<Roommate> roommates = new ArrayList<Roommate>();
	
	// @Scheduled(cron = "0 1 1 * * ?")
	@RequestMapping("/updateDB")
	@ResponseBody
	public String scheduleTaskWithFixedRate() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
	    new UTDDB().run();
	    readSearches();
		readProfToRating();
		return "done! you'll never see this but hey, at least now you know!";
	}
	
	@RequestMapping("/room")
	public String roommateSearch() {
		return "room.html";
	}
	
	@RequestMapping("/memoryCheck")
	@ResponseBody
	public String memoryCheck() {
		return Math.round((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * 1.0 / 1024 / 1024) + " MB used.";
	}
	
	@RequestMapping("/")
	public String index() {
		return "index.html";
	}
	
	@RequestMapping("/result")
	public ModelAndView result(@RequestParam String name, @RequestParam String email, @RequestParam int gender, @RequestParam int gender_o, @RequestParam int wake, @RequestParam int wake_o, @RequestParam int sleep, @RequestParam int sleep_o, @RequestParam int party, @RequestParam int party_o, @RequestParam int politics, @RequestParam String politics_o, @RequestParam int religion, @RequestParam String religion_o) {
		System.out.println(profToRating.size());
		long t = System.currentTimeMillis();
		
		roommates = (ArrayList<Roommate>) readFile("roommates.ser");
		if(roommates == null) roommates = new ArrayList<Roommate>();
		Roommate r = new Roommate(name, email, gender, gender_o, wake, wake_o, sleep, sleep_o, party, party_o, politics, politics_o, religion, religion_o);
		if(!roommates.contains(r)) {
			roommates.add(r);
		} else {
			roommates.remove(r);
			roommates.add(r);
		}
		
		ArrayList<Roommate> comp = new ArrayList<Roommate>();
		for(Roommate r2 : roommates) {
			if(!r2.equals(r) && (r.getDiff(r2) == 0 || r2.name.equals("IMatch WithEveryone"))) {
				comp.add(r2);
			}
		}
		
		String output = "<table style=\"width: 100%;\" id=\"professors\">"
				+ "<thead><tr data-sort-method=\"none\">"
				+ "<th>Name</th>"
				+ "<th>Email</th>"
				+ "<th>Gender</th>"
				+ "<th>Wake Time</th>"
				+ "<th>Sleep Time</th>"
				+ "<th>Party Rank</th>"
				+ "<th>Political View</th>"
				+ "<th>Religious View</th>"
				+ "</tr></thead>";
		
		for(Roommate r2 : comp) {
			output += "<tr>";
			output += "<td>" + r2.getName() + "</td>";
			output += "<td>" + r2.getEmail() + "</td>";
			output += "<td>" + Roommate.genderMap[r2.getGender()] + "</td>";
			output += "<td>" + Roommate.wakeMap[r2.getWake()] + "</td>";
			output += "<td>" + Roommate.sleepMap[r2.getSleep()] + "</td>";
			output += "<td>" + Roommate.partyMap[r2.getParty()] + "</td>";
			output += "<td>" + Roommate.politicsMap[r2.getPolitics()] + "</td>";
			output += "<td>" + Roommate.religionMap[r2.getReligion()] + "</td>";
			output += "</tr>";
		}
		
		output += "</table>";
		
		saveFile("roommates.ser", roommates);
		System.out.println("Writing new roommate success, size: " + roommates.size() + ", newest: " + r.name);
		
		double time = (System.currentTimeMillis() - t);
		ModelAndView model = new ModelAndView("/resultTemp");
		model.addObject("time", time);
		model.addObject("numRoommates", roommates.size());
		model.addObject("output", output);
		return model;
	}
	
	@RequestMapping("/resultTemp")
	public String resultTemp() {
		return "result.jsp";
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
	
	UTDDB udb = new UTDDB();
	
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
				"       <col span=\"1\" style=\"width: 8%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 22%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 10%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 10%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 10%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 9%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 26%;\">\r\n" + 
				"  </colgroup>"
				+ "<thead><tr data-sort-method=\"none\"><th>Status</th>"
				+ "<th role=\"columnheader\">Course</th>"
				+ "<th role=\"columnheader\">Name</th>"
				+ "<th role=\"columnheader\">Professor</th>"
				+ "<th role=\"columnheader\">Rating</th>"
				+ "<th role=\"columnheader\">Avg. GPA</th>"
				+ "<th role=\"columnheader\"><div class=\"tooltip\">Overall<span class=\"tooltiptext\">30% RMP + 70% GPA</span></div></th>"
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
				if(prof.contains(",")) {
					prof = prof.split(",")[0].trim();
				}
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
				if(rating.contains("based on") && !avgGPA.contentEquals("N/A") && !avgGPA.contains("0 Records Found")) {
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
					output += "<td" + add + "><a class='popup_rmp' href=\"https://www.ratemyprofessors.com/ShowRatings.jsp?tid=" + gtid + "\">" + rating + "</a></td>";
				}
				
				// UTDGrades IFrame info: $('.grades').magnificPopup({
				// https://saitanayd.github.io/utd-grades/?subj=GOVT&num=2305&prof=Travis Hadley
				String subj = sect.split(" ")[0];
				String num = sect.split(" ")[1];
				String pr = prof.contains("Staff") ? "" : prof;
				String searchString = "https://saitanayd.github.io/utd-grades/?subj=" + subj + "&num=" + num + "&prof=" + pr;
				
				if(avgGPA.contains("0 Records")) {
					output += "<td>" + avgGPA + "</td>";
				} else {
					output += "<td><a class=\"popup_grade\" href=\"" + searchString + "\">" + avgGPA + "</a></td>";
				}
				output += "<td>" + overallRating + "</td>"; //  data-sort-method='number'
				
				// time shortening
				String[] timeInfo = time.replaceAll("\r", "\n").split("\n");
				int i = timeInfo[0].contentEquals("Examination") ? 1 : 0;
				String timeFormatted = "";
				for(; i < (timeInfo.length) / 3; i++) {
					String days = timeInfo[0+i*3];
					days = days.replace("Tuesday & Thursday", "TTh");
					days = days.replace("Monday & Wednesday", "MW");
					days = days.replace("Monday, Wednesday, Friday", "MWF");
					days = days.replace("Monday & Wednesday", "MW");
					String timeRange = timeInfo[1+i*3];
					String location = timeInfo[2+i*3];
					timeFormatted += days + " " + timeRange + " " + location + "\n";
				}
				
				output += "<td>" + timeFormatted + "</td>";
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
