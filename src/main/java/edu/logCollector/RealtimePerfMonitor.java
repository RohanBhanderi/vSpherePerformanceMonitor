package edu.logCollector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfEntityMetricCSV;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricSeriesCSV;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

import edu.infrastructure.InfraEndpoints;


public class RealtimePerfMonitor {

	public static void printStats(ServiceInstance si, ManagedEntity vm, CounterIDCounterInfoMapper ccm) throws Exception {
		File file = null;
		try {
			// Create log file
			String fileName = InfraEndpoints.OUT_LOG_DIR_PATH + "Perf" + InfraEndpoints.OUT_FILE_EXTN;
			file = new File(fileName);
			File parentFolders = file.getParentFile();
			// check if directory already exists or not
			if (!parentFolders.exists()) {
				// create the non existent directory if any
				if (parentFolders.mkdirs()) {
					System.out.println("Log file directory at " + parentFolders.getCanonicalPath() + " created successfully");
				} else {
					System.out.println("Log file directory at " + parentFolders.getCanonicalPath() + " creation failed.");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		// true => append
		try(FileOutputStream out = new FileOutputStream(file, true))
		{
			// Connect print stream to the output stream
			try(PrintStream ps = new PrintStream(out))
			{

				/**Stats config**/   
				PerformanceManager perfMgr = si.getPerformanceManager();

				// find out the refresh rate for the virtual machine
				PerfProviderSummary pps = perfMgr.queryPerfProviderSummary(vm);
				int refreshRate = pps.getRefreshRate().intValue();

	
				// specific stats
				PerfMetricId[] pmis = createPerfMetricId(InfraEndpoints.PERF_COUNTERS, ccm);


				/**
				 * Create the query specification for queryPerf().
				 * Specify ONLY 1 value showing up
				 **/
				PerfQuerySpec qSpec = createPerfQuerySpec(vm, pmis, 1, refreshRate);


				/**
				 * Call queryPerf()
				 *
				 * QueryPerf() returns the statistics specified by the provided
				 * PerfQuerySpec objects. When specified statistics are unavailable -
				 * for example, when the counter doesn't exist on the target
				 * ManagedEntity - QueryPerf() returns null for that counter.
				 **/
				PerfEntityMetricBase[] retrievedStats = perfMgr.queryPerf(new PerfQuerySpec[] {qSpec});



				/**
				 * Cycle through the PerfEntityMetricBase objects. Each object contains
				 * a set of statistics for a single ManagedEntity.
				 **/
				for(PerfEntityMetricBase singleEntityPerfStats : retrievedStats) {
					/*
					 * Cast the base type (PerfEntityMetricBase) to the csv-specific sub-class.
					 */
					PerfEntityMetricCSV entityStatsCsv = (PerfEntityMetricCSV)singleEntityPerfStats;
					/* Retrieve the list of sampled values. */
					PerfMetricSeriesCSV[] metricsValues = entityStatsCsv.getValue();
					if(metricsValues == null) {
						System.out.println("No stats retrieved. " + "Check whether the virtual machine is powered on.");
						throw new Exception();
					}


					/** 
					 * Output format:
					 * Timestamp VMType VMName GroupInfo NameInfo rollupType UnitInfo value
					 **/


					/**
					 * Retrieve time interval information (PerfEntityMetricCSV.sampleInfoCSV).
					 **/

					/*String csvTimeInfoAboutStats = entityStatsCsv.getSampleInfoCSV();
					// Print the time and interval information
					ps.println("Collection: interval (seconds),time (yyyy-mm-ddThh:mm:ssZ)");
					ps.println(csvTimeInfoAboutStats);*/
					//java.util.Date date = new java.util.Date();
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
					String nowAsString = df.format(new Date());

					/**
					 * Cycle through the PerfMetricSeriesCSV objects. Each object contains
					 * statistics for a single counter on the ManagedEntity.
					 **/
					StringBuffer sb = new StringBuffer("");
					//sb.append(new Timestamp(date.getTime()) + ",");
					sb.append(nowAsString + ",");
					sb.append(vm.getName());
					Map<String,String> values = new HashMap<String,String>();
					for(PerfMetricSeriesCSV csv : metricsValues) {
						/*
						 * Use the counterId to obtain the associated PerfCounterInfo object
						 */
						PerfCounterInfo pci = ccm.get(csv.getId().getCounterId());
						/* Print out the metadata for the counter. */
						
						//ps.print(new Timestamp(date.getTime()) + " ");
						if(vm instanceof VirtualMachine) {
							//ps.print("VirtualMachine " + vm.getName() + " ");
						} else if(vm instanceof HostSystem) {
							//ps.print("HostSystem " + vm.getName() + " ");
						}
						//ps.print(pci.getGroupInfo().getKey() + " " + pci.getNameInfo().getKey() + " " + pci.getRollupType() + " " + pci.getUnitInfo().getKey() + " ");

						if(Double.parseDouble(csv.getValue()) < 0) {
							//ps.print(0);
						}
						else {
							String perfCounter = pci.getGroupInfo().getKey()+"."+pci.getNameInfo().getKey()+"."+pci.getRollupType();
							if(pci.getGroupInfo().getKey().equals("cpu")) {
								//ps.print((Double.parseDouble(csv.getValue())/100) + "\n");
								values.put(perfCounter, (Double.parseDouble(csv.getValue())/100) + "");
							} 
							else if(pci.getGroupInfo().getKey().equals("mem")){
								values.put(perfCounter, (Double.parseDouble(csv.getValue())/100) + "");
							}
							else {
								//ps.print(csv.getValue() + "\n");
								values.put(perfCounter, csv.getValue() + "");
							}
						}
					}
					
					String[] counters = InfraEndpoints.PERF_COUNTERS;
					for (String counter : counters) {
						sb.append(",");
						sb.append((values.get(counter)!=null)?values.get(counter):0);
					}
					ps.println(sb.toString());
				}
			}//Try ps
		}//Try out
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private static PerfMetricId[] createPerfMetricId(String[] counterNames, CounterIDCounterInfoMapper ccm) {
		PerfMetricId[] pmis = new PerfMetricId[counterNames.length];

		for(int i=0; i<counterNames.length; i++) {

			// Create the PerfMetricId object for the counterName.
			// Use an asterisk to select all metrics associated with counterId (instances and rollup).

			PerfMetricId mid = new PerfMetricId();
			//System.out.println(counterNames[i]);
			// Get the ID for this counter. 
			mid.setCounterId(ccm.get(counterNames[i]));
			mid.setInstance("*");
			pmis[i] = mid;
		}
		return pmis;
	}

	private static PerfQuerySpec createPerfQuerySpec(ManagedEntity me, PerfMetricId[] metricIds, int maxSample, int interval) {
		PerfQuerySpec qSpec = new PerfQuerySpec();
		qSpec.setEntity(me.getMOR());
		// set the maximum of metrics to be return
		// only appropriate in real-time performance collecting
		qSpec.setMaxSample(new Integer(maxSample));
		qSpec.setMetricId(metricIds);
		// optionally you can set format as "normal"
		qSpec.setFormat("csv");
		// set the interval to the refresh rate for the entity
		qSpec.setIntervalId(new Integer(interval));

		return qSpec;
	}
}