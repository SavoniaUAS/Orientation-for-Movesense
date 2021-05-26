package fi.digi.savonia.movesense.Tools.Listeners;

import com.movesense.mds.MdsException;

import fi.digi.savonia.movesense.Tools.MovesenseHelper;

/**
 * Kuuntelijan metodit Movesense-sensori MovesenseHelper-luokan käyttöön
 */
public interface MovesenseActionListener {
    void ConnectionResult(boolean success);
    void OnDisconnect(String reason);
    void OnError(MdsException mdsException);
    void OnError(String s);
    void OnDataReceived(Object Data, MovesenseHelper.Sensor sensor);
    void OnInfoReceived(Object Data, MovesenseHelper.Sensor sensor);

}
