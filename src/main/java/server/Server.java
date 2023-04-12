package server;

import javafx.util.Pair;
import server.models.Course;
import server.models.RegistrationForm;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Cette class crée un socket de Server quand elle est instanciée, elle permet de recevoir des InputStream et d'écrire
 * des outputStream avec le socket de Client ainsi que traiter les events selon la requette de la part des étudiants.
 */
public class Server {

    public final static String REGISTER_COMMAND = "INSCRIRE";
    public final static String LOAD_COMMAND = "CHARGER";
    private final ServerSocket server;
    private Socket client;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final ArrayList<EventHandler> handlers;

    public Server(int port) throws IOException {
        this.server = new ServerSocket(port, 1);
        this.handlers = new ArrayList<EventHandler>();
        this.addEventHandler(this::handleEvents);
    }



    /**
     * Cette méthode permet d'ajouter une instance de EventHandler(interface fonctionnelle) dans ArrayList<EventHandler>
     * handlers.
     * <p>
     * Elle peut être utilisée avec lambda expression dans la class ServerLauncher pour initialiser EventHandler pour
     * l'ajouter dans "handlers".
     *
     * @param h une instance de EventHandler(interface fonctionnelle) qui représente un event.
     */
    public void addEventHandler(EventHandler h) {
        this.handlers.add(h);
    }

    /**
     * Cette méthode fait une for loop pour avertir chaque event h dans l'ArrayList handlers pour les annoncer quand une
     * requête est passée et choisir le bon event à réagir.
     * <p>
     * Cette méthode va être appelée dans la méthode listen() après avoir lu et traité ce que les clients mettent dans
     * la commande line.
     *
     * @param cmd la commande entrée dans la ligne de commande du client.
     * @param arg l'argument entré dans la ligne de commande du client.  ????????
     */
    private void alertHandlers(String cmd, String arg) {
        for (EventHandler h : this.handlers) {
            h.handle(cmd, arg);
        }
    }

    /**
     * Cette méthode permet d'attendre la connection d'un client. Ensuite elle crée les input
     * stream et output stream pour communiquer avec le client et traiter input du client en appelant la méthode
     * listen().
     * <p>
     * Elle utilise dans une boucle while infinie pour attendre continuellement la connection du nouveau utilisateur et
     * le client se déconnecte une fois qu'une opération et elle va imprimer une trace quand une exception est arrivée.  ????
     */
    public void run() {
        while (true) {
            try {
                client = server.accept();
                System.out.println("Connecté au client: " + client);
                objectInputStream = new ObjectInputStream(client.getInputStream());
                objectOutputStream = new ObjectOutputStream(client.getOutputStream());

                listen();
                disconnect();
                System.out.println("Client déconnecté!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cette méthode permet de lire les requêtes du client et les transforme en String: line. Ensuite, elle appelle la
     * méthode processCommandLine en mettant line comme argument pour traiter et diviser les requêtes pour assigner cmd
     * et arg et appelle alertHandlers pour annoncer les events dans le ArrayList handlers avec le bon commande et
     * argument.
     *
     * @throws IOException s'il y a une I/O exception quand on lit avec objectInputStream.
     * @throws ClassNotFoundException si l'on trouve pas la classe.
     */
    public void listen() throws IOException, ClassNotFoundException {
        String line;
        if ((line = this.objectInputStream.readObject().toString()) != null) {
            Pair<String, String> parts = processCommandLine(line);
            String cmd = parts.getKey();
            String arg = parts.getValue();
            this.alertHandlers(cmd, arg);
        }
    }

    /**
     * Cette méthode permet de traiter la ligne entrée par le client et de diviser le String line par un espace pour
     * obtenir la commande et l'argument.
     *
     * @param line l'entrée du client transformée en String après avoir être lue avec readObject().
     * @return un objet de la classe Pair qui contient une commande et un argument.
     */
    public Pair<String, String> processCommandLine(String line) {
        String[] parts = line.split(" ");
        String cmd = parts[0];
        String args = String.join(" ", Arrays.asList(parts).subList(1, parts.length));
        return new Pair<>(cmd, args);
    }


    /**
     * Cette méthode permet de fermer les ObjectOutputStream, ObjectInputStream le client socket dans le but de
     * déconnecter le client.
     * @throws IOException s'il y a un problème de I/O quand on ferme les Streams et le socket client.
     */
    public void disconnect() throws IOException {
        objectOutputStream.close();
        objectInputStream.close();
        client.close();
    }

    public void handleEvents(String cmd, String arg) {
        if (cmd.equals(REGISTER_COMMAND)) {
            handleRegistration();
        } else if (cmd.equals(LOAD_COMMAND)) {
            handleLoadCourses(arg);
        }
    }


    /**
     Lire un fichier texte contenant des informations sur les cours et les transofmer en liste d'objets 'Course'.
     La méthode filtre les cours par la session spécifiée en argument.
     Ensuite, elle renvoie la liste des cours pour une session au client en utilisant l'objet 'objectOutputStream'.
     La méthode gère les exceptions si une erreur se produit lors de la lecture du fichier ou de l'écriture de l'objet dans le flux.
     @param arg la session pour laquelle on veut récupérer la liste des cours
     */
    public void handleLoadCourses(String arg) {
        // TODO: implémenter cette méthode

        ArrayList<Course> courses = new ArrayList<>();

        try{
            Scanner scan = new Scanner(new File("cours.txt"));

            while (scan.hasNextLine()){
                String a = scan.nextLine();
                String[] b = a.split("\t");
                String code = b[0];
                String name = b[1];
                String session = b[2];

                Course testing = new Course(name,code,session);

                if (testing.getSession().equals(arg)){
                    courses.add(testing);
                }
            }
            scan.close();
            System.out.println(courses);

            objectOutputStream.writeObject(courses);
            objectOutputStream.flush();
            objectOutputStream.close();

        }catch (FileNotFoundException e){
            System.out.println("Erreur à l'ouverture du fichier");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     Récupérer l'objet 'RegistrationForm' envoyé par le client en utilisant 'objectInputStream', l'enregistrer dans un fichier texte
     et renvoyer un message de confirmation au client.
     La méthode gére les exceptions si une erreur se produit lors de la lecture de l'objet, l'écriture dans un fichier ou dans le flux de sortie.
     */
    public void handleRegistration() {
        try {
            client = server.accept();
            String filePath = "inscription.txt";
            ObjectInputStream input = new ObjectInputStream(client.getInputStream());
            RegistrationForm form = (RegistrationForm) input.readObject();
            Writer writer = new FileWriter(filePath);
            writer.write(form.getCourse().getSession() + "\t" + form.getCourse().getCode() + "\t" + form.getMatricule() + "\t" + form.getNom() + "\t" + form.getPrenom() + "\t" + form.getEmail());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // TODO: implémenter cette méthode
    }
}
