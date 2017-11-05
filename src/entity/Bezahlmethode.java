/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.htlstp.fotoherfert4school_schueler.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author 20100226
 */
@Entity
@Table(name = "bezahlmethode")
public class Bezahlmethode implements Serializable {

    @Id
    @Column(name = "bezahl_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "bezahl_bezeichnung", length = 50)
    private String bezeichnung;

    @Column(name = "bezahl_gueltig_von")
    @Temporal(TemporalType.TIMESTAMP)
    private Date gueltig_von = new Date();

    @Column(name = "bezahl_gueltig_bis")
    @Temporal(TemporalType.TIMESTAMP)
    private Date gueltig_bis = null;

    @OneToMany(mappedBy = "bezahlmethode")
    private List<Reko> rechnungen = new ArrayList<>();

    public Bezahlmethode() {
    }

    public Bezahlmethode(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public void setBezeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public Date getGueltig_von() {
        return gueltig_von;
    }

    public void setGueltig_von(Date gueltig_von) {
        this.gueltig_von = gueltig_von;
    }

    public Date getGueltig_bis() {
        return gueltig_bis;
    }

    public void setGueltig_bis(Date gueltig_bis) {
        this.gueltig_bis = gueltig_bis;
    }

    public List<Reko> getRechnungen() {
        return rechnungen;
    }

    public void setRechnungen(List<Reko> rechnungen) {
        this.rechnungen = rechnungen;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Bezahlmethode other = (Bezahlmethode) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
}
