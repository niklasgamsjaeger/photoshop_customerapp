/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.htlstp.fotoherfert4school_schueler.others;

import at.htlstp.fotoherfert4school_schueler.controller.SchuelerController;
import at.htlstp.fotoherfert4school_schueler.db.HibernateDAO;
import at.htlstp.fotoherfert4school_schueler.db.IDAO;
import at.htlstp.fotoherfert4school_schueler.entity.Reko;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author 20110386
 */
public class DownloadPhaseListener implements PhaseListener {

    @Override
    public void afterPhase(PhaseEvent event) {
        //
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        ExternalContext ext = context.getExternalContext();
        HttpServletRequest rq = (HttpServletRequest) ext.getRequest();
        String reqURL = rq.getRequestURL().toString();

        System.out.println("------------RequestURL: " + reqURL);
        if (reqURL.contains("download.xhtml")) {
            try {
                IDAO dao = new HibernateDAO();
                String downloadRekoId = rq.getParameter("rId");
                String downloadId = rq.getParameter("dId");
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
                            File f = new File("/var/fotoherfertData/download/" + downloadId + ".zip");
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
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }

}
