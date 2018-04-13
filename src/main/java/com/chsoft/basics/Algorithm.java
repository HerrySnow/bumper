package com.chsoft.basics;

public class Algorithm {
	public static void main(String[] args) {
		int[] arr = {1,1,2,0,9,3,12,7,8,3,4,65,22};
		Algorithm.bubble(arr);
        for(int i=0;i<arr.length;i++){
            System.out.print(arr[i]+" ");
        }
        //
        System.out.println(recursion(100));
        
        
        multiplicationTable();
	}
	
	//冒泡
	public static void bubble(int[] arr){
		for(int i=0;i<arr.length;i++){
			for(int j=1;j<arr.length-i;j++){
				if(arr[j-1]>arr[j]){
					int temp;
					temp=arr[j-1];
					arr[j-1]=arr[j];
					arr[j] = temp;
				}
			}
		}
	}
	
	//递归,求1~100之和
	public static int recursion(int n){
		if(n>0){
		   return n+recursion(n-1);
		}else {
			return 0;
		}
	}
	//递归法写乘法表
	public static void multiplicationTable(){
		for(int i=1; i<=9;i++){  
           for(int j=1; j<=i; j++){  
               System.out.print(j+" * "+i+ " = "+(i*j) +"  ");  
           }  
           System.out.println();  
        }  
	}
}
