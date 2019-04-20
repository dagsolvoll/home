package eu.nets.sis.esign.demoapp.model;

import no.bbs.tt.trustsign.tsm.xml.messages.containers.SDOSignature;

import java.util.ArrayList;

public class Certificate {
    private final ArrayList<Signatur> signatureList;
    private Signatur seal;
    private String data;



    public Certificate() {
        signatureList = new ArrayList<Signatur>();
    }

    public void addSignatur(SDOSignature sign) {
        signatureList.add(new Signatur(sign));
    }

    public ArrayList<Signatur> getSignatureList() {
        return this.signatureList;
    }


    public Signatur getSeal() {
        return seal;
    }

    public void setSeal(Signatur seal) {
        this.seal = seal;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


}
