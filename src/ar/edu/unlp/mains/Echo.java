package ar.edu.unlp.mains;

import java.util.Scanner;

public class Echo {

	public static void main(String[] args) {
		 Scanner scanner = new Scanner(System.in);
	        while (true) {
	            String line = scanner.nextLine();
	            System.out.printf("echo > %s%n", line);
	        }
    }

}
