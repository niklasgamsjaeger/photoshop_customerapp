/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import at.htlstp.fotoherfert4school_schueler.db.HibernateDAO;
import at.htlstp.fotoherfert4school_schueler.db.IDAO;
import at.htlstp.fotoherfert4school_schueler.entity.Artikel;
import at.htlstp.fotoherfert4school_schueler.entity.Artikeltyp;
import at.htlstp.fotoherfert4school_schueler.entity.Bezahlmethode;
import at.htlstp.fotoherfert4school_schueler.entity.Klasse;
import at.htlstp.fotoherfert4school_schueler.entity.Kunde;
import at.htlstp.fotoherfert4school_schueler.entity.Reko;
import at.htlstp.fotoherfert4school_schueler.entity.Rezl;
import at.htlstp.fotoherfert4school_schueler.entity.Schule;
import at.htlstp.fotoherfert4school_schueler.others.MailUtilities;
import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.primefaces.context.RequestContext;
import org.xml.sax.SAXException;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentReq;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentRequestType;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentResponseType;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsReq;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsRequestType;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutReq;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutRequestType;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutResponseType;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.CurrencyCodeType;
import urn.ebay.apis.eBLBaseComponents.DoExpressCheckoutPaymentRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.PaymentActionCodeType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsItemType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsType;
import urn.ebay.apis.eBLBaseComponents.SetExpressCheckoutRequestDetailsType;

/**
 *
 * @author 20100226
 */
@Named(value = "schuelerCon")
@SessionScoped
public class SchuelerController implements Serializable {

    private static final String JDBCURL = "jdbc:postgresql://193.170.118.29:54322/fotoherfert4school";
    private final String BENUTZERNAME = "5BHIF_15";
    private final String PASSWORT = "scre20";

    // Abk\u00FCrzungen aller Fototypen
    private static final String PF = "PF";
    private static final String PF_E = "PF_e";
    private static final String KF = "KF";
    private static final String GF = "GF";
    private static final String GF_E = "GF_e";

    private IDAO dao;

    // Login Zugangsschl\u00FCssel
    private String zugangsSchluessel;
    // angemeldete Klasse
    private Klasse aktKlasse;
    // speichert alle Klassefotos der angemeldeten KLasse
    private List<Artikel> klassenFotos;
    // speichert alle Fotos des ausgew\u00E4hlten Sch\u00FClers
    private List<Artikel> selectedArtikeln;
    // beinhaltet die ausgew\u00E4hlten Artikeln
    private List<Artikel> warenkorb;
    // listet alle Artikel auf, die gekauft werden
    private List<Artikel> wk_ueberpruefenBezahlen;
    // alle Artikeltypen mit den Abk\u00FCrzungen
    private Map<String, Artikeltyp> artikelPreise;
    // speichert die selektierten Fotos
    private Map<Artikel, Boolean> selections;
    // speichert die Gesamtsumme aller Artikeln im Warenkorb
    private BigDecimal summe;
    // speichert die E-Mail f\u00FCr die Bank\u00FCberweisung
    private String email;
    // bestimmt, ob der Preis angezeigt wird
    private String showPreis;
    // bestimmt, ob die Partner-URL angezeigt wird
    private String zeigePartner;
    private String downloadRekoId;
    private String downloadId;
    // speichert die Webseitnavigations-URL
    private String goBackTo;
    // zeigt button normal an
    private String alleBilderButton = "display:block;";
    private boolean bereitsGesendet = false;
    private boolean paypalSuccess = false;
    private boolean bankSuccess = false;
    private String payKey;
//    private String payPalBenutzername = "jb-us-seller_api1.paypal.com";
    private String payPalBenutzername = "L.Lcjeid_api1.gmx.at";
//    private String payPalPasswort = "WX4WTU3S8MY44S7F";
    private String payPalPasswort = "RZ9TM26C5E99UE2S";
//    private String payPalSignatur = "AFcWxV21C7fd0v3bYYYRCpSSRl31A7yDhhsPUU2XhtMoZXsWHFxu-RWy";
    private String payPalSignatur = "AMR8udfJqOFuiSxWx-fAmo1WSK4LACDKHO.GUliFB3x1m6dlbOWjR5bb";

    /**
     * Diese Variable zeigt an welcher Bezahlmethode ausgew\u00E4hlt ist.
     * default = "paypal"
     */
    private String bezahlen = "paypal";

    @PostConstruct
    public void init() {
        dao = new HibernateDAO();
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SchuelerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.zugangsSchluessel = "";
        this.aktKlasse = null;
        this.selectedArtikeln = new ArrayList<>();
        this.selections = new ConcurrentHashMap<>();
        this.warenkorb = new ArrayList<>();
        this.wk_ueberpruefenBezahlen = new ArrayList<>();
        this.artikelPreise = new HashMap<>();
        this.showPreis = "display:none";
        this.zeigePartner = "display:none";
    }

    /**
     * Creates a new instance of BenutzerController
     */
    public SchuelerController() {
    }

    /**
     * \u00FCberpr\u00FCft ob die Variable bezahltyp den Wert 'paypal' besitzt.
     *
     * @return true falls bezahltyp gleich 'paypal' ist.
     */
    public boolean isPayPal() {
        return "paypal".equals(bezahlen);
    }

    /**
     * \u00FCberpr\u00FCft ob die Variable bezahltyp den Wert 'bank' besitzt.
     *
     * @return true falls bezahltyp gleich 'bank' ist.
     */
    public boolean isBank() {
        return "bank".equals(bezahlen);
    }

    public String bezahlen() {
        if (isPayPal()) {
            bezahlPayPal();
        } else if (isBank()) {
            return "bank";
        }
        return null;
    }

    public String paypalText() {
        if (paypalSuccess) {
            return "Die Zahlung war ein Erfolg!";
        } else {
            return "Bei der Zahlung ist ein Fehler aufgetreten!";
        }
    }

