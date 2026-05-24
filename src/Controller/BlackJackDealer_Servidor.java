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

            System.out.println("DEALER A ESPERA DE JOGADORES");

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void darCartasInicio(Jogador jogador) {
        Card carta1;
        Card carta2;

        synchronized (jogadoresAtivos) {
            carta1 = this.gameDeck.deal();
            carta2 = this.gameDeck.deal();

            jogador.addCartas(carta1);
            jogador.addCartas(carta2);
        }
    }

    public void darCartasDealer() {
        if (cartasDealer.isEmpty()) {
            Card carta1 = new Card("bv", 0, 0);
            Card carta2 = this.gameDeck.deal();
            cartasDealer.add(carta1);
            cartasDealer.add(carta2);
        } else {
            Card carta1 = this.gameDeck.deal();
            cartasDealer.add(carta1);
        }
    }

    public void comecarRonda() {
        this.round = true;

        this.gameDeck = new Deck();
        this.gameDeck.shuffle();

        darCartasDealer();
        Jogador primeiroAjogar = jogadoresAtivos.get(indiceJogadorAjogar);

        try {
            for (Jogador jo : jogadoresAtivos) {
                jo.setIsPlaying(true);
                jo.setNumeroFichas(-2);
                darCartasInicio(jo);
            }
            for (Jogador j : allJogadores) {
                j.getRefJogador().receberTurno(primeiroAjogar.getId(), primeiroAjogar.getNome());
            }
        } catch (RemoteException e) {
            System.out.println(e);

        }
        atualizarJogador();
    }

    public void acabarRounda() {
        this.round = false;

        int totalValorDealer = 0;
        for (int i = 0; i < this.cartasDealer.size(); i++) {
            totalValorDealer += this.cartasDealer.get(i).getValue();
        }
        while (totalValorDealer < 17) {
            darCartasDealer();
            atualizarJogador();

            totalValorDealer = 0;
            for (int i = 0; i < this.cartasDealer.size(); i++) {
                totalValorDealer += this.cartasDealer.get(i).getValue();
            }
        }

        if (totalValorDealer > 21) {
            for (Jogador j : jogadoresAtivos) {
                if (j.isIsPlaying()) {
                    j.setNumeroFichas(4);
                }
            }
        } else if (totalValorDealer == 21 && cartasDealer.size() == 2) {
            for (Jogador j : jogadoresAtivos) {
                if (j.isIsPlaying()) {
                    if (j.getValorCartas() < 21) {
                        j.setNumeroFichas(0);
                    } else if (j.getValorCartas() == 21 && j.getCartas().size() == 2) {
                        j.setNumeroFichas(2);
                    } else if (j.getValorCartas() == 21) {
                        j.setNumeroFichas(0);
                    }
                }
            }
        } else if (totalValorDealer == 21) {
            for (Jogador jog : jogadoresAtivos) {
                if (jog.isIsPlaying()) {
                    if (jog.getValorCartas() < 21) {
                        jog.setNumeroFichas(0);
                    } else if (jog.getValorCartas() == 21 && jog.getCartas().size() == 2) {
                        jog.setNumeroFichas(4);
                    } else if (jog.getValorCartas() == 21) {
                        jog.setNumeroFichas(2);
                    }
                }
            }

        } else {
            for (Jogador jo : jogadoresAtivos) {
                if (jo.isIsPlaying()) {
                    if (jo.getValorCartas() == 21 && jo.getCartas().size() == 2) {
                        jo.setNumeroFichas(5);
                    } else if (jo.getValorCartas() > totalValorDealer) {
                        jo.setNumeroFichas(4);
                    } else if (jo.getValorCartas() == totalValorDealer) {
                        jo.setNumeroFichas(2);
                    } else if (jo.getValorCartas() < totalValorDealer) {
                        jo.setNumeroFichas(0);
                    }
                }

            }
        }
        
        //limpar cartas jogadores e dealer
        for(Jogador joga: jogadoresAtivos){
            joga.getCartas().clear();
        }     
        cartasDealer.clear();
        
        atualizarJogador();
        
        //comecarRonda();
        
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

        if (jogadoresAtivos.isEmpty()) {
            jogadoresAtivos.add(jogador);
            return A_JOGAR;
        } else if (jogadoresAtivos.size() < 3) {
            if (round) {
                jogador.setIsEspectador(true);
                jogador.getRefJogador().meioRounda();
                jogadoresAtivos.add(jogador);
                atualizarJogador();
                return EM_RONDA;
            } else {
                jogadoresAtivos.add(jogador);
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
                    j.setIsPlaying(false);
                    if (proximoJogador >= jogadoresAtivos.size()) {
                        acabarRounda();
                    } else {
                        this.indiceJogadorAjogar = proximoJogador;
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
        if (proximoJogador >= jogadoresAtivos.size() || jogadoresAtivos.get(proximoJogador).isIsEspectador()) {
            acabarRounda();
        } else {
            this.indiceJogadorAjogar = proximoJogador;

            for (Jogador j : allJogadores) {
                try {
                    j.getRefJogador().receberTurno(jogadoresAtivos.get(this.indiceJogadorAjogar).getId(), jogadoresAtivos.get(this.indiceJogadorAjogar).getNome());
                } catch (RemoteException e) {
                    System.out.println(e);
                }
            }
        }
    }
}
