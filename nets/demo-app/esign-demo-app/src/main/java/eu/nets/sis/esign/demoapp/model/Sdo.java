package eu.nets.sis.esign.demoapp.model;

public class Sdo {
    private Certificate certificate;
    private String base64SDOString;

    public Sdo(Certificate cert, String base64Sdo) {
        this.certificate = cert;
        this.base64SDOString = base64Sdo;
    }
    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public String getBase64SDOString() {
        return base64SDOString;
    }

    public void setBase64SDOString(String base64SDOString) {
        this.base64SDOString = base64SDOString;
    }


}
