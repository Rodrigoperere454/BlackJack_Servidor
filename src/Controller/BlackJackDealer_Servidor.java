package Controller;

import Model.Deck;
import Model.Jogador;
import Model.Card;
import Model.TimerRound;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class BlackJackDealer_Servidor extends UnicastRemoteObject implements InterfaceJogador {

    private Deck gameDeck;
    final static int NOME_IGUAL = 0;
    final static int A_JOGAR = 1;
    final static int EM_RONDA = 2;
    final static int EM_ESP = 3;
    final static int UNICO_MESA = 4;
    final static int SUCC = 9;

    //Mesagens
    final static String ENTROU = "ENTROU";
    final static String ESPECT = "ESPETADOR";
    final static String HIT = "HIT";
    final static String STAND = "STAND";
    final static String VEZ = "VEZ";
    final static String PERDEU = "PERDEU";
    final static String GANHOU = "GANHOU:";
    final static String SAIU = "SAIU";
    final static String COMECAR = "COMECAR";
    final static String ACABOU = "Acabou";
    final static String EMPATE = "EMPATE";
    final static String BLACKJACK = "BLACKJACK";

    List<Jogador> jogadoresAtivos = new ArrayList<Jogador>();
    Queue<Jogador> jogadoresEspectadores = new LinkedList<Jogador>();

    List<Jogador> allJogadores = new ArrayList<Jogador>();

    List<Card> cartasDealer = new ArrayList<Card>();

    AtomicInteger idJogador = new AtomicInteger(0);

    private boolean round;
    private int indiceJogadorAjogar = 0;

    private static TimerRound timer;

    BlackJackDealer_Servidor() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        try {
            Registry reg = LocateRegistry.createRegistry(1099);
            BlackJackDealer_Servidor serv = new BlackJackDealer_Servidor();

            timer = new TimerRound(serv);

            reg.rebind("gestorBJ", serv);

            System.out.println("DEALER A ESPERA DE JOGADORES");

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void limparCartasDealer() {

        cartasDealer.clear();

        for (Jogador j : jogadoresAtivos) {
            j.getCartas().clear();
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

            if (jogador.getValorCartas() > 21) {
                for (Card c : jogador.getCartas()) {
                    if (c.getName().equalsIgnoreCase("c1") || c.getName().equalsIgnoreCase("d1") || c.getName().equalsIgnoreCase("s1") || c.getName().equalsIgnoreCase("h1")) {
                        c.setValue(1);
                        atualizarJogador();
                        break;
                    }
                }
            }
        }
    }

    public void chamarJogadorEspectador() {
        if (!jogadoresEspectadores.isEmpty() && jogadoresAtivos.size() < 3) {
            if (jogadoresEspectadores.peek().getNumeroFichas() != 0) {
                Jogador paraJogar = jogadoresEspectadores.remove();
                System.out.println(paraJogar.getNome());
                paraJogar.setIsEspectador(false);
                jogadoresAtivos.add(paraJogar);
            }
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

    public void tirarCartasMesa() {
        for (Jogador j : allJogadores) {
            try {
                j.getRefJogador().limparCardLabels();
            } catch (RemoteException e) {
                System.out.println(e);
            }

        }
    }

    public void comecarRonda() {
        enviarMensagens(COMECAR, null);
        chamarJogadorEspectador();

        this.round = true;
        atualizarJogador();

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
            timer.comecarTimer();
        } catch (RemoteException e) {
            System.out.println(e);

        }
        atualizarJogador();
    }

    public void acabarRounda() {
        enviarMensagens(ACABOU, null);
        timer.desligarTimer();
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
                    enviarMensagens(GANHOU + "4", j);
                    enviarResultados("Ganhou 4 fichas", j);
                    j.setNumeroFichas(4);
                }
            }
        } else if (totalValorDealer == 21 && cartasDealer.size() == 2) {
            for (Jogador j : jogadoresAtivos) {
                if (j.isIsPlaying()) {
                    if (j.getValorCartas() < 21) {

                        enviarMensagens(PERDEU, j);
                        enviarResultados("Perdes-te", j);
                        j.setNumeroFichas(0);
                    } else if (j.getValorCartas() == 21 && j.getCartas().size() == 2) {

                        enviarMensagens(EMPATE, j);
                        enviarResultados("Empatas-te com o Dealer", j);
                        j.setNumeroFichas(2);
                    } else if (j.getValorCartas() == 21) {

                        enviarMensagens(PERDEU, j);
                        enviarResultados("Perdes-te", j);
                        j.setNumeroFichas(0);
                    }
                }
            }

        } else if (totalValorDealer == 21) {
            for (Jogador jog : jogadoresAtivos) {
                if (jog.isIsPlaying()) {
                    if (jog.getValorCartas() < 21) {

                        enviarResultados("Perdes-te", jog);
                        enviarMensagens(PERDEU, jog);
                        jog.setNumeroFichas(0);
                    } else if (jog.getValorCartas() == 21 && jog.getCartas().size() == 2) {

                        enviarMensagens(BLACKJACK, jog);
                        enviarMensagens(GANHOU + "5", jog);
                        enviarResultados("Ganhas-te com Blackjack." + "\n" + "5 fichas", jog);
                        jog.setNumeroFichas(5);
                    } else if (jog.getValorCartas() == 21) {

                        enviarMensagens(EMPATE, jog);
                        enviarResultados("Empatas-te com o Dealer", jog);
                        jog.setNumeroFichas(2);
                    }
                }
            }

        } else {
            for (Jogador jo : jogadoresAtivos) {
                if (jo.isIsPlaying()) {
                    if (jo.getValorCartas() == 21 && jo.getCartas().size() == 2) {

                        enviarMensagens(BLACKJACK, jo);
                        enviarMensagens(GANHOU + "5", jo);
                        enviarResultados("Ganhas-te com Blackjack." + "\n" + "5 fichas", jo);
                        jo.setNumeroFichas(5);
                    } else if (jo.getValorCartas() > totalValorDealer) {

                        enviarMensagens(GANHOU + "4", jo);
                        enviarResultados("Ganhas-te 4 fichas", jo);
                        jo.setNumeroFichas(4);
                    } else if (jo.getValorCartas() == totalValorDealer) {

                        enviarMensagens(EMPATE, jo);
                        enviarResultados("Empatas-te com o Dealer", jo);
                        jo.setNumeroFichas(2);

                    } else if (jo.getValorCartas() < totalValorDealer) {

                        enviarMensagens(PERDEU, jo);
                        enviarResultados("Perdes-te", jo);
                        jo.setNumeroFichas(0);
                    }
                }

            }
        }

        List<Jogador> toEspetador = new ArrayList<Jogador>();
        for (Jogador joga : jogadoresAtivos) {
            joga.setIsEspectador(false);
            if (!joga.isIsPlaying()) {
                enviarResultados("Perdes-te", joga);
            }

            if (joga.getNumeroFichas() == 0 && jogadoresAtivos.size() == 1) {
                joga.setNumeroFichas(10);
            } else if (joga.getNumeroFichas() == 0 && jogadoresAtivos.size() > 1) {
                joga.setIsEspectador(true);
                toEspetadorCallback(joga);
                enviarResultados("Ficas-te sem fichas. És espetador", joga);             
                //joga.setNumeroFichas(10);
                toEspetador.add(joga);
            }
        }

        if (!toEspetador.isEmpty()) {
            for (Jogador j : toEspetador) {
                j.getCartas().clear();
                jogadoresAtivos.remove(j);
                jogadoresEspectadores.add(j);
            }
        }

        this.indiceJogadorAjogar = 0;
        limparCartasDealer();
    }

    public void enviarResultados(String results, Jogador jo) {
        try {
            jo.getRefJogador().resultsFinal(results);
        } catch (RemoteException e) {
            System.out.println(e);
        }
    }

    public void enviarMensagens(String info, Jogador jogador) {
        try {
            if (info.equalsIgnoreCase(ENTROU)) {
                for (Jogador j : allJogadores) {
                    j.getRefJogador().mensagem("Jogador " + jogador.getNome() + " Entrou na Mesa.");
                }
            } else if (info.equalsIgnoreCase(ESPECT)) {
                for (Jogador j : allJogadores) {
                    j.getRefJogador().mensagem("Jogador " + jogador.getNome() + " Entrou como Espetador.");
                }
            } else if (info.equalsIgnoreCase(HIT)) {
                String carta = jogador.getCartas().getLast().getName();
                for (Jogador j : allJogadores) {
                    j.getRefJogador().mensagem("Jogador " + jogador.getNome() + " pediu uma carta - " + carta);
                }
            } else if (info.equalsIgnoreCase(STAND)) {
                for (Jogador j : allJogadores) {
                    j.getRefJogador().mensagem("Jogador " + jogador.getNome() + " pediu Stand - ");
                }
            } else if (info.equalsIgnoreCase(VEZ)) {
                for (Jogador j : allJogadores) {
                    j.getRefJogador().mensagem("É a vez do " + jogador.getNome());
                }
            } else if (info.equalsIgnoreCase(PERDEU)) {
                for (Jogador j : allJogadores) {
                    j.getRefJogador().mensagem("O jogador " + jogador.getNome() + " Perdeu.");
                }
            } else if (info.contains(GANHOU)) {
                int indexFichas = info.indexOf(":") + 1;
                String fichas = info.substring(indexFichas, info.length());
                for (Jogador j : allJogadores) {
                    j.getRefJogador().mensagem("O jogador " + jogador.getNome() + " Ganhou " + fichas + " fichas.");
                }
            } else if (info.equalsIgnoreCase(SAIU)) {
                for (Jogador j : allJogadores) {
                    j.getRefJogador().mensagem("O jogador " + jogador.getNome() + " Saiu da mesa.");
                }
            } else if (info.equalsIgnoreCase(COMECAR)) {
                for (Jogador j : allJogadores) {
                    j.getRefJogador().mensagem("COMECOU:Começou nova Rounda.");
                }
            } else if (info.equalsIgnoreCase(ACABOU)) {
                for (Jogador j : allJogadores) {
                    j.getRefJogador().mensagem("Acabou a Rounda.");
                }
            } else if (info.equalsIgnoreCase(EMPATE)) {
                for (Jogador j : allJogadores) {
                    j.getRefJogador().mensagem("O jogador " + jogador.getNome() + " Empatou com o Dealer");
                }
            } else if (info.equalsIgnoreCase(BLACKJACK)) {
                for (Jogador j : allJogadores) {
                    j.getRefJogador().mensagem("O jogador " + jogador.getNome() + " fez BLACKJACK!! PARABÉNS !");
                }
            }
        } catch (RemoteException e) {
            System.out.println(e);
        }

    }

    public void atualizarJogador() {
        List<Jogador> saidasInesperadas = new ArrayList<>();

        if (jogadoresAtivos.isEmpty()) {
            return;
        }

        synchronized (allJogadores) {
            for (Jogador j : allJogadores) {
                try {
                    if (jogadoresAtivos.size() == 1) {
                        j.getRefJogador().atualizarJanelaJogo(jogadoresAtivos.get(0), null, null, cartasDealer, jogadoresEspectadores);

                    } else if (jogadoresAtivos.size() == 2) {
                        j.getRefJogador().atualizarJanelaJogo(jogadoresAtivos.get(0), jogadoresAtivos.get(1), null, cartasDealer, jogadoresEspectadores);
                    } else {
                        j.getRefJogador().atualizarJanelaJogo(jogadoresAtivos.get(0), jogadoresAtivos.get(1), jogadoresAtivos.get(2), cartasDealer, jogadoresEspectadores);
                    }

                } catch (RemoteException e) {
                    saidasInesperadas.add(j);
                    e.printStackTrace();
                    System.out.println("Jogador saiu brutamente");
                }

            }

            if (!saidasInesperadas.isEmpty()) {
                for (Jogador i : saidasInesperadas) {
                    if(jogadoresAtivos.contains(i)){
                        jogadoresAtivos.remove(i);
                    }else if(jogadoresEspectadores.contains(i)){
                        jogadoresEspectadores.remove(i);
                    }
                    
                    allJogadores.remove(i);
                    tirarCartasMesa();
                    atualizarJogador();
                    System.out.println("Jogador removido" + i.getNome());
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

            enviarMensagens(ENTROU, jogador);
            return A_JOGAR;
        } else if (jogadoresAtivos.size() < 3) {
            if (round) {
                jogador.setIsEspectador(true);
                jogador.getRefJogador().meioRounda();
                jogadoresAtivos.add(jogador);
                atualizarJogador();
                enviarMensagens(ENTROU, jogador);
                return EM_RONDA;
            } else {
                jogadoresAtivos.add(jogador);
                enviarMensagens(ENTROU, jogador);
                return A_JOGAR;
            }
        } else {
            jogador.setIsEspectador(true);
            jogadoresEspectadores.add(jogador);
            atualizarJogador();
            enviarMensagens(ESPECT, jogador);
            return EM_ESP;
        }

    }

    @Override
    public void iniciarJogo() throws RemoteException {
        if (jogadoresAtivos.size() > 0) {
            tirarCartasMesa();
            comecarRonda();
        }
    }

    @Override
    public void jogadorPediuHit(int idJogador) throws RemoteException {
        int proximoJogador = this.indiceJogadorAjogar + 1;
        int totalValor = 0;
        Jogador jogador = null;
        for (Jogador j : jogadoresAtivos) {
            if (j.getId() == idJogador) {
                jogador = j;
                darHitJogador(j);
                enviarMensagens(HIT, jogador);
                for (Card ca : j.getCartas()) {
                    totalValor += ca.getValue();
                }

                if (totalValor > 21) {
                    for (Card c : jogador.getCartas()) {
                        if (c.getName().equalsIgnoreCase("c1") || c.getName().equalsIgnoreCase("d1") || c.getName().equalsIgnoreCase("s1") || c.getName().equalsIgnoreCase("h1")) {
                            c.setValue(1);
                            atualizarJogador();
                        }
                    }
                }
                totalValor = 0;
                for (Card ca : j.getCartas()) {
                    totalValor += ca.getValue();
                }
                if (totalValor > 21) {
                    timer.desligarTimer();
                    j.getRefJogador().indicarPerdeu();
                    enviarMensagens(PERDEU, jogador);
                    j.setIsPlaying(false);
                    if (proximoJogador >= jogadoresAtivos.size() || jogadoresAtivos.get(proximoJogador).isIsEspectador()) {
                        acabarRounda();
                        return;
                    } else {
                        this.indiceJogadorAjogar = proximoJogador;
                        for (Jogador jog : allJogadores) {
                            jog.getRefJogador().receberTurno(jogadoresAtivos.get(this.indiceJogadorAjogar).getId(), jogadoresAtivos.get(this.indiceJogadorAjogar).getNome());
                        }
                        timer.comecarTimer();

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
        timer.desligarTimer();
        int proximoJogador = this.indiceJogadorAjogar + 1;
        enviarMensagens(STAND, jogadoresAtivos.get(this.indiceJogadorAjogar));
        if (proximoJogador >= jogadoresAtivos.size() || jogadoresAtivos.get(proximoJogador).isIsEspectador()) {
            acabarRounda();
        } else {
            this.indiceJogadorAjogar = proximoJogador;
            atualizarJogador();
            enviarMensagens(VEZ, jogadoresAtivos.get(this.indiceJogadorAjogar));
            for (Jogador j : allJogadores) {
                try {
                    j.getRefJogador().receberTurno(jogadoresAtivos.get(this.indiceJogadorAjogar).getId(), jogadoresAtivos.get(this.indiceJogadorAjogar).getNome());
                } catch (RemoteException e) {
                    System.out.println(e);
                }
            }
            timer.comecarTimer();
        }
    }

    @Override
    public void logout(int idJogador) throws RemoteException {
        Jogador jogadorRemove = null;

        for (Jogador j : jogadoresAtivos) {
            if (j.getId() == idJogador) {
                jogadorRemove = j;
            }
        }

        if (jogadorRemove != null) {
            if (jogadoresAtivos.size() == 1) {
                jogadoresAtivos.remove(jogadorRemove);
                allJogadores.remove(jogadorRemove);
                tirarCartasMesa();
                atualizarJogador();
                acabarRounda();

            } else {
                jogadoresAtivos.remove(jogadorRemove);
                allJogadores.remove(jogadorRemove);
                tirarCartasMesa();
                atualizarJogador();
                enviarMensagens(SAIU, jogadorRemove);
            }
        }
    }
    
    public void toEspetadorCallback(Jogador jogador){
        try{
            jogador.getRefJogador().toEspetador(jogador);
        }catch(RemoteException e){
            System.out.println(e);
        }
    }

    @Override
    public int passarEspetador(int idJogador) throws RemoteException {
        Jogador jogadorEspetador = null;
        for (Jogador j : jogadoresAtivos) {
            if (j.getId() == idJogador) {
                jogadorEspetador = j;
            }
        }
        if (jogadorEspetador != null) {
            if (jogadoresAtivos.size() == 1) {
                return UNICO_MESA;
            } else {
                if (this.round) {
                    jogadorPediuStand();
                    jogadorEspetador.getCartas().clear();
                    jogadoresAtivos.remove(jogadorEspetador);
                    jogadoresEspectadores.add(jogadorEspetador);
                    enviarMensagens(ESPECT, jogadorEspetador);
                    return EM_ESP;
                }

            }
        }
        return SUCC;
    }
}
