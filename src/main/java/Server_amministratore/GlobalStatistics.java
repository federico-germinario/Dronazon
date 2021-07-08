package Server_amministratore;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Classe che rappresenta le statistiche globali ricevute dal drone master
 *
 * @author Federico Germinario
 */
@XmlRootElement
public class GlobalStatistics {
    private float media_consegne;
    private float media_km;
    private float media_inquinamento;
    private float media_batteriaResidua;
    private long timestamp;

    public GlobalStatistics(){}

    public GlobalStatistics(int media_consegne, int media_km, int media_inquinamento, int media_batteriaResidua, int timestamp){
        this.media_consegne = media_consegne;
        this.media_km = media_km;
        this.media_inquinamento = media_inquinamento;
        this.media_batteriaResidua = media_batteriaResidua;
        this.timestamp = timestamp;
    }

    public float getMedia_consegne() {
        return media_consegne;
    }

    public void setMedia_consegne(float media_consegne) {
        this.media_consegne = media_consegne;
    }

    public float getMedia_km() {
        return media_km;
    }

    public void setMedia_km(float media_km) {
        this.media_km = media_km;
    }

    public float getMedia_inquinamento() {
        return media_inquinamento;
    }

    public void setMedia_inquinamento(float media_inquinamento) {
        this.media_inquinamento = media_inquinamento;
    }

    public float getMedia_batteriaResidua() {
        return media_batteriaResidua;
    }

    public void setMedia_batteriaResidua(float media_batteriaResidua) {
        this.media_batteriaResidua = media_batteriaResidua;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
