package com.mvcApp.test.mvcApp.rest;

import java.io.IOException;
import java.net.MalformedURLException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

public class Test {

	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		UTDDB.init();
		UTDDB.setupClient();
		String output = UTDDB.newSearch("Ivor Page", "term_20f");
		System.out.println(output);
	}

}