package com.tunibus.api;
public class LignesHoraires {
    private String nomLigne;
    private String dateDepart;
    private String id;

    public LignesHoraires(String nomLigne, String dateDepart,String id) {
        setNomLigne(nomLigne);
        setDateDepart(dateDepart);
        setId(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomLigne() {
        return nomLigne;
    }

    public void setNomLigne(String nomLigne) {
        this.nomLigne = nomLigne;
    }

    public String getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(String dateDepart) {
        this.dateDepart = dateDepart;
    }
}
