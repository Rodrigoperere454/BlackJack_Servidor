package Model;

import java.util.*;

// representa um deck de cartas utilizadas no blackjack (52 cartas)
public class Deck {

    private List<Card> cardList;
    private int nextToDeal;  

    public Deck() {
        Card[] cards = new Card[52];    // array de cartas (temporário) 
        cardList = new ArrayList<>();   // lista de cartas do deck

        cards[0] = new Card("c1", 1, 11);
        cards[1] = new Card("ck", 2, 10);
        cards[2] = new Card("cq", 3, 10);
        cards[3] = new Card("cj", 4, 10);
        cards[4] = new Card("c10", 5, 10);
        cards[5] = new Card("c9", 6, 9);
        cards[6] = new Card("c8", 7, 8);
        cards[7] = new Card("c7", 8, 7);
        cards[8] = new Card("c6", 9, 6);
        cards[9] = new Card("c5", 10, 5);
        cards[10] = new Card("c4", 11, 4);
        cards[11] = new Card("c3", 12, 3);
        cards[12] = new Card("c2", 13, 2);

        cards[13] = new Card("d1", 14, 11);
        cards[14] = new Card("dk", 15, 10);
        cards[15] = new Card("dq", 16, 10);
        cards[16] = new Card("dj", 17, 10);
        cards[17] = new Card("d10", 18, 10);
        cards[18] = new Card("d9", 19, 9);
        cards[19] = new Card("d8", 20, 8);
        cards[20] = new Card("d7", 21, 7);
        cards[21] = new Card("d6", 22, 6);
        cards[22] = new Card("d5", 23, 5);
        cards[23] = new Card("d4", 24, 4);
        cards[24] = new Card("d3", 25, 3);
        cards[25] = new Card("d2", 26, 2);        

        cards[26] = new Card("s1", 27, 11);
        cards[27] = new Card("sk", 28, 10);
        cards[28] = new Card("sq", 29, 10);
        cards[29] = new Card("sj", 30, 10);
        cards[30] = new Card("s10", 31, 10);
        cards[31] = new Card("s9", 32, 9);
        cards[32] = new Card("s8", 33, 8);
        cards[33] = new Card("s7", 34, 7);
        cards[34] = new Card("s6", 35, 6);
        cards[35] = new Card("s5", 36, 5);
        cards[36] = new Card("s4", 37, 4);
        cards[37] = new Card("s3", 38, 3);
        cards[38] = new Card("s2", 39, 2);  
        
        cards[39] = new Card("h1", 40, 11);
        cards[40] = new Card("hk", 41, 10);
        cards[41] = new Card("hq", 42, 10);
        cards[42] = new Card("hj", 43, 10);
        cards[43] = new Card("h10", 44, 10);
        cards[44] = new Card("h9", 45, 9);
        cards[45] = new Card("h8", 46, 8);
        cards[46] = new Card("h7", 47, 7);
        cards[47] = new Card("h6", 48, 6);
        cards[48] = new Card("h5", 49, 5);
        cards[49] = new Card("h4", 50, 4);
        cards[50] = new Card("h3", 51, 3);
        cards[51] = new Card("h2", 52, 2); 
        
        for (int i = 0; i < 52; i++) {
            cardList.add(cards[i]);
        }
        nextToDeal = 0;   
    }

    //embaralha as cartas
    public void shuffle() {
        Collections.shuffle(cardList);
        nextToDeal = 0;
    } 
    
    // retorna a próxima carta
    public Card deal(){
       int pos = nextToDeal++;
       if(pos > 51) return null; 
       return cardList.get(pos);
    }
    
    // retorna as primeiras 13 cartas, ordenadas por naipe
    public Card[] getHand() {

        List<Card> list = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            list.add(cardList.get(i));  
        }
        
        Collections.sort(list, new CardComparable()); // ordena as cartas
 
        Card[] handCards = new Card[13];
        for (int i = 0; i < 13; i++) {
            handCards[i] = list.get(i);
        }

        return handCards;
    }

    // necessária para ordenar das cartas 
    public class CardComparable implements Comparator<Card> {

        @Override
        public int compare(Card o1, Card o2) {
            return (o1.getOrder() < o2.getOrder() ? -1 : (o1.getOrder() == o2.getOrder() ? 0 : 1));
        }
    }

}
