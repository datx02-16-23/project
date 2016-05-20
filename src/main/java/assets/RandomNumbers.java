package assets;

import java.util.Arrays;

public class RandomNumbers {
	public static void main(String[] args){
		int[] arr = new int[12];
		for(int i = 0; i < arr.length; i++){
			arr[i] = (int)(Math.random() * 21);
		}
		System.out.println(Arrays.toString(arr));
	}
}
