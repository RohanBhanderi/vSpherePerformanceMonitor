package edu.perfMonitor;

public class PerfMonitor {

	public static void main(String[] args) throws Exception {
		//args = new String[]{"T12-VM01-Ubu"};
		LogsCollectorStarter logCollector = new LogsCollectorStarter();
		logCollector.start();
	}

}
