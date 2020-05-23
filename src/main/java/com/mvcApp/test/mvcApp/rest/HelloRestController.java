package com.mvcApp.test.mvcApp.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

@EnableScheduling
@Controller
public class HelloRestController {

	public static final boolean FORCENEW = true;
	SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

	// @Scheduled(cron = "0 1 1 * * ?")
	@RequestMapping("/updateDB")
	@ResponseBody
	public String scheduleTaskWithFixedRate()
			throws FailingHttpStatusCodeException, MalformedURLException, IOException {
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
		return Math.round((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * 1.0 / 1024 / 1024)
				+ " MB used.";
	}

	@RequestMapping("/")
	public String index() {
		return "index.html";
	}

	@RequestMapping("/result")
	public ModelAndView result(@RequestParam String name, @RequestParam String email, @RequestParam int gender,
			@RequestParam int gender_o, @RequestParam int wake, @RequestParam int wake_o, @RequestParam int sleep,
			@RequestParam int sleep_o, @RequestParam int party, @RequestParam int party_o, @RequestParam int politics,
			@RequestParam String politics_o, @RequestParam int religion, @RequestParam String religion_o) {
		long t = System.currentTimeMillis();
		String output = UTDDB.processRoommate(name, email, gender, gender_o, wake, wake_o, sleep, sleep_o, party,
				party_o, politics, politics_o, religion, religion_o);
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

	@RequestMapping("rmp_temp")
	public ModelAndView rmp(@RequestParam String course, @RequestParam String term, HttpSession session) {
		long t = System.currentTimeMillis();

		if (course.toLowerCase().startsWith("cs ")) {
			course = course.toLowerCase().replace("cs ", "cs"); // quick fix for CS search issue
		}

		String output = UTDDB.rmp(course, term, null);
		double time = (System.currentTimeMillis() - t);
		System.out.println("Completed (fake) Request in " + time / 1000.0 + " seconds.");

		ArrayList<String> hist = (ArrayList<String>) session.getAttribute("history");
		if (hist == null)
			hist = new ArrayList<String>();
		hist.add(course + "@@" + term);
		session.setAttribute("history", hist);

		String searchHistory = "";
		for (int i = 0; i < hist.size(); i++) {
			String s = hist.get(i);
			String c1 = s.split("@@")[0];
			String c2 = s.split("@@")[1];
			String cct = "<a href=\"rmp?term=" + c2 + "&course=" + c1 + "\">" + c1 + "</a>";
			searchHistory += cct + (i == hist.size() - 1 ? "." : ", ");
		}

		ArrayList<String> classes = (ArrayList<String>) session.getAttribute("classes");
		if (classes == null) {
			classes = new ArrayList<String>();
		}
		session.setAttribute("classes", classes);
		String classesList = returnClasses(classes);

		ModelAndView model = new ModelAndView("/home");
		model.addObject("output", output);
		model.addObject("course", course);
		model.addObject("classes", classesList);
		model.addObject("time", time / 1000.0);
		model.addObject("history", searchHistory);
		model.addObject("numProfs", UTDDB.getProfs());
		return model;
	}

	@RequestMapping("/clear_history")
	@ResponseBody
	public String clear_history(HttpSession session) {
		ArrayList<String> hist = (ArrayList<String>) session.getAttribute("history");
		if (hist != null && hist.size() != 0)
			hist = new ArrayList<String>();
		session.setAttribute("history", hist);
		return "";
	}

	@RequestMapping("/add_course")
	@ResponseBody
	public String add_course(@RequestParam String course, HttpSession session) {
		ArrayList<String> classes = (ArrayList<String>) session.getAttribute("classes");
		if (classes == null)
			classes = new ArrayList<String>();
		classes.add(course);
		session.setAttribute("classes", classes);
		return returnClasses(classes);
	}

	@RequestMapping("/remove_course")
	@ResponseBody
	public String remove_course(@RequestParam String course, HttpSession session) {
		ArrayList<String> classes = (ArrayList<String>) session.getAttribute("classes");
		if (classes == null)
			classes = new ArrayList<String>();
		if (classes.contains(course))
			classes.remove(course);
		session.setAttribute("classes", classes);
		return returnClasses(classes);
	}

	public String returnClasses(ArrayList<String> classes) {
		String classesList = "";
		for (int i = 0; i < classes.size(); i++) {
			classesList += "<a class='drop' onclick='removeCourse(this)'>" + classes.get(i) + "</a>"; // add remove
																										// button
		}
		return classesList;
	}

	private static Map<String, SseEmitter> sseEmitters = new HashMap<String, SseEmitter>();

	private synchronized SseEmitter newEmitterForUser(String username) {

		SseEmitter emitter = new SseEmitter();
		
		// if(sseEmitters.containsKey(username)) sseEmitters.get(username).complete();

		Runnable remover = new Runnable() {
			@Override
			public void run() {
				removeEmitter(username);
			}
		};

		// emitter.onCompletion(remover);
		// emitter.onTimeout(remover);

		sseEmitters.put(username, emitter);

		return emitter;
	}

	private synchronized SseEmitter getEmitterForUser(String username) {
		return sseEmitters.get(username);
	}

	synchronized void removeEmitter(String username) {
		sseEmitters.remove(username);
	}

	void notifyUser(String username, Object data) {
		SseEmitter emitter = getEmitterForUser(username);

		if (emitter != null)
			try {
				emitter.send(SseEmitter.event().data(data));
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	@RequestMapping("/sendMsg")
	@ResponseBody
	public String testMsg(@RequestParam String course, @RequestParam String term, HttpSession session) {
		String id = session.getId();
		if(sseEmitters.containsKey(id)) {
			UTDDB.rmp(course, term, sseEmitters.get(id));
			// notifyUser(id, course);
		}
		return "test";
	}
	
	@RequestMapping("/feed")
	public ResponseBodyEmitter feed(HttpSession session) {
		System.out.println("Feed Reporting");
		SseEmitter emitter = sseEmitters.get(session.getId());
		System.out.println(session.getId() + " null?: " + (emitter == null));
		return emitter;
	}
	
	@RequestMapping("rmp")
	public ModelAndView stream(@RequestParam String course, @RequestParam String term, HttpSession session) {
		System.out.println("RMP2 reporting");
		long t = System.currentTimeMillis();
		
		String id = session.getId();
		if(sseEmitters.containsKey(id)) {
			sseEmitters.get(id).complete();
		}
		newEmitterForUser(id);
		System.out.println(session.getId() + " null?: " + (sseEmitters.get(id) == null));
		
		// run test from streaming
		if (course.toLowerCase().startsWith("cs ")) {
			course = course.toLowerCase().replace("cs ", "cs"); // quick fix for CS search issue
		}
		final String cc = course;
		Thread one = new Thread() {
		    public void run() {
		    	String id = session.getId();
				UTDDB.rmp(cc, term, sseEmitters.get(id));
		    }
		};
		one.start();
		
		// String output = UTDDB.rmp(course, term, null);
		// double time = (System.currentTimeMillis() - t);
		// System.out.println("Completed (fake) Request in " + time / 1000.0 + " seconds.");

		ArrayList<String> hist = (ArrayList<String>) session.getAttribute("history");
		if (hist == null)
			hist = new ArrayList<String>();
		hist.add(course + "@@" + term);
		session.setAttribute("history", hist);

		String searchHistory = "";
		for (int i = 0; i < hist.size(); i++) {
			String s = hist.get(i);
			String c1 = s.split("@@")[0];
			String c2 = s.split("@@")[1];
			String cct = "<a href=\"rmp?term=" + c2 + "&course=" + c1 + "\">" + c1 + "</a>";
			searchHistory += cct + (i == hist.size() - 1 ? "." : ", ");
		}

		ArrayList<String> classes = (ArrayList<String>) session.getAttribute("classes");
		if (classes == null) {
			classes = new ArrayList<String>();
		}
		session.setAttribute("classes", classes);
		String classesList = returnClasses(classes);

		long time = (System.currentTimeMillis() - t);
		
		ModelAndView model = new ModelAndView("/home");
		// model.addObject("output", output);
		model.addObject("course", course);
		model.addObject("classes", classesList);
		model.addObject("time", "---");
		model.addObject("history", searchHistory);
		model.addObject("numProfs", UTDDB.getProfs());
		return model;
	}
}
