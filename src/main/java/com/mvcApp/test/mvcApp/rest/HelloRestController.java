package com.mvcApp.test.mvcApp.rest;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static ArrayList<String> daysToArray(String days) {
		ArrayList<String> dates = new ArrayList<String>();
		if(days.contains("Monday"))
			dates.add("2020-04-20");
		else if(days.contains("Tuesday"))
			dates.add("2020-04-21");
		else if(days.contains("Wednesday"))
			dates.add("2020-04-22");
		else if(days.contains("Thursday"))
			dates.add("2020-04-23");
		else if(days.contains("Friday"))
			dates.add("2020-04-24");
		else if(days.contains("Saturday"))
			dates.add("2020-04-25");
		return dates;
	}
	
	@RequestMapping("/calendar")
	public void calendar(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		/// ModelAndView mav = new ModelAndView("calendar.jsp");
		HashMap<String, String[]> s = new HashMap<String, String[]>();
		if(session.getAttribute("classes") != null) {
			ArrayList<String> classes = (ArrayList<String>) session.getAttribute("classes");
			for(String cls : classes) {
				String label = cls.split(" -- ")[0]; // change to !! to get full label
				String[] info = cls.split("!!")[1].split("@@");
				if(info.length == 3 || info.length == 4) {
					int pre = info.length - 3;
					String days = info[0 + pre];
					String timeRange = info[1 + pre];
					String room = info[2 + pre];
					
					String[] begEnd = timeRange.split(" - ");
					begEnd[0] = (begEnd[0].length() == 6) ? "0" + begEnd[0] : begEnd[0];
					begEnd[1] = (begEnd[1].length() == 6) ? "0" + begEnd[1] : begEnd[1];
					if(begEnd[0].contains("pm")) 
						begEnd[0] = Integer.parseInt(begEnd[0].substring(0, 2)) + 12 + begEnd[0].substring(2, 5);
					else begEnd[0] = begEnd[0].substring(0, 5);
					if(begEnd[1].contains("pm")) 
						begEnd[1] = Integer.parseInt(begEnd[1].substring(0, 2)) + 12 + begEnd[1].substring(2, 5);
					else begEnd[1] = begEnd[1].substring(0, 5);
					
					timeRange = timeRange.replaceAll("am", "");
					ArrayList<String> ds = daysToArray(days);
					for(String s1 : ds) {
						String[] times = new String[] {s1 + "T" + begEnd[0], s1 + "T" + begEnd[1]};
						s.put(label, times);
					}
				}
			}
			
		}
		
		// s.put("CS 3345", new String[] { "2020-04-27T08:30:00", "2020-04-27T09:30:00" });
		// s.put("CS 1200", new String[] { "2020-04-27T10:30:00", "2020-04-27T12:30:00" });
		String output = "<script>var calendarEl = document.getElementById('calendar');"
				+ "var calendar = new FullCalendar.Calendar(calendarEl, {\r\n" + 
				"    	    plugins: [ 'timeGrid' ],\r\n" + 
				"    	    defaultView: 'timeGridWeek',\r\n" + 
				"    	    defaultDate: '2020-04-20',\r\n" + 
				"    	    header: {\r\n" + 
				"    	      left: 'prev,next',\r\n" + 
				"    	      center: 'title',\r\n" + 
				"    	      right: 'timeGridWeek'\r\n" + 
				"    	    },\r\n" + 
				"    	    events: [\r\n";
				
		for (Map.Entry<String, String[]> me : s.entrySet()) {
			output += "{";
			output += "title: '" + me.getKey() + "',";
			output += "start: '" + me.getValue()[0] + "',";
			output += "end: '" + me.getValue()[1] + "',";
			output += "},";
		}
		
		output += "    	    ]\r\n" + 
				"    	  });calendar.render();";
		System.out.println(output);

		try {
			request.setAttribute("events", OBJECT_MAPPER.writeValueAsString(output));
			request.getRequestDispatcher("calendar.jsp").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// mav.addObject("events", output);
		// return mav;
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

	@RequestMapping("/feedback")
	@ResponseBody
	public String feedback(@RequestParam String info) {
		try {
			String filename = "feedback.txt";
			FileWriter fw = new FileWriter(filename, true);
			fw.write(info + "\n");
			fw.close();
		} catch (IOException e) {
			System.out.println("Write error");
		}
		return "Saved!";
	}

	public String returnClasses(ArrayList<String> classes) {
		String classesList = "";
		for (int i = 0; i < classes.size(); i++) {
			String ret = classes.get(i).split("!!")[0];
			classesList += "<a class='drop' onclick='removeCourse(this)' value='" + classes.get(i) + "'>" + ret + "</a>"; // add remove
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
		if (sseEmitters.containsKey(id)) {
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
		if (sseEmitters.containsKey(id)) {
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
		// System.out.println("Completed (fake) Request in " + time / 1000.0 + "
		// seconds.");

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
