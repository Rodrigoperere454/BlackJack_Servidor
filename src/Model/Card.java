package Model;

import java.io.Serializable;

public class Card implements Serializable{
   private String name;  // nomes das carta (mesmo nome do ficheiro .png)
                         // c - cubs(paus);   d - diamonds(ouros)
                         // h - heads(copas); s - spades(espadas)
                         // + 1,2,3,4,5,6,7,8,9,10,j,k,q
                         // exemplo: c1 - ás de paus
                         
   private int order;  // ordem de apresentação numa mão (não utilizado no blackjack)
   private int value;  // numero de pontos que a carta vale (no blackjack o ás vale 11 ou 1)
   
   public Card (String _name, int _order, int _value){
       name = _name;
       order = _order;
       value = _value;
   }
   
   public String getName(){
       return name;
   }

   public int getOrder(){
       return order;
   }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
