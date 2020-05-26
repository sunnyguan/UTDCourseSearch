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
import java.util.Map;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.gargoylesoftware.htmlunit.BrowserVersion;
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

@SuppressWarnings("unchecked")
public class UTDDB {

    static SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
    static HashMap<String, String> searches = new HashMap<String, String>();
    static HashMap<String, String> profToRating = new HashMap<String, String>();
    static HashMap<String, String> profToGPA = new HashMap<String, String>();
    static ArrayList<Roommate> roommates = new ArrayList<Roommate>();
    static WebClient client;

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
	if (profToGPA == null)
	    profToGPA = new HashMap<String, String>();
	System.out.println("Read complete! GPA Size = " + profToGPA.size());
    }

    static void readProfToRating() {
	profToRating = (HashMap<String, String>) readFile("profToRating.ser");
	if (profToRating == null)
	    profToRating = new HashMap<String, String>();
	System.out.println("Read complete! Rating size = " + profToRating.size());
    }

    static void saveProfToRating() {
	boolean status = saveFile("profToRating.ser", profToRating);
	System.out.println("Writing profToRating success: " + status);
    }

    static void readSearches() {
	searches = (HashMap<String, String>) readFile("searches.ser");
	if (searches == null)
	    searches = new HashMap<String, String>();
	System.out.println("Read complete! Search size = " + searches.size());
    }

    static void saveSearch() {
	// no longer storing searches as of 5/23/2020
	// boolean status = saveFile("searches.ser", searches);
	// System.out.println("Writing searches success: " + status);
    }

    static int getProfs() {
	return (profToRating != null) ? profToRating.size() : 0;
    }

    public static int getRoommateSize() {
	return (roommates == null) ? 0 : roommates.size();
    }

    public static String padRight(String s, int n) {
	return String.format("%-" + n + "s", s);
    }

    /**
     * Updates the database periodically to ensure that RMP ratings and CourseBook
     * search results are up to date
     */
    public static void updateDB() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
	init();
	System.out.println("INFO: Old ratings and searches loaded.");

	// update hashmaps
	HashMap<String, String> newProfToRating = new HashMap<String, String>();
	int i = 0;
	for (Map.Entry<String, String> s : profToRating.entrySet()) {
	    String name = s.getKey();
	    String newOutput = rating(name);
	    System.out.println("Updating professor " + padRight(i++ + "", 5) + "/" + profToRating.size() + ": "
		    + padRight(name, 40) + " --> " + newOutput);
	    newProfToRating.put(name, newOutput);
	}
	profToRating = newProfToRating;
	saveProfToRating(); // saves searches
	System.out.println("INFO: Ratings updated.");

	// update searches
	// obsolete as of may 23
	/*
	 * HashMap<String, String> newSearches = new HashMap<String, String>(); i = 0;
	 * for(Map.Entry<String, String> s : searches.entrySet()) { String search =
	 * s.getKey(); System.out.println("Updating search " + padRight(i++ + "", 5) +
	 * "/" + searches.size() + ": " + search); String newOutput =
	 * newSearch(search.split("%%")[0], search.split("%%")[1], null);
	 * newSearches.put(search, newOutput); } searches = newSearches; saveSearch();
	 * // saves searches
	 * System.out.println("INFO: Searches and ratings updated. Upgrade complete.");
	 */
    }

    public static void init() {
	readSearches();
	readProfToRating();
	readProfToGPA();
	setupClient();
    }

    static void setupClient() {
	client = new WebClient(BrowserVersion.BEST_SUPPORTED);
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

    public static String processRoommate(String name, String email, int gender, int gender_o, int wake, int wake_o,
	    int sleep, int sleep_o, int party, int party_o, int politics, String politics_o, int religion,
	    String religion_o) {
	roommates = (ArrayList<Roommate>) readFile("roommates.ser");
	if (roommates == null)
	    roommates = new ArrayList<Roommate>();
	Roommate r = new Roommate(name, email, gender, gender_o, wake, wake_o, sleep, sleep_o, party, party_o, politics,
		politics_o, religion, religion_o);
	if (!roommates.contains(r)) {
	    roommates.add(r);
	} else {
	    roommates.remove(r);
	    roommates.add(r);
	}

	ArrayList<Roommate> comp = new ArrayList<Roommate>();
	for (Roommate r2 : roommates) {
	    if (!r2.equals(r) && (r.getDiff(r2) == 0 || r2.name.equals("IMatch WithEveryone"))) {
		comp.add(r2);
	    }
	}

	String output = "<table style=\"width: 100%;\" id=\"professors\">" + "<thead><tr data-sort-method=\"none\">"
		+ "<th>Name</th>" + "<th>Email</th>" + "<th>Gender</th>" + "<th>Wake Time</th>" + "<th>Sleep Time</th>"
		+ "<th>Party Rank</th>" + "<th>Political View</th>" + "<th>Religious View</th>" + "</tr></thead>";

	for (Roommate r2 : comp) {
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

	return output;
    }

    // Normal search functions

    /**
     * Returns the rate my professor results in the form of "x based on y
     * ratings@@RMP_ID" RMP_ID is used to link IFrame
     */
    public static String rmp(String course, String term, SseEmitter se) {
	System.out.println(profToRating.size());
	String output = "";
	String identifier = course + "%%" + term;
	output = newSearch(course, term, se);
	try {
	    se.complete();
	} catch (Exception e) {
	    System.out.println("oof im already gone");
	}

	return output;
    }

    private static String SCHOOL = "The University of Texas at Dallas";
    private static String URL_rmp = "https://www.ratemyprofessors.com/search.jsp?query=";

    /**
     * Helper function for rmp, returns a new rating
     */
    public static String rating(String prof) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
	String rating = no_rmp_data;

	if (prof.equals("-Staff-")) {
	    profToRating.put(prof, rating);
	    return rating;
	}

	String url = URL_rmp + prof;
	HtmlPage rmp = client.getPage(url);
	HtmlUnorderedList allProfs = (HtmlUnorderedList) rmp.getFirstByXPath("//*[@id=\"searchResultsBox\"]/div[2]/ul");
	int index = -1;

	if (allProfs == null) {
	    profToRating.put(prof, no_rmp_data);
	    return rating;
	}

	for (int i = 1; i <= allProfs.getChildElementCount() && index == -1; i++) {
	    HtmlSpan school = (HtmlSpan) rmp
		    .getFirstByXPath("//*[@id=\"searchResultsBox\"]/div[2]/ul/li[" + i + "]/a/span[2]/span[2]");
	    if (school != null && school.asText().contains(SCHOOL)) {
		index = i;
	    }
	}

	if (index != -1) {
	    HtmlAnchor a = (HtmlAnchor) rmp
		    .getFirstByXPath("//*[@id=\"searchResultsBox\"]/div[2]/ul/li[" + index + "]/a");
	    rmp = a.click();
	    String tid = rmp.getUrl().toString().split("=")[1];

	    rating = ((HtmlDivision) rmp
		    .getFirstByXPath("//*[@id=\"root\"]/div/div/div[2]/div[1]/div[1]/div[1]/div[1]/div/div[1]"))
			    .asText();

	    if (!rating.contains("N/A")) {
		rating += " based on " + ((HtmlAnchor) rmp
			.getFirstByXPath("//*[@id=\"root\"]/div/div/div[2]/div[1]/div[1]/div[1]/div[2]/div/a"))
				.asText();
	    } else {
		rating = no_rmp_data;
	    }
	    rating += "@@" + tid;
	}

	profToRating.put(prof, rating);
	return rating;
    }

    private static String no_rmp_data = "0 Ratings Found";
    private static String no_gpa_data = "0 Records Found";

    /**
     * returns the table html for a search
     */
    public static String newSearch(String course, String term, SseEmitter se) {
	System.out.println(dateFormatLocal.format(new Date()) + "\tRequested Course: " + course);

	String output = "<table style=\"width: 100%;\" id=\"professors\">" + "<colgroup>\r\n"
		+ "       <col span=\"1\" style=\"width: 5%;\">\r\n"
		+ "       <col span=\"1\" style=\"width: 8%;\">\r\n"
		+ "       <col span=\"1\" style=\"width: 22%;\">\r\n"
		+ "       <col span=\"1\" style=\"width: 10%;\">\r\n"
		+ "       <col span=\"1\" style=\"width: 10%;\">\r\n"
		+ "       <col span=\"1\" style=\"width: 10%;\">\r\n"
		+ "       <col span=\"1\" style=\"width: 6%;\">\r\n"
		+ "       <col span=\"1\" style=\"width: 20%;\">\r\n"
		+ "       <col span=\"1\" style=\"width: 9%;\">\r\n" + "  </colgroup>"
		+ "<thead><tr data-sort-method=\"none\"><th>Status</th>" + "<th role=\"columnheader\">Course</th>"
		+ "<th role=\"columnheader\">Name</th>" + "<th role=\"columnheader\">Professor</th>"
		+ "<th role=\"columnheader\">Rating</th>" + "<th role=\"columnheader\">Avg. GPA</th>"
		+ "<th role=\"columnheader\" data-sort-default><div class=\"tooltip\">Overall<span class=\"tooltiptext\">30% RMP + 70% GPA</span></div></th>"
		+ "<th role=\"columnheader\">Schedule</th>" + "<th role=\"columnheader\">Add Class (Not Galaxy)</th>"
		+ "</tr></thead>";
	String line = "";
	String searchQuery = course.trim().replace(" ", "/") + "/" + term + "?";
	long timeTrack = System.currentTimeMillis();
	if (client == null)
	    init();
	profToRating.put("-Staff-", no_rmp_data);
	try {
	    String searchUrl = "https://coursebook.utdallas.edu/" + searchQuery;
	    HtmlPage page = client.getPage(searchUrl);

	    System.out
		    .println(padRight("Retrieving CourseBook: ", 40) + (System.currentTimeMillis() - timeTrack) + "ms");

	    List l = page.getByXPath("//*/td[4]");

	    for (int section = 1; section <= l.size(); section++) {
		// Thread.sleep(1000);
		timeTrack = System.currentTimeMillis();

		HtmlTableRow tr = (HtmlTableRow) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]");

		HtmlSpan openSpan = (HtmlSpan) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]/td[1]/span");
		String open = (openSpan == null) ? "Unknown" : openSpan.asText();

		String name = tr.getCell(2).asText();
		String prof = tr.getCell(3).asText();
		String time = tr.getCell(4).asText();

		if (prof.contains(",")) {
		    prof = prof.split(",")[0].trim(); // if two or more professors for one section, only
						      // retreive RMP for the first
		}

		String rating = no_rmp_data;
		if (!profToRating.containsKey(prof)) {
		    System.out.println("Professor not found in database, searching RMP for: " + prof);
		    rating = rating(prof);
		} else {
		    rating = profToRating.get(prof);
		}

		String avgGPA = profToGPA.containsKey(prof) ? profToGPA.get(prof) : no_gpa_data;
		String overallRating = "0 (N/A)";
		double gpaWeight = 70;
		if (!rating.equals(no_rmp_data) && !avgGPA.equals(no_gpa_data)) {
		    double db_avgGPA = Double.parseDouble(avgGPA.split(" ")[0]) / 4;
		    double db_rating = Double.parseDouble(rating.split(" ")[0]) / 5;
		    double scores = db_rating * (100 - gpaWeight) + db_avgGPA * gpaWeight;
		    overallRating = Math.round(scores * 100) / 100.0 + "";
		}

		String formatName = name.replaceAll("\\(.*\\)", "").replace("CV Honors", "CV");

		HtmlAnchor a = (HtmlAnchor) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]/td[2]/a");
		// System.out.println(a.getAttribute("href"));
		String url = a.getAttribute("href").split("search/")[1];

		String sect = "N/A";
		try {
		    String[] cc = url.split("\\.")[0].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
		    sect = cc[0].toUpperCase() + " " + cc[1];
		} catch (Exception e) {
		}

		// line += "<tr>";
		line += "<td>" + open + "</td>";
		line += "<td>" + sect + "</td>";
		line += "<td><a class='popup_details' target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://coursebook.utdallas.edu/clips/clip-section-v2.zog?id="
			+ url + "\">";
		if (name.contains("CV Honors"))
		    line += "<b>" + formatName + "</b></a></td>";
		else
		    line += formatName + "</a></td>";
		line += "<td>" + prof + "</td>";

		if (rating.equals(no_rmp_data)) {
		    line += "<td>" + rating + "</td>";
		} else {
		    String gtid = "";
		    gtid = rating.split("@@")[1];
		    rating = rating.split("@@")[0];
		    line += "<td><a class='popup_rmp' href=\"https://www.ratemyprofessors.com/ShowRatings.jsp?tid="
			    + gtid + "\">" + rating + "</a></td>";
		}

		String subj = sect.split(" ")[0];
		String num = sect.split(" ")[1];
		String pr = prof.contains("Staff") ? "" : prof;
		String searchString = "https://saitanayd.github.io/utd-grades/?subj=" + subj + "&num=" + num + "&prof="
			+ pr;

		if (avgGPA.equals(no_gpa_data))
		    line += "<td>" + avgGPA + "</td>";
		else
		    line += "<td><a class=\"popup_grade\" href=\"" + searchString + "\">" + avgGPA + "</a></td>";
		line += "<td>" + overallRating + "</td>"; // data-sort-method='number'

		// time shortening
		time = time.replaceAll("\r", "");
		String[] timeInfo = time.split("\n");
		// System.out.println("Time: " + time);
		int i = !timeInfo[0].contains("day") ? 1 : 0;
		String timeFormatted = "";
		if (timeInfo.length != 0 && timeInfo.length % 3 == 1) {
		    timeFormatted = timeInfo[0] + " ";
		}
		while (i < timeInfo.length) {
		    String days = timeInfo[i++];
		    String timeRange = timeInfo[i++];
		    String location = timeInfo[i++];

		    days = days.replace("Tuesday & Thursday", "TTh");
		    days = days.replace("Monday & Wednesday", "MW");
		    days = days.replace("Monday, Wednesday, Friday", "MWF");
		    days = days.replace("Monday & Wednesday", "MW");

		    timeFormatted += days + " " + timeRange + " " + location + "\n";
		}

		line += "<td><a class='add' value='" + sect + " -- " + prof + " -- " + overallRating + "!!"
			+ time.replaceAll("\n", "@@") + "' onclick='addCourse(this)'>Add</a></td>";
		line += "<td>" + timeFormatted + "</td>";

		// line += "</tr>";
		output += line;
		if (se != null) {
		    try {
			se.send(SseEmitter.event().data(line));
		    } catch (Exception e) {
			System.out.println("bye bye 1");
			return "remove me";
		    }
		}
		line = "";
		System.out
			.println("Processing " + padRight(prof, 40) + (System.currentTimeMillis() - timeTrack) + "ms");
	    }
	    output += "</table>";
	} catch (Exception e) {
	    e.printStackTrace();
	}

	if (se != null) {
	    try {
		se.send(SseEmitter.event().data("done"));
	    } catch (Exception e) {
		System.out.println("bye bye 2");
		return "remove me";
	    }
	}
	return output;
    }

    public void findBest(ArrayList<String> courses) {

    }

}
