package it.polimi.ingsw.Server;

import it.polimi.ingsw.Events.ClientToServer.EventToServer;
import it.polimi.ingsw.Events.ServerToClient.*;
import it.polimi.ingsw.Controller.ObserveConnectionToClient;
import it.polimi.ingsw.Events.ServerToClient.BuyDevelopmentCardTurnToClient.SendReselectedDevelopmentCardAvailableToClient;
import it.polimi.ingsw.Events.ServerToClient.SupportClass.DevelopmentCardToClient;
import it.polimi.ingsw.Events.ServerToClient.BuyDevelopmentCardTurnToClient.SendSpaceDevelopmentCardToClient;
import it.polimi.ingsw.Events.ServerToClient.SupportClass.LeaderCardToClient;
import it.polimi.ingsw.Events.ServerToClient.SupportClass.MarketToClient;
import it.polimi.ingsw.Events.ServerToClient.MarketTurnToClient.SendReorganizeDepositToClient;
import it.polimi.ingsw.Events.ServerToClient.StartConnectionToClient.SendNumPlayerToClient;
import it.polimi.ingsw.Events.ServerToClient.StartConnectionToClient.SendPlayerNameToClient;
import it.polimi.ingsw.Events.ServerToClient.SupportClass.PlayerInformationToClient;
import it.polimi.ingsw.Model.DevelopmentCard.DevelopmentCard;
import it.polimi.ingsw.Model.Exceptions.LeaderCardException;
import it.polimi.ingsw.Model.FaithTrack.FaithTrack;
import it.polimi.ingsw.Model.Game.Player;
import it.polimi.ingsw.Model.Gameboard.Gameboard;
import it.polimi.ingsw.Model.LeaderCard.LeaderCard;
import it.polimi.ingsw.Model.Market.Market;
import it.polimi.ingsw.Model.Resource.Resource;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ConnectionToClient implements Runnable, EventToClientNotifier {
    private Socket clientSocket;
    private boolean active;
    private String namePlayer;
    private int numPlayer;

    //receiver of the event from the client
    private ObserveConnectionToClient observeConnectionToClient;

    // Input and Output steams
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public ConnectionToClient(Socket clientSocket){
        this.clientSocket = clientSocket;
        active = true;
        numPlayer = 0;

        try{
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            input = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try{
            while(isActive()){
                EventToServer event = receiveEvent();
                observeConnectionToClient.observeEvent(event); // ControllerConnection
                Thread.sleep(10);
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void sendEvent(EventToClient event){
        if(isActive()){
            try{
                output.writeObject(event);   // write the event
                output.flush();              // send the event
                output.reset();              // clean buffer
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // send the event with a thread to the client of this connection
    private synchronized void asyncSendEvent(EventToClient event){
        new Thread(() -> sendEvent(event)).start();
    }

    // receive the event form the client of this connection
    private EventToServer receiveEvent() throws IOException, ClassNotFoundException {
        return (EventToServer) input.readObject();
    }

    // close the connection and set the client like inactive
    private void closeConnection(){
        try{
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setActive(false);
    }

    public boolean isActive(){
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setNamePlayer(String namePlayer) {
        this.namePlayer = namePlayer;
    }

    public String getNamePlayer() {
        return namePlayer;
    }

    public void setObserveConnectionToClient(ObserveConnectionToClient observeConnectionToClient){
        this.observeConnectionToClient = observeConnectionToClient;
    }

    // -------------------------------------------------------
    // EVENTS FOR THE START OF THE CONNECTION WITH THE CLIENT
    // -------------------------------------------------------
    @Override
    public void sendPlayerName(String playerName) {
        SendPlayerNameToClient sendPlayerNameToClient = new SendPlayerNameToClient(playerName);
        asyncSendEvent(sendPlayerNameToClient);
    }

    @Override
    public void sendNumPlayer(String message) {
        SendNumPlayerToClient sendNumPlayerToClient = new SendNumPlayerToClient(message);
        asyncSendEvent(sendNumPlayerToClient);
    }

    // ----------------------------------------
    // EVENTS THAT SEND LEADER CARD INFORMATION
    // ----------------------------------------
    @Override
    public void sendArrayLeaderCards(ArrayList<LeaderCard> leaderCards, boolean initialLeaderCards) {
        SendArrayLeaderCardsToClient sendArrayLeaderCardsToClient = new SendArrayLeaderCardsToClient(leaderCardToSend(leaderCards), initialLeaderCards);
        asyncSendEvent(sendArrayLeaderCardsToClient);
    }

    // ----------------------------------
    // EVENTS FOR THE MARKET TURN
    // ----------------------------------

    @Override
    public void sendReorganizeDeposit(ArrayList<Resource> marketResources, ArrayList<Resource> depositState) {
        SendReorganizeDepositToClient sendReorganizeDepositToClient = new SendReorganizeDepositToClient(marketResources, depositState);
        asyncSendEvent(sendReorganizeDepositToClient);
    }

    // ----------------------------------------
    // EVENTS FOR THE BUY DEVELOPMENT CARD TURN
    // ----------------------------------------

    @Override
    public void sendReselectedDevelopmentCards(String message) {
        SendReselectedDevelopmentCardAvailableToClient sendReselectedDevelopmentCardAvailableToClient = new SendReselectedDevelopmentCardAvailableToClient(message);
        asyncSendEvent(sendReselectedDevelopmentCardAvailableToClient);
    }

    @Override
    public void sendDevelopmentCardSpace(ArrayList<Boolean> developmentCardSpace) {
        SendSpaceDevelopmentCardToClient sendSpaceDevelopmentCardToClient = new SendSpaceDevelopmentCardToClient(developmentCardSpace);
        asyncSendEvent(sendSpaceDevelopmentCardToClient);
    }

    // ----------------------------------
    // OTHER EVENTS
    // ----------------------------------
    @Override
    public void sendNotify(String message) {
        NotifyToClient notifyToClient = new NotifyToClient(message);
        asyncSendEvent(notifyToClient);
    }

    @Override
    public void sendNewTurn(int turnNumber, Market market, DevelopmentCard[][] developmentCards, Player[] players, FaithTrack faithTrack) {
        NewTurnToClient newTurnToClient = new NewTurnToClient(turnNumber, marketToSend(market),
                developmentCardAvailableToSend(developmentCards), playerInformationToSend(players, faithTrack));
        asyncSendEvent(newTurnToClient);
    }


    @Override
    public void sendEndGame(String message) {
        EndGameToClient endGameToClient = new EndGameToClient(message);
        asyncSendEvent(endGameToClient);
    }

    @Override
    public void sendInitialResources(int numResources, ArrayList<Resource> depositState) {
        SendInitialResourcesToClient sendInitialResourcesToClient = new SendInitialResourcesToClient(numResources, depositState);
        asyncSendEvent(sendInitialResourcesToClient);
    }


    private ArrayList<LeaderCardToClient> leaderCardToSend(ArrayList<LeaderCard> leaderCards){
        ArrayList<LeaderCardToClient> tmp = new ArrayList<>();
        if(leaderCards!=null) {
            for (int i = 0; i < leaderCards.size(); i++) {
                LeaderCardToClient leaderCardToClient = new LeaderCardToClient(leaderCards.get(i).getName(),
                        leaderCards.get(i).getSpecialAbility().getRequirements(),
                        leaderCards.get(i).getSpecialAbility().getVictoryPoints(),
                        leaderCards.get(i).getSpecialAbility().getEffect(),
                        leaderCards.get(i).getSpecialAbility().getMaterialType().toString());
                tmp.add(leaderCardToClient);
            }
        }
        return tmp;
    }

    private ArrayList<DevelopmentCardToClient> developmentCardActiveToSend( ArrayList<DevelopmentCard> developmentCardActive){
        ArrayList<DevelopmentCardToClient> tmp = new ArrayList<>();
        for(int i=0; i<developmentCardActive.size(); i++){
            DevelopmentCard tmpD = developmentCardActive.get(i);
            if(tmpD!=null){
                tmp.add(new DevelopmentCardToClient(tmpD.getColor().name(), tmpD.getLevel(),
                        tmpD.getCost(), tmpD.getVictoryPoint(), tmpD.getMaterialRequired(), tmpD.getProductionResult()));
            }else{
                tmp.add(null);
            }

        }
        return tmp;
    }

    private DevelopmentCardToClient[][] developmentCardAvailableToSend(DevelopmentCard[][] devCards){
        DevelopmentCardToClient[][] availableToSend = new DevelopmentCardToClient[4][3];
        DevelopmentCard cardToCopy;

        for(int i=0; i<devCards.length; i++){
            for(int j=0; j<devCards[i].length; j++){
                cardToCopy = devCards[i][j];
                if(cardToCopy!=null) {
                    availableToSend[i][j] = new DevelopmentCardToClient(cardToCopy.getColor().name(), cardToCopy.getLevel(),
                            cardToCopy.getCost(), cardToCopy.getVictoryPoint(), cardToCopy.getMaterialRequired(), cardToCopy.getProductionResult());
                }
                else{
                    availableToSend[i][j] = null;
                }
            }
        }
        return availableToSend;
    }

    private MarketToClient marketToSend(Market market){
        MarketToClient marketToClient = new MarketToClient(market.getGrid(), market.getOutMarble());
        return marketToClient;
    }

    private  ArrayList<PlayerInformationToClient> playerInformationToSend(Player[] players, FaithTrack faithTrack){
        ArrayList<PlayerInformationToClient> playerInformation = new ArrayList<>();
        for(int i=0; i<players.length; i++){
            Gameboard tmpGameBoard = players[i].getPlayerBoard();
            try {
                PlayerInformationToClient tmp = new PlayerInformationToClient(players[i].getName(), tmpGameBoard.getResourceHandler().getDepositState(),
                        tmpGameBoard.getResourceHandler().getStrongboxState(), leaderCardToSend(tmpGameBoard.getLeaderCardHandler().getLeaderCardsActive()),
                        developmentCardActiveToSend(tmpGameBoard.getDevelopmentCardHandler().getActiveDevelopmentCard()),
                        tmpGameBoard.getPopeFavorTilesState(), faithTrack.getPosition(i));
                playerInformation.add(tmp);
            } catch (LeaderCardException e) {
                PlayerInformationToClient tmp = new PlayerInformationToClient(players[i].getName(), tmpGameBoard.getResourceHandler().getDepositState(),
                        tmpGameBoard.getResourceHandler().getStrongboxState(), leaderCardToSend(null),
                        developmentCardActiveToSend(tmpGameBoard.getDevelopmentCardHandler().getActiveDevelopmentCard()),
                        tmpGameBoard.getPopeFavorTilesState(), faithTrack.getPosition(i));
                playerInformation.add(tmp);
            }

        }
        return playerInformation;
    }


}
