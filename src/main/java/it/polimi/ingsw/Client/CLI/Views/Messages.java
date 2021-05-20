package it.polimi.ingsw.Client.CLI.Views;

public class Messages {
    String message;

    public Messages(String message){
        this.message = message;
    }

    public void printMessage(){
        System.out.print("\u001B[2J\u001B[3J\u001B[H");
        System.out.print("\u001B[31m");

        for(int i=0; i<24; i++)
            System.out.println("                                                                                                                           ");

        for(int j=0; j< (123 - (this.message.length() +4))/2; j++){
            System.out.print(" ");
        }

        System.out.print("┌─");

        for(int k=0; k<message.length(); k++)
            System.out.print("─");

        System.out.print("─┐\n");

        for(int j=0; j<(123 - (this.message.length() +4))/2; j++){
            System.out.print(" ");
        }

        System.out.print("│ " + message.toUpperCase() + " │\n");

        for(int j=0; j< ((123 - (this.message.length()+4))/2); j++){
            System.out.print(" ");
        }

        System.out.print("└─");

        for(int k=0; k<message.length(); k++)
            System.out.print("─");

        System.out.print("─┘");

        for(int i=0; i<24; i++)
            System.out.println("                                                                                                                           ");

        System.out.print("\u001B[0;00m");
    }
}
