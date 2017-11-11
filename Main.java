import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public class Main {
	public static void main(String[] args) throws FileNotFoundException {
		
		int T;//number of tasks
		int R;//number of resource types
		ArrayList<Integer> resources = new ArrayList<Integer>();//number of each resource types
		
		ArrayList<task> task1 = new ArrayList<task>();
		ArrayList<task> task2 = new ArrayList<task>();
		
		
		
		
		if(args.length < 1) {
			System.out.println("Error: missing input file name.");
			System.exit(1);
		}
		
		File inputFile = new File(args[0]);
		
		if(!inputFile.exists()) {
			System.out.println("Error: file does not exist.");
			System.exit(1);
		}
		if(!inputFile.canRead()) {
			System.out.println("Error: cannot read file.");
			System.exit(1);
		}
		
		
		Scanner input = new Scanner(inputFile);
		
		
		//process input
		int index = 0;
		T = input.nextInt();
		//create task objects
		for(int i = 1; i <= T; i++) {
			task t1 = new task(i);
			task1.add(t1);
			task t2 = new task(i);
			task2.add(t2);
		}
		
		R = input.nextInt();
		for(int i = 0; i < R; i++) {
			resources.add(input.nextInt());
		}
		
		//store each activity in the acticityList of the task object
		while(input.hasNext()) {
			int[] act1 = new int[4];
			String status = input.next();
			if (status.equalsIgnoreCase("initiate")) {
				act1[0]=0;
			}
			else if (status.equalsIgnoreCase("request")) {
				act1[0]=1;
			}
			else if (status.equalsIgnoreCase("release")) {
				act1[0]=2;
			}
			else if (status.equalsIgnoreCase("terminate")) {
				act1[0]=3;
			}
			
			int taskid = input.nextInt();
			act1[3] = input.nextInt(); //delay
			act1[1] = input.nextInt(); //resource
			act1[2] = input.nextInt(); //units
			task1.get(taskid - 1).activity.add(act1);
			
			int[] act2 = new int[4];
			for(int i = 0; i < 4; i++) {
				act2[i] = act1[i];
			}
			task2.get(taskid - 1).activity.add(act2);
		}
		
		
		
		Opt.run(task1, resources);
		System.out.println();
		Banker.run(task2, resources);
		
	}
}