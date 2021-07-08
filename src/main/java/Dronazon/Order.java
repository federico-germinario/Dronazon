package Dronazon;

/**
 * Classe che rappresenta un ordine di un prodotto
 *
 * @author Federico Germinario
 */
public class Order {
    private final int id;
    private final Coordinate ritiro;
    private final Coordinate consegna;

    /**
     * Inizializza in maniera casuale i parametri id, punto di ritiro e punto di consegna
     */
    public Order(){
        this.id = 1 + (int) (Math.random() * ((1000-1) + 1)); // 1-1000
        int x1 = (int) (Math.random() * (9 + 1));   // 0-9
        int y1 = (int) (Math.random() * (9 + 1));   // 0-9
        int x2 = (int) (Math.random() * (9 + 1));   // 0-9
        int y2 = (int) (Math.random() * (9 + 1));   // 0-9
        Coordinate cRitiro = new Coordinate(x1,y1);
        Coordinate cConsegna = new Coordinate(x2,y2);
        this.ritiro = cRitiro;
        this.consegna = cConsegna;
    }

    /**
     * Inizializza in maniera casuale il punto di ritiro e il punto di consegna mentre
     * l'id viene inizializzato con il parametro in ingresso
     *
     * @param id
     */
    public Order(int id){
        int x1 = (int) (Math.random() * (9 + 1));   //0-9
        int y1 = (int) (Math.random() * (9 + 1));   //0-9
        int x2 = (int) (Math.random() * (9 + 1));   //0-9
        int y2 = (int) (Math.random() * (9 + 1));   //0-9
        Coordinate cRitiro = new Coordinate(x1,y1);
        Coordinate cConsegna = new Coordinate(x2,y2);
        this.id = id;
        this.ritiro = cRitiro;
        this.consegna = cConsegna;
    }

    /**
     * Inizializzazione dei parametri
     * @param id
     * @param ritiro
     * @param consegna
     */
    public Order(int id, Coordinate ritiro, Coordinate consegna){
        this.id = id;
        this.ritiro = ritiro;
        this.consegna = consegna;
    }

    public int getId(){
        return id;
    }

    public Coordinate getRitiro(){
        return ritiro;
    }

    public Coordinate getConsegna(){
        return consegna;
    }

    @Override
    public String toString() {
        return "Dronazon.Ordine{" +
                "Id='" + id + '\'' +
                ", Ritiro='" + ritiro.toString() + '\'' +
                ", Consegna=" + consegna.toString() +
                '}';
    }
}
