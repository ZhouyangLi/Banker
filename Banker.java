import java.util.ArrayList;

public class Banker {

		//resource units that are currently available
		static ArrayList<Integer> resourcesA = new ArrayList<Integer>();
		static ArrayList<task> tasksc = new ArrayList<task>();
		static int[] releasedP;

		/**
		 * 
		 * This method checks for safety by checking if all tasks can be finished
		 * if the request is granted.
		 * 
		 */
		static boolean checkSafety(int[] totalresources, ArrayList<task> remaintasks) {
			//if there is no process remaining, return true
			
			boolean empty = remaintasks.isEmpty();
			if(empty) {
				
				return true;
			}
			
			int i = 0;
			while (i < remaintasks.size()) {
				
				task t = remaintasks.get(i);
					//check if t's maximum additional request for each resource type
					//is less than or equal to remaining resource of this type
					boolean norun = false;
					for(int j = 0; j < totalresources.length; j++) {
						if(totalresources[j] < t.claim[j]) {
							norun = true;
							break;
						}
					}
					
					
					if(!norun) {
						//check all future steps
						int s = 0;
						while (s < totalresources.length) {
							totalresources[s] += t.resourceh[s];
							s++;
						}
						remaintasks.remove(i--);
						
						if(checkSafety(totalresources, remaintasks)) {
							return true;
						}
						else {
							int q = 0;
							while (q< totalresources.length) {
								totalresources[q] -= t.resourceh[q];
								q++;
							}
						}
					} 
					i++;
				
			}
			
			//if there is no process remaining, return true
			empty = remaintasks.isEmpty();
					if(empty) {
						
						return true;
					}
			
			return false;
		}
		
