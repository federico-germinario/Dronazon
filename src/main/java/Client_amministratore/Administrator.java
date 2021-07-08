package Client_amministratore;

import java.util.Scanner;

/**
 * Client che interroga il Server Amministratore per ottenere
 * informazioni sulle statistiche relative ai droni e alle consegne
 *
 * @author Federico Germinario
 */
public class Administrator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Menu menu = new Menu(scanner);
        int select;

        do {
            menu.printMenu();

            while(!scanner.hasNextInt()) {
                scanner.nextLine();
                System.out.println("Enter an integer from 1 to 5");
                menu.printMenu();
                scanner.next();
            }

            select = scanner.nextInt();
            menu.selectOption(select);
        }while (select != 5);

        scanner.close();
        System.out.println("Administrator client stopped");
    }
}
