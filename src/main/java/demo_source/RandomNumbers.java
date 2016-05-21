package demo_source;

import java.util.Arrays;

public class RandomNumbers {
	public static final int[] DEMO = {3, 5, 15, 20, 17, 11, 15, 13, 12, 9, 18, 9};
	
	public static void main(String[] args){
		int[] arr = new int[12];
		for(int i = 0; i < arr.length; i++){
			arr[i] = (int)(Math.random() * 21);
		}
		System.out.println(Arrays.toString(arr));
	}
}
