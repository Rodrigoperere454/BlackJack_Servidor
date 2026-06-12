
package Model;
import Controller.InterfaceJogadorCB;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
public class Jogador implements Serializable{
    private int id;
    private String nome;
    private int numeroFichas = 10;
    private InterfaceJogadorCB refJogador;
    private boolean isEspectador;
    private boolean isPlaying;
    private boolean isQuited;

    public boolean isIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }
    private List<Card> cartas = new ArrayList<Card>();
    
    public Jogador(String nome, InterfaceJogadorCB refJogador) throws RemoteException{
        this.nome = nome;
        this.refJogador = refJogador;
    }

    public int getNumeroFichas() {
        return numeroFichas;
    }

    public void setNumeroFichas(int numeroFichas) {
        this.numeroFichas += numeroFichas;
    }

    public InterfaceJogadorCB getRefJogador() {
        return refJogador;
    }

    public void setRefJogador(InterfaceJogadorCB refJogador) {
        this.refJogador = refJogador;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isIsEspectador() {
        return isEspectador;
    }

    public void setIsEspectador(boolean isEspectador) {
        this.isEspectador = isEspectador;
    }


    public List<Card> getCartas() {
        return cartas;
    }

    public void addCartas(Card carta) {
        this.cartas.add(carta);
    }
    
    public int getValorCartas(){
        int total = 0;
        for(Card c: this.cartas){
            total += c.getValue();
        }     
        return total;
    }

    public boolean isIsQuited() {
        return isQuited;
    }

    public void setIsQuited(boolean isQuited) {
        this.isQuited = isQuited;
    }
    
     
}
