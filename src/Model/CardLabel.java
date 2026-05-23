package Model;

import javax.swing.*;

// cada carta é apresentada num JLabel
public class CardLabel extends JLabel{

    public CardLabel(){ 
    
    }

    // define o icone do label, que é a imagem da carta
    public void setCardImage (String name){
        ImageIcon im = new ImageIcon(getClass().getResource("../cartas/"+ name + ".png"));
        this.setIcon(im);
    }

    // define se a carta vai ser coberta parciamente por outra
    public void setCardCovered(boolean covered) {
        Icon ic = this.getIcon();
        int cardWidth = ic.getIconWidth();
        int cardHeight = ic.getIconHeight();
       
        if(covered)
            this.setSize(20,cardHeight);
        else
            this.setSize(cardWidth,cardHeight);
    }

}
