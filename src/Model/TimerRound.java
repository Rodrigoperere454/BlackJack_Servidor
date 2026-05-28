
package Model;

import Controller.BlackJackDealer_Servidor;
import java.util.Timer;
import java.util.TimerTask;

public class TimerRound{
    private Timer timer;
    private static BlackJackDealer_Servidor server;
    
    public TimerRound(BlackJackDealer_Servidor server){        
        this.server = server;
    }
    
    
    public void comecarTimer(){
        this.timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try{
                    server.jogadorPediuStand();
                }catch(Exception e){
                    
                }
            }
        };
        
        this.timer.schedule(task, 20000);
    }
    
    
    public void desligarTimer(){
        if(this.timer != null){
            this.timer.cancel();
        }     
    }
}
