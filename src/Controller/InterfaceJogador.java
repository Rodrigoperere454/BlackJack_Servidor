package Controller;
import Model.Jogador;
import Model.Jogador;
import java.rmi.*;
//import rmi.Item;
    
public interface InterfaceJogador extends Remote{
    
    /*Item obtemItem(int id) throws RemoteException;
    Item[] obtemTodosItens() throws RemoteException;
    boolean adicionaItem(Item novo)throws RemoteException;
    boolean removeItem(int id) throws RemoteException;
    int oferece(int id, float valor) throws RemoteException;*/
    
    //callback
    int login(Jogador jogador) throws RemoteException;
    void jogadorPediuHit(int idJogador) throws RemoteException;
    void jogadorPediuStand() throws RemoteException;
    void iniciarJogo() throws RemoteException;
}
