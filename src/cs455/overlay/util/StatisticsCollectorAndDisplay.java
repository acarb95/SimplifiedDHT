package cs455.overlay.util;

import java.util.ArrayList;

/***
 * Collects all the statistics into an array and then displays them with formatting.
 * 
 * @author acarbona
 *
 */
public class StatisticsCollectorAndDisplay {
	
	private volatile ArrayList<StatisticsEntry> entries;
	private volatile int size = 0;
	
	public StatisticsCollectorAndDisplay() {
		entries = new ArrayList<StatisticsEntry>();
	}
	
	public synchronized int getSize() {
		return size;
	}
	
	public synchronized void addInformation(int nodeID, int sent, int relayed, int received, long sumSent, long sumReceived) {
		entries.add(new StatisticsEntry(sent, received, relayed, sumSent, sumReceived, nodeID));
		size++;
	}
	
	public synchronized void printInformation() {
		System.out.printf("\t%s\t%s\t%s\t\t%s\t\t%s\n", "Packets Sent", "Packets Received", "Packets Relayed", "Sum Values Sent", "Sum Values Received");
		int i = 1;
		for (StatisticsEntry e : entries) {
			if (e.getID() != -1) {
				System.out.printf("Node %d\t%d\t\t%d\t\t\t%d\t\t\t%d\t\t%d\n", i, e.getSent(), e.getReceived(), e.getRelayed(), e.getSumSent(), e.getSumReceived());
				i++;
			} else {
				System.out.printf("Sum \t%d\t\t%d\t\t\t%d\t\t\t%d\t\t%d\n", e.getSent(), e.getReceived(), e.getRelayed(), e.getSumSent(), e.getSumReceived());
			}
		}
	}
	
	public void clear() {
		entries = new ArrayList<StatisticsEntry>();
		size = 0;
	}
	
	public synchronized void sumInformation() {
		entries.add(new StatisticsEntry(sumPacketsSent(), sumPacketsReceived(), sumPacketsRelayed(), sumSentSummations(), sumReceivedSummations(), -1));
	}
	
	private synchronized int sumPacketsSent() {
		int sum = 0;
		
		for (StatisticsEntry e : entries) {
			sum+= e.getSent();
		}
		
		return sum;
	}
	
	private synchronized int sumPacketsRelayed() {
		int sum = 0;
		
		for (StatisticsEntry e : entries) {
			sum+= e.getRelayed();
		}
		
		return sum;
	}
	
	private synchronized int sumPacketsReceived() {
		int sum = 0;
		
		for (StatisticsEntry e : entries) {
			sum+= e.getReceived();
		}
		
		return sum;
	}
	
	private synchronized long sumSentSummations() {
		long sum = 0;
		
		for (StatisticsEntry e : entries) {
			sum+= e.getSumSent();
		}
		
		return sum;
	}
	
	private synchronized long sumReceivedSummations() {
		long sum = 0;
		
		for (StatisticsEntry e : entries) {
			sum+= e.getSumReceived();
		}
		
		return sum;
	}
}
