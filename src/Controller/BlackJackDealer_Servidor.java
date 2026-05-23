package Controller;

import Model.Deck;
import Model.Jogador;
import Model.Card;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class BlackJackDealer_Servidor extends UnicastRemoteObject implements InterfaceJogador {

    private Deck gameDeck;
    final static int NOME_IGUAL = 0;
    final static int A_JOGAR = 1;
    final static int EM_RONDA = 2;
    final static int EM_ESP = 3;

    List<Jogador> jogadoresAtivos = new ArrayList<Jogador>();
    Queue<Jogador> jogadoresEspectadores = new LinkedList<Jogador>();

    List<Jogador> allJogadores = new ArrayList<Jogador>();

    List<Card> cartasDealer = new ArrayList<Card>();

    AtomicInteger idJogador = new AtomicInteger(0);

    private boolean round;
    private int indiceJogadorAjogar = 0;

    BlackJackDealer_Servidor() throws RemoteException {
        super();
    }

    public static void main(String[] args) {

        try {
            Registry reg = LocateRegistry.createRegistry(1099);
            BlackJackDealer_Servidor serv = new BlackJackDealer_Servidor();

            reg.rebind("gestorBJ", serv);

            System.out.println("Servidor RMI iniciado");

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void darCartasInicio(Jogador jogador) {
        List<Jogador> saidasInesperadas = new ArrayList<>();
        Card carta1;
        Card carta2;

        synchronized (jogadoresAtivos) {
            try {
                if (jogadoresAtivos.size() == 0) {
                    jogadoresAtivos.add(jogador);
                    this.gameDeck = new Deck();
                    this.gameDeck.shuffle();
                    carta1 = gameDeck.deal();
                    carta2 = gameDeck.deal();

                    jogador.addCartas(carta1);
                    jogador.addCartas(carta2);
                    darCartasDealer();
                    comecarRonda();
                    atualizarJogador();

                } else {
                    carta1 = gameDeck.deal();
                    carta2 = gameDeck.deal();

                    jogador.addCartas(carta1);
                    jogador.addCartas(carta2);
                    jogadoresAtivos.add(jogador);
                    atualizarJogador();

                }
            } catch (Exception e) {
                saidasInesperadas.add(jogador);
                e.printStackTrace();
                System.out.println("Jogador saiu brutamente");
            }

            if (saidasInesperadas.size() > 0) {
                for (Jogador i : saidasInesperadas) {
                    jogadoresAtivos.remove(i);
                    allJogadores.remove(i);
                    System.out.println("Jogador removido");
                }
            }
        }
    }

    public void darCartasDealer() {
        if (cartasDealer.size() == 0) {
            Card carta1 = new Card("bv", 0, 0);
            Card carta2 = this.gameDeck.deal();
            cartasDealer.add(carta1);
            cartasDealer.add(carta2);
        } else {
            Card carta1 = this.gameDeck.deal();
            cartasDealer.add(carta1);
        }

        for (Jogador j : allJogadores) {
            atualizarJogador();

        }
    }

    public void comecarRonda() {
        //this.round = true;
        Jogador atual = jogadoresAtivos.get(indiceJogadorAjogar);

        for (Jogador j : allJogadores) {
            try {
                j.getRefJogador().receberTurno(atual.getId(), atual.getNome());
            } catch (RemoteException e) {
                System.out.println(e);
            }
        }

    }

    public void atualizarJogador() {
        List<Jogador> saidasInesperadas = new ArrayList<>();

        synchronized (allJogadores) {
            for (Jogador j : allJogadores) {
                try {
                    if (jogadoresAtivos.size() == 1) {
                        j.getRefJogador().atualizarJanelaJogo(jogadoresAtivos.get(0), null, null, cartasDealer);

                    } else if (jogadoresAtivos.size() == 2) {
                        j.getRefJogador().atualizarJanelaJogo(jogadoresAtivos.get(0), jogadoresAtivos.get(1), null, cartasDealer);
                    } else {
                        j.getRefJogador().atualizarJanelaJogo(jogadoresAtivos.get(0), jogadoresAtivos.get(1), jogadoresAtivos.get(2), cartasDealer);
                    }
                } catch (RemoteException e) {
                    saidasInesperadas.add(j);
                    e.printStackTrace();
                    System.out.println("Jogador saiu brutamente");
                }

            }

            if (saidasInesperadas.size() > 0) {
                for (Jogador i : saidasInesperadas) {
                    jogadoresAtivos.remove(i);
                    allJogadores.remove(i);
                    System.out.println("Jogador removido");
                }
            }

        }

    }

    public void atribuirIDjogador(Jogador jogador, int id) {
        jogador.setId(id);
        try {
            jogador.getRefJogador().setIDjogador(id);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("Jogador saiu brutamente");
        }

    }

    @Override
    public int login(Jogador jogador) throws RemoteException {

        for (Jogador j : allJogadores) {
            if (j.getNome().equalsIgnoreCase(jogador.getNome())) {
                return NOME_IGUAL;
            }
        }

        atribuirIDjogador(jogador, idJogador.incrementAndGet());
        allJogadores.add(jogador);

        if (jogadoresAtivos.size() < 3) {
            if (round) {
                //jogador.setIsEspectador(true);
                jogadoresAtivos.add(jogador);
                atualizarJogador();
                return EM_RONDA;
            } else {
                darCartasInicio(jogador);
                return A_JOGAR;
            }
        } else {
            jogador.setIsEspectador(true);
            jogadoresEspectadores.add(jogador);
            return EM_ESP;
        }

    }

    @Override
    public void iniciarJogo() throws RemoteException {
        if (jogadoresAtivos.size() > 0) {
            comecarRonda();
        }
    }

    @Override
    public void jogadorPediuHit(int idJogador) throws RemoteException {
        int proximoJogador = this.indiceJogadorAjogar + 1;
        int totalValor = 0;
        for (Jogador j : jogadoresAtivos) {
            if (j.getId() == idJogador) {
                darHitJogador(j);
                for (int i = 0; i < j.getCartas().size(); i++) {
                    totalValor += j.getCartas().get(i).getValue();
                }
                if (totalValor > 21) {
                    j.getRefJogador().indicarPerdeu();
                    if (proximoJogador >= jogadoresAtivos.size()) {
                        System.out.println("Perde");
                    } else {
                        this.indiceJogadorAjogar = proximoJogador;
                        System.out.println("minha vez");
                        for (Jogador jog : allJogadores) {
                            jog.getRefJogador().receberTurno(jogadoresAtivos.get(this.indiceJogadorAjogar).getId(), jogadoresAtivos.get(this.indiceJogadorAjogar).getNome());
                        }

                    }
                }

            }
        }
    }

    public void darHitJogador(Jogador jogador) {
        Card cartaHit = this.gameDeck.deal();
        jogador.addCartas(cartaHit);

        atualizarJogador();

    }

    @Override
    public void jogadorPediuStand() throws RemoteException {
        int proximoJogador = this.indiceJogadorAjogar + 1;
        if (proximoJogador >= jogadoresAtivos.size()) {
            //terminar 
        } else {
            this.indiceJogadorAjogar = proximoJogador;
            
            for (Jogador j : allJogadores) {
                try {
                    j.getRefJogador().receberTurno(jogadoresAtivos.get(this.indiceJogadorAjogar).getId(), jogadoresAtivos.get(this.indiceJogadorAjogar).getNome());
                } catch (RemoteException e) {
                    System.out.println(e);
                }
            }
            //this.round = false;
            //Codigo para verificar valores e pedir cartas do dealer
        }
    }

}
