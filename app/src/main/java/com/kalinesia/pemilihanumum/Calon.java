package com.kalinesia.pemilihanumum;

public class Calon {
    String baseUrl, nomorUrut, name, imageUrl, visiMisi; // deklarasi object

    public Calon(String baseUrl, String nomorUrut,String name, String imageUrl, String visiMisi) {
        this.baseUrl = baseUrl; // set value baseUrl
        this.nomorUrut = nomorUrut; // set value nomorUrut
        this.name = name; // set value name
        this.imageUrl = imageUrl; // set value imageUrl
        this.visiMisi = visiMisi; // set value visiMisi
    }

    public String getBaseUrl() { // get data baseUrl
        return baseUrl;
    }
    public String getNomorUrut() { // get data nomorUrut
        return nomorUrut;
    }
    public String getName() { // get data name
        return name;
    }

    public String getImageUrl() { // get data imageUrl
        return imageUrl;
    }
    public String getVisiMisi() { // get data visiMisi
        return visiMisi;
    }
}
