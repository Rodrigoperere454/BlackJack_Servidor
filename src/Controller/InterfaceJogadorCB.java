
package Controller;
import Model.Card;
import Model.Jogador;
import java.rmi.*;
import java.util.List;
import java.util.Queue;
public interface InterfaceJogadorCB extends Remote{
    

    void atualizarJanelaJogo(Jogador cartasJogador1, Jogador cartasJogador2, Jogador cartasJogador3, List<Card> cartasDealer, Queue<Jogador> espetadores) throws RemoteException;
    void receberTurno(int idJogador, String nome) throws RemoteException;
    void mensagem(String msg) throws RemoteException;
    void setIDjogador(int id) throws RemoteException;
    void indicarPerdeu() throws RemoteException;
    void meioRounda() throws RemoteException;
    void resultsFinal(String msg) throws RemoteException;
    void limparCardLabels() throws RemoteException;
    void toEspetador(Jogador jogador) throws RemoteException;
}
