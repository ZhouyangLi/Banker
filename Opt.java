import java.util.ArrayList;
import java.util.List;

public class Opt {
	
	static ArrayList<Integer> resourcesA = new ArrayList<Integer>(); //resources available
	static ArrayList<task> tasksc = new ArrayList<task>();
	static int[] releasedP;
	
	/**
	 * This method check for deadlock. By checking if the activities remain are all requesting 
	 * resources that the system cannot grant.
	 * 
	 */
	static void detectDeadlock(int[] totalresources) {
		boolean deadlock = true;
		for(int i=0; i< tasksc.size(); i++) {
			task t = tasksc.get(i);
			boolean empty = t.activity.isEmpty();
			if(!empty) {
				int[] nextact = t.activity.get(0);
				if(nextact[0]!=1) {
					deadlock = false;
					break;
				}
				else {
					int resourcetype = nextact[1];
					int unit = nextact[2];
					boolean ra = (totalresources[resourcetype - 1] >= unit);
					if(ra) {
						deadlock = false;
						break;
					}
				}
				
			}
		}
		boolean allEmpty = true;
		for(int i=0; i< tasksc.size(); i++) {
			task t = tasksc.get(i);
			boolean empty = t.activity.isEmpty();
			if(!empty) {
				allEmpty = false;
				break;
			}
		}
		if(allEmpty) {
			deadlock = false;
		}
		
		if(deadlock) {
			task abort = tasksc.get(0);
			abort.aborted = true;
			abort.activity.clear();
			//release the resources the aborted task has
			int i = 0;
			while(i< totalresources.length) {
				totalresources[i] += abort.resourceh[i];
				releasedP[i] += abort.resourceh[i];
				i++;
			}
			tasksc.remove(0);
			
			if(!tasksc.isEmpty()) {
				detectDeadlock(totalresources);
			}
		}
	}
	
	/**
	 * This method allocate resources using fifo. It first checks for deadlock, then tries to finish pending
	 * request, and lastly run each remaining tasks.
	 * 
	 */
	public static void run(ArrayList<task> tasklist, ArrayList<Integer> resourcelist) {
		int time = 0;
		ArrayList<int[]> waitlist = new ArrayList<int[]>();
		
		//make a copy of the resources
		resourcesA.clear();
		int a = 0;
		while (a < resourcelist.size()) {
			resourcesA.add(resourcelist.get(a));
			a++;
		}
		
		//make a copy of the task and initialize the curResources list
		tasksc.clear();
		for(int b=0; b< tasklist.size(); b++) {
			task t = tasklist.get(b);
			tasksc.add(t);
			t.resourceh = new int[resourcelist.size()];
			int c = 0;
			while (c < resourcelist.size()) {
				t.resourceh[c] = 0;
				c++;
			}
		}
		
		
		
		while(tasksc.size()>0) {
			
		
			
			//resources pending to be added back in this run
			releasedP = new int[resourcesA.size()];
			int d = 0;
			while (d < releasedP.length) {
				releasedP[d] = 0;
				d++;
			}
			
			//detect deadlock
			int[] total_resources = new int[resourcesA.size()];
			int e = 0;
			while (e< resourcesA.size()) {
				total_resources[e] = resourcesA.get(e);
				e++;
			}
			detectDeadlock(total_resources);
			
			//process pending requests first
			int f = 0;
			while (f < waitlist.size()) {
				int[] request = waitlist.get(f);
				task task = tasklist.get(request[0] - 1);
				if(!task.aborted) {
					int resource_type = request[1];
					int unit = request[2];
					boolean rna = resourcesA.get(resource_type - 1) < unit;
					if(rna) {
						task.taskwait();
					}
					else {
						//if the resources are available, allocate them to task and finish the activity
						task.resourceh[resource_type - 1] += unit;
						resourcesA.set(resource_type - 1, resourcesA.get(resource_type - 1) - unit);
						//finish the activity
						task.activity.remove(0);
						
						//remove the request from waitlist
						waitlist.remove(f);
						f--;
					}
					
					//mark the task as checked
					task.checked = true;
				}
				f++;
			}
			
			
			//run each tasks
			for(int g = 0; g < tasksc.size(); g++) {
				task task = tasksc.get(g);
				boolean empty = task.activity.isEmpty();
				if(empty) {
					//if the task was terminated, continue
					continue;
				}
				
				if(task.checked) {
					//if this task was processed, continue
					task.checked = false;
					continue;
				}
				
				int[] activity = task.activity.get(0);
				
				if(activity[0]==0) {
					task.activity.remove(0);
				}
				else if(activity[0]==1) {
					if (activity[3] > 0) {
						activity[3] -=1;
					}
					else {
						int resourcetype = activity[1];
						int unit = activity[2];
						boolean rna = (resourcesA.get(resourcetype - 1) < unit); //resource available
						if (rna) {
							//if the resources are unavailable, make the task wait for next cycle
							int[] wlrequest = {task.ID, resourcetype, unit};
							waitlist.add(wlrequest);
							task.taskwait();
						}
						else{
							task.resourceh[resourcetype - 1] += unit;
							resourcesA.set(resourcetype - 1, resourcesA.get(resourcetype - 1) - unit);
							task.activity.remove(0);
						}
						
					}
				}
				else if(activity[0]==2) {
					if (activity[3] > 0) {
						activity[3] -= 1;
					}
					else {
						int resourcetype = activity[1];
						int unit = activity[2];
						task.resourceh[resourcetype - 1] -= unit;
						releasedP[resourcetype - 1] += unit;
						task.activity.remove(0);
					}
				}

				else if(activity[0]==3) {
					if (activity[3] > 0) {
						activity[3] -= 1;
					}
					else {
						task.activity.remove(0);
						task.time = time;
						int h = 0;
						while (h< resourcesA.size()) {
							releasedP[h] += task.resourceh[h];
							h++;
						}
						tasksc.remove(g);
						g--;
					}
				}
			}
			
			//return pending released units back to the resource list
			int l = 0;
			while (l<resourcesA.size()) {
				resourcesA.set(l, resourcesA.get(l) + releasedP[l]);
				l++;
			}
			
			

			
			time ++;
		}
		System.out.println("  	   FIFO");
		int totaltime = 0;
		int totalwait = 0;
		for(int m = 0; m<tasklist.size(); m++) {
			task t = tasklist.get(m);
			if(t.aborted) {
				System.out.println("Task " + t.ID + "	aborted");
			}
			else {
				totaltime += t.time;
				totalwait += t.waittime;
				System.out.println("Task "+ t.ID + "	" + t.time + "	" +
						t.waittime + "	" + t.waittime*100/t.time + "%");
			}
			
		}
		System.out.println("Total	" + totaltime +"	" + totalwait +"	"+ totalwait*100/totaltime + "%");
	}
	
}