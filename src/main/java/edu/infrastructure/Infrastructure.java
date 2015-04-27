package edu.infrastructure;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class Infrastructure {
	

	private  List<ManagedEntity>  hostSystems ;
	
	public synchronized List<ManagedEntity> getHostSystems() {
		return hostSystems;
	}

	private static Infrastructure instance ;
	private  ServiceInstance serviceInstance ;
	private  ServiceInstance adminServiceInstance ;
	
	private Infrastructure() throws MalformedURLException, RemoteException{
		hostSystems = new ArrayList<ManagedEntity>();
		URL url = new URL(InfraEndpoints.TEAM12_VC_URL);
		URL urlAdmin = new URL(InfraEndpoints.ADMIN_VC_URL);
		serviceInstance = new ServiceInstance(url, InfraEndpoints.TEAM12_USERNAME, InfraEndpoints.TEAM12_PASSWORD, true);
		adminServiceInstance = new ServiceInstance(urlAdmin, InfraEndpoints.ADMIN_USERNAME, InfraEndpoints.ADMIN_PASSWORD, true);
	}
	
	public  ServiceInstance getServiceInstance(){
		return serviceInstance;
	}
	
	public  ServiceInstance getAdminServiceInstance(){
		return adminServiceInstance;
	}
	
	public static Infrastructure getInstance(){
		if(instance==null){
			try {
				instance = new Infrastructure();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return instance;
	}
	
	public static ArrayList<ManagedEntity> getAllInstance(Folder rootFolder, String instanceType) throws InvalidProperty, RuntimeFault, RemoteException {
		// Access all instances
		ManagedEntity[] instanceArray = new InventoryNavigator(rootFolder).searchManagedEntities(instanceType);

		ArrayList<ManagedEntity> instanceList = new ArrayList<ManagedEntity>(Arrays.asList(instanceArray));
		
		if(instanceList == null || instanceList.size() == 0) {
			System.out.println("No " + instanceType + " exists in the inventory");
		}
		
		return instanceList;
	}
		
	public static ManagedEntity getResourcePool(Folder rootFolder, String ResourcePoolName) throws InvalidProperty, RuntimeFault, RemoteException {
		
		ArrayList<ManagedEntity> resourcePoolList;
		resourcePoolList = getAllInstance(rootFolder, "ResourcePool");
		
		for(ManagedEntity resourcePool : resourcePoolList) {
//			System.out.println("ResourcePool name: " + ((ResourcePool)resourcePool).getName());
			if(((ResourcePool)resourcePool).getName().equals(ResourcePoolName)) {
				return resourcePool;
			}			
		}
		
		System.out.println("No resource pool for that name found");
		return null;		
	}
	
	private void fetchHostSystems(){
		Folder rootFolder = this.serviceInstance.getRootFolder();
		try {
			ManagedEntity[] mngEntity = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
			for(int index=0;index<mngEntity.length;index++){
				hostSystems.add(mngEntity[index]);
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
	}
	
	public VirtualMachine getVirtualMachine(String vmname){
		Folder rootFolder = this.serviceInstance.getRootFolder();
		try {
			this.hostSystems.clear();
			ManagedEntity vm = new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmname);
			return (VirtualMachine)vm;
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
		return null;
	}
	
	public ManagedEntity getHostOfVm(ManagedEntity vm) throws InvalidProperty, RuntimeFault, RemoteException {
		/** VM to vHost **/
		fetchHostSystems();
		// for each host, get all VMs belongs to the host
		for(ManagedEntity host : hostSystems) {
//			System.out.println("host name: " + host.getName());

			// get all the VMs for the host
			ArrayList<ManagedEntity> vmList = getAllVMsOfHost(host);

			// for each vm in the host list, check if the name match with the given vm's name
			for(ManagedEntity vms : vmList) {
				if(vms.getName().equals(vm.getName())) {
					return host;
				}
			}
		}
		System.out.println("something really wrong...");
		return null;		
	}
	
	public static ArrayList<ManagedEntity> getAllVMsOfHost(ManagedEntity host) throws InvalidProperty, RuntimeFault, RemoteException {
		ManagedEntity[] vmArray = ((HostSystem)host).getVms();
		return (new ArrayList<ManagedEntity>(Arrays.asList(vmArray)));
	}
}
