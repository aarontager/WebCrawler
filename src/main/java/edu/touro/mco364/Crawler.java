package edu.touro.mco364;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

public class Crawler implements Runnable {
	private String url;
	private Document webPage;
	private Set<String> foundLinks;
	private Matcher finder;

	public Crawler(String url) {
		foundLinks = new HashSet<>();
		this.url = url;
	}

	@Override
	public void run() {
		try {
			webPage = Jsoup.connect(url).ignoreHttpErrors(true).followRedirects(true).userAgent("Mozilla/17.0").get();

			finder = Main.EMAIL_PATTERN.matcher(webPage.text());
			while(finder.find()) {
				String email = finder.group().toLowerCase();
				Main.emailsFound.add(email);
			}

			foundLinks.addAll(webPage.select("a[href]").eachAttr("abs:href"));
			foundLinks.removeAll(Main.linksVisited);

			foundLinks.removeIf(link ->
					Main.BAN_LIST_PATTERN.matcher(link).find()
			);

			foundLinks.removeIf(link ->
					Main.linksVisited.contains(link.substring(Math.max(0, link.indexOf('#'))))
			);

			foundLinks.removeIf(link ->
					link.contains("../")
			);

			foundLinks.removeIf(link ->
					!link.contains("//")
			);

			for(String link : foundLinks) {
				String[] splitURL = (new URL(link)).getHost().split("\\.");
				String secondLevelDomain = splitURL[Math.max(splitURL.length - 2, 0)];
				if(visitsLeft(secondLevelDomain)) {
					synchronized(Main.linksToVisit) {
						if(!Main.linksToVisit.contains(link)) {
							if(Main.PRIORITY_LIST_PATTERN.matcher(link.substring(Math.max(link.lastIndexOf('.'), 0))).find()) {
								Main.linksToVisit.addFirst(link);
							}
							else {
								Main.linksToVisit.addLast(link);
							}
						}
					}
				}
			}
			Main.linksProcessed.add(url);
		}
		catch(IOException e) {
//				e.printStackTrace();
		}
	}

	private boolean visitsLeft(String site) {
		int visitCount = (Main.siteCounter.get(site) != null)? Main.siteCounter.get(site) : 0;
		if(visitCount < Main.MAX_VISITS) {
			Main.siteCounter.put(site, visitCount + 1);
			return true;
		}
		return Main.PRIORITY_LIST_PATTERN.matcher(site).find();
	}
}
