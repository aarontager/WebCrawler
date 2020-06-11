package edu.touro.mco364;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Main {
	public static final int EMAIL_MAX = 10_000, MAX_VISITS = 15;
	public static final Pattern EMAIL_PATTERN = Pattern.compile("[\\w%.+\\-]+@[\\w.\\-]+\\.[a-zA-z]{2,4}"),
			BAN_LIST_PATTERN = Pattern.compile("gif|mp[34]|png|jpe?g|pdf|zip|doc|bz2|mailto|tel|javascript",
					Pattern.CASE_INSENSITIVE),
			PRIORITY_LIST_PATTERN = Pattern.compile("contact|directory|feedback|member|info|email|help",
					Pattern.CASE_INSENSITIVE);

	protected static Deque<String> linksToVisit;
	protected static Set<String> linksVisited,
			linksProcessed,
			emailsFound;
	protected static Map<String, Integer> siteCounter;

	public static void main(String[] args) {
		linksToVisit = new ConcurrentLinkedDeque<>();
		linksVisited = ConcurrentHashMap.newKeySet();
		linksProcessed = ConcurrentHashMap.newKeySet();
		emailsFound = ConcurrentHashMap.newKeySet();
		siteCounter = new ConcurrentHashMap<>();
		ExecutorService threadPool = Executors.newFixedThreadPool(500);

		linksToVisit.add("https://www.touro.edu");

		while(emailsFound.size() < EMAIL_MAX) {
			if(!linksToVisit.isEmpty()) {
				synchronized(linksToVisit) {
					String currentLink = linksToVisit.poll();
					threadPool.execute(new Crawler(currentLink));
					linksVisited.add(currentLink);
				}
				System.out.println("Emails found: " + emailsFound.size());
				System.out.println("Links visited: " + linksProcessed.size());
				System.out.println();
			}
		}
		threadPool.shutdownNow();

		System.out.println("Total emails: " + emailsFound.size());
		System.out.println("Total links visited: " + linksProcessed.size());
		EmailUploader.uploadEmails();
		System.out.println("Upload complete!");
	}
}
