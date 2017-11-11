import java.util.ArrayList;


public class task {
	
	int ID;//task ID
	int time;
	int waittime;//total wait time
	boolean checked;//true if this task is processed
	boolean aborted;//true if this task is aborted

	ArrayList<int[]> activity = new ArrayList<int[]>(); //list of activity
	int[] resourceh;//number of resources the task holding
	int[] claim;//number of resources the task claimed
	
	//constructors
	public task(int n) {
		this.ID = n;
		this.waittime = 0;
		this.checked = false;
		this.aborted = false;
	}
	
	//wait method
	public void taskwait() {
		this.waittime ++;
	}
	
	

}//end of class