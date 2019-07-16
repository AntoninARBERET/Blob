package eu.su.mas.dedale.sandbox;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import eu.su.mas.dedale.mas.knowledge.SocTabEntry;

public class testMap {

	public static void main(String[] args) {
		HashMap<String, SocTabEntry> testMap = new HashMap<String, SocTabEntry>();
		HashMap<String, SocTabEntry> testMap2 = new HashMap<String, SocTabEntry>();
		testMap.put("A", new SocTabEntry("A", 12));
		System.out.println(testMap);
		
		for(Map.Entry<String, SocTabEntry> entry : testMap.entrySet()) {
			entry.getValue().setGoodness(15);
		}
		
		System.out.println(testMap);
		testMap2.putAll(testMap);
		for(Map.Entry<String, SocTabEntry> entry : testMap.entrySet()) {
			entry.getValue().setGoodness(1);
		}
		System.out.println(testMap2);
	}

}
