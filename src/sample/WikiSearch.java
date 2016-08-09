package sample;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

import redis.clients.jedis.Jedis;


/**
 * Represents the results of a search query.
 *
 */
public class WikiSearch {
	
	// map from URLs that contain the term(s) to relevance score
	private Map<String, Double> map;

	/**
	 * Constructor.
	 * 
	 * @param map
	 */
	public WikiSearch(Map<String, Double> map) {
		this.map = map;
	}
	
	/**
	 * Looks up the relevance of a given URL.
	 * 
	 * @param url
	 * @return
	 */
	public Double getRelevance(String url) {
		Double relevance = map.get(url);
		return relevance==null ? 0: relevance;
	}
	
	/**
	 * Prints the contents in order of term frequency.
	 */
	private  void print() {
		List<Entry<String, Double>> entries = sort();
		for (Entry<String, Double> entry: entries) {
			System.out.println( entry );
		}
	}

	public Set<String> getKeys() {
		return map.keySet();
	}


	/**
	 * Computes the union of two search results.
	 *
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
		Map<String, Double> union = new HashMap<String, Double>(map);
		for (String term: that.map.keySet()) {
			double relevance = totalRelevance(this.getRelevance(term), that.getRelevance(term));
			union.put(term, relevance);
		}
		return new WikiSearch(union);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
		Map<String, Double> intersection = new HashMap<String, Double>();
		for (String term: map.keySet()) {
			if (that.map.containsKey(term)) {
				double relevance = totalRelevance(this.map.get(term), that.map.get(term));
				intersection.put(term, relevance);
			}
		}
		return new WikiSearch(intersection);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
		Map<String, Double> difference = new HashMap<String, Double>(map);
		for (String term: that.map.keySet()) {
			difference.remove(term);
		}
		return new WikiSearch(difference);
	}
	
	/**
	 * Computes the relevance of a search with multiple terms.
	 * 
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected double totalRelevance(Double rel1, Double rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance.
	 * 
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Double>> sort() {
		// NOTE: this can be done more concisely in Java 8.  See
		// http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java

		// make a list of entries
		List<Entry<String, Double>> entries =
				new LinkedList<Entry<String, Double>>(map.entrySet());

		// make a Comparator object for sorting
		Comparator<Entry<String, Double>> comparator =
				new Comparator<Entry<String, Double>>() {
					@Override
					public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
						return e1.getValue().compareTo(e2.getValue());
					}
				};

		// sort and return the entries
		Collections.sort(entries, comparator);
		return entries;
	}

	/**
	 * Performs a search and makes a WikiSearch object.
	 * 
	 * @param term
	 * @param index
	 * @return
	 */
	public static WikiSearch searchAnd(String term, JedisIndex index) {
		WikiSearch wiki_term;
		WikiSearch answer = null;
		String[] words = term.split(" ");
		for(int i = 0; i < words.length; i++) {
			wiki_term = new WikiSearch(index.getCountsFaster(words[i]));
			if(i == 0) {
				answer = wiki_term;
			} else {
				answer = answer.and(wiki_term);
			}

		}
		return answer;
	}

	public static WikiSearch search(String term, JedisIndex index) {
		StringBuffer searchTerm = new StringBuffer();
		String[] words = term.split(" ");
		LinkedList<String> termsToOr = new LinkedList<>();
		WikiSearch answer;

		for(String word:words) {
			if(word.equals("or")) {
				termsToOr.add(searchTerm.substring(0,searchTerm.length()-1));
				searchTerm.delete(0,searchTerm.length());
			} else {
				searchTerm.append(word + " ");
			}
		}
		termsToOr.add(searchTerm.substring(0,searchTerm.length()-1));
		answer = multipleOr(termsToOr,index);

		return answer;
	}

	public static WikiSearch multipleOr(LinkedList<String> words, JedisIndex index) {
			WikiSearch wiki_term;
			WikiSearch answer = null;
			for(int i = 0; i < words.size(); i++) {
				wiki_term = searchAnd(words.get(i),index);
				if(i == 0) {
					answer = wiki_term;
				} else {
					answer = answer.or(wiki_term);
				}
			}
			return answer;
	}

	public static void main(String[] args) throws IOException {
		
		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		
		// search for the first term
		String term1 = "java";
		System.out.println("Query: " + term1);
		WikiSearch search1 = search(term1, index);
		search1.print();
		
		// search for the second term
		String term2 = "programming";
		System.out.println("Query: " + term2);
		WikiSearch search2 = search(term2, index);
		search2.print();
		
		// compute the intersection of the searches
		System.out.println("Query: " + term1 + " AND " + term2);
		WikiSearch intersection = search1.and(search2);
		intersection.print();
	}
}
