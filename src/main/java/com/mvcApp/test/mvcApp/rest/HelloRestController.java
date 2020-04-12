package com.mvcApp.test.mvcApp.rest;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

@Controller
public class HelloRestController {

	@RequestMapping("/")
	public String home() {
		return "home.jsp";
	}
	
	
	@RequestMapping("rmp")
	public ModelAndView rmp(@RequestParam String dept, @RequestParam String course) {
		String output = "<table style=\"width: 100%;\" id=\"professors\"><thead><tr data-sort-method=\"none\"><th>Open Status</th>"
				+ "<th>Name</th>"
				+ "<th>Professor</th>"
				+ "<th>Rating</th>"
				+ "<th>Schedule</th></tr></thead>";

		String term = "term_20f?";

		String searchQuery = dept + "/" + course + "/" + term;

		WebClient client = new WebClient();
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		
		HashMap<String, String> ratings = new HashMap<String, String>();
		try {
			String searchUrl = "https://coursebook.utdallas.edu/" + searchQuery;
			HtmlPage page = client.getPage(searchUrl);

			List l = page.getByXPath("//*/td[4]");

			for (int section = 1; section <= l.size(); section++) {
				HtmlSpan open = (HtmlSpan) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]/td[1]/span");
				HtmlTableDataCell name = (HtmlTableDataCell) page
						.getFirstByXPath("//*[@id=\"r-" + section + "\"]/td[3]");
				HtmlTableDataCell prof = (HtmlTableDataCell) page
						.getFirstByXPath("//*[@id=\"r-" + section + "\"]/td[4]");
				HtmlTableDataCell time = (HtmlTableDataCell) page
						.getFirstByXPath("//*[@id=\"r-" + section + "\"]/td[5]");

				String rating = "Not Found";
				if(!ratings.containsKey(prof.asText())) {
					if (!prof.asText().toLowerCase().contains("staff")) {
						// Rate My Professor Scan
						String url = "https://www.ratemyprofessors.com/search.jsp?query=" + prof.asText().trim();
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

							HtmlAnchor a = (HtmlAnchor) rmp
									.getFirstByXPath("//*[@id=\"searchResultsBox\"]/div[2]/ul/li[" + index + "]/a");
							rmp = a.click();
							rating = ((HtmlDivision) rmp
									.getFirstByXPath("//*[@id=\"root\"]/div/div/div[2]/div[1]/div[1]/div[1]/div[1]/div/div[1]"))
											.asText();
							rating += " based on " + ((HtmlAnchor) rmp
									.getFirstByXPath("//*[@id=\"root\"]/div/div/div[2]/div[1]/div[1]/div[1]/div[2]/div/a"))
											.asText();
							ratings.put(prof.asText(), rating);
						}
					}
				} else {
					rating = ratings.get(prof.asText());
				}
				
				output += "<tr>";
				output += "<td>" + open.asText() + "</td>";
				if (name.asText().contains("CV Honors"))
					output += "<td>" + name.asText() + "</td>";
				else
					output += "<td>" + name.asText() + "</td>";
				output += "<td>" + prof.asText() + "</td>";
				output += "<td>" + rating + "</td>";
				output += "<td>" + time.asText() + "</td>";
				output += "</tr>";
			}
			
			output += "</table>";

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ModelAndView model = new ModelAndView("/");
		model.addObject("output", output);
		return model;
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
