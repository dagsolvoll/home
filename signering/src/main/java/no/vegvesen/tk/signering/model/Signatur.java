package no.vegvesen.tk.signering.model;

import no.bbs.tt.trustsign.tsm.xml.messages.containers.SDOSignature;

public class Signatur {
    private final String ssn;
    private final String cn;
    private final String pkiVendor;
    private final String orgnumber;
    private final String o;
    private final String policy;
    private final String issuerCn;
    private final String uniqueId;
    private final long from;
    private final long to;

    public Signatur(SDOSignature sign) {
        this.ssn = sign.getSignerCertificateInfo().getSsn();
        this.cn = sign.getSignerCertificateInfo().getCn();
        this.pkiVendor = sign.getSignerCertificateInfo().getPkiVendor();
        this.orgnumber = sign.getSignerCertificateInfo().getOrganizationNumber();
        this.o = sign.getSignerCertificateInfo().getO();
        this.policy = sign.getSignerCertificateInfo().getCertificatePolicy();
        this.issuerCn = sign.getSignerCertificateInfo().getIssuerCN();
        this.uniqueId = sign.getSignerCertificateInfo().getUniqueId();
        this.from = sign.getSignerCertificateInfo().getValidFrom();
        this.to = sign.getSignerCertificateInfo().getValidTo();
    }


    public String getSsn() {
        return ssn;
    }

    public String getCn() {
        return cn;
    }

    public String getPkiVendor() {
        return pkiVendor;
    }

    public String getOrgnumber() {
        return orgnumber;
    }

    public String getO() {
        return o;
    }

    public String getPolicy() {
        return policy;
    }

    public String getIssuerCn() {
        return issuerCn;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

}
