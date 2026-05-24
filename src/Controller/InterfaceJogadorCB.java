
package Controller;
import Model.Card;
import Model.Jogador;
import java.rmi.*;
import java.util.List;
public interface InterfaceJogadorCB extends Remote{
    
    void receberCartasGUI(Card carta1, Card carta2, boolean isJogador) throws RemoteException;
    void receberCartasDealer(Card carta1 ) throws RemoteException;
    void atualizarJanelaJogo(Jogador cartasJogador1, Jogador cartasJogador2, Jogador cartasJogador3, List<Card> cartasDealer) throws RemoteException;
    void receberTurno(int idJogador, String nome) throws RemoteException;
    void setIDjogador(int id) throws RemoteException;
    void indicarPerdeu() throws RemoteException;
    void meioRounda() throws RemoteException;
}
