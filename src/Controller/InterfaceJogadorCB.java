
package Controller;
import Model.Card;
import Model.Jogador;
import java.rmi.*;
import java.util.List;
public interface InterfaceJogadorCB extends Remote{
    

    void atualizarJanelaJogo(Jogador cartasJogador1, Jogador cartasJogador2, Jogador cartasJogador3, List<Card> cartasDealer) throws RemoteException;
    void receberTurno(int idJogador, String nome) throws RemoteException;
    void mensagem(String msg) throws RemoteException;
    void setIDjogador(int id) throws RemoteException;
    void indicarPerdeu() throws RemoteException;
    void meioRounda() throws RemoteException;
    void playAgain() throws RemoteException;
    void limparCardLabels() throws RemoteException;
}
