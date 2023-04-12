package Client;

import server.models.Course;

import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

public class ClientCLI {
	public static void main(String[] args) {


		ArrayList<Course> course = new ArrayList<>();


		try {
			Socket cS = new Socket("127.0.0.1", 1337);

			ObjectOutputStream os = new ObjectOutputStream(cS.getOutputStream());

			System.out.println("*** Bienvenue au portail d'inscription de cours de l'UDEM ***");
			System.out.println("Veuillez choisir la session pour laquelle vous voulez consulter la liste des cours:");
			System.out.println("1. Automne");
			System.out.println("2. Hiver");
			System.out.println("3. Ete");

			Scanner sc = new Scanner(System.in);
			String line = sc.nextLine();

			System.out.println("J'ai envoyé " + line);

			if (line.equals("1")) {
				line = "CHARGER Automne";
			}
			if (line.equals("2")) {
				line = "CHARGER Hiver";
			}
			if (line.equals("3")) {
				line = "CHARGER Ete";
			}
			os.writeObject(line);

			try {
				ObjectInputStream in = new ObjectInputStream(cS.getInputStream());
				course = (ArrayList<Course>) in.readObject(); // read the object sent by the server
				System.out.println("Les cours offerts pendant la session d'automne sont:");
				for (int i = 0; i < course.size(); i++) {
					System.out.println(i + 1 + "." + course.get(i).getCode() + "\t" + course.get(i).getName());
				}

			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			cS.close();
		} catch (ConnectException x) {
			System.out.println("Connexion impossible sur port 1337: pas de serveur.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Choix:");
		System.out.println("1. Consulter les cours offerts pour une autre session");
		System.out.println("2. Inscription à un cours");
	}

}