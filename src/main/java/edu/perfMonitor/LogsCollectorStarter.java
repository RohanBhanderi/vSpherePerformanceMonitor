package edu.perfMonitor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.List;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

import edu.infrastructure.InfraEndpoints;
import edu.infrastructure.Infrastructure;
import edu.logCollector.CounterIDCounterInfoMapper;
import edu.logCollector.RealtimePerfMonitor;


public class LogsCollectorStarter {

	private ServiceInstance center;
	private ManagedEntity vm;
	private ManagedEntity host;
	private CounterIDCounterInfoMapper ccm;
	
	
	// constructor
	public LogsCollectorStarter() throws Exception {

		center = Infrastructure.getInstance().getServiceInstance();
		vm = Infrastructure.getInstance().getVirtualMachine(getVirtualMachineName());
		host = Infrastructure.getInstance().getHostOfVm(vm);
		ccm = new CounterIDCounterInfoMapper(center, (VirtualMachine)vm);
	}
	
	public void start() throws Exception {
		long interval = InfraEndpoints.REFRESH_INTERVAL * 1000;
		
		while(true) {
			System.out.println("Extracting logs...");
			startRealtimePerfMonitor(vm, host);//, host
			
			System.out.println("Sleeping "+ InfraEndpoints.REFRESH_INTERVAL +" seconds...\n\n");
			Thread.sleep(interval);
		}		
		
	}
	
	private void startRealtimePerfMonitor(ManagedEntity ... lists ) throws Exception {
		for(ManagedEntity vm : lists) {
			RealtimePerfMonitor.printStats(center, vm, ccm);
		}
	}
	
	private static String getVirtualMachineName(){
		
		InetAddress iAddress = null;
		try {
			iAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String currentIp = iAddress.getHostAddress();
		
		List<ManagedEntity> vms = null;
		try {
			vms = Infrastructure.getAllInstance(Infrastructure.getInstance().getServiceInstance().getRootFolder(),"VirtualMachine");
			
			for (ManagedEntity onevm : vms) {
				VirtualMachine vm = (VirtualMachine) onevm;
				if(!vm.getConfig().template){
					if(vm.getGuest().getIpAddress() != null && currentIp.equals(vm.getGuest().getIpAddress())){
						String vmName = vm.getName();
						return vmName;
					}
				}
			}
		} catch (InvalidProperty e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "T12-VM01-Ubu";
	}
}
