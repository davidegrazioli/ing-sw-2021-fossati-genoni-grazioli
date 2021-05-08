package it.polimi.ingsw.Controller.Turns;

import it.polimi.ingsw.Controller.ControllerToModel;
import it.polimi.ingsw.Model.DevelopmentCard.DevelopmentCard;
import it.polimi.ingsw.Model.DevelopmentCard.ProductedMaterials;
import it.polimi.ingsw.Model.Exceptions.LeaderCardException;
import it.polimi.ingsw.Model.Exceptions.ResourceException;
import it.polimi.ingsw.Model.Gameboard.Gameboard;
import it.polimi.ingsw.Model.LeaderCard.LeaderCard;
import it.polimi.ingsw.Model.Resource.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivateProductionTurn {
    private final ControllerToModel controllerToModel;

    public ActivateProductionTurn(ControllerToModel controllerToModel) {
        this.controllerToModel = controllerToModel;
    }

    public void productionsActivation(boolean useBaseProduction, Resource resourceRequested1, Resource resourceRequested2,
                                    ProductedMaterials resourceGranted, ArrayList<Boolean> useLeaders, ArrayList<Resource> materialLeaders,
                                    ArrayList<Boolean> useDevelop, String playerName){

        int currentPlayerIndex = controllerToModel.getCurrentPlayerIndex();
        Gameboard actualPlayerBoard = controllerToModel.getPlayers()[currentPlayerIndex].getPlayerBoard();

        Map<Resource,Integer> materialRequested = new HashMap<>();
        for(Resource r : Resource.values())
            materialRequested.put(r,0);

        Map<ProductedMaterials, Integer> materialGranted = new HashMap<>();
        for(ProductedMaterials p : ProductedMaterials.values())
            materialGranted.put(p,0);


        if(useBaseProduction){
            materialRequested.put(resourceRequested1,materialRequested.get(resourceRequested1)+1);
            materialRequested.put(resourceRequested2,materialRequested.get(resourceRequested2)+1);
            materialGranted.put(resourceGranted, materialGranted.get(resourceGranted)+1);
        }


        if(useLeaders.contains(true)) {
            ArrayList<ProductedMaterials> productedByLeader = new ArrayList<>();

            for (Resource r : materialLeaders) { //TRASFORM MATERIAL LEADER --> RESOURCE IN PRODUCTEDMATERIALS
                if (r != null) {
                    productedByLeader.add(ProductedMaterials.valueOf(r.name()));
                }
                else{
                    productedByLeader.add(null);
                }
            }

            try {
                ArrayList<LeaderCard> activeLeaders = actualPlayerBoard.getLeaderCardHandler().getLeaderCardsActive();

                for (int i = 0; i < activeLeaders.size(); i++) {
                    if (useLeaders.get(i) && productedByLeader.get(i)!=null) {
                        if(activeLeaders.get(i).getSpecialAbility().getEffect().equals("additionalProduction")) {
                            Resource requestFromLeader = activeLeaders.get(i).getSpecialAbility().getMaterialType();
                            materialRequested.put(requestFromLeader, materialRequested.get(requestFromLeader) + 1);
                            materialGranted.put(productedByLeader.get(i),materialGranted.get(productedByLeader.get(i))+1);
                            materialGranted.put(ProductedMaterials.FAITHPOINT, materialGranted.get(ProductedMaterials.FAITHPOINT)+1);
                        }
                        else{
                            System.out.println("leader "+i+" not additionalProduction");
                        }
                    }
                }

            }catch (LeaderCardException e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

        }



        if(useDevelop.contains(true)) {

            ArrayList<DevelopmentCard> activeDevelopment = actualPlayerBoard.getDevelopmentCardHandler().getActiveDevelopmentCard();

            for(int i=0; i < activeDevelopment.size(); i++){
                if (activeDevelopment.get(i)!=null && useDevelop.get(i)){
                    System.out.println("Active prod number "+ i +" ?");

                    Map<Resource,Integer> mapRequest = activeDevelopment.get(i).getMaterialRequired();
                    for(Resource r : mapRequest.keySet())
                        materialRequested.put(r,materialRequested.get(r) + mapRequest.get(r));

                    Map<ProductedMaterials,Integer> mapProd = activeDevelopment.get(i).getProductionResult();
                    for(ProductedMaterials prod : mapProd.keySet())
                        materialGranted.put(prod, materialGranted.get(prod) + mapProd.get(prod));
                }
                else {
                    System.out.println("Have not active DevelopmentCard in space " + i);
                }
            }
        }


        //ADD ALL TO STRONGBOX TODO SE LA MOSSA E' NULLA (0 RISORSE DI OGNI COSA COMUNICARLO)
        if(actualPlayerBoard.getResourceHandler().checkMaterials(materialRequested)){
            try {
                actualPlayerBoard.getResourceHandler().takeMaterials(materialRequested);
            } catch (ResourceException e) {
                e.printStackTrace();
            }

            Map<Resource, Integer> materialForStrongBox = new HashMap<>();

            for(Resource r : Resource.values())
                materialForStrongBox.put(r, materialGranted.get(ProductedMaterials.valueOf(r.name())));

            actualPlayerBoard.getResourceHandler().addMaterialStrongbox(materialForStrongBox);
            int faithPoints = materialGranted.get(ProductedMaterials.FAITHPOINT);
            for(int i=0; i<faithPoints; i++){
                if(controllerToModel.getGame().getPlayersFaithTrack().forwardPos(currentPlayerIndex)){
                    controllerToModel.controlPlayerPath(currentPlayerIndex);
                }
            }
            System.out.println("Risorse aggiunte correttamente alla strongbox");
            controllerToModel.getConnectionsToClient().get(currentPlayerIndex).sendNotify("Risorse aggiunte correttamente alla strongbox");
            controllerToModel.getConnectionsToClient().get(currentPlayerIndex).sendNotify("Avanzi di "+ faithPoints + " punti fede");
        }
        else{
            System.out.println("non ci sono abbastanza risorse");
            controllerToModel.getConnectionsToClient().get(currentPlayerIndex).sendNotify("NON HAI ABBASTANZA RISORSE!!!");
        }
    }
}