		/**
		 * This method abort task by setting its aborted status to true, clearing its activity list,
		 * returning its resources, and removing it from task list.
		 */
		static void Abort(task task) {
			task.aborted = true;
			task.activity.clear();
			//release the resources holding by aborted task
			for(int j = 0; j< releasedP.length; j++) {
				releasedP[j] += task.resourceh[j];
				task.resourceh[j] = 0;
				task.claim[j] = 0;
			}
			tasksc.remove(task);
			
		}
		
		
		/**
		 * This method allocate resources using Banker algorithm. It first try to finish pending
		 * request and check for safety, and then run each remaining tasks and check for safety.
		 */
		public static void run(ArrayList<task> tasklist, ArrayList<Integer> resourcelist) {
			
			int time = 0;
			ArrayList<int[]> waitlist = new ArrayList<int[]>();
			
			//make a copy of the resources
			resourcesA.clear();
			int e = 0;
			while(e < resourcelist.size()) {
				resourcesA.add(resourcelist.get(e));
				e++;
			}
			
			//make a copy of the task and initialize the curResources list
			tasksc.clear();
			for(int u = 0; u < tasklist.size(); u++) {
				task t = tasklist.get(u);
				t.resourceh = new int[resourcelist.size()];
				t.claim = new int[resourcelist.size()];
				int v = 0;
				while (v < resourcelist.size()) {
					t.resourceh[v] = 0;
					v++;
				}
							
				tasksc.add(t);
			}
			
			
					
			while(tasksc.size()>0) {
				
				//resources pending to be added back in this run
				releasedP = new int[resourcesA.size()];
				for(int i = 0; i < releasedP.length; i++) {
					releasedP[i] = 0;
				}
				
				//process pending requests first
				
				for(int i = 0; i < waitlist.size(); i++) {
					int[] request = waitlist.get(i);
					task task = tasklist.get(request[0] - 1);
					int resource_type = request[1];
					int unit = request[2];
					
					int[] total_resources = new int[resourcesA.size()];
					for(int j = 0; j < resourcesA.size(); j++) {
						total_resources[j] = resourcesA.get(j);
					}
					//try to grant this request
					total_resources[resource_type - 1] -= unit;
					task.claim[resource_type - 1] -= unit;
					task.resourceh[resource_type - 1] += unit;
					
					ArrayList<task> remain_tasks = new ArrayList<task>();
					for(task t: tasksc) {
						remain_tasks.add(t);
					}
					
					if (!checkSafety(total_resources, remain_tasks)) {
						//return the resources we try to grant the task
						task.resourceh[resource_type - 1] -= unit;
						task.claim[resource_type - 1] += unit;
						//keep it waiting
						task.taskwait();
					}
					
					else {
						//grant the resource to task
						resourcesA.set(resource_type - 1, resourcesA.get(resource_type - 1) - unit);
						//finish the activity
						task.activity.remove(0);
						//remove the request from waitlist
						waitlist.remove(i);
						i--;
					}
					
					task.checked = true;
				}
				
				//run each task
				
				for(int i = 0 ; i < tasksc.size(); i++) {
					task task = tasksc.get(i);
					
					if(task.activity.isEmpty()) {
						//if the task already terminated or aborted, continue
						
						continue;
					}
					
					if(task.checked) {
						//if this task was processed, continue
						task.checked = false;
						continue;
					}
					
					int[] activity = task.activity.get(0);
					
					if(activity[0]==0) {
						int resourcetype = activity[1];
						int unit = activity[2];
						boolean abort = (resourcesA.get(resourcetype - 1) < unit);
						if(abort) {
							
							Abort(task);
							i--;
							System.out.println("Banker aborts task " + task.ID + " before run begins:");
							System.out.println("	claim for resourse " + resourcetype + " (" + unit +
									 ") exceeds number of units present (" + resourcesA.get(resourcetype - 1) + ")");
		
							
							continue;
						}
						task.claim[resourcetype-1] = unit;
						task.activity.remove(0);
						
						
						
					}
					else if(activity[0]==1) {
						if (activity[3] > 0) {
							activity[3] -= 1;
						}
						else {
							int resourcetype = activity[1];
							int unit = activity[2];

							//check for safety
							boolean legit = (unit <= task.claim[resourcetype -1]);
							if(!legit) {
								//print message and abort task
								System.out.printf("Task %d's request exceeds claim; aborted.\n", task.ID);
								Abort(task);
								continue;
							}

							int[] total_resources = new int[resourcesA.size()];
							int v = 0;
							while (v < resourcesA.size()) {
								total_resources[v] = resourcesA.get(v);
								v++;
							}
							//try to grant this request
							total_resources[resourcetype - 1] -= unit;
							task.claim[resourcetype - 1] -= unit;
							task.resourceh[resourcetype - 1] += unit;

							ArrayList<task> remain_tasks = new ArrayList<task>();
							
							for(int w = 0; w < tasksc.size(); w++) {
								task t = tasksc.get(w);
								remain_tasks.add(t);
							}

							if(!checkSafety(total_resources, remain_tasks)) {
								//return the resources we try to grant the task
								task.resourceh[resourcetype - 1] -= unit;
								task.claim[resourcetype - 1] += unit;
								//put it on waitlist
								int[] wlrequest = {task.ID, resourcetype, unit};
								waitlist.add(wlrequest);
								task.taskwait();
							}
							else {
								//grant the resource to task
								resourcesA.set(resourcetype - 1, resourcesA.get(resourcetype - 1) - unit);
								//finish the activity
								task.activity.remove(0);

							}
							
						}
					}
					else if(activity[0]==2) {
						if (activity[3] > 0) {
							activity[3] -= 1;
						}
						else {
							int resource_type = activity[1];
							int unit = activity[2];
							task.resourceh[resource_type - 1] -= unit;
							task.claim[resource_type -1] += unit;
							releasedP[resource_type - 1] += unit;
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
							while(h < resourcesA.size()) {
								releasedP[h] += task.resourceh[h];
								task.claim[h] = 0;
								h++;
							}
							tasksc.remove(i);
							i--;
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
			
			//print result
			System.out.println("	   BANKER'S");
			int total_time = 0;
			int total_wait = 0;
			for(int j = 0;j< tasklist.size(); j++) {
				task t = tasklist.get(j);
				if (t.aborted) {
					System.out.println("Task " + t.ID + "	aborted");
				}
				else {
					total_time += t.time;
					total_wait += t.waittime;
					System.out.println("Task " + t.ID + "	" + t.time + "	" 
											+t.waittime + "	" + t.waittime*100/t.time + "%");
				}
				
			}
			System.out.println("Total	" + total_time + "	" + total_wait + "	" +
									total_wait*100/total_time + "%");
		}
		

	}




