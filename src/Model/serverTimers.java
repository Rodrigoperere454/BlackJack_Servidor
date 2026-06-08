
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
        
        this.timer.schedule(task, 10000);          
    }
    
    
    public void comecarTimerRonda(){
        this.timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try{
                    server.jogadorPediuStand();
                }catch(Exception e){
                    System.out.println(e);
                }
            }
        };
        
        this.timer.schedule(task, 20000);
    }
    
    
    public void desligarTimer(){
        if(this.timer != null){
            this.timer.cancel();
            //this.timer.purge();
            //this.timer = null;
        }     
    }
}