    public String bankText() {
        if (bankSuccess) {
            return "Die Bestellung war ein Erfolg!";
        } else {
            return "Die Bestellung ist bereits vorhanden!";
        }
    }

    public String paypalErfolg() {
        if (paypalSuccess) {
            return "Bestellung abgeschlossen";
        } else {
            return "Bestellung abgebrochen";
        }
    }

    public String bankErfolg() {
        if (bankSuccess) {
            return "Bestellung abgeschlossen";
        } else {
            return "Bestellung abgebrochen";
        }
    }

    public void bezahlPayPal() {

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

        try {

            List<PaymentDetailsItemType> lineItems = new ArrayList<>();
            PaymentDetailsType paymentDetails = new PaymentDetailsType();
            paymentDetails.setPaymentAction(PaymentActionCodeType.fromValue("Sale"));
            PaymentDetailsItemType item = new PaymentDetailsItemType();
            BasicAmountType amt = new BasicAmountType();
            amt.setCurrencyID(CurrencyCodeType.fromValue("EUR"));
            double totalAmount = 0;

            for (Artikel a : wk_ueberpruefenBezahlen) {
                amt.setValue(String.valueOf(a.getArtikeltyp().getPreis().doubleValue()));
                item.setQuantity(1);
                item.setName(a.getArtikeltyp().getBezeichnung());
                item.setAmount(amt);
                if (Double.valueOf(amt.getValue()) != 0) {
                    lineItems.add(item);
                    totalAmount += Double.valueOf(amt.getValue());
                }
            }
            paymentDetails.setPaymentDetailsItem(lineItems);

            BasicAmountType orderTotal = new BasicAmountType();
            orderTotal.setCurrencyID(CurrencyCodeType.fromValue("EUR"));
            orderTotal.setValue(String.valueOf(totalAmount));
            paymentDetails.setOrderTotal(orderTotal);

            List<PaymentDetailsType> paymentDetailsList = new ArrayList<>();
            paymentDetailsList.add(paymentDetails);

            SetExpressCheckoutRequestDetailsType setExpressCheckoutRequestDetails = new SetExpressCheckoutRequestDetailsType();
            setExpressCheckoutRequestDetails.setReturnURL(FacesContext.getCurrentInstance().getExternalContext().getInitParameter("webseiten_url") + "faces/schueler/paypalAntwort.xhtml");
            setExpressCheckoutRequestDetails.setCancelURL(FacesContext.getCurrentInstance().getExternalContext().getInitParameter("webseiten_url") + "faces/schueler/paypalAntwort.xhtml");

            setExpressCheckoutRequestDetails.setPaymentDetails(paymentDetailsList);

            SetExpressCheckoutRequestType setExpressCheckoutRequest = new SetExpressCheckoutRequestType(setExpressCheckoutRequestDetails);
            setExpressCheckoutRequest.setVersion("104.0");

            SetExpressCheckoutReq setExpressCheckoutReq = new SetExpressCheckoutReq();
            setExpressCheckoutReq.setSetExpressCheckoutRequest(setExpressCheckoutRequest);

            Map<String, String> sdkConfig = new HashMap<>();
            sdkConfig.put("mode", "sandbox");
            sdkConfig.put("acct1.UserName", payPalBenutzername);
            sdkConfig.put("acct1.Password", payPalPasswort);
            sdkConfig.put("acct1.Signature", payPalSignatur);
            PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(sdkConfig);
            SetExpressCheckoutResponseType setExpressCheckoutResponse = service.setExpressCheckout(setExpressCheckoutReq);

            payKey = "" + setExpressCheckoutResponse.getToken();
            System.out.println("++++++" + setExpressCheckoutResponse.getErrors().toString());
            System.out.println("++++++" + setExpressCheckoutResponse.getAck().getValue());
            System.out.println("++++++++++++" + payKey);

            externalContext.redirect("https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=" + payKey);

        } catch (SSLConfigurationException | InvalidCredentialException | IOException | HttpErrorException | InvalidResponseDataException | ClientActionRequiredException | MissingCredentialException | InterruptedException | OAuthException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(SchuelerController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void responsePayPal() {

        HttpServletRequest hsr = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        payKey = String.valueOf(hsr.getRequestURL().subSequence('=', '&'));
        System.out.println("++++++++++++++" + payKey);

        boolean newPayKey = true;

        List<Reko> lr = dao.liefereRekos();
        if (lr == null) {
            for (Reko r : lr) {
                if (payKey.equals(r.getPaypalKey())) {
                    newPayKey = false;
                }
            }
        }

        if (newPayKey) {
            try {
                GetExpressCheckoutDetailsRequestType getExpressCheckoutDetailsRequest = new GetExpressCheckoutDetailsRequestType(payKey);
                getExpressCheckoutDetailsRequest.setVersion("104.0");

                GetExpressCheckoutDetailsReq getExpressCheckoutDetailsReq = new GetExpressCheckoutDetailsReq();
                getExpressCheckoutDetailsReq.setGetExpressCheckoutDetailsRequest(getExpressCheckoutDetailsRequest);

                Map<String, String> sdkConfig = new HashMap<>();
                sdkConfig.put("mode", "sandbox");
                sdkConfig.put("acct1.UserName", payPalBenutzername);
                sdkConfig.put("acct1.Password", payPalPasswort);
                sdkConfig.put("acct1.Signature", payPalSignatur);
                PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(sdkConfig);
                GetExpressCheckoutDetailsResponseType getExpressCheckoutDetailsResponse = service.getExpressCheckoutDetails(getExpressCheckoutDetailsReq);

                String payerID = getExpressCheckoutDetailsResponse.getGetExpressCheckoutDetailsResponseDetails().getPayerInfo().getPayerID();

                PaymentDetailsType paymentDetails = new PaymentDetailsType();
                paymentDetails.setNotifyURL("http://replaceIpnUrl.com");
                paymentDetails.setPaymentAction(PaymentActionCodeType.fromValue("Sale"));
                List<PaymentDetailsItemType> lineItems = new ArrayList<>();
                double orderTotalamt = 0.00;

                for (Artikel artikel : wk_ueberpruefenBezahlen) {
                    PaymentDetailsItemType item = new PaymentDetailsItemType();
                    BasicAmountType amt = new BasicAmountType();
                    double itemAmount = artikel.getArtikeltyp().getPreis().doubleValue();
                    amt.setCurrencyID(CurrencyCodeType.fromValue("EUR"));
                    amt.setValue(String.valueOf(itemAmount));
                    int itemQuantity = 1;
                    item.setQuantity(itemQuantity);
                    item.setName(artikel.getArtikeltyp().getBezeichnung());
                    item.setAmount(amt);
                    lineItems.add(item);
                    orderTotalamt += artikel.getArtikeltyp().getPreis().doubleValue() * itemQuantity;

                }
                paymentDetails.setPaymentDetailsItem(lineItems);
                BasicAmountType orderTotal = new BasicAmountType();
                orderTotal.setCurrencyID(CurrencyCodeType.fromValue("EUR"));
                orderTotal.setValue(String.valueOf(orderTotalamt));
                paymentDetails.setOrderTotal(orderTotal);
                List<PaymentDetailsType> paymentDetailsList = new ArrayList<>();
                paymentDetailsList.add(paymentDetails);

                DoExpressCheckoutPaymentRequestDetailsType doExpressCheckoutPaymentRequestDetails = new DoExpressCheckoutPaymentRequestDetailsType();
                doExpressCheckoutPaymentRequestDetails.setToken(payKey);
                doExpressCheckoutPaymentRequestDetails.setPayerID(payerID);
                doExpressCheckoutPaymentRequestDetails.setPaymentDetails(paymentDetailsList);

                DoExpressCheckoutPaymentRequestType doExpressCheckoutPaymentRequest = new DoExpressCheckoutPaymentRequestType(doExpressCheckoutPaymentRequestDetails);
                doExpressCheckoutPaymentRequest.setVersion("104.0");

                DoExpressCheckoutPaymentReq doExpressCheckoutPaymentReq = new DoExpressCheckoutPaymentReq();
                doExpressCheckoutPaymentReq.setDoExpressCheckoutPaymentRequest(doExpressCheckoutPaymentRequest);

                Map<String, String> sdkConfig1 = new HashMap<>();
                sdkConfig1.put("mode", "sandbox");
                sdkConfig1.put("acct1.UserName", payPalBenutzername);
                sdkConfig1.put("acct1.Password", payPalPasswort);
                sdkConfig1.put("acct1.Signature", payPalSignatur);
                PayPalAPIInterfaceServiceService service1 = new PayPalAPIInterfaceServiceService(sdkConfig1);
                DoExpressCheckoutPaymentResponseType doExpressCheckoutPaymentResponse = service1.doExpressCheckoutPayment(doExpressCheckoutPaymentReq);

                if (doExpressCheckoutPaymentResponse.getDoExpressCheckoutPaymentResponseDetails() != null) {
                    if ("COMPLETED".equals(doExpressCheckoutPaymentResponse.
                            getDoExpressCheckoutPaymentResponseDetails().
                            getPaymentInfo().get(0).getPaymentStatus().toString())
                            && "SUCCESS".equals(doExpressCheckoutPaymentResponse.
                                    getAck().toString())) {

                        paypalSuccess = true;

                        List<Bezahlmethode> lb = dao.liefereBezahlmethoden();
                        Bezahlmethode bm = null;
                        List<Kunde> lk = dao.liefereKunden();
                        Kunde ku = null;
                        String emailkunde = getExpressCheckoutDetailsResponse.
                                getGetExpressCheckoutDetailsResponseDetails().
                                getPayerInfo().getPayer();

                        for (Bezahlmethode b : lb) {
                            if ("PayPal".equals(b.getBezeichnung())) {
                                bm = b;
                            }
                        }
                        if (bm == null) {
                            bm = new Bezahlmethode("PayPal");
                            dao.insertBezahlmethode(bm);
                        }

                        for (Kunde k : lk) {
                            if (emailkunde.equals(k.getEmail())) {
                                ku = k;
                            }
                        }

                        if (ku == null) {
                            ku = new Kunde(emailkunde);
                            dao.insertKunde(ku);
                        }

                        List<Rezl> lrl = new ArrayList<>();
                        Reko re = new Reko(ku, bm, null, aktKlasse.getSchule());
                        re.setBezahldatum(new Date());
                        // URL ABlaufdatum setzen
                        Schule s = aktKlasse.getSchule();
                        Date d = new Date(java.sql.Date.valueOf(LocalDate.now()
                                .plusDays(s.getDownloadfrist())).getTime());
                        re.setUrlAblaufdatum(d);
                        re.setPaypalKey(payKey);
                        int i = 1;
                        dao.insertReko(re);
                        for (Artikel a : wk_ueberpruefenBezahlen) {
                            Rezl rl = new Rezl(i, re, a);
                            dao.insertRezl(rl);
                            lrl.add(rl);
                            i++;
                        }
                        re.setRezls(lrl);
                        dao.updateReko(re);

                        sendeBestellungsMail(re, erstelleBestellungsPDF(re));
                    }
                }
            } catch (SSLConfigurationException | InvalidCredentialException | IOException | HttpErrorException | InvalidResponseDataException | ClientActionRequiredException | MissingCredentialException | InterruptedException | OAuthException | ParserConfigurationException | SAXException | SQLException | ClassNotFoundException | JRException ex) {
                Logger.getLogger(SchuelerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Schreibt den Rechnungskopf und die Rechnungszeilen in die Datenbank und
     * leitet auf die n\u00E4chste Seite weiter.
     *
     * @return Die Seite auf die weitergeleitet wird.
     */
    public String bezahlBank() {
        if (!this.bereitsGesendet) {
            this.bereitsGesendet = true;
            List<Bezahlmethode> lb = dao.liefereBezahlmethoden();
            Bezahlmethode bm = null;
            List<Kunde> lk = dao.liefereKunden();
            Kunde ku = null;

            for (Bezahlmethode b : lb) {
                if ("Bank\u00FCberweisung".equals(b.getBezeichnung())) {
                    bm = b;
                }
            }
            if (bm == null) {
                bm = new Bezahlmethode("Bank\u00FCberweisung");
                dao.insertBezahlmethode(bm);
            }

            for (Kunde k : lk) {
                if (email.equals(k.getEmail())) {
                    ku = k;
                }
            }

            if (ku == null) {
                ku = new Kunde(email);
                dao.insertKunde(ku);
            }

            List<Rezl> lrl = new ArrayList<>();
            Reko re = new Reko(ku, bm, null, aktKlasse.getSchule());
            int i = 1;
            dao.insertReko(re);
            for (Artikel a : wk_ueberpruefenBezahlen) {
                Rezl rl = new Rezl(i, re, a);
                dao.insertRezl(rl);
                lrl.add(rl);
                i++;
            }
            re.setRezls(lrl);
            dao.updateReko(re);
            bankSuccess = true;

            if (bankSuccess) {
                sendeZahlungsinformationenMail(re);
            }

            return "zahlung";
        }

        return null;
    }

    private void sendeZahlungsinformationenMail(Reko rk) {
        try {
            BigDecimal amt = new BigDecimal(0.0);
            for (Rezl rezl : rk.getRezls()) {
                amt = amt.add(rezl.getVk_preis());
            }
            Session s = MailUtilities.getGMailSession("fotoherfert4school@gmail.com", "Micmos87");
            String html = "<img alt='Bild' style='float: right;' "
                    + "src='http://gfhost.htlstp.ac.at/fotoherfert4school_kunde/web_resources/fotoherfert_logo.gif'/>"
                    + "<div><p>\n"
                    + "Firma <br />\n"
                    + "fotoherfert<br />\n"
                    + "He&szlig;stra&szlig;e 16<br />\n"
                    + "3100 St. P&ouml;lten\n"
                    + "</p></div>\n"
                    + "\n<br />"
                    + "<h2>fotoherfert4school - Webshop f&uuml;r Sch&uuml;lerfotos</h2>\n"
                    + "<h3>Zahlungsinformationen</h3>\n"
                    + "\n"
                    + "<p>Vielen Dank f&uuml;r Ihre Bestellung bei fotoherfert4school. Bitte zahlen Sie den angegebenen Betrag unter folgenden Koto ein:</p>\n"
                    + "<p>Empf&auml;nger: Josef Herfert<br>\n"
                    + "IBAN: AT22 7867 9238 8723<br />\n"
                    + "BIC: OWSTA78<br />\n"
                    + "Verwendungszweck: " + rk.getId() + "<br />\n"
                    + "Betrag: " + String.format(Locale.GERMANY, "%.2f", amt.doubleValue()) + " &euro;</p>\n"
                    + "<p>Bitte geben Sie unbedingt den richtigen Verwendungszweck an, sonst kann Ihre Rechnung nicht richtig zugeordnet werden!</p></div>";
            MailUtilities.postHtml(s, rk.getKunde().getEmail(), "fotoherfert4school - Zahlungsinformationen",
                    html);
        } catch (MessagingException ex) {
            Logger.getLogger(SchuelerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Diese Methode erstellt ein Bestellungs-PDF und liefert seinen Pfad
     * innerhalb des Projekts zurÃƒÆ’Ã‚Â¼ck.
     *
     * @param rk Rechnungskopf fÃƒÆ’Ã‚Â¼r den das Bestellungs-PDF erstellt
     * werden soll.
     * @return Pfad des PDFs innerhalb des Projekts
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws JRException
     */
    private String erstelleBestellungsPDF(Reko rk) throws IOException, SQLException,
            ClassNotFoundException, JRException {
        JasperPrint jasperPrint;
        Class.forName("org.postgresql.Driver");
        try (Connection con = DriverManager.getConnection(JDBCURL, BENUTZERNAME, PASSWORT)) {
            List<Rezl> rezls = rk.getRezls();
            BigDecimal summe = new BigDecimal(0);
            for (Rezl rezl : rezls) {
                summe = summe.add(rezl.getVk_preis());
            }

            HashMap<String, Object> params = new HashMap<>();
            params.put("reko_id", rk.getId());
            params.put("rezl_sum", summe);
            String logo = FacesContext.getCurrentInstance().getExternalContext().
                    getRealPath("/resources/images/herfert-und-herfert-logo.gif");
            params.put("logo_pfad", logo);

            ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();
            try (InputStream is = ext.getResourceAsStream("/resources/reports/bestellung_jdbc.jasper")) {
                jasperPrint = JasperFillManager.fillReport(is, params, con);
                JasperExportManager.exportReportToPdfFile(jasperPrint,
                        ext.getRealPath("/resources/reports/") + "bestellung_" + rk.getId() + ".pdf");
                return ext.getRealPath("/resources/reports/") + "bestellung_" + rk.getId() + ".pdf";
            }
        }
    }

    private void sendeBestellungsMail(Reko rk, String bestellung) {
        try {
            Session s = MailUtilities.getGMailSession("fotoherfert4school@gmail.com", "Micmos87");
            String html = "<img alt='Bild' style='float: right;' "
                    + "src='http://gfhost.htlstp.ac.at/fotoherfert4school_kunde/web_resources/fotoherfert_logo.gif'/>"
                    + "<div><p>\n"
                    + "Firma \n<br />"
                    + "fotoherfert\n<br />"
                    + "He&szlig;stra&szlig;e 16\n<br />"
                    + "3100 St. P&ouml;lten\n"
                    + "</p></div>\n"
                    + "\n"
                    + "<div><h2>fotoherfert4school - Webshop f&uuml;r Sch&uuml;lerfotos</h2>\n"
                    + "<h3>Zahlungsbest&auml;tigung</h3>\n"
                    + "\n"
                    + "<p>Vielen Dank f&uuml;r Ihre Bestellung bei fotoherfert4school. Ihre Zahlung ist erfolgreich eingegangen."
                    + "Im Anhang finden Sie dazu Ihre Bestellung.</p></div><br />\n";

            html = html + "<p>Unter folgendem Link k&ouml;nnen Sie die Fotos innerhalb der n&auml;chsten ";
            html = html + rk.getRezls().get(0).getArtikel().getKlasse().getSchule().getDownloadfrist();
            html = html + " Tage herunterladen: </p><a href='";
            html = html + rk.getUrl() + "'>Downloadlink</a><br>";
            Schule schule = rk.getRezls().get(0).getArtikel().getKlasse().getSchule();
            if (schule.isPartnerAnzeige()) {
                html = html + "F&uuml;r die Entwicklung Ihrer Bilder empfehlen wir folgende Webseite: ";
                html = html + "<a href='" + schule.getPartnerURL() + "'>Fotostudio</a>";
            }
            MailUtilities.postHtmlWithAttachement(s, rk.getKunde().getEmail(), "fotoherfert4school - Zahlungsbest\u00E4tigung",
                    html, bestellung, "Bestellung_" + rk.getId() + ".pdf");
            System.out.println("E-Mail gesendet!");
            // L\u00F6schen der Bestellung aus dem Resource-Ordner
            File f = new File(bestellung);
            if (f.delete()) {
                System.out.println("Die Datei " + bestellung + " wurde gel\u00F6scht.");
            } else {
                System.out.println("Die Datei " + bestellung + " wurde nicht gel\u00F6scht.");
            }

        } catch (MessagingException ex) {
            Logger.getLogger(SchuelerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Wenn der eingegebene Zugangsschl\u00FCssel in der Datenbank existiert,
     * wird der Benutzer auf die Fotoansicht weitergeleitet. Falls der
     * Zugangsschl\u00FCssel falsch ist, wird eine Fehlermeldung angezeigt.
     *
     * @return String zur Fotoansicht
     */
    public String login() {
        this.aktKlasse = this.dao.login(this.zugangsSchluessel);

        if (this.aktKlasse == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falscher Zugangsschl\u00FCssel!", ""));
            return null;
        }

        this.zugangsSchluessel = "";

        // \u00FCberpr\u00FCfen, ob Anzeigefrist abgelaufen ist
        Schule schule = aktKlasse.getSchule();
        Date frist = schule.getAnzeigefrist();
        if (frist.after(new Date())) {
            this.klassenFotos = new ArrayList<>();
            for (Artikel a : aktKlasse.getArtikel()) {
                if (KF.equals(a.getArtikeltyp().getAbkz())) {
                    this.klassenFotos.add(a);
                }
            }

            if (this.aktKlasse.getSchule().isPartnerAnzeige()) {
                this.zeigePartner = "display:compact;";
            }

            this.warenkorb.clear();
            this.selectedArtikeln.clear();
            this.wk_ueberpruefenBezahlen.clear();
            this.bereitsGesendet = false;
            this.email = "";

            return "fotoansicht";
        } else {
            // Downloadfrist + Anzeigefrist berechnen
            Date anzeigefristDownloadfrist = new Date(
                    java.sql.Date.valueOf(LocalDate.now().
                            plusDays(schule.getDownloadfrist())).getTime());
            if (anzeigefristDownloadfrist.before(new Date())) {
                dao.deleteSchule(schule);
            }
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falscher Zugangsschl\u00FCssel!", ""));
            return null;
        }
    }

    /**
     * Auf der Fotoansicht-Seite werden nur die Masterfotos des jeweiligen
     * Sch\u00FClers angezeigt.
     *
     * @return Liste mit Artikeln der einzelnen Sch\u00FCler
     */
    public List<Artikel> liefereMasterfotos() {
        List<Artikel> artikeln = new ArrayList<>();
        if (this.aktKlasse != null) {
            for (Artikel a : this.aktKlasse.getArtikel()) {
                if (a.isMaster()) {
                    artikeln.add(a);
                }
            }
            return artikeln;
        } else {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("../schuelerLogin.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(SchuelerController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }

    public int getArtikelID(Artikel art) {
        String imgNameAr[] = art.getUrl().split("_");
        String datei[] = imgNameAr[imgNameAr.length - 1].split("\\.");
        try {
            if ("KF".equals(datei[0].toUpperCase())
                    || "GF".equals(datei[0].toUpperCase())) {
                return Integer.parseInt(imgNameAr[imgNameAr.length - 2]);
            } else {
                return Integer.parseInt(datei[0]);
            }
        } catch (NumberFormatException e) {
            System.err.println("Dateiname konnte nicht in einen Integer gecastet werden!\n" + e.getMessage());
        }
        return 0;
    }

    /**
     * Der Pfad der Fotos wird auf '/' statt '\' ver\u00E4ndert und nur der Name
     * des Bildes retourniert.
     *
     * @param artikel
     * @return Bezeichnung eines Bildes
     */
    public String getImage(Artikel artikel) {
        if (artikel == null) {
            return null;
        }
        String wasserzeichen = FacesContext.getCurrentInstance()
                .getExternalContext().getInitParameter("wasserzeichen");
        String pfad_prefix = FacesContext.getCurrentInstance()
                .getExternalContext().getInitParameter("path_prefix");
        String[] s = artikel.getUrl().split("/");
        if (s.length > 0) {
            String path = "";
            for (int i = 0; i <= s.length - 2; i++) {
                path += s[i] + "/";
            }

            return path + wasserzeichen + s[s.length - 1];
        }
        return null;
    }

    /**
     * Wird ein Bild auf der Fotoansicht ausgew\u00E4hlt, werden alle Bilder des
     * Sch\u00FClers in der 'selectedArtikeln' Liste mitgelieft. Au\u00DFerdem
     * werden alle Preise zu den Fotos angezeigt.
     *
     * @param art das ausgew\u00E4hlte Foto
     * @return weiterleiten auf die Sch\u00FClerfotoansicht
     */
    public String selektiereFoto(Artikel art) {
        this.selectedArtikeln.clear();

        for (Artikel a : this.aktKlasse.getArtikel()) {
            if (a.getKnr() == art.getKnr()
                    && !KF.equals(art.getArtikeltyp().getAbkz())) {
                this.selectedArtikeln.add(a);
            }
        }
        // das erste Klassenfoto wird angezeigt
        if (!this.klassenFotos.isEmpty()) {
            this.selectedArtikeln.add(klassenFotos.get(0));
        }

        if (this.selectedArtikeln.size() == 1) {
            this.alleBilderButton = "display:none;";
        }

        this.selections.clear();
        this.warenkorb.clear();
        this.wk_ueberpruefenBezahlen.clear();

        this.artikelPreise.clear();
        this.artikelPreise.put("PF", dao.liefereArtikeltyp("PF"));
        this.artikelPreise.put("PF_e", dao.liefereArtikeltyp("PF_e"));
        this.artikelPreise.put("GF", dao.liefereArtikeltyp("GF"));
        this.artikelPreise.put("GF_e", dao.liefereArtikeltyp("GF_e"));
        this.artikelPreise.put("KF", dao.liefereArtikeltyp("KF"));

        return "schuelerFotoansicht";
    }

    /**
     * Fuegt alle verfuegbaren Bilder dem Warenkorb hinzu.
     */
    public void alleBilderAdden() {
        this.warenkorb.clear();
        for (Map.Entry<Artikel, Boolean> me : this.selections.entrySet()) {
            if (GF.equals(me.getKey().getArtikeltyp().getAbkz())
                    || PF.equals(me.getKey().getArtikeltyp().getAbkz())) {
                this.selections.put(me.getKey(), Boolean.TRUE);
                System.out.println("Map selections size: " + me.getKey().getArtikeltyp().getBezeichnung()
                        + ", " + me.getValue());
                preiseBerechnen();
            }
        }
    }

    /**
     * Wenn Artikeln ausgewaehlt sind, werden diese dem Warenkorb hinzugefuegt
     * und der Preis zu den Waren berechnet.
     *
     */
    public void befuelleWarenkorb() {
        this.warenkorb.clear();
        preiseBerechnen();
    }

    /**
     * Liefert die Anzeigefrist der Fotos der angemeldeten Klasse. Falls die
     * aktuelle Klasse nicht vorhanden ist, wird der Benutzer auf die Loginseite
     * weitergeleitet.
     *
     * @return Anzeigefrist
     */
    public String getAnzeigefrist() {
        if (this.aktKlasse == null) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("../schuelerLogin.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(SchuelerController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        return sdf.format(this.aktKlasse.getSchule().getAnzeigefrist());
    }

    /**
     * Der mitgelierte Artikel wird aus dem Warenkorb gel\u00F6scht und die
     * Preise werden neu berechnet.
     *
     * @param art Artikel zum Loeschen
     */
    public void entferneArtikel(Artikel art) {
        this.warenkorb.clear();

        if (art != null) {
            this.selections.remove(art);
        }

        preiseBerechnen();
    }

    /**
     * Diese Methode berechnet die Summe aller selektierten Artikeln.
     * Zusaetzlich werden die Preise fuer jedes weitere Foto in die Summe
     * aufgenommen. Wird ein Portraitfoto hinzugefuegt, wird auch automatisch
     * ein Klassenfoto mitgegeben und der Preis des Klassenfotos wird auf Null
     * gesetzt. Ist kein Porttraitfoto im Warenkorb, wird der normale Preis des
     * Klassenfotos angezeigt.
     */
    public void preiseBerechnen() {
        this.summe = new BigDecimal(0);
        int pf = 0, gf = 0;

        this.showPreis = "display:block";

        this.warenkorb.clear();

        for (Map.Entry<Artikel, Boolean> me : this.selections.entrySet()) {
            if (me.getValue()) {
                Artikel art = me.getKey();

                switch (art.getArtikeltyp().getAbkz()) {
                    case PF:
                        if (pf > 0) {
                            art.getArtikeltyp().setPreis(artikelPreise.get(PF_E).getPreis());
//                            art.getArtikeltyp().setBezeichnung("Weiteres Portraitfoto");
                        } else {
                            art.getArtikeltyp().setPreis(artikelPreise.get(PF).getPreis());

                            if (getArtikelAusWarenkorb(KF) == null) {
                                Artikel klfoto = klassenFotos.get(0);
                                klfoto.getArtikeltyp().setPreis(new BigDecimal(0));

                                for (Artikel a : this.selectedArtikeln) {
                                    if (KF.equals(a.getArtikeltyp().getAbkz())) {
                                        this.warenkorb.add(klfoto);
                                        this.selections.put(a, Boolean.TRUE);
                                        break;
                                    }
                                }
                            } else {
                                Artikeltyp KF_arttyp = getArtikelAusWarenkorb(KF)
                                        .getArtikeltyp();
                                summe = summe.subtract(KF_arttyp.getPreis());
                                KF_arttyp.setPreis(new BigDecimal(0));
                            }
                            pf++;
                        }
                        art.setArtikeltyp(generateNewArtikeltyp(art));
                        this.warenkorb.add(art);
                        summe = summe.add(art.getArtikeltyp().getPreis());
                        break;
                    case KF:
                        if (getArtikelAusWarenkorb(KF) == null) {
                            // falls Portraitfoto im Warenkorb -> gratis Klassenfoto
                            if (pf > 0) {
                                art.getArtikeltyp().setPreis(new BigDecimal(0));
                            } else {
                                art.getArtikeltyp().setPreis(artikelPreise.get(KF).getPreis());
                            }
                            art.setArtikeltyp(generateNewArtikeltyp(art));
                            this.warenkorb.add(art);
                            summe = summe.add(art.getArtikeltyp().getPreis());
                        }
                        break;
                    case GF:
                        if (gf > 0) {
                            art.getArtikeltyp().setPreis(artikelPreise.get(GF_E).getPreis());
//                            art.getArtikeltyp().setBezeichnung("Weiteres Geschwisterfoto");
                        } else {
                            art.getArtikeltyp().setPreis(artikelPreise.get(GF).getPreis());
                            gf++;
                        }
                        art.setArtikeltyp(generateNewArtikeltyp(art));
                        this.warenkorb.add(art);
                        summe = summe.add(art.getArtikeltyp().getPreis());
                        break;
                }
            }
        }

        if (this.warenkorb.isEmpty()) {
            this.showPreis = "display:none";
        }
    }

    /**
     * Liefert das Artikel mit der Abk\u00FCrzung abkz.
     *
     * @param abkz Abk\u00FCrzung
     * @return Artikel
     */
    public Artikel getArtikelAusWarenkorb(String abkz) {
        for (Artikel a : this.warenkorb) {
            if (abkz.equals(a.getArtikeltyp().getAbkz())) {
                return a;
            }
        }
        return null;
    }

    /**
     * Ein neues Artikeltyp-Objekt mit den Daten aus art wird erstellt und
     * retourniert.
     *
     * @param art Artikel
     * @return Artikeltyp
     */
    private Artikeltyp generateNewArtikeltyp(Artikel art) {
        Artikeltyp arttypNeu = new Artikeltyp();
        arttypNeu.setPreis(art.getArtikeltyp().getPreis());
        arttypNeu.setAbkz(art.getArtikeltyp().getAbkz());
        arttypNeu.setArtikel(art.getArtikeltyp().getArtikel());
        arttypNeu.setBezeichnung(art.getArtikeltyp().getBezeichnung());
        arttypNeu.setGueltig_bis(art.getArtikeltyp().getGueltig_bis());
        arttypNeu.setGueltig_von(art.getArtikeltyp().getGueltig_von());
        arttypNeu.setId(art.getArtikeltyp().getId());
        return arttypNeu;
    }

    /**
     * F\u00FChrt einen Javascript Befehl aus, um einen Dialog zum gegebenen ID
     * anzuzeigen.
     *
     * @param id
     */
    public void oeffneFotoDialog(int id) {
        System.out.println("boolean: " + this.aktKlasse.getSchule().isGrossansicht());
        if (this.aktKlasse.getSchule().isGrossansicht()) {
            RequestContext.getCurrentInstance().execute("PF('photoDlg" + id + "').show();");
        }
    }

    /**
     * Falls Artikeln im Warenkorb vorhanden sind, wird man zum Warenkorb
     * weitergeleitet. Zus\u00E4tzlich werden alle Klassenfotos in den Warenkorb
     * geladen.
     *
     * @return Warenkorb View
     */
    public String zumWarenkorb() {
        this.wk_ueberpruefenBezahlen.clear();
        if (!this.warenkorb.isEmpty()) {
            if (isPFoderKFImWK()) {
                for (Artikel a : this.aktKlasse.getArtikel()) {
                    if (KF.equals(a.getArtikeltyp().getAbkz())
                            && !a.isMaster()) {
                        a.getArtikeltyp().setPreis(new BigDecimal(0));
                        wk_ueberpruefenBezahlen.add(a);
                    }
                }
            }
            wk_ueberpruefenBezahlen.addAll(this.warenkorb);
//            RequestContext.getCurrentInstance().
            return "warenkorb";
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Im Warenkorb befinden sich keine Artikeln!", ""));
        return null;
    }

    private boolean isPFoderKFImWK() {
        for (Artikel a : this.warenkorb) {
            if (PF.equals(a.getArtikeltyp().getAbkz())
                    || KF.equals(a.getArtikeltyp().getAbkz())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initiert den Download der Artikel der Rechnung, die \u00FCber die
     * Parameter downloadId und downloadRekoId identifiziert wird.
     */
    public void download() {
        String speicherpfadZIP = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("speicherpfadZIP");
        try {
            if (downloadId != null && downloadRekoId != null) {
                int rekoId = Integer.parseInt(downloadRekoId);
                List<Reko> rekos = dao.liefereRekos();
                System.out.println("-------------Download REKOS: " + rekos);
                Reko reko = null;
                for (Reko rk : rekos) {
                    if (rk.getId() == rekoId) {
                        reko = rk;
                    }
                }
                System.out.println("--------------Download 2 REKO:" + reko);
                if (reko != null) {
                    String[] params = reko.getUrl().split("\\?")[1].split("&");
                    System.out.println("------------Download 3 PARAMS: " + Arrays.toString(params));
                    if (params[0].split("=")[1].equals(downloadRekoId)
                            && params[1].split("=")[1].equals(downloadId)) {
                        FacesContext fc = FacesContext.getCurrentInstance();
                        ExternalContext ec = fc.getExternalContext();

                        System.out.println("------------Download 4 IDs: " + downloadRekoId + " " + downloadId);

                        ec.responseReset();
                        ec.setResponseContentType("application/zip");
                        File f = new File(speicherpfadZIP + downloadId + ".zip");
                        ec.setResponseContentLength((int) f.length());

                        //Setzen des angezeigten FileNamens
                        ec.setResponseHeader("Content-Disposition", "attachment; "
                                + "filename=\"" + "bestellung_" + reko.getId() + ".zip" + "\"");

                        OutputStream output = ec.getResponseOutputStream();

                        //Schreiben der ZIP-Datei in den OutputStream
                        try (FileInputStream fis = new FileInputStream(f)) {
                            int n;
                            byte[] b = new byte[1024];
                            while ((n = fis.read(b)) != -1) {
                                System.out.println("---------------Download 5: WRITE");
                                output.write(b, 0, n);
                            }
                            output.flush();
                        }
                        System.out.println("----------------------Download 6: RESPONSE COMPLETE");
                        fc.responseComplete();
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SchuelerController.class.getName()).log(Level.SEVERE, null, ex);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler beim Download",
                            "Es gab einen Fehler bei der Abarbeitung des Downloads. Bitte versuchen Sie es nochmal."));
        }
    }

    public void checkenObBenutzerEingeloggt() {
        if (this.aktKlasse == null) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("../schuelerLogin.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(SchuelerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /* Getter und Setter */
    public String getZeigePartner() {
        return zeigePartner;
    }

    public void setZeigePartner(String zeigePartner) {
        this.zeigePartner = zeigePartner;
    }

    public List<Artikel> getWk_ueberpruefenBezahlen() {
        if (this.aktKlasse == null) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("../schuelerLogin.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(SchuelerController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
        return wk_ueberpruefenBezahlen;
    }

    public void redirectTo(String site) {
        try {
            String url = FacesContext.getCurrentInstance().getViewRoot().getViewId();
            ExternalContext el = FacesContext
                    .getCurrentInstance().getExternalContext();
            if (("/schuelerLogin.xhtml").equals(url)) {
                goBackTo = url;
                el.redirect(el.getRequestContextPath() + "/faces" + site);
            } else {
                if (!"/schueler/impressum.xhtml".equals(url) && !"/schueler/kontakt.xhtml".equals(url)) {
                    goBackTo = url;
                }
                el.redirect(".." + site);
            }
        } catch (IOException ex) {
            Logger.getLogger(SchuelerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getGoBackTo() {
        return goBackTo;
    }

    public void setGoBackTo(String goBackTo) {
        this.goBackTo = goBackTo;
    }

    public void setWk_ueberpruefenBezahlen(List<Artikel> wk_ueberpruefenBezahlen) {
        this.wk_ueberpruefenBezahlen = wk_ueberpruefenBezahlen;
    }

    public String getShowPreis() {
        return showPreis;
    }

    public void setShowPreis(String showPreis) {
        this.showPreis = showPreis;
    }

    public BigDecimal getSumme() {
        return summe;
    }

    public void setSumme(BigDecimal summe) {
        this.summe = summe;
    }

    public List<Artikel> getKlassenFotos() {
        return klassenFotos;
    }

    public void setKlassenFotos(List<Artikel> klassenFotos) {
        this.klassenFotos = klassenFotos;
    }

    public List<Artikeltyp> getArtikelTypen() {
        return new ArrayList<>(artikelPreise.values());
    }

    public Map<String, Artikeltyp> getArtikelPreise() {
        return artikelPreise;
    }

    public void setArtikelPreise(Map<String, Artikeltyp> artikelPreise) {
        this.artikelPreise = artikelPreise;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getZugangsSchluessel() {
        return zugangsSchluessel;
    }

    public List<Artikel> getSelectedArtikeln() {
        return selectedArtikeln;
    }

    public void setSelectedArtikeln(List<Artikel> selectedArtikeln) {
        this.selectedArtikeln = selectedArtikeln;
    }

    public boolean isPaypalSuccess() {
        return paypalSuccess;
    }

    public void setPaypalSuccess(boolean paypalSuccess) {
        this.paypalSuccess = paypalSuccess;
    }

    public String getPayKey() {
        return payKey;
    }

    public void setPayKey(String payKey) {
        this.payKey = payKey;
    }

    public String getPayPalBenutzername() {
        return payPalBenutzername;
    }

    public void setPayPalBenutzername(String payPalBenutzername) {
        this.payPalBenutzername = payPalBenutzername;
    }

    public String getPayPalPasswort() {
        return payPalPasswort;
    }

    public void setPayPalPasswort(String payPalPasswort) {
        this.payPalPasswort = payPalPasswort;
    }

    public String getPayPalSignatur() {
        return payPalSignatur;
    }

    public void setPayPalSignatur(String payPalSignatur) {
        this.payPalSignatur = payPalSignatur;
    }

    public String getBezahlen() {
        return bezahlen;
    }

    public void setBezahlen(String bezahlen) {
        this.bezahlen = bezahlen;
    }

    public List<Artikel> getWarenkorb() {
        return warenkorb;
    }

    public void setWarenkorb(List<Artikel> warenkorb) {
        this.warenkorb = warenkorb;
    }

    public Map<Artikel, Boolean> getSelections() {
        return selections;
    }

    public void setSelections(Map<Artikel, Boolean> selections) {
        this.selections = selections;
    }

    public void setZugangsSchluessel(String zugangsSchluessel) {
        this.zugangsSchluessel = zugangsSchluessel;
    }

    public Klasse getAktKlasse() {
        return aktKlasse;
    }

    public void setAktKlasse(Klasse aktKlasse) {
        this.aktKlasse = aktKlasse;
    }

    public String getDownloadRekoId() {
        return downloadRekoId;
    }

    public void setDownloadRekoId(String downloadRekoId) {
        this.downloadRekoId = downloadRekoId;
    }

    public String getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(String downloadId) {
        this.downloadId = downloadId;
    }

    public String getAlleBilderButton() {
        return alleBilderButton;
    }

    public void setAlleBilderButton(String alleBilderButton) {
        this.alleBilderButton = alleBilderButton;
    }

}
