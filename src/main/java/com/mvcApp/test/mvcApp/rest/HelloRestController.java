package com.mvcApp.test.mvcApp.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.http.HttpSession;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

@EnableScheduling
@Controller
public class HelloRestController {

	public static final boolean FORCENEW = false;
	SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
	
	// @Scheduled(cron = "0 1 1 * * ?")
	@RequestMapping("/updateDB")
	@ResponseBody
	public String scheduleTaskWithFixedRate() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
	    UTDDB.updateDB();
	    return "done! you'll never see this but hey, at least I tried!";
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
		long t = System.currentTimeMillis();
		String output = UTDDB.processRoommate(name, email, gender, gender_o, wake, wake_o, sleep, sleep_o, party, party_o, politics, politics_o, religion, religion_o);
		double time = (System.currentTimeMillis() - t);
		ModelAndView model = new ModelAndView("/resultTemp");
		model.addObject("time", time);
		model.addObject("numRoommates", UTDDB.getRoommateSize());
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
	
	public static void startup() {
		UTDDB.init();
	}
	
	public String padRight(String s, int n) {
		return String.format("%-" + n + "s", s);
	}
	
	@RequestMapping("rmp")
	public ModelAndView rmp(@RequestParam String course, @RequestParam String term, HttpSession session) {
		long t = System.currentTimeMillis();
		
		if(term.toUpperCase().startsWith("CS ")) {
			term = term.replace("CS ", "CS"); // quick fix for CS search issue
		}
		
		String output = UTDDB.rmp(course, term);
		double time = (System.currentTimeMillis() - t);
		System.out.println("Completed Request in " + time/1000.0 + " seconds.");
		
		ArrayList<String> hist = (ArrayList<String>) session.getAttribute("history");
		if(hist == null) hist = new ArrayList<String>();
		hist.add(course + "@@" + term);
		session.setAttribute("history", hist);
		
		String searchHistory = "";
		for(int i = 0; i < hist.size(); i++) {
			String s = hist.get(i);
			String c1 = s.split("@@")[0];
			String c2 = s.split("@@")[1];
			String cct = "<a href=\"rmp?term=" + c2 + "&course=" + c1 + "\">" + c1 + "</a>";
			searchHistory += cct + (i == hist.size() - 1 ? "." : ", ");
		}
		
		ModelAndView model = new ModelAndView("/home");
		model.addObject("output", output);
		model.addObject("course", course);
		model.addObject("time", time / 1000.0);
		model.addObject("history", searchHistory);
		model.addObject("numProfs", UTDDB.getProfs());
		return model;
	}
	
	@RequestMapping("/clear_history")
	@ResponseBody
	public String clear_history(HttpSession session) {
		ArrayList<String> hist = (ArrayList<String>) session.getAttribute("history");
		if(hist != null && hist.size() != 0) hist = new ArrayList<String>();
		session.setAttribute("history", hist);
		return "";
	}
	
}
