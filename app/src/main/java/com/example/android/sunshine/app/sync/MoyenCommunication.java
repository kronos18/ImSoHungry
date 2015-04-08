package com.example.android.sunshine.app.sync;

/**
 * Created by devisi on 08/04/15.
 */
public class MoyenCommunication {
    private final String type;

    private final String contenu;

    //Constructeur
    public MoyenCommunication(String type, String contenu)
    {
        this.type = type;
        this.contenu = contenu;
    }

    public String getType() {
        return type;
    }

    public String getContenu() {
        return contenu;
    }
}
