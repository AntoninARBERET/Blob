package eu.su.mas.dedale.sandbox;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import eu.su.mas.dedale.mas.knowledge.SocTabEntry;

public class testMap {

	public static void main(String[] args) {
		HashMap<String, SocTabEntry> testMap = new HashMap<String, SocTabEntry>();
		testMap.put("A", new SocTabEntry("A", 12));
		System.out.println(testMap);
		
		for(Map.Entry<String, SocTabEntry> entry : testMap.entrySet()) {
			entry.getValue().setGoodness(15);
		}
		System.out.println(testMap.containsKey("A"));
		
		
		System.out.println(1/2);
		
		ArrayList<String> L = new ArrayList<String>();
		L.add("lol");
		L.add("lel");
		L.remove(0);
		System.out.println(L.get(0));
		int a =17;
		System.out.println(a++);
		
	}

}
