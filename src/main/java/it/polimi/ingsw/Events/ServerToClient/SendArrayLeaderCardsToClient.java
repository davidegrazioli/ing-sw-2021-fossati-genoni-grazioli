package it.polimi.ingsw.Events.ServerToClient;

import it.polimi.ingsw.Events.ServerToClient.SupportClass.LeaderCardToClient;

import java.util.ArrayList;
import java.util.Map;

/**
 * This class represents the event to send to the client for the leader card turn.
 *
 * @author Stefano Fossati
 */
public class SendArrayLeaderCardsToClient extends EventToClient{
    private final ArrayList<LeaderCardToClient> leaderCardArray;
    private final Map<String, Integer> numberOfDevelopmentCards;
    private final boolean initialLeaderCards;

    /**
     * Constructs the event.
     * @param leaderCardArray The leader cards of the player.
     * @param numberOfDevelopmentCards The total number of the development card of the player.
     * @param initialLeaderCards If the event is for the choice of the initial leader cards or not.
     */
    public SendArrayLeaderCardsToClient(ArrayList<LeaderCardToClient> leaderCardArray, Map<String, Integer> numberOfDevelopmentCards, boolean initialLeaderCards) {
        this.leaderCardArray = leaderCardArray;
        this.numberOfDevelopmentCards = numberOfDevelopmentCards;
        this.initialLeaderCards = initialLeaderCards;
    }


    /**
     * Getter of the leader cards of the player.
     * @return The leader cards of the player.
     */
    public ArrayList<LeaderCardToClient> getLeaderCardArray() {
        return leaderCardArray;
    }
    /**
     * Getter of the total number of the development card of the player.
     * @return The total number of the development card of the player.
     */
    public Map<String, Integer> getNumberOfDevelopmentCards() {
        return numberOfDevelopmentCards;
    }
    /**
     * Getter of boolean that indicates if the event is for the choice of the initial leader cards or not.
     * @return The boolean that indicates if the event is for the choice of the initial leader cards or not.
     */
    public boolean isInitialLeaderCards() {
        return initialLeaderCards;
    }

    @Override
    public void acceptVisitor(EventToClientVisitor visitor) {
        visitor.visit(this);
    }
}
