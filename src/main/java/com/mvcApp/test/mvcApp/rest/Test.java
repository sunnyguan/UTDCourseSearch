package com.mvcApp.test.mvcApp.rest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.MessagingException;

public class Test {

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

    static HashMap<String, String> profToRating = new HashMap<String, String>();
    static HashMap<String, String> profToGPA = new HashMap<String, String>();

    class Professor implements Comparable {
	String name;
	double rating;
	double gpa;
	int ratingCount;
	@Override
	public int compareTo(Object o) {
	    Professor o2 = (Professor) o;
	    if(o2.gpa != gpa) return (int) ((gpa - o2.gpa) * 100);
	    else return (int) ((rating - o2.rating) * 100);
	}
	public Professor(String name, double rating, double gpa, int ratingCount) {
	    super();
	    this.name = name;
	    this.rating = rating;
	    this.gpa = gpa;
	    this.ratingCount = ratingCount;
	}
    }
    
    public static void main(String[] args) throws MessagingException, InterruptedException, FileNotFoundException {
	readProfToGPA();
	readProfToRating();
	int i = 0;
	int limit = 10000;

	PrintWriter pw = new PrintWriter("test.csv");
	Test t = new Test();
	Plot myPlot = new Plot("Title", -1, 5, 0.5, -1, 6, .5); // x is GPA, y is Rating
	myPlot.setPointSize(5);
	ArrayList<Professor> profs = new ArrayList<Professor>();
	for (Map.Entry<String, String> gpa : profToGPA.entrySet()) {
	    if (i++ == limit)
		break;
	    String professor = gpa.getKey();
	    double avgGPA = Double.parseDouble(gpa.getValue().split(" ")[0]);
	    if (avgGPA != 0 && profToRating.containsKey(professor) && !profToRating.get(professor).startsWith("0")) {
		int numRatings = Integer.parseInt(profToRating.get(professor).split("on ")[1].split(" ratings")[0]);
		if (numRatings > 20) {
		    double avgRating = Double.parseDouble(profToRating.get(professor).split(" ")[0]);
		    pw.println(avgGPA + "," + avgRating);
		    profs.add(t.new Professor(professor, avgRating, avgGPA, numRatings));
		}
	    }
	}
	
	Collections.sort(profs);
	for(Professor p : profs) {
	    System.out.println(p.name + ": GPA is " + p.gpa + ", RMP is " + p.rating + " with " + p.ratingCount + " ratings.");
	    myPlot.addPoint(p.gpa, p.rating);
	    Thread.sleep(100);
	}
	
	
	System.out.println("done");
	pw.close();
    }
}
