package edu.perfMonitor;

public class PerfMonitor {

	public static void main(String[] args) throws Exception {
		//args = new String[]{"T12-VM01-Ubu"};
		if(args == null || args.length <= 0){
			args = new String[]{"T12-VM01-Ubu"};
		}
		LogsCollectorStarter logCollector = new LogsCollectorStarter(args[0]);
		logCollector.start();
	}

}
