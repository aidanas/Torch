package com.aidanas.torch.interfaces;

/**
 * Created by Aidanas
 * Created on 12/02/2016.
 *
 * Implementation will receive updates about eh currently transmitting index pointer in the text.`
 */
public interface CurrentIndexReceiver {

    void newIndex(int index);
}
