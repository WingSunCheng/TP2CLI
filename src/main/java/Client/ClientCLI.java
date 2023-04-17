package Client;

import server.models.Course;
import server.models.RegistrationForm;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientCLI {

	public static void main(String[] args) throws IOException {
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


		try {
			boolean a = true;
			while (a) {
				Socket socket = new Socket("127.0.0.1", 1337);
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				Scanner scanner1 = new Scanner(System.in);
				String liner = scanner1.nextLine();
				System.out.println("J'ai envoyé " + liner);

				if (liner.equals("1")) {
					System.out.println("Veuillez choisir la session pour laquelle vous voulez consulter la liste des " +
							"cours:");
					System.out.println("1. Automne");
					System.out.println("2. Hiver");
					System.out.println("3. Ete");

					Scanner scanner2 = new Scanner(System.in);
					String line2 = scanner2.nextLine();

					System.out.println("J'ai envoyé " + line2);

					if (line2.equals("1")) {
						line2 = "CHARGER Automne";
					}
					if (line2.equals("2")) {
						line2 = "CHARGER Hiver";
					}
					if (line2.equals("3")) {
						line2 = "CHARGER Ete";
					}
					outStream.writeObject(line2);
					try {
						ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
						course = (ArrayList<Course>) in.readObject(); // read the object sent by the server
						System.out.println("Les cours offerts pendant la session d'automne sont:");
						for (int i = 0; i < course.size(); i++) {
							System.out.println(i + 1 + "." + course.get(i).getCode() + "\t" + course.get(i).getName());
						}
						System.out.println("Choix:");
						System.out.println("1. Consulter les cours offerts pour une autre session");
						System.out.println("2. Inscription à un cours");

					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				}
				if(liner.equals("2")){
					Course classIs = null;

					Scanner firstName = new Scanner(System.in);
					System.out.println("Veuillez saisir votre prénom: ");
					String prenom = firstName.nextLine();

					System.out.println("Veuillez saisir votre nom: ");
					Scanner name = new Scanner(System.in);
					String nom = name.nextLine();

					System.out.println("Veuillez saisir votre email: ");

					Scanner mail = new Scanner(System.in);
					String email = name.nextLine();

					System.out.println("Veuillez saisir votre matricule: ");
					Scanner matric = new Scanner(System.in);
					String matricule = matric.nextLine();

					System.out.println("Veuillez saisir le code du cours: ");
					Scanner classCode = new Scanner(System.in);
					String codeCours = classCode.nextLine();

					for (int i = 0; i < course.size(); i++) {
						if (course.get(i).getCode().equals(codeCours)){
							classIs = course.get(i);
						}
					}

					//Vérifier si les informations sont correctement écrites
					String mailRegex = "[^@\\s]+@[^@\\s]+\\.\\w+";  //PIAZA NICHOLASCOOPER
					boolean validEmail = email.matches(mailRegex);

					boolean validMatricule = true;
					if(matricule.length() != 6){
						validMatricule = false;
					}
					boolean validCode = true;
					if (classIs == null){
						validCode = false;
					}


					if(validCode == false){
						System.out.println("INVALID CLASS COURSE");
					}

					if (validMatricule == false){
						System.out.println("INVALID MATRICULE");
					}

					if (validEmail == false){
						System.out.println("INVALID EMAIL");
					}

					if (liner.equals("2")) {

						if((validEmail == false) || (validEmail == false) || (validMatricule == false)){
							System.out.println("Échec à l'inscription");
							throw new Exception("Invalide form, please recconect to the server");
						}else {
							RegistrationForm ins = new RegistrationForm(prenom, nom, email, matricule, classIs);
							outStream.writeObject("INSCRIRE");
							Socket kek = new Socket("127.0.0.1", 1337);
							ObjectOutputStream kik = new ObjectOutputStream(kek.getOutputStream());
							kik.writeObject(ins);
							System.out.println("Félicitations! Inscription réussie de " + ins.getPrenom() +
									" au cours: " + ins.getCourse().getCode());
						}
					}

					System.out.println("BYE");
					a = false;
				}

			}} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (RuntimeException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
