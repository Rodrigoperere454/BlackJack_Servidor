
package Model;

import Controller.BlackJackDealer_Servidor;
import java.util.Timer;
import java.util.TimerTask;

public class serverTimers{
    private Timer timer;
    private static BlackJackDealer_Servidor server;
    
    public serverTimers(BlackJackDealer_Servidor server){        
        this.server = server;
    }
    
    public void playAgainTimer(){
        this.timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try{
                    server.iniciarJogo();
                }catch(Exception e){
                    System.out.println(e);
                }
            }
        };
        
        this.timer.schedule(task, 15000);          
    }
    
    
    public void comecarTimerTurno(Jogador jogador){
        if(jogador.isIsQuited()){
            try{
                server.jogadorPediuStand();
            }catch(Exception e){
                System.out.println(e);
            }            
        }else{
            this.timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try{
                    server.jogadorPediuStand();
                    System.out.println("Opasaaaaaa");
                }catch(Exception e){
                    System.out.println(e);
                }
            }
        };
        
        this.timer.schedule(task, 20000);
        }
        
    }
    
    
    public void desligarTimer(){
        if(this.timer != null){
            this.timer.cancel();
            //this.timer.purge();
            //this.timer = null;
        }     
    }
}
