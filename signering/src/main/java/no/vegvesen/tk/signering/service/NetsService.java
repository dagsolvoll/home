package no.vegvesen.tk.signering.service;

import no.bbs.trust.esignclientapi.exception.ESignClientException;
import no.bbs.trust.esignclientapi.impl.ESigningFacade;
import no.bbs.trust.esignclientapi.impl.ESigningFactory;
import no.bbs.trust.esignclientapi.impl.MerchantContext;
import no.bbs.trust.esignclientapi.statics.Environment;
import no.bbs.trust.esignclientapi.statics.KeyStoreType;
import no.bbs.tt.trustsign.tsm.xml.messages.*;
import no.bbs.tt.trustsign.tsm.xml.messages.containers.SDO;
import no.bbs.tt.trustsign.tsm.xml.messages.containers.SDOSignature;
import no.vegvesen.tk.signering.model.Certificate;
import no.vegvesen.tk.signering.model.Signatur;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

@Component
@PropertySource("classpath:application.properties")
public class NetsService {
    private static final Logger LOGGER = Logger.getLogger(NetsService.class);

    @Value("classpath:${truststore2.file}")
    private Resource truststoreFile;

    @Value("${truststore2.pwd}")
    private String truststorePwd;

    @Value("${keystore.file}")
    private String keystoreFile;

    @Value("${keystore.pwd}")
    private String keystorePwd;

    @Value("${merchant.id}")
    private String merchantId;

    @Value("${merchant.name}")
    private String merchantName;

    @Autowired
    private ApplicationArguments applicationArguments;


    @PostConstruct
    public void registerMerchantContext() throws ESignClientException, IOException {

        String[] args = applicationArguments.getSourceArgs();

        Security.addProvider(new BouncyCastleProvider());

        MerchantContext merchantContext = new MerchantContext();
        merchantContext.setSslKeystorePath(keystoreFile);
        merchantContext.setSslKeystorePwd(keystorePwd);
        merchantContext.setSigningKeystorePath(keystoreFile);
        merchantContext.setSigningKeystorePwd(keystorePwd);
        merchantContext.setTruststorePath(truststoreFile.getFile().getPath());
        merchantContext.setTruststorePwd(truststorePwd);
        merchantContext.setTruststoreType(KeyStoreType.JKS);
        merchantContext.setCommTimeout(10000);
        merchantContext.setEnv(Environment.PRE_PRODUCTION);
        merchantContext.setMerchantId(merchantId);
        merchantContext.setMerchantName(merchantName);
        ESigningFactory.INSTANCE.registerMerchantContext(merchantContext);
    }

    public void validateSDO() throws IOException, ESignClientException {
        ESigningFacade facade = ESigningFactory.INSTANCE.getESigningFacade(merchantName);
        ValidateSDORequest request = new ValidateSDORequest();
        request.setMerchantID(Long.valueOf(merchantId));
        request.setMessageID("esignclientapi-test-1");
        request.setTime(new Date());
//        request.setOrderID(merchantId + "-esc-" + System.currentTimeMillis());


        String contents = new String(Files.readAllBytes(Paths.get("text_sdo.xml")));
        request.setB64SDOBytes(
                Base64.getEncoder().encodeToString(contents.getBytes()));
        request.setSdoSealed(true);

        ValidateSDOResponse response = facade.validateSDO(request);
        LOGGER.debug(response);
    }

    public Certificate getSsn() throws IOException, ESignClientException {
        //String contents = new String(Files.readAllBytes(Paths.get("text_sdo.xml")));
        String contents = new String(Files.readAllBytes(Paths.get("decoded-signert-pant.xml")));
        String base64Sring = Base64.getEncoder().encodeToString(contents.getBytes());

        getSdoDetails(base64Sring);

        contents = new String(Files.readAllBytes(Paths.get("decoded-signert-pant.xml")));
        base64Sring = Base64.getEncoder().encodeToString(contents.getBytes());
        return getSdoDetails(base64Sring);
    }

    public Certificate getSsn(String base64Sring) throws IOException, ESignClientException {
        return getSdoDetails(base64Sring);
    }

    private Certificate getSdoDetails(String fileContent) throws IOException, ESignClientException  {
        int i=0, j=0;
        LOGGER.debug(fileContent);
        ArrayList<Certificate> list = new ArrayList<Certificate>();
        Certificate cert = new Certificate();

        ESigningFacade facade = ESigningFactory.INSTANCE.getESigningFacade(merchantName);
        GetSDODetailsRequest sdoRequest = new GetSDODetailsRequest();
        sdoRequest.setMerchantID(Long.valueOf(merchantId));
        sdoRequest.setMessageID("esignclientapi-test-2");
        sdoRequest.setTime(new Date());

        sdoRequest.setB64SDO(fileContent);
        sdoRequest.setVerifySDO(true);
        sdoRequest.setReturnSSN(true);
        sdoRequest.setReturnOrganizationNumber(true);

        GetSDODetailsResponse sdoResponse = facade.getSDODetails(sdoRequest);

        for (SDO sdos : sdoResponse.getSdos()) {
            i++;
            cert.setData(sdos.getB64SignedData());
            cert.setSeal(new Signatur(sdos.getSealSignature()));
            //list.add(new Certificate(true, sdos.getSealSignature()));
            for (SDOSignature signatures : sdos.getSdoSignatures()) {
                //list.add(new Certificate(false, signatures));
                cert.addSignatur(signatures);
                j++;
                LOGGER.info("-----------------Cert info in " + "filename" + " (" + i + ", " + j + ")");
                LOGGER.info("SDO cert1.SSN        = " + signatures.getSignerCertificateInfo().getSsn());
                LOGGER.info("SDO cert1.Cn         = " + signatures.getSignerCertificateInfo().getCn());
                LOGGER.info("SDO cert1.PKI Vendor = " + signatures.getSignerCertificateInfo().getPkiVendor());
                LOGGER.info("SDO cert1.OrganiNbmr = " + signatures.getSignerCertificateInfo().getOrganizationNumber());
                LOGGER.info("SDO cert1.O          = " + signatures.getSignerCertificateInfo().getO());
                LOGGER.info("SDO cert1.Policy     = " + signatures.getSignerCertificateInfo().getCertificatePolicy());
                LOGGER.info("SDO cert1.IssuerCn   = " + signatures.getSignerCertificateInfo().getIssuerCN());
                LOGGER.info("SDO cert1.UniqueId   = " + signatures.getSignerCertificateInfo().getUniqueId());
                LOGGER.info("SDO cert1.From       = " + signatures.getSignerCertificateInfo().getValidFrom());
                LOGGER.info("SDO cert1.To         = " + signatures.getSignerCertificateInfo().getValidTo());
                LOGGER.info("-----------------------------------------------------");
            }
        }
        return cert;
    }

    public NetsService() {

    }

}
