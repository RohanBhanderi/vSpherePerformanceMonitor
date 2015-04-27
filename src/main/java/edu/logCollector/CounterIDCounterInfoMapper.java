package edu.logCollector;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.HashMap;

import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class CounterIDCounterInfoMapper {
	
	/** HashMap Declarations **/
	// Map of counter IDs indexed by counter name.
	// The full counter name is the hash key - group.name.ROLLUP-TYPE.
	private HashMap<String, Integer> countersIdMap = new HashMap<String, Integer>();
	
	// Map of performance counter data (PerfCounterInfo) indexed by counter ID (PerfCounterInfo.key property).
	private HashMap<Integer, PerfCounterInfo> countersInfoMap = new HashMap<Integer, PerfCounterInfo>();
	

	
	public CounterIDCounterInfoMapper(ServiceInstance si, VirtualMachine vm) throws RuntimeFault, RemoteException, MalformedURLException {
		
		PerformanceManager perfMgr = si.getPerformanceManager();

		// find out the refresh rate for the virtual machine
		PerfProviderSummary pps = perfMgr.queryPerfProviderSummary(vm);
		int refreshRate = pps.getRefreshRate().intValue();
		
		// retrieve all the available perf metrics for vm
		PerfMetricId[] pmis = perfMgr.queryAvailablePerfMetric(vm, null, null, refreshRate);
		
		int[] counterIds = new int[pmis.length];
		for(int i=0; i<pmis.length; i++) {
			counterIds[i] = pmis[i].getCounterId();
		}
		
		//PerfCounterInfo[] perfCounters = perfMgr.queryPerfCounter(counterIds);
		
		PerfCounterInfo[] perfCounters = perfMgr.getPerfCounter();

		
		/*
		 * Cycle through the PerfCounterInfo objects and load the maps.
		 */
		for(PerfCounterInfo perfCounter : perfCounters) {
		    Integer counterId = new Integer(perfCounter.getKey());
		    /*
		     * This map uses the counter ID to index performance counter metadata.
		     */
		    countersInfoMap.put(counterId, perfCounter);
		    /*
		     * Obtain the name components and construct the full counter name,
		     * for example – power.power.AVERAGE.
		     * This map uses the full counter name to index counter IDs.
		     */
		    String counterGroup = perfCounter.getGroupInfo().getKey();
		    String counterName = perfCounter.getNameInfo().getKey();
		    String counterRollupType = perfCounter.getRollupType().toString();
		    String fullCounterName = counterGroup + "." + counterName + "." + counterRollupType;
		    /*
		     * Store the counter ID in a map indexed by the full counter name.
		     */
		    countersIdMap.put(fullCounterName, counterId);
		}
//		
//		System.out.println("countersInfoMap: " + countersInfoMap);
//		System.out.println("countersIdMap: " + countersIdMap);
	}
	
	
	public Integer get(String fullCounterName) {
		return countersIdMap.get(fullCounterName);
	}
	
	public PerfCounterInfo get(Integer counterId) {
		return countersInfoMap.get(counterId);
	}

}